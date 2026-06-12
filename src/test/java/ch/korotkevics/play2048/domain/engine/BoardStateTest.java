package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BoardStateTest {

    @Test
    public void testBoardStateImmutability() {
        int[][] originalGrid = {{2, 4}, {8, 16}};
        BoardState state = new BoardState(originalGrid);
        
        // Modifying original grid should not affect the BoardState
        originalGrid[0][0] = 32;
        assertEquals(state.getValue(0, 0), 2);
    }

    @Test
    public void testSizeAndGetValue() {
        int[][] grid = {{2, 4}, {8, 16}};
        BoardState state = new BoardState(grid);
        
        assertEquals(state.size(), 2);
        assertEquals(state.getValue(1, 1), 16);
    }

    @Test
    public void testEqualsAndHashCode() {
        int[][] grid1 = {{2, 4}, {8, 16}};
        int[][] grid2 = {{2, 4}, {8, 16}};
        int[][] grid3 = {{2, 4}, {8, 32}};
        
        BoardState state1 = new BoardState(grid1);
        BoardState state2 = new BoardState(grid2);
        BoardState state3 = new BoardState(grid3);
        
        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());
        assertNotEquals(state1, state3);
        assertNotEquals(state1, null);
        assertNotEquals(state1, new Object());
    }

    @Test
    public void testToString() {
        int[][] grid = {{2, 0}, {0, 4}};
        BoardState state = new BoardState(grid);
        String str = state.toString();
        assertTrue(str.contains("2, 0"));
        assertTrue(str.contains("0, 4"));
    }
    
    @Test(expectedExceptions = NullPointerException.class)
    public void testNullGrid() {
        new BoardState(null);
    }
}
