package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.stages.GivenAi;
import ch.korotkevics.play2048.domain.ai.stages.ThenAi;
import ch.korotkevics.play2048.domain.ai.stages.WhenAi;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import static ch.korotkevics.play2048.domain.engine.Direction.LEFT;
import static ch.korotkevics.play2048.domain.engine.Direction.UP;
import static ch.korotkevics.play2048.domain.engine.Direction.RIGHT;

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

    @Test
    public void deterministic_ai_prefers_corner_locking() {
        // Here moving UP would put the 1024 into a higher weight cell (0,0)
        int[][] grid = {
                {0, 0, 0, 0},
                {1024, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_deterministic_ai()
                .and().a_board_with_grid(grid);
        when().the_ai_is_asked_for_a_move();
        then().the_suggested_direction_is(UP);
    }

    @Test
    public void deterministic_ai_avoids_game_over() {
        int[][] grid = {
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 0, 512}, // can merge RIGHT
                {2, 4, 8, 16}
        };
        given().a_deterministic_ai()
                .and().a_board_with_grid(grid);
        when().the_ai_is_asked_for_a_move();
        then().the_suggested_direction_is(RIGHT);
    }

    @Test
    public void deterministic_ai_returns_empty_when_no_moves() {
        int[][] grid = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };
        given().a_deterministic_ai()
                .and().a_board_with_grid(grid);
        when().the_ai_is_asked_for_a_move();
        then().no_suggestion_is_made();
    }

    @Test
    public void deterministic_ai_adapts_to_custom_probabilities() {
        // Grid where moving LEFT merges two 2s.
        // But if 1024 spawns with 100% probability, maybe it prefers another move?
        // Actually, just changing probabilities and verifying it doesn't crash 
        // and returns a valid move is a start.
        int[][] grid = {
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        ch.korotkevics.play2048.domain.engine.GameSettings settings = new ch.korotkevics.play2048.domain.engine.GameSettings();
        settings.getSpawnConfiguration().update(2, 0.1);
        settings.getSpawnConfiguration().update(1024, 0.9);

        given().a_deterministic_ai()
                .and().a_board_with_grid(grid)
                .and().custom_game_settings(settings);
        when().the_ai_is_asked_for_a_move();
        then().a_suggestion_is_made();
    }

    @Test
    public void deterministic_ai_handles_complex_merges_internally() {
        // This test ensures the internal simulateMoveFast (with processLine) works
        int[][] grid = {
                {2, 2, 2, 2},
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
