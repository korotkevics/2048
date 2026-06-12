package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import java.util.Random;
import static org.testng.Assert.*;

public class ExhaustiveEngineTest {

    @Test
    public void testNewGameDefaults() {
        Game2048Engine engine = Game2048Engine.newGame();
        assertEquals(engine.board().length, 4);
        assertEquals(engine.score(), 0);
        assertNotNull(engine.settings());
    }

    @Test
    public void testNewGameWithSizeAndRandom() {
        Game2048Engine engine = Game2048Engine.newGame(5, new Random(42));
        assertEquals(engine.board().length, 5);
    }

    @Test
    public void testFromGridDefaults() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid);
        assertEquals(engine.score(), 0);
        assertEquals(engine.board().length, 2);
    }

    @Test
    public void testFromGridWithScoreAndRandom() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid, 100, new Random());
        assertEquals(engine.score(), 100);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeScore() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine.from(grid, -10, new Random());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullRandom() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine.from(grid, 0, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullSettings() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine.from(grid, 0, new Random(), null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullDirection() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid);
        engine.simulateMove(null);
    }

    @Test
    public void testSimulateMoveDoesNotSpawnTile() {
        int[][] grid = {{2, 0}, {2, 0}};
        Game2048Engine engine = Game2048Engine.from(grid);
        
        MoveResult result = engine.simulateMove(Direction.UP);
        assertTrue(result.moved());
        assertEquals(result.scoreGained(), 4);
        
        BoardState nextState = result.nextEngine().boardState();
        assertEquals(nextState.getValue(0, 0), 4);
        assertEquals(nextState.getValue(1, 0), 0);
        
        // No new tile should be spawned because it's just a simulation!
        int count = 0;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                if (nextState.getValue(r, c) != 0) {
                    count++;
                }
            }
        }
        assertEquals(count, 1); // Only the merged 4
    }

    @Test
    public void testMoveSpawnsTile() {
        int[][] grid = {{2, 0}, {2, 0}};
        Game2048Engine engine = Game2048Engine.from(grid);
        
        MoveResult result = engine.move(Direction.UP);
        assertTrue(result.moved());
        
        BoardState nextState = result.nextEngine().boardState();
        int count = 0;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                if (nextState.getValue(r, c) != 0) {
                    count++;
                }
            }
        }
        assertEquals(count, 2); // The merged 4 AND a new random tile
    }

    @Test
    public void testMoveWhenNoMovement() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid);
        
        MoveResult result = engine.move(Direction.UP);
        assertFalse(result.moved());
        assertEquals(result.scoreGained(), 0);
        
        // The engine returned should be identical
        assertEquals(result.nextEngine().score(), 0);
    }

    @Test
    public void testIsWon() {
        int[][] grid = {{2048, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid);
        assertTrue(engine.isWon());
    }

    @Test
    public void testIsGameOver() {
        int[][] grid = {{2, 4}, {8, 16}};
        Game2048Engine engine = Game2048Engine.from(grid);
        assertTrue(engine.isGameOver());
    }
}
