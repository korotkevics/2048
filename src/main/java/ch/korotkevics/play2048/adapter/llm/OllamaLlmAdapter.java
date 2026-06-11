package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.ai.llm.LlmClient;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Optional;

public final class OllamaLlmAdapter implements LlmClient {

    private final String model;
    private final String baseUrl;

    public OllamaLlmAdapter(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public Optional<Direction> askForMove(BoardState boardState) {
        // This is a placeholder for the actual Ollama API communication logic.
        // It will involve constructing a prompt with boardState.grid(),
        // sending a JSON request to baseUrl/api/generate (or /api/chat),
        // and parsing the response to extract a Direction.
        
        System.out.println("OllamaAdapter: Communicating with " + model + " at " + baseUrl);
        return Optional.empty(); // Not implemented for real network IO in pure logic phase
    }
}
