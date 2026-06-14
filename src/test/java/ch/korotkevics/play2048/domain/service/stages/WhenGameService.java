package ch.korotkevics.play2048.domain.service.stages;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.service.GameService;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import java.util.Optional;

public class WhenGameService extends Stage<WhenGameService> {

    @ExpectedScenarioState
    private GameService gameService;

    @ExpectedScenarioState
    private String clientId;

    @ProvidedScenarioState
    private MoveResult moveResult;

    @ProvidedScenarioState
    private Optional<MoveResult> undoResult;

    public WhenGameService a_new_game_is_started() {
        moveResult = gameService.startNewGame(clientId);
        return this;
    }

    public WhenGameService a_move_is_made_in_direction(Direction direction) {
        moveResult = gameService.makeMove(clientId, direction);
        return this;
    }

    public WhenGameService an_ai_suggestion_is_requested() {
        gameService.requestAiSuggestion(clientId);
        return this;
    }

    public WhenGameService an_undo_is_requested() {
        undoResult = gameService.undo(clientId);
        return this;
    }

    public WhenGameService settings_are_updated(UserSettings userSettings, GameSettings gameSettings) {
        gameService.updateSettings(clientId, userSettings, gameSettings);
        return this;
    }

    public WhenGameService auto_play_is_started() {
        gameService.startAutoPlay(clientId);
        return this;
    }

    public WhenGameService auto_play_is_stopped() {
        gameService.stopAutoPlay(clientId);
        return this;
    }

    public WhenGameService the_game_is_abandoned() {
        gameService.abandonGame(clientId);
        return this;
    }

    public WhenGameService the_stale_games_are_cleaned_up() {
        gameService.cleanupStaleGames();
        return this;
    }
}
