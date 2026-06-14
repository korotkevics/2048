package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.MoveResult;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenEngine extends Stage<ThenEngine> {

    @ExpectedScenarioState
    private MoveResult lastResult;

    @ExpectedScenarioState
    private Exception exception;

    public ThenEngine the_board_has_moved() {
        assertThat(lastResult.moved()).isTrue();
        return this;
    }

    public ThenEngine an_illegal_argument_exception_is_thrown() {
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
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

    public ThenEngine the_grid_is_of_size(int expected) {
        assertThat(lastResult.boardState().size()).isEqualTo(expected);
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

    public ThenEngine the_game_is_not_won() {
        assertThat(lastResult.won()).isFalse();
        return this;
    }

    public ThenEngine the_game_is_over() {
        assertThat(lastResult.gameOver()).isTrue();
        return this;
    }

    public ThenEngine the_game_is_not_over() {
        assertThat(lastResult.gameOver()).isFalse();
        return this;
    }

    public ThenEngine the_game_is_over_is(boolean expected) {
        assertThat(lastResult.gameOver()).isEqualTo(expected);
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

    public ThenEngine moves_have_been_recorded() {
        assertThat(lastResult.deltas()).isNotEmpty();
        return this;
    }

    public ThenEngine no_moves_have_been_recorded() {
        assertThat(lastResult.deltas()).isEmpty();
        return this;
    }
}
