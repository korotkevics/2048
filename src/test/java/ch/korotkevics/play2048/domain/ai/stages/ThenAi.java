package ch.korotkevics.play2048.domain.ai.stages;

import ch.korotkevics.play2048.domain.engine.Direction;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenAi extends Stage<ThenAi> {

    @ExpectedScenarioState
    private Optional<Direction> suggestion;

    public ThenAi a_suggestion_is_made() {
        assertThat(suggestion).isPresent();
        return this;
    }

    public ThenAi no_suggestion_is_made() {
        assertThat(suggestion).isEmpty();
        return this;
    }

    public ThenAi the_suggested_direction_is(Direction expected) {
        assertThat(suggestion).contains(expected);
        return this;
    }
}
