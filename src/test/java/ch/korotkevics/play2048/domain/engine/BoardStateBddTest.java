package ch.korotkevics.play2048.domain.engine;

import ch.korotkevics.play2048.domain.engine.stages.GivenBoardState;
import ch.korotkevics.play2048.domain.engine.stages.ThenBoardState;
import ch.korotkevics.play2048.domain.engine.stages.WhenBoardState;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

public class BoardStateBddTest extends ScenarioTest<GivenBoardState, WhenBoardState, ThenBoardState> {

    @Test
    public void board_state_has_meaningful_string_representation() {
        int[][] grid = {
                {2, 0},
                {0, 4}
        };
        given().a_board_state_from_grid(grid);
        when().the_string_representation_is_requested();
        then().the_string_contains("2")
                .and().the_string_contains("4");
    }

    @Test
    public void board_state_equality_works() {
        int[][] grid1 = {{2, 2}, {2, 2}};
        int[][] grid2 = {{2, 2}, {2, 2}};
        int[][] grid3 = {{4, 2}, {2, 2}};

        given().a_board_state_from_grid(grid1);
        when().it_is_compared_to(new BoardState(grid2));
        then().the_equality_result_is(true);

        when().it_is_compared_to(new BoardState(grid3));
        then().the_equality_result_is(false);

        when().it_is_compared_to(null);
        then().the_equality_result_is(false);

        when().it_is_compared_to("not a board state");
        then().the_equality_result_is(false);
    }

    @Test
    public void board_state_has_hash_code() {
        given().a_board_state_from_grid(new int[2][2]);
        when().the_hash_code_is_requested();
        then().the_hash_code_is_non_zero();
    }

    @Test
    public void board_state_allows_adding_tiles() {
        given().a_board_state_from_grid(new int[2][2]);
        when().a_tile_is_added_at(0, 0, 2)
                .and().the_value_at_is_requested(0, 0);
        then().the_value_is(2);
    }
}
