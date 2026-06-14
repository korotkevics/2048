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
            String prompt = constructAugmentedPrompt(boardState, settings);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of(
                            "temperature", 0.0,
                            "num_predict", 15, 
                            "top_k", 20,
                            "top_p", 0.9,
                            "stop", Arrays.asList("\n", "Board:")
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .timeout(Duration.ofSeconds(30)) 
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String textResponse = root.path("response").asText().trim().toUpperCase();
            
            System.out.println("[Ollama] Augmented AI Response: '" + textResponse + "'");

            return parseDirection(textResponse);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String constructAugmentedPrompt(BoardState boardState, GameSettings settings) {
        StringBuilder gridBuilder = new StringBuilder();
        int[][] grid = boardState.grid();
        for (int r = 0; r < 4; r++) {
            gridBuilder.append("| ");
            for (int c = 0; c < 4; c++) {
                gridBuilder.append(grid[r][c] == 0 ? "." : grid[r][c]).append(" | ");
            }
            gridBuilder.append("\n");
        }

        // Use dynamic settings for pre-simulation
        Game2048Engine engine = Game2048Engine.from(grid, 0, new java.util.Random(), settings);
        String moveStats = Arrays.stream(Direction.values())
                .map(d -> {
                    MoveResult res = engine.simulateMove(d);
                    if (!res.moved()) return null;
                    long merges = res.deltas().stream().filter(m -> m.merged()).count();
                    int emptyCells = countEmpty(res.boardState().grid());
                    return "- %s: Merges: %d, Score Gained: %d, Resulting Empty Cells: %d"
                            .formatted(d.name(), merges/2, res.scoreGained(), emptyCells);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining("\n"));

        return """
                [INST]
                Task: Select the best move for the 2048 game.
                Goal: Maximize empty cells and merges. Keep the largest numbers in the corners.
                
                Current Board:
                %s
                
                Available Moves & Outcomes:
                %s
                
                Decision: Respond with ONLY the direction name from the list above.
                [/INST]
                Selection:""".formatted(gridBuilder.toString(), moveStats);
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

    private Optional<Direction> parseDirection(String text) {
        String cleanText = text.replaceAll("[^A-Z]", " ");
        String[] words = cleanText.split("\\s+");
        for (String word : words) {
            for (Direction dir : Direction.values()) {
                if (word.equals(dir.name())) return Optional.of(dir);
            }
        }
        return Optional.empty();
    }
}
