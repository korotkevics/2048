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
        publish(event.clientId(), new GameEvent("STARTED", event.clientId(), new StartedPayload(event.initialBoard(), event.highScore())));
    }

    @EventListener
    public void handleMoveMade(DomainEventStream.MoveMade event) {
        publish(event.clientId(), new GameEvent("MOVE", event.clientId(), event.result()));
    }

    @EventListener
    public void handleAiSuggestion(DomainEventStream.AiSuggestionProduced event) {
        publish(event.clientId(), new GameEvent("AI_SUGGESTION", event.clientId(), event.suggestion()));
    }

    private void publish(String clientId, GameEvent event) {
        messagingTemplate.convertAndSend("/topic/game/" + clientId, event);
    }

    public record GameEvent(String type, String gameId, Object payload) {}
    public record StartedPayload(ch.korotkevics.play2048.domain.engine.BoardState initialBoard, int highScore) {}
}
