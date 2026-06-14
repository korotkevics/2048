package ch.korotkevics.play2048.domain.engine;

import ch.korotkevics.play2048.domain.engine.stages.GivenEngine;
import ch.korotkevics.play2048.domain.engine.stages.ThenEngine;
import ch.korotkevics.play2048.domain.engine.stages.WhenEngine;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import static ch.korotkevics.play2048.domain.engine.Direction.LEFT;
import static ch.korotkevics.play2048.domain.engine.Direction.UP;

public class Game2048EngineTest extends ScenarioTest<GivenEngine, WhenEngine, ThenEngine> {

    @Test
    public void tiles_are_merged_correctly() {
        int[][] initial = {
                {2, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_made(LEFT);
        then().the_board_has_moved()
                .and().the_score_gained_is(4);
    }

    @Test
    public void complex_merges_and_gaps_are_handled() {
        int[][] initial = {
                {2, 0, 2, 4},
                {4, 4, 8, 8},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        int[][] expected = {
                {4, 4, 0, 0},
                {8, 16, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(expected)
                .and().the_score_gained_is(4 + 8 + 16);
    }

    @Test
    public void winning_condition_is_detected() {
        int[][] initial = {
                {1024, 1024, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_made(LEFT);
        then().the_game_is_won();

        // Check negative case
        int[][] notWon = {{2, 2}, {2, 2}};
        given().a_game_from_grid(notWon);
        when().a_move_is_simulated(UP);
        then().the_game_is_not_won();
    }

    @Test
    public void game_over_condition_is_detected() {
        int[][] initial = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };
        // Any move on this grid should result in game over if no merges possible
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(Direction.RIGHT);
        then().the_game_is_over();

        // Check negative case
        int[][] notGameOver = {{2, 2}, {0, 0}};
        given().a_game_from_grid(notGameOver);
        when().a_move_is_simulated(UP);
        then().the_game_is_not_over();
    }

    @Test
    public void move_spawns_a_new_tile_deterministically() {
        int[][] initial = {
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        // Seed 42 might spawn a tile at a specific place.
        given().a_game_with_random_seed(initial, 42);
        when().a_move_is_made(Direction.DOWN);
        then().the_board_has_moved()
                .and().the_number_of_tiles_is(2);
    }

    @Test
    public void simulate_move_does_not_spawn_a_new_tile() {
        int[][] initial = {
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(Direction.DOWN);
        then().the_board_has_moved()
                .and().the_number_of_tiles_is(1);
    }

    @Test
    public void no_movement_returns_moved_false() {
        int[][] initial = {
                {2, 4, 8, 16},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_made(UP);
        then().the_board_has_not_moved()
                .and().the_score_gained_is(0)
                .and().no_moves_have_been_recorded();
    }

    @Test
    public void complex_merges_in_different_directions() {
        // Test RIGHT merge
        int[][] initial = {
                {0, 0, 2, 2},
                {2, 0, 2, 0},
                {2, 2, 2, 2},
                {4, 4, 8, 8}
        };
        int[][] expected = {
                {0, 0, 0, 4},
                {0, 0, 0, 4},
                {0, 0, 4, 4},
                {0, 0, 8, 16}
        };
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(Direction.RIGHT);
        then().the_grid_is(expected)
                .and().the_score_gained_is(4 + 4 + 4 + 4 + 8 + 16)
                .and().moves_have_been_recorded();
    }

    @Test
    public void engine_validates_grid_structure() {
        int[][] nonSquare = {
                {2, 2},
                {2, 2, 2}
        };
        given().a_game_from_grid(nonSquare);
        then().an_illegal_argument_exception_is_thrown();

        int[][] invalidPowerOfTwo = {
                {2, 3},
                {2, 2}
        };
        given().a_game_from_grid(invalidPowerOfTwo);
        then().an_illegal_argument_exception_is_thrown();

        int[][] negativeValue = {
                {-2, 2},
                {2, 2}
        };
        given().a_game_from_grid(negativeValue);
        then().an_illegal_argument_exception_is_thrown();
    }

    @Test
    public void new_game_spawns_exact_initial_tiles() {
        GameSettings settings = new GameSettings();
        settings.setInitialTileCount(2);
        
        given().a_game_with_settings(new int[4][4], settings);
        then().the_number_of_tiles_is(2);
    }

    @Test
    public void engine_handles_all_directions() {
        int[][] initial = {
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        
        given().a_game_from_grid(initial);
        
        when().a_move_is_simulated(Direction.RIGHT);
        then().the_grid_is(new int[][]{{0,0,0,2},{0,0,0,0},{0,0,0,0},{0,0,0,0}});
        
        when().a_move_is_simulated(Direction.DOWN);
        then().the_grid_is(new int[][]{{0,0,0,0},{0,0,0,0},{0,0,0,0},{2,0,0,0}});
        
        when().a_move_is_simulated(Direction.UP);
        then().the_grid_is(new int[][]{{2,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}});
    }

    @Test
    public void multiple_merges_in_one_line() {
        int[][] initial = {{2, 2, 2, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] expected = {{4, 4, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(expected).and().the_score_gained_is(8);
    }
}
