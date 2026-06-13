package ch.korotkevics.play2048.adapter.ws;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import ch.korotkevics.play2048.domain.service.DomainEventStream;
import ch.korotkevics.play2048.domain.service.GameId;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GameWebSocketAdapterTest {

    private static final String CLIENT_ID = "test-client";
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final GameWebSocketAdapter adapter = new GameWebSocketAdapter(messagingTemplate);

    @Test
    public void handlesGameStartedEvent() {
        DomainEventStream.GameStarted event = new DomainEventStream.GameStarted(CLIENT_ID, null);

        adapter.handleGameStarted(event);

        ArgumentCaptor<GameWebSocketAdapter.GameEvent> captor = ArgumentCaptor.forClass(GameWebSocketAdapter.GameEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + CLIENT_ID), captor.capture());
        
        assertThat(captor.getValue().type()).isEqualTo("STARTED");
        assertThat(captor.getValue().gameId()).isEqualTo(CLIENT_ID);
    }

    @Test
    public void handlesMoveMadeEvent() {
        MoveResult result = mock(MoveResult.class);
        DomainEventStream.MoveMade event = new DomainEventStream.MoveMade(CLIENT_ID, result);

        adapter.handleMoveMade(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + CLIENT_ID), eq(new GameWebSocketAdapter.GameEvent("MOVE", CLIENT_ID, result)));
    }

    @Test
    public void handlesAiSuggestionEvent() {
        DomainEventStream.AiSuggestionProduced event = new DomainEventStream.AiSuggestionProduced(CLIENT_ID, Direction.UP);

        adapter.handleAiSuggestion(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + CLIENT_ID), eq(new GameWebSocketAdapter.GameEvent("AI_SUGGESTION", CLIENT_ID, Direction.UP)));
    }
}
