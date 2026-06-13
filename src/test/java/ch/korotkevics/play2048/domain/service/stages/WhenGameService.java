package ch.korotkevics.play2048.domain.service.stages;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.service.GameId;
import ch.korotkevics.play2048.domain.service.GameService;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class WhenGameService extends Stage<WhenGameService> {

    private static final String CLIENT_ID = "test-client";

    @ExpectedScenarioState
    private GameService gameService;

    @ProvidedScenarioState
    private GameId gameId;

    public WhenGameService a_new_game_is_started() {
        gameId = gameService.startNewGame(CLIENT_ID);
        return this;
    }

    public WhenGameService a_move_is_made_in_direction(Direction direction) {
        gameService.makeMove(CLIENT_ID, direction);
        return this;
    }

    public WhenGameService an_ai_suggestion_is_requested() {
        gameService.requestAiSuggestion(CLIENT_ID);
        return this;
    }
}
