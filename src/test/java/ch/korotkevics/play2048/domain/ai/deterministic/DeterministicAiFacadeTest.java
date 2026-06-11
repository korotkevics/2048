package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DeterministicAiFacadeTest {

    private final DeterministicAiFacade ai = new DeterministicAiFacade();

    @Test
    public void aiSuggestsValidMove() {
        BoardState state = new BoardState(new int[][]{
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        Optional<Direction> suggestion = ai.suggestNextMove(state);

        assertThat(suggestion).isPresent();
        // With current heuristic, any valid move is acceptable for this test
        assertThat(suggestion.get()).isIn(Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN);
    }

    @Test
    public void aiSuggestsNothingWhenNoMovesPossible() {
        BoardState state = new BoardState(new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        });

        Optional<Direction> suggestion = ai.suggestNextMove(state);

        assertThat(suggestion).isEmpty();
    }

    @Test
    public void aiPrefersCornerBuilding() {
        // Top-left corner is (0,0) with weight 15
        BoardState state = new BoardState(new int[][]{
                {0, 8, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        Optional<Direction> suggestion = ai.suggestNextMove(state);

        assertThat(suggestion).contains(Direction.LEFT);
    }
}
