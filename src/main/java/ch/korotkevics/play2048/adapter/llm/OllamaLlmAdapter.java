package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.ai.llm.LlmClient;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
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
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<Direction> askForMove(BoardState boardState) {
        try {
            String prompt = constructPrompt(boardState);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of("temperature", 0.0)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Ollama API error: " + response.statusCode() + " - " + response.body());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String textResponse = root.path("response").asText().trim().toUpperCase();

            return parseDirection(textResponse);
        } catch (Exception e) {
            System.err.println("Failed to communicate with Ollama: " + e.getMessage());
            return Optional.empty();
        }
    }

    private String constructPrompt(BoardState boardState) {
        return """
                You are playing 2048.
                Current board state:
                %s
                
                Suggest the next move. Respond ONLY with one of the following words: UP, RIGHT, DOWN, LEFT.
                Do not provide any explanation.
                """.formatted(Arrays.deepToString(boardState.grid()));
    }

    private Optional<Direction> parseDirection(String text) {
        return Arrays.stream(Direction.values())
                .filter(d -> text.contains(d.name()))
                .findFirst();
    }
}
