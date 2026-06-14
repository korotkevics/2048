package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.ai.stages.ThenAi;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.testng.ScenarioTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AiFacadeTest extends ScenarioTest<AiFacadeTest.GivenAiFacade, AiFacadeTest.WhenAiFacade, ThenAi> {

    @Test
    public void facade_delegates_to_deterministic_ai() {
        given().an_ai_facade_with_type(AiType.ALGO_BASED)
                .and().a_board_with_grid(new int[][] {
                        {2, 2, 0, 0},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0}
                });
        when().the_ai_is_asked_for_a_move();
        then().a_suggestion_is_made();
    }

    @Test
    public void facade_returns_empty_when_no_moves_available() {
        given().an_ai_facade_with_type(AiType.ALGO_BASED)
                .and().a_board_with_grid(new int[][] {
                        {2, 4, 2, 4},
                        {4, 2, 4, 2},
                        {2, 4, 2, 4},
                        {4, 2, 4, 2}
                });
        when().the_ai_is_asked_for_a_move();
        then().no_suggestion_is_made();
    }

    public static class GivenAiFacade extends Stage<GivenAiFacade> {
        @ProvidedScenarioState
        private AiFacade ai;
        @ProvidedScenarioState
        private BoardState boardState;
        @ProvidedScenarioState
        private UserSettings settings = new UserSettings();

        public GivenAiFacade an_ai_facade_with_type(AiType type) {
            MoveSuggester mockAlgo = mock(MoveSuggester.class);
            MoveSuggester mockLlm = mock(MoveSuggester.class);
            
            // Stub default behavior for mocks
            when(mockAlgo.suggestNextMove(any(), any(), any())).thenAnswer(invocation -> {
                BoardState bs = invocation.getArgument(0);
                if (bs.grid()[0][0] == 2 && bs.grid()[0][1] == 2) {
                    return Optional.of(Direction.LEFT);
                }
                return Optional.empty();
            });

            ai = new AiFacade(mockAlgo, mockLlm);
            settings.setAiType(type);
            return this;
        }

        public GivenAiFacade a_board_with_grid(int[][] grid) {
            boardState = new BoardState(grid);
            return this;
        }
    }

    public static class WhenAiFacade extends Stage<WhenAiFacade> {
        @ProvidedScenarioState
        private AiFacade ai;
        @ProvidedScenarioState
        private BoardState boardState;
        @ProvidedScenarioState
        private UserSettings settings;
        @ProvidedScenarioState
        private Optional<Direction> suggestion;

        public WhenAiFacade the_ai_is_asked_for_a_move() {
            suggestion = ai.suggestNextMove(boardState, settings, new GameSettings());
            return this;
        }
    }
}
