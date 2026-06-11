package ch.korotkevics.play2048.domain.ai.llm;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Optional;

public interface LlmClient {
    Optional<Direction> askForMove(BoardState boardState);
}
