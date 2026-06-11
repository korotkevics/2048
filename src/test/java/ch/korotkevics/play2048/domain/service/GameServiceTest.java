package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.Direction;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Test
    public void managesIndependentGames() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        DomainEventStream mockStream = mock(DomainEventStream.class);
        GameService service = new GameService(mockAi, mockStream);

        GameId id1 = service.startNewGame();
        GameId id2 = service.startNewGame();

        assertThat(id1).isNotEqualTo(id2);
        verify(mockStream, atLeastOnce()).publish(any(DomainEventStream.GameStarted.class));

        service.makeMove(id1, Direction.LEFT);
        service.makeMove(id2, Direction.RIGHT);

        verify(mockStream, atLeastOnce()).publish(any(DomainEventStream.MoveMade.class));
    }

    @Test
    public void providesAiSuggestionsForActiveGames() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        DomainEventStream mockStream = mock(DomainEventStream.class);
        GameService service = new GameService(mockAi, mockStream);
        GameId id = service.startNewGame();

        when(mockAi.suggestNextMove(any())).thenReturn(Optional.of(Direction.UP));

        service.requestAiSuggestion(id);

        verify(mockStream).publish(any(DomainEventStream.AiSuggestionProduced.class));
    }

    @Test
    public void handlesUnknownGamesGracefully() {
        MoveSuggester mockAi = mock(MoveSuggester.class);
        DomainEventStream mockStream = mock(DomainEventStream.class);
        GameService service = new GameService(mockAi, mockStream);
        GameId unknownId = GameId.generate();

        service.makeMove(unknownId, Direction.UP);
        service.requestAiSuggestion(unknownId);

        // Should not publish move or suggestion for unknown games.
        // We verify that NO events were published AFTER the constructor phase.
        verifyNoMoreInteractions(mockStream);
    }

    @Test
    public void canAbandonGames() {
        GameService service = new GameService(mock(MoveSuggester.class), mock(DomainEventStream.class));
        GameId id = service.startNewGame();

        service.abandonGame(id);
    }
}
