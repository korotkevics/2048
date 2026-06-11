package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AiFacadeTest {

    @Test
    public void switchesBetweenSuggestersBasedOnSettings() {
        MoveSuggester deterministic = mock(MoveSuggester.class);
        MoveSuggester llm = mock(MoveSuggester.class);
        UserSettings settings = new UserSettings();
        BoardState state = new BoardState(new int[4][4]);

        AiFacade facade = new AiFacade(Map.of(
                UserSettings.AiType.DETERMINISTIC, deterministic,
                UserSettings.AiType.LLM, llm
        ), settings);

        // Default is DETERMINISTIC
        when(deterministic.suggestNextMove(state)).thenReturn(Optional.of(Direction.UP));
        assertThat(facade.suggestNextMove(state)).contains(Direction.UP);
        verify(deterministic).suggestNextMove(state);
        verifyNoInteractions(llm);

        // Switch to LLM
        settings.setAiType(UserSettings.AiType.LLM);
        when(llm.suggestNextMove(state)).thenReturn(Optional.of(Direction.LEFT));
        assertThat(facade.suggestNextMove(state)).contains(Direction.LEFT);
        verify(llm).suggestNextMove(state);
    }
}
