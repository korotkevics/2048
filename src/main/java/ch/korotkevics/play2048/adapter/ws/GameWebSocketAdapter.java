package ch.korotkevics.play2048.adapter.ws;

import ch.korotkevics.play2048.domain.service.DomainEventStream;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public final class GameWebSocketAdapter {

    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleGameStarted(DomainEventStream.GameStarted event) {
        publish(event.gameId().value().toString(), new GameEvent("STARTED", event.gameId().value().toString(), event.initialBoard()));
    }

    @EventListener
    public void handleMoveMade(DomainEventStream.MoveMade event) {
        publish(event.gameId().value().toString(), new GameEvent("MOVE", event.gameId().value().toString(), event.result()));
    }

    @EventListener
    public void handleAiSuggestion(DomainEventStream.AiSuggestionProduced event) {
        publish(event.gameId().value().toString(), new GameEvent("AI_SUGGESTION", event.gameId().value().toString(), event.suggestion()));
    }

    private void publish(String gameId, GameEvent event) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
    }

    public record GameEvent(String type, String gameId, Object payload) {}
}
