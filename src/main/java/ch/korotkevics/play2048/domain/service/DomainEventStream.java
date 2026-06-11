package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;

public interface DomainEventStream {
    void publish(GameEvent event);

    sealed interface GameEvent {
        GameId gameId();
    }

    record GameStarted(GameId gameId) implements GameEvent {}
    record MoveMade(GameId gameId, MoveResult result) implements GameEvent {}
    record AiSuggestionProduced(GameId gameId, Direction suggestion) implements GameEvent {}
}
