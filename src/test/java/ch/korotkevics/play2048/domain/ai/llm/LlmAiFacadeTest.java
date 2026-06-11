package ch.korotkevics.play2048.domain.ai.llm;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LlmAiFacadeTest {

    @Test
    public void suggestNextMoveDelegatesToLlmClient() {
        LlmClient mockClient = mock(LlmClient.class);
        LlmAiFacade facade = new LlmAiFacade(mockClient);
        BoardState state = new BoardState(new int[4][4]);
        
        when(mockClient.askForMove(state)).thenReturn(Optional.of(Direction.UP));

        Optional<Direction> suggestion = facade.suggestNextMove(state);

        assertThat(suggestion).contains(Direction.UP);
    }
}
