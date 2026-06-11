package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.stages.GivenAi;
import ch.korotkevics.play2048.domain.ai.stages.ThenAi;
import ch.korotkevics.play2048.domain.ai.stages.WhenAi;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import static ch.korotkevics.play2048.domain.engine.Direction.LEFT;

public class DeterministicAiFacadeTest extends ScenarioTest<GivenAi, WhenAi, ThenAi> {

    @Test
    public void deterministic_ai_suggests_a_valid_move() {
        int[][] grid = {
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_deterministic_ai()
                .and().a_board_with_grid(grid);
        when().the_ai_is_asked_for_a_move();
        then().a_suggestion_is_made()
                .and().the_suggested_direction_is(LEFT);
    }
}
