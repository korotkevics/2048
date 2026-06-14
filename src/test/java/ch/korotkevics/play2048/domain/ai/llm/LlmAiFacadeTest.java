package ch.korotkevics.play2048.domain.ai.llm;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.stages.ThenAi;
import ch.korotkevics.play2048.domain.ai.stages.WhenAi;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LlmAiFacadeTest extends ScenarioTest<LlmAiFacadeTest.GivenLlmAi, WhenAi, ThenAi> {

    @Test
    public void llm_ai_delegates_to_client() {
        given().a_llm_ai_with_client_that_suggests(Direction.RIGHT)
                .and().a_board_with_grid(new int[4][4]);
        when().the_ai_is_asked_for_a_move();
        then().the_suggested_direction_is(Direction.RIGHT);
    }

    @Test
    public void llm_ai_handles_empty_suggestion() {
        given().a_llm_ai_with_client_that_suggests(null)
                .and().a_board_with_grid(new int[4][4]);
        when().the_ai_is_asked_for_a_move();
        then().no_suggestion_is_made();
    }

    public static class GivenLlmAi extends Stage<GivenLlmAi> {
        @ProvidedScenarioState
        private MoveSuggester ai;
        @ProvidedScenarioState
        private BoardState boardState;

        public GivenLlmAi a_llm_ai_with_client_that_suggests(Direction direction) {
            LlmClient client = mock(LlmClient.class);
            org.mockito.Mockito.when(client.askForMove(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(Optional.ofNullable(direction));
            ai = new LlmAiFacade(client);
            return this;
        }

        public GivenLlmAi a_board_with_grid(int[][] grid) {
            boardState = new BoardState(grid);
            return this;
        }
    }
}
