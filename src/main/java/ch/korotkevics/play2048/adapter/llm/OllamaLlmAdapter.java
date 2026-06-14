package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.ai.llm.LlmClient;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class OllamaLlmAdapter implements LlmClient {

    private static final String DEFAULT_URL = "http://localhost:11434";
    private static final String ENV_VAR_URL = "OLLAMA_API_URL";

    private final String model;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Same professional weight matrix as the Algo AI
    private static final double[][] WEIGHT_MATRIX = {
        {2048, 1024, 512, 256},
        {16,   32,   64,  128},
        {8,    4,    2,   1},
        {0.5,  0.25, 0.1, 0.05}
    };

    public OllamaLlmAdapter(String model) {
        this(System.getenv().getOrDefault(ENV_VAR_URL, DEFAULT_URL), model);
    }

    public OllamaLlmAdapter(String baseUrl, String model) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<Direction> askForMove(BoardState boardState, GameSettings settings) {
        try {
            String prompt = constructHardenedPrompt(boardState, settings);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "format", "json",
                    "stream", false,
                    "options", Map.of(
                            "temperature", 0.0,
                            "num_predict", 15
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .timeout(Duration.ofSeconds(30)) 
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return Optional.empty();

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode responseData = objectMapper.readTree(root.path("response").asText());
            String move = responseData.path("move").asText().toUpperCase();

            System.out.println("[LLM] Hardened choice: " + move);

            return Arrays.stream(Direction.values())
                    .filter(d -> d.name().equals(move))
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String constructHardenedPrompt(BoardState boardState, GameSettings settings) {
        Game2048Engine engine = Game2048Engine.from(boardState.grid(), 0, new java.util.Random(), settings);
        
        String moveAnalysis = Arrays.stream(Direction.values())
                .map(d -> {
                    MoveResult res = engine.simulateMove(d);
                    if (!res.moved()) return null;
                    
                    double strat = calculateHeuristic(res.boardState().grid());
                    int empty = countEmpty(res.boardState().grid());
                    
                    // Add 1-step foresight (best merge in next turn)
                    long nextMerges = 0;
                    Game2048Engine nextEngine = Game2048Engine.from(res.boardState().grid(), 0, new java.util.Random(), settings);
                    for (Direction nextDir : Direction.values()) {
                        long m = nextEngine.simulateMove(nextDir).deltas().stream().filter(dm -> dm.merged()).count();
                        nextMerges = Math.max(nextMerges, m/2);
                    }

                    return "- %s: StrategyScore=%.0f, Space=%d, ForesightMerges=%d"
                            .formatted(d.name(), strat, empty, nextMerges);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining("\n"));

        return """
                You are a Professional 2048 AI.
                CRITICAL RULE: Highest number MUST stay in the TOP-LEFT corner (0,0). 
                Any move that fails this is penalized by -1,000,000.
                
                Outcomes:
                %s
                
                Decision: Pick the move with the highest StrategyScore. Output JSON ONLY.
                JSON: {"move": "..."}""".formatted(moveAnalysis);
    }

    private double calculateHeuristic(int[][] grid) {
        double score = 0;
        int maxVal = 0;
        int maxR = 0, maxC = 0;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int val = grid[r][c];
                if (val > maxVal) {
                    maxVal = val;
                    maxR = r; maxC = c;
                }
                if (val != 0) {
                    score += (Math.log(val) / Math.log(2)) * WEIGHT_MATRIX[r][c];
                }
            }
        }

        // DRACONIAN CORNER-LOCK PENALTY
        if (maxR != 0 || maxC != 0) {
            score -= 1_000_000;
        }

        return score;
    }

    private int countEmpty(int[][] grid) {
        int count = 0;
        for (int[] row : grid) {
            for (int val : row) {
                if (val == 0) count++;
            }
        }
        return count;
    }
}
