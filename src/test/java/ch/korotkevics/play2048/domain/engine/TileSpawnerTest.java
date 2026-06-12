package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import java.util.Random;
import static org.testng.Assert.*;

public class TileSpawnerTest {

    private final TileSpawner spawner = new TileSpawner();

    @Test
    public void testSpawnInitialTiles() {
        Board board = new Board(4);
        GameSettings settings = new GameSettings();
        settings.setInitialTileCount(2);
        
        Random random = new Random(42); // deterministic
        Board newBoard = spawner.spawnInitialTiles(board, settings, random);
        
        assertEquals(newBoard.emptyCells().size(), 14);
        
        int twoCount = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (newBoard.getValue(r, c) == 2) {
                    twoCount++;
                }
            }
        }
        assertEquals(twoCount, 2);
    }

    @Test
    public void testSpawnRandomTile() {
        Board board = new Board(4);
        GameSettings settings = new GameSettings();
        
        Random random2 = new Random() {
            @Override
            public double nextDouble() {
                return 0.5; // less than 0.9, should spawn 2
            }
            @Override
            public int nextInt(int bound) {
                return 0; // always pick first empty cell
            }
        };
        
        Board newBoard = spawner.spawnRandomTile(board, settings, random2);
        assertEquals(newBoard.getValue(0, 0), 2);
        
        Random random4 = new Random() {
            @Override
            public double nextDouble() {
                return 0.95; // greater than 0.9, should spawn 4
            }
            @Override
            public int nextInt(int bound) {
                return 0; // always pick first empty cell (which is 0,1 since 0,0 is taken)
            }
        };
        
        Board newerBoard = spawner.spawnRandomTile(newBoard, settings, random4);
        assertEquals(newerBoard.getValue(0, 1), 4);
    }

    @Test
    public void testSpawnOnFullBoard() {
        int[][] fullGrid = {{2, 4}, {8, 16}};
        Board board = new Board(fullGrid);
        GameSettings settings = new GameSettings();
        Random random = new Random();
        
        Board newBoard = spawner.spawnRandomTile(board, settings, random);
        assertEquals(newBoard, board); // same instance or identical board
        assertEquals(newBoard.emptyCells().size(), 0);
    }
    
    @Test
    public void testSpawnWithEmptyProbabilities() {
        Board board = new Board(2);
        GameSettings settings = new GameSettings();
        
        try {
            java.lang.reflect.Field field = GameSettings.TileSpawnConfiguration.class.getDeclaredField("probabilities");
            field.setAccessible(true);
            java.util.Map map = (java.util.Map) field.get(settings.getSpawnConfiguration());
            map.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0; 
            }
        };
        Board newBoard = spawner.spawnRandomTile(board, settings, random);
        assertEquals(newBoard.getValue(0, 0), 2);
    }
}
