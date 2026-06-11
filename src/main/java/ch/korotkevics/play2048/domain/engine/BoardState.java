package ch.korotkevics.play2048.domain.engine;

import java.util.Arrays;
import java.util.Objects;

public record BoardState(int[][] grid) {
    public BoardState {
        grid = copyOf(grid);
    }

    public int size() {
        return grid.length;
    }

    public int getValue(int row, int column) {
        return grid[row][column];
    }

    private static int[][] copyOf(int[][] board) {
        Objects.requireNonNull(board, "board must not be null");
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = Arrays.copyOf(board[row], board[row].length);
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardState that = (BoardState) o;
        return Arrays.deepEquals(grid, that.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    @Override
    public String toString() {
        return "BoardState{grid=" + Arrays.deepToString(grid) + "}";
    }
}
