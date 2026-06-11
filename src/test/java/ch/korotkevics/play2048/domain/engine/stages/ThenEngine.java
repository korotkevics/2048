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

    public ThenEngine the_score_gained_is(int expected) {
        assertThat(lastResult.scoreGained()).isEqualTo(expected);
        return this;
    }

    public ThenEngine the_grid_is(int[][] expected) {
        assertThat(lastResult.boardState().grid()).isDeepEqualTo(expected);
        return this;
    }
}
