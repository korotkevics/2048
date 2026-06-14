package ch.korotkevics.play2048.domain.ai.stages;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.ai.deterministic.DeterministicAiFacade;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import java.util.Optional;

public class GivenAi extends Stage<GivenAi> {

    @ProvidedScenarioState
    private MoveSuggester ai;

    @ProvidedScenarioState
    private BoardState boardState;

    @ProvidedScenarioState
    private UserSettings settings = new UserSettings();

    @ProvidedScenarioState
    private GameSettings gameSettings = new GameSettings();

    public GivenAi a_deterministic_ai() {
        ai = new DeterministicAiFacade();
        return this;
    }

    public GivenAi a_board_with_grid(int[][] grid) {
        boardState = new BoardState(grid);
        return this;
    }
}
