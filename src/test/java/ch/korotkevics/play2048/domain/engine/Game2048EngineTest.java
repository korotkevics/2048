package ch.korotkevics.play2048.domain.engine;

import ch.korotkevics.play2048.domain.engine.stages.GivenEngine;
import ch.korotkevics.play2048.domain.engine.stages.ThenEngine;
import ch.korotkevics.play2048.domain.engine.stages.WhenEngine;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import static ch.korotkevics.play2048.domain.engine.Direction.LEFT;

public class Game2048EngineTest extends ScenarioTest<GivenEngine, WhenEngine, ThenEngine> {

    @Test
    public void tiles_are_merged_correctly() {
        int[][] initial = {
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        // We simulate move which is deterministic
        given().a_game_from_grid(initial);
        when().a_move_is_made(LEFT);
        then().the_board_has_moved()
                .and().the_score_gained_is(4);
    }
}
