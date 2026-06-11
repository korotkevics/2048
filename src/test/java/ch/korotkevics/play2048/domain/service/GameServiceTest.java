package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.engine.Direction;
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
}
