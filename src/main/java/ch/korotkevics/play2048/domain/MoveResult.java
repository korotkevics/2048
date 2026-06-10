package ch.korotkevics.play2048.domain;

import java.util.Arrays;
import java.util.Objects;

public record MoveResult(
        Direction direction,
        boolean moved,
        int scoreGained,
        int score,
        boolean gameOver,
        boolean won,
        int[][] board
) {
    public MoveResult {
        board = copyOf(board);
    }

    private static int[][] copyOf(int[][] board) {
        Objects.requireNonNull(board, "board must not be null");
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = Arrays.copyOf(board[row], board[row].length);
        }
        return copy;
    }
}
