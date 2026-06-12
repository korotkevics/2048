package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import java.util.List;
import static org.testng.Assert.*;

public class BoardLogicTest {

    @Test
    public void testEmptyBoardCreation() {
        Board board = new Board(4);
        assertEquals(board.size(), 4);
        assertEquals(board.emptyCells().size(), 16);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidSize() {
        new Board(1);
    }

    @Test
    public void testBoardFromGrid() {
        int[][] grid = {{2, 4}, {8, 16}};
        Board board = new Board(grid);
        assertEquals(board.size(), 2);
        assertEquals(board.getValue(0, 0), 2);
        assertEquals(board.getValue(1, 1), 16);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBoardFromGridInvalidSize() {
        int[][] grid = {{2}};
        new Board(grid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBoardFromGridNotSquare() {
        int[][] grid = {{2, 4}, {8}};
        new Board(grid);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBoardFromGridNullRow() {
        int[][] grid = new int[2][];
        grid[0] = new int[]{2, 4};
        grid[1] = null;
        new Board(grid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBoardFromGridNegativeValue() {
        int[][] grid = {{-2, 4}, {8, 16}};
        new Board(grid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBoardFromGridNotPowerOfTwo() {
        int[][] grid = {{3, 4}, {8, 16}};
        new Board(grid);
    }

    @Test
    public void testEmptyCells() {
        int[][] grid = {{2, 0}, {0, 4}};
        Board board = new Board(grid);
        List<Board.Cell> empty = board.emptyCells();
        assertEquals(empty.size(), 2);
        assertTrue(empty.contains(new Board.Cell(0, 1)));
        assertTrue(empty.contains(new Board.Cell(1, 0)));
    }

    @Test
    public void testWithValue() {
        Board board = new Board(2);
        Board newBoard = board.withValue(0, 1, 2);
        assertEquals(newBoard.getValue(0, 1), 2);
        assertEquals(board.getValue(0, 1), 0); // immutability
    }

    @Test
    public void testGetLine() {
        int[][] grid = {{2, 4}, {8, 16}};
        Board board = new Board(grid);
        
        assertEquals(board.getLine(0, Direction.LEFT), new int[]{2, 4});
        assertEquals(board.getLine(0, Direction.RIGHT), new int[]{4, 2});
        assertEquals(board.getLine(1, Direction.UP), new int[]{4, 16});
        assertEquals(board.getLine(1, Direction.DOWN), new int[]{16, 4});
    }

    @Test
    public void testWithLine() {
        Board board = new Board(2);
        Board newBoard = board.withLine(0, Direction.RIGHT, new int[]{4, 2});
        
        // Right means offset 0 maps to size-1-0 = 1, so column 1 gets 4
        // offset 1 maps to size-1-1 = 0, so column 0 gets 2
        assertEquals(newBoard.getValue(0, 1), 4);
        assertEquals(newBoard.getValue(0, 0), 2);
        
        Board upBoard = board.withLine(1, Direction.UP, new int[]{8, 16});
        assertEquals(upBoard.getValue(0, 1), 8);
        assertEquals(upBoard.getValue(1, 1), 16);
    }

    @Test
    public void testHasTileWithValue() {
        int[][] grid = {{2, 4}, {8, 16}};
        Board board = new Board(grid);
        assertTrue(board.hasTileWithValue(16));
        assertTrue(board.hasTileWithValue(8));
        assertFalse(board.hasTileWithValue(32));
    }

    @Test
    public void testIsGameOver() {
        // Empty cells remain
        int[][] grid1 = {{2, 4}, {8, 0}};
        assertFalse(new Board(grid1).isGameOver());

        // No empty cells, can merge horizontal
        int[][] grid2 = {{2, 2}, {8, 16}};
        assertFalse(new Board(grid2).isGameOver());

        // No empty cells, can merge vertical
        int[][] grid3 = {{2, 4}, {2, 16}};
        assertFalse(new Board(grid3).isGameOver());

        // Game over
        int[][] grid4 = {{2, 4}, {8, 16}};
        assertTrue(new Board(grid4).isGameOver());
    }
}
