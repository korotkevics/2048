package ch.korotkevics.play2048.domain.ai.stages;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import java.util.Optional;

public class WhenAi extends Stage<WhenAi> {

    @ExpectedScenarioState
    private MoveSuggester ai;

    @ExpectedScenarioState
    private BoardState boardState;

    @ExpectedScenarioState
    private UserSettings settings;

    @ExpectedScenarioState
    private GameSettings gameSettings;

    @ProvidedScenarioState
    private Optional<Direction> suggestion;

    public WhenAi the_ai_is_asked_for_a_move() {
        if (gameSettings == null) {
            gameSettings = new GameSettings();
        }
        suggestion = ai.suggestNextMove(boardState, settings, gameSettings);
        return this;
    }
}
