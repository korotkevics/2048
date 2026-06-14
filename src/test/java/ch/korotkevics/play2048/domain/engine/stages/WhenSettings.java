package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class WhenSettings extends Stage<WhenSettings> {

    @ExpectedScenarioState
    @ProvidedScenarioState
    private GameSettings settings;

    @ProvidedScenarioState
    private Exception exception;

    public WhenSettings the_initial_tile_count_is_set_to(int count) {
        try {
            settings.setInitialTileCount(count);
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public WhenSettings a_spawn_probability_is_updated(int value, double probability) {
        try {
            settings.getSpawnConfiguration().update(value, probability);
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public WhenSettings a_spawn_value_is_removed(int value) {
        try {
            settings.getSpawnConfiguration().remove(value);
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public WhenSettings the_configuration_is_validated() {
        try {
            settings.getSpawnConfiguration().validate();
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }
}
