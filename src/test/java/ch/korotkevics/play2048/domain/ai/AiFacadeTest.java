package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.ai.stages.ThenAi;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AiFacadeTest extends ScenarioTest<AiFacadeTest.GivenAiFacade, AiFacadeTest.WhenAiFacade, ThenAi> {

    @Test
    public void ai_facade_switches_strategies_based_on_settings() {
        given().an_ai_facade_with_deterministic_and_llm_strategies()
                .and().a_board_with_grid(new int[4][4]);
        
        when().the_active_strategy_is_set_to(UserSettings.AiType.DETERMINISTIC)
                .and().the_ai_is_asked_for_a_move();
        then().the_suggested_direction_is(Direction.UP);

        when().the_active_strategy_is_set_to(UserSettings.AiType.LLM)
                .and().the_ai_is_asked_for_a_move();
        then().the_suggested_direction_is(Direction.DOWN);
    }

    public static class GivenAiFacade extends Stage<GivenAiFacade> {
        @ProvidedScenarioState
        private MoveSuggester ai;
        @ProvidedScenarioState
        private BoardState boardState;
        @ProvidedScenarioState
        private UserSettings settings;

        public GivenAiFacade an_ai_facade_with_deterministic_and_llm_strategies() {
            MoveSuggester det = mock(MoveSuggester.class);
            MoveSuggester llm = mock(MoveSuggester.class);
            settings = new UserSettings();
            
            org.mockito.Mockito.when(det.suggestNextMove(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.of(Direction.UP));
            org.mockito.Mockito.when(llm.suggestNextMove(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.of(Direction.DOWN));

            ai = new AiFacade(Map.of(
                    UserSettings.AiType.DETERMINISTIC, det,
                    UserSettings.AiType.LLM, llm
            ), settings);
            return this;
        }

        public GivenAiFacade a_board_with_grid(int[][] grid) {
            boardState = new BoardState(grid);
            return this;
        }
    }

    public static class WhenAiFacade extends Stage<WhenAiFacade> {
        @ExpectedScenarioState
        private MoveSuggester ai;
        @ExpectedScenarioState
        private BoardState boardState;
        @ExpectedScenarioState
        private UserSettings settings;
        @ProvidedScenarioState
        private Optional<Direction> suggestion;

        public WhenAiFacade the_active_strategy_is_set_to(UserSettings.AiType type) {
            settings.setAiType(type);
            return this;
        }

        public WhenAiFacade the_ai_is_asked_for_a_move() {
            suggestion = ai.suggestNextMove(boardState);
            return this;
        }
    }
}
