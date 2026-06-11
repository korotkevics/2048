package ch.korotkevics.play2048.domain.ai.llm;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Objects;
import java.util.Optional;

public final class LlmAiFacade implements MoveSuggester {

    private final LlmClient llmClient;

    public LlmAiFacade(LlmClient llmClient) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
    }

    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState) {
        return llmClient.askForMove(boardState);
    }
}
