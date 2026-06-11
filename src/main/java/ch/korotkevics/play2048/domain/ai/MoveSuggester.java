package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Optional;

public interface MoveSuggester {
    Optional<Direction> suggestNextMove(BoardState boardState);
}
