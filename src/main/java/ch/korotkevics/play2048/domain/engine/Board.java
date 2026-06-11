package ch.korotkevics.play2048.domain.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class Board {
    private final int size;
    private final int[][] grid;

    Board(int size) {
        if (size < 2) {
            throw new IllegalArgumentException("size must be at least 2");
        }
        this.size = size;
        this.grid = new int[size][size];
    }

    Board(int[][] grid) {
        validateBoard(grid);
        this.size = grid.length;
        this.grid = copyOf(grid);
    }

    int size() {
        return size;
    }

    int[][] grid() {
        return copyOf(grid);
    }

    int getValue(int row, int column) {
        return grid[row][column];
    }

    List<Cell> emptyCells() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (grid[row][column] == 0) {
                    emptyCells.add(new Cell(row, column));
                }
            }
        }
        return emptyCells;
    }

    int[] getLine(int index, Direction direction) {
        int[] line = new int[size];
        for (int offset = 0; offset < size; offset++) {
            Cell cell = cellFor(index, offset, direction);
            line[offset] = grid[cell.row()][cell.column()];
        }
        return line;
    }

    Board withValue(int row, int column, int value) {
        int[][] newGrid = grid();
        newGrid[row][column] = value;
        return new Board(newGrid);
    }

    Board withLine(int index, Direction direction, int[] values) {
        int[][] newGrid = grid();
        for (int offset = 0; offset < size; offset++) {
            Cell cell = cellFor(index, offset, direction);
            newGrid[cell.row()][cell.column()] = values[offset];
        }
        return new Board(newGrid);
    }

    boolean hasTileWithValue(int target) {
        for (int[] row : grid) {
            for (int value : row) {
                if (value >= target) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isGameOver() {
        if (!emptyCells().isEmpty()) {
            return false;
        }

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (row + 1 < size && grid[row][column] == grid[row + 1][column]) {
                    return false;
                }
                if (column + 1 < size && grid[row][column] == grid[row][column + 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    private Cell cellFor(int index, int offset, Direction direction) {
        return switch (direction) {
            case LEFT -> new Cell(index, offset);
            case RIGHT -> new Cell(index, size - 1 - offset);
            case UP -> new Cell(offset, index);
            case DOWN -> new Cell(size - 1 - offset, index);
        };
    }

    private static int[][] copyOf(int[][] board) {
        Objects.requireNonNull(board, "board must not be null");
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = Arrays.copyOf(board[row], board[row].length);
        }
        return copy;
    }

    private static void validateBoard(int[][] board) {
        if (board.length < 2) {
            throw new IllegalArgumentException("board must be at least 2x2");
        }

        for (int[] row : board) {
            if (row == null || row.length != board.length) {
                throw new IllegalArgumentException("board must be square");
            }
            for (int value : row) {
                if (value < 0 || (value != 0 && (value & (value - 1)) != 0)) {
                    throw new IllegalArgumentException("board values must be zero or powers of two");
                }
            }
        }
    }

    record Cell(int row, int column) {
    }
}
