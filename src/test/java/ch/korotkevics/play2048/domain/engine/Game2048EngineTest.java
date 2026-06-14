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
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(Direction.RIGHT);
        then().the_game_is_over();

        int[][] notGameOver = {{2, 2}, {0, 0}};
        given().a_game_from_grid(notGameOver);
        when().a_move_is_simulated(UP);
        then().the_game_is_not_over();
    }

    @Test
    public void game_is_not_over_if_merges_are_possible() {
        int[][] horizontalMerge = {
                {2, 2, 4, 8},
                {4, 8, 16, 32},
                {8, 16, 32, 64},
                {16, 32, 64, 128}
        };
        given().a_game_from_grid(horizontalMerge);
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
    public void board_does_not_move_if_blocked() {
        int[][] blocked = {
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 2, 4},
                {8, 16, 32, 64}
        };
        given().a_game_from_grid(blocked);
        when().a_move_is_made(UP);
        then().the_board_has_not_moved()
                .and().the_score_gained_is(0);
    }

    @Test
    public void complex_merges_in_different_directions() {
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
        int[][] nonSquare = {{2, 2}, {2, 2, 2}};
        given().a_game_from_grid(nonSquare);
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
    public void multiple_merges_in_one_line() {
        int[][] initial = {{2, 2, 2, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        int[][] expected = {{4, 4, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid(initial);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(expected)
                .and().the_score_gained_is(8)
                .and().the_number_of_move_deltas_is(4);
    }

    @Test
    public void various_merge_scenarios_to_kill_mutants() {
        // 0, 2, 2, 2 -> 4, 2, 0, 0
        int[][] line1 = {{0, 2, 2, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid(line1);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(new int[][]{{4, 2, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}});

        // 2, 2, 0, 2 -> 4, 2, 0, 0
        int[][] line2 = {{2, 2, 0, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid(line2);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(new int[][]{{4, 2, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}});

        // 2, 4, 8, 16 (blocked)
        int[][] line3 = {{2, 4, 8, 16}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid(line3);
        when().a_move_is_simulated(LEFT);
        then().the_board_has_not_moved();
    }

    @Test
    public void score_accumulates_correctly() {
        int[][] initial = {{2, 2, 0, 0}, {2, 2, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid_with_score(initial, 100);
        when().a_move_is_made(LEFT);
        then().the_total_score_is(108);
    }

    @Test
    public void move_spawns_tile_with_correct_value_based_on_probability() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().update(2, 0.0);
        settings.getSpawnConfiguration().update(4, 1.0);
        int[][] initial = {{2, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        given().a_game_from_grid_with_settings(initial, settings);
        when().a_move_is_made(Direction.DOWN);
        then().the_total_sum_of_tiles_is(6);
    @Test
    public void engine_handles_different_board_sizes() {
        // 2x2 board
        int[][] grid2x2 = {{2, 2}, {0, 0}};
        given().a_game_from_grid(grid2x2);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(new int[][]{{4, 0}, {0, 0}});
        
        // 3x3 board
        int[][] grid3x3 = {{2, 2, 2}, {0, 0, 0}, {0, 0, 0}};
        given().a_game_from_grid(grid3x3);
        when().a_move_is_simulated(LEFT);
        then().the_grid_is(new int[][]{{4, 2, 0}, {0, 0, 0}, {0, 0, 0}});
    }
}
