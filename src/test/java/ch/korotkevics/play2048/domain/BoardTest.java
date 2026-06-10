package ch.korotkevics.play2048.domain;

import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoardTest {

    @Test
    public void boardInitialization() {
        Board board = new Board(4);
        assertThat(board.size()).isEqualTo(4);
        assertThat(board.grid()).isDeepEqualTo(new int[4][4]);
    }

    @Test
    public void boardFromGrid() {
        int[][] grid = {
                {2, 0},
                {0, 4}
        };
        Board board = new Board(grid);
        assertThat(board.size()).isEqualTo(2);
        assertThat(board.grid()).isDeepEqualTo(grid);
    }

    @Test
    public void defensiveCopying() {
        int[][] grid = {
                {2, 0},
                {0, 0}
        };
        Board board = new Board(grid);
        grid[0][0] = 4;
        assertThat(board.getValue(0, 0)).isEqualTo(2);

        int[][] exported = board.grid();
        exported[0][0] = 8;
        assertThat(board.getValue(0, 0)).isEqualTo(2);
    }

    @Test
    public void emptyCells() {
        Board board = new Board(new int[][]{
                {2, 0},
                {0, 4}
        });
        List<Board.Cell> empty = board.emptyCells();
        assertThat(empty).containsExactlyInAnyOrder(
                new Board.Cell(0, 1),
                new Board.Cell(1, 0)
        );
    }

    @Test
    public void isGameOver() {
        Board board = new Board(new int[][]{
                {2, 4},
                {4, 2}
        });
        assertThat(board.isGameOver()).isTrue();

        board.setValue(0, 0, 0);
        assertThat(board.isGameOver()).isFalse();

        board.setValue(0, 0, 2);
        board.setValue(0, 1, 2);
        assertThat(board.isGameOver()).isFalse();
    }

    @Test
    public void invalidBoardsAreRejected() {
        assertThatThrownBy(() -> new Board(new int[][]{{2}}))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> new Board(new int[][]{{2, 2}, {2}}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
