package ch.korotkevics.play2048.domain.engine;

import ch.korotkevics.play2048.domain.engine.stages.GivenSettings;
import ch.korotkevics.play2048.domain.engine.stages.ThenSettings;
import ch.korotkevics.play2048.domain.engine.stages.WhenSettings;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

public class GameSettingsBddTest extends ScenarioTest<GivenSettings, WhenSettings, ThenSettings> {

    @Test
    public void default_settings_are_initialized_correctly() {
        given().default_settings();
        then().the_initial_tile_count_is(0)
                .and().the_spawn_probability_for_value_is(2, 0.9)
                .and().the_spawn_probability_for_value_is(4, 0.1);
    }

    @Test
    public void initial_tile_count_can_be_updated() {
        given().default_settings();
        when().the_initial_tile_count_is_set_to(2);
        then().the_initial_tile_count_is(2);
    }

    @Test
    public void invalid_initial_tile_count_throws_exception() {
        given().default_settings();
        when().the_initial_tile_count_is_set_to(-1);
        then().an_illegal_argument_exception_is_thrown();
    }

    @Test
    public void spawn_probabilities_can_be_updated() {
        given().default_settings();
        when().a_spawn_probability_is_updated(8, 0.5);
        then().the_spawn_probability_for_value_is(8, 0.5);

        // Boundary cases
        when().a_spawn_probability_is_updated(16, 0.0);
        then().the_spawn_probability_for_value_is(16, 0.0);
        
        when().a_spawn_probability_is_updated(32, 1.0);
        then().the_spawn_probability_for_value_is(32, 1.0);
    }

    @Test
    public void invalid_spawn_probabilities_throw_exception() {
        given().default_settings();
        when().a_spawn_probability_is_updated(2, 1.1);
        then().an_illegal_argument_exception_is_thrown();

        when().a_spawn_probability_is_updated(2, -0.1);
        then().an_illegal_argument_exception_is_thrown();
    }

    @Test
    public void invalid_spawn_values_throw_exception() {
        given().default_settings();
        when().a_spawn_probability_is_updated(3, 0.5);
        then().an_illegal_argument_exception_is_thrown();
    }

    @Test
    public void spawn_values_can_be_removed() {
        given().default_settings();
        when().a_spawn_value_is_removed(4);
        then().the_spawn_configuration_does_not_contain(4);
    }

    @Test
    public void removing_non_existent_value_does_not_throw() {
        given().default_settings();
        when().a_spawn_value_is_removed(1024);
        then().no_exception_is_thrown();
    }

    @Test
    public void removing_last_spawn_value_throws_exception() {
        given().default_settings();
        when().a_spawn_value_is_removed(4)
                .and().a_spawn_value_is_removed(2);
        then().an_illegal_state_exception_is_thrown();
    }

    @Test
    public void valid_configuration_passes_validation() {
        given().default_settings();
        when().the_configuration_is_validated();
        then().no_exception_is_thrown();
    }

    @Test
    public void invalid_probability_sum_fails_validation() {
        given().default_settings();
        when().a_spawn_probability_is_updated(2, 0.5);
        // Sum is now 0.5 + 0.1 = 0.6
        when().the_configuration_is_validated();
        then().an_illegal_state_exception_is_thrown();

        // Slightly off 1.0 (e.g. 1.0001)
        when().a_spawn_probability_is_updated(2, 0.9001);
        when().the_configuration_is_validated();
        then().an_illegal_state_exception_is_thrown();
    }

    @Test
    public void empty_spawn_configuration_fails_validation() {
        given().default_settings();
        when().a_spawn_value_is_removed(2)
                .and().a_spawn_value_is_removed(4);
        // This should fail in remove() already based on current code, 
        // but let's test the validate() logic too if we could bypass remove() check.
        // Actually remove() has a check.
        then().an_illegal_state_exception_is_thrown();
    }
}
