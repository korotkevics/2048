package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.MoveResult;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenEngine extends Stage<ThenEngine> {

    @ExpectedScenarioState
    private MoveResult lastResult;

    public ThenEngine the_board_has_moved() {
        assertThat(lastResult.moved()).isTrue();
        return this;
    }

    public ThenEngine the_board_has_not_moved() {
        assertThat(lastResult.moved()).isFalse();
        return this;
    }

    public ThenEngine the_score_gained_is(int expected) {
        assertThat(lastResult.scoreGained()).isEqualTo(expected);
        return this;
    }

    public ThenEngine the_total_score_is(int expected) {
        assertThat(lastResult.score()).isEqualTo(expected);
        return this;
    }

    public ThenEngine the_grid_is(int[][] expected) {
        assertThat(lastResult.boardState().grid()).isDeepEqualTo(expected);
        return this;
    }

    public ThenEngine the_game_is_won() {
        assertThat(lastResult.won()).isTrue();
        return this;
    }

    public ThenEngine the_game_is_over() {
        assertThat(lastResult.gameOver()).isTrue();
        return this;
    }

    public ThenEngine the_number_of_tiles_is(int expected) {
        int count = 0;
        int[][] grid = lastResult.boardState().grid();
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell != 0) count++;
            }
        }
        assertThat(count).isEqualTo(expected);
        return this;
    }
}
