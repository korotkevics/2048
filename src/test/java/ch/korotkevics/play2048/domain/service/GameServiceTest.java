package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Test
    public void managesIndependentGames() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        GameService service = new GameService(mockAi);

        GameId id1 = service.startNewGame();
        GameId id2 = service.startNewGame();

        assertThat(id1).isNotEqualTo(id2);

        Optional<MoveResult> result1 = service.makeMove(id1, Direction.LEFT);
        Optional<MoveResult> result2 = service.makeMove(id2, Direction.RIGHT);

        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().direction()).isEqualTo(Direction.LEFT);
        assertThat(result2.get().direction()).isEqualTo(Direction.RIGHT);
    }

    @Test
    public void providesAiSuggestionsForActiveGames() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        GameService service = new GameService(mockAi);
        GameId id = service.startNewGame();

        when(mockAi.suggestNextMove(any())).thenReturn(Optional.of(Direction.UP));

        Optional<Direction> suggestion = service.getAiSuggestion(id);

        assertThat(suggestion).contains(Direction.UP);
    }

    @Test
    public void handlesUnknownGamesGracefully() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        GameService service = new GameService(mockAi);
        GameId unknownId = GameId.generate();

        assertThat(service.makeMove(unknownId, Direction.UP)).isEmpty();
        assertThat(service.getAiSuggestion(unknownId)).isEmpty();
    }

    @Test
    public void canAbandonGames() {
        GameService service = new GameService(mock(MoveSuggester.class));
        GameId id = service.startNewGame();

        service.abandonGame(id);

        assertThat(service.makeMove(id, Direction.UP)).isEmpty();
    }
}
