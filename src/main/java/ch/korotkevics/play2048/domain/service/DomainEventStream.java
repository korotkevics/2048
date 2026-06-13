package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;

public interface DomainEventStream {
    void publish(GameEvent event);

    sealed interface GameEvent {
        String clientId();
    }

    record GameStarted(String clientId, BoardState initialBoard, int highScore) implements GameEvent {}
    record MoveMade(String clientId, MoveResult result) implements GameEvent {}
    record AiSuggestionProduced(String clientId, Direction suggestion) implements GameEvent {}
}
