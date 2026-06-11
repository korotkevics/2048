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

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final GameWebSocketAdapter adapter = new GameWebSocketAdapter(messagingTemplate);

    @Test
    public void handlesGameStartedEvent() {
        GameId id = GameId.generate();
        DomainEventStream.GameStarted event = new DomainEventStream.GameStarted(id);

        adapter.handleGameStarted(event);

        ArgumentCaptor<GameWebSocketAdapter.GameEvent> captor = ArgumentCaptor.forClass(GameWebSocketAdapter.GameEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + id.value()), captor.capture());
        
        assertThat(captor.getValue().type()).isEqualTo("STARTED");
        assertThat(captor.getValue().gameId()).isEqualTo(id.value().toString());
    }

    @Test
    public void handlesMoveMadeEvent() {
        GameId id = GameId.generate();
        MoveResult result = mock(MoveResult.class);
        DomainEventStream.MoveMade event = new DomainEventStream.MoveMade(id, result);

        adapter.handleMoveMade(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + id.value()), eq(new GameWebSocketAdapter.GameEvent("MOVE", id.value().toString(), result)));
    }

    @Test
    public void handlesAiSuggestionEvent() {
        GameId id = GameId.generate();
        DomainEventStream.AiSuggestionProduced event = new DomainEventStream.AiSuggestionProduced(id, Direction.UP);

        adapter.handleAiSuggestion(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + id.value()), eq(new GameWebSocketAdapter.GameEvent("AI_SUGGESTION", id.value().toString(), Direction.UP)));
    }
}
