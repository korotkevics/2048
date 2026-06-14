package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import java.util.List;
import static org.testng.Assert.*;

/**
 * Supplemental logic verification to kill mutations in internal mathematical invariants.
 * This suite bypasses JGiven proxies to ensure PIT tracks coverage correctly in JDK 24 environments.
 */
public class InternalMutationKillerTest {

    @Test
    public void line_processor_logic_exhaustively() {
        LineProcessor lp = new LineProcessor();
        
        // Basic merge
        assertEquals(lp.process(new int[]{2, 2, 0, 0}).values(), new int[]{4, 0, 0, 0});
        assertEquals(lp.process(new int[]{2, 2, 0, 0}).scoreGained(), 4);
        
        // Sliding
        assertEquals(lp.process(new int[]{0, 2, 0, 2}).values(), new int[]{4, 0, 0, 0});
        
        // Multiple merges
        LineProcessor.ProcessedLine res = lp.process(new int[]{2, 2, 2, 2});
        assertEquals(res.values(), new int[]{4, 4, 0, 0});
        assertEquals(res.scoreGained(), 8);
        
        // No merges
        assertEquals(lp.process(new int[]{2, 4, 8, 16}).values(), new int[]{2, 4, 8, 16});
        assertEquals(lp.process(new int[]{2, 4, 8, 16}).scoreGained(), 0);
        
        // Partial slide
        assertEquals(lp.process(new int[]{0, 0, 2, 4}).values(), new int[]{2, 4, 0, 0});
    }

    @Test
    public void board_boundary_checks() {
        int[][] grid = {
            {2, 4, 2, 4},
            {4, 2, 4, 2},
            {2, 4, 2, 4},
            {4, 2, 4, 2}
        };
        Board board = new Board(grid);
        assertTrue(board.isGameOver());
        assertFalse(board.hasTileWithValue(2048));
        assertEquals(board.emptyCells().size(), 0);
    }

    @Test
    public void game_settings_precision() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().update(2, 0.9);
        settings.getSpawnConfiguration().update(4, 0.1);
        settings.getSpawnConfiguration().validate();
        
        try {
            settings.getSpawnConfiguration().update(2, 0.8);
            settings.getSpawnConfiguration().validate();
            fail("Should have thrown IllegalStateException for sum 0.9");
        } catch (IllegalStateException e) {
            // Success
        }
    }
}
