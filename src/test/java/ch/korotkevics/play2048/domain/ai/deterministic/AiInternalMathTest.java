package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import org.testng.annotations.Test;
import java.util.Optional;
import static org.testng.Assert.*;

public class AiInternalMathTest {

    @Test
    public void expectimax_math_invariants() {
        DeterministicAiFacade ai = new DeterministicAiFacade();
        int[][] grid = {
            {2, 2, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };
        // Verify it doesn't crash and returns the obvious move
        Optional<Direction> move = ai.suggestNextMove(new BoardState(grid), new UserSettings(), new GameSettings());
        assertTrue(move.isPresent());
        assertEquals(move.get(), Direction.LEFT);
    }
}
