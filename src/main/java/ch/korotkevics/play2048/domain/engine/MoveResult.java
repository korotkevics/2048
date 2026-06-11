package ch.korotkevics.play2048.domain.engine;

import java.util.Arrays;
import java.util.Objects;

public record MoveResult(
        Direction direction,
        boolean moved,
        int scoreGained,
        int score,
        boolean gameOver,
        boolean won,
        BoardState boardState,
        Game2048Engine nextEngine
) {
}
