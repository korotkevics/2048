package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenSettings extends Stage<ThenSettings> {

    @ExpectedScenarioState
    private GameSettings settings;

    @ExpectedScenarioState
    private Exception exception;

    public ThenSettings the_initial_tile_count_is(int expected) {
        assertThat(settings.getInitialTileCount()).isEqualTo(expected);
        return this;
    }

    public ThenSettings the_spawn_probability_for_value_is(int value, double expected) {
        assertThat(settings.getSpawnConfiguration().getProbabilities().get(value)).isEqualTo(expected);
        return this;
    }

    public ThenSettings the_spawn_configuration_does_not_contain(int value) {
        assertThat(settings.getSpawnConfiguration().getProbabilities()).doesNotContainKey(value);
        return this;
    }

    public ThenSettings an_illegal_argument_exception_is_thrown() {
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        return this;
    }

    public ThenSettings an_illegal_state_exception_is_thrown() {
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        return this;
    }

    public ThenSettings no_exception_is_thrown() {
        assertThat(exception).isNull();
        return this;
    }
}
