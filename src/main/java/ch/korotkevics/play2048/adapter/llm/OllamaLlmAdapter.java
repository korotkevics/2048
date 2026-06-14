package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.ai.llm.LlmClient;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
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
    public Optional<Direction> askForMove(BoardState boardState) {
        try {
            String prompt = constructContextualPrompt(boardState);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of(
                            "temperature", 0.0,
                            "num_predict", 5, // Extremely strict token limit
                            "top_k", 20,
                            "top_p", 0.9,
                            "stop", Arrays.asList("\n", "Selection:", "Board:", "Task:", "Allowed:", "Instruction:", " ")
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
            
            System.out.println("[Ollama] Raw AI Response: '" + textResponse + "'");

            Optional<Direction> direction = parseDirection(textResponse);
            if (direction.isPresent()) {
                System.out.println("[Ollama] Interpreted as: " + direction.get());
            } else {
                System.err.println("[Ollama] Direction not found in raw response. Attempting secondary recovery...");
                // Secondary parsing: try with less strict cleaning
                direction = parseDirectionFuzzy(textResponse);
            }
            return direction;
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String constructContextualPrompt(BoardState boardState) {
        StringBuilder gridBuilder = new StringBuilder();
        int[][] grid = boardState.grid();
        for (int r = 0; r < 4; r++) {
            gridBuilder.append("| ");
            for (int c = 0; c < 4; c++) {
                gridBuilder.append(grid[r][c] == 0 ? "." : grid[r][c]).append(" | ");
            }
            gridBuilder.append("\n");
        }

        Game2048Engine engine = Game2048Engine.from(grid);
        String availableMoves = Arrays.stream(Direction.values())
                .filter(d -> engine.simulateMove(d).moved())
                .map(Direction::name)
                .collect(Collectors.joining(", "));

        return """
                [INST]
                Task: Select one move for 2048.
                Rules: Merge identical adjacent numbers.
                Board:
                %s
                Allowed: %s
                Decision: Output only one word from Allowed.
                [/INST]
                Selection:""".formatted(gridBuilder.toString(), availableMoves);
    }

    private Optional<Direction> parseDirection(String text) {
        // Strict word boundary matching
        String[] words = text.split("[^A-Z]+");
        for (String word : words) {
            for (Direction dir : Direction.values()) {
                if (word.equals(dir.name())) {
                    return Optional.of(dir);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Direction> parseDirectionFuzzy(String text) {
        // Fallback for conversational models
        for (Direction dir : Direction.values()) {
            if (text.contains(dir.name())) {
                return Optional.of(dir);
            }
        }
        return Optional.empty();
    }
}
