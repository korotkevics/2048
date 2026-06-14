package ch.korotkevics.play2048.domain.engine.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenBoardState extends Stage<ThenBoardState> {

    @ExpectedScenarioState
    private String toStringResult;

    @ExpectedScenarioState
    private boolean equalsResult;

    @ExpectedScenarioState
    private int hashCodeResult;

    @ExpectedScenarioState
    private int lastValue;

    public ThenBoardState the_string_contains(String text) {
        assertThat(toStringResult).contains(text);
        return this;
    }

    public ThenBoardState the_equality_result_is(boolean expected) {
        assertThat(equalsResult).isEqualTo(expected);
        return this;
    }

    public ThenBoardState the_hash_code_is_non_zero() {
        assertThat(hashCodeResult).isNotZero();
        return this;
    }

    public ThenBoardState the_value_is(int expected) {
        assertThat(lastValue).isEqualTo(expected);
        return this;
    }
}
