package ch.korotkevics.play2048.domain.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record MoveResult(
        Direction direction,
        boolean moved,
        int scoreGained,
        int score,
        int highScore,
        boolean gameOver,
        boolean won,
        BoardState boardState,
        List<GlobalTileMove> deltas,
        Game2048Engine nextEngine
) {
}
