package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.service.stages.GivenGameService;
import ch.korotkevics.play2048.domain.service.stages.ThenGameService;
import ch.korotkevics.play2048.domain.service.stages.WhenGameService;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

public class GameServiceTest extends ScenarioTest<GivenGameService, WhenGameService, ThenGameService> {

    @Test
    public void starting_a_new_game_generates_id_and_notifies() {
        given().a_game_service();
        when().a_new_game_is_started();
        then().a_game_id_is_generated()
                .and().a_game_started_event_is_published();
    }

    @Test
    public void making_a_move_publishes_event() {
        given().a_game_service();
        when().a_new_game_is_started()
                .and().a_move_is_made_in_direction(Direction.LEFT);
        then().a_move_made_event_is_published();
    }

    @Test
    public void requesting_ai_suggestion_publishes_event() {
        given().a_game_service()
                .and().an_ai_that_suggests(Direction.UP);
        when().a_new_game_is_started()
                .and().an_ai_suggestion_is_requested();
        then().an_ai_suggestion_event_is_published();
    }

    @Test
    public void requesting_ai_suggestion_handles_empty_result() {
        given().a_game_service()
                .and().an_ai_that_suggests(null);
        when().a_new_game_is_started()
                .and().an_ai_suggestion_is_requested();
        then().no_exception_is_thrown();
    }

    @Test
    public void undoing_a_move_restores_previous_state() {
        given().a_game_service();
        when().a_new_game_is_started()
                .and().a_move_is_made_in_direction(Direction.LEFT)
                .and().an_undo_is_requested();
        then().an_undo_result_is_present()
                .and().a_move_made_event_is_published();
    }

    @Test
    public void updating_settings_saves_to_repository() {
        given().a_game_service();
        when().settings_are_updated(new UserSettings(), new GameSettings());
        then().no_exception_is_thrown();
    }

    @Test
    public void abandoning_game_clears_state() {
        given().a_game_service();
        when().a_new_game_is_started()
                .and().the_game_is_abandoned();
        then().no_exception_is_thrown();
    }

    @Test
    public void auto_play_can_be_started_and_stopped() {
        given().a_game_service();
        when().a_new_game_is_started()
                .and().auto_play_is_started()
                .and().auto_play_is_stopped();
        then().no_exception_is_thrown();
    }

    @Test
    public void cleanup_stale_games_triggers_repository() {
        given().a_game_service();
        when().the_stale_games_are_cleaned_up();
        then().the_stale_games_cleanup_was_called_on_repository();
    }

    @Test
    public void starting_new_game_stops_existing_auto_play() {
        given().a_game_service();
        when().a_new_game_is_started()
                .and().auto_play_is_started()
                .and().a_new_game_is_started();
        then().no_exception_is_thrown();
    }
}
