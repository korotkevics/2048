package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class GivenSettings extends Stage<GivenSettings> {

    @ProvidedScenarioState
    private GameSettings settings;

    public GivenSettings default_settings() {
        settings = new GameSettings();
        return this;
    }
}
