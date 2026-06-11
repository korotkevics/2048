package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Game Master" of the domain.
 * Manages active game sessions and coordinates moves and AI suggestions.
 */
public final class GameService {

    private final Map<GameId, Game2048Engine> activeGames = new ConcurrentHashMap<>();
    private final MoveSuggester aiFacade;
    private final DomainEventStream eventStream;

    public GameService(MoveSuggester aiFacade, DomainEventStream eventStream) {
        this.aiFacade = aiFacade;
        this.eventStream = eventStream;
    }

    public GameId startNewGame() {
        GameId gameId = GameId.generate();
        activeGames.put(gameId, Game2048Engine.newGame());
        eventStream.publish(new DomainEventStream.GameStarted(gameId));
        return gameId;
    }

    public void makeMove(GameId gameId, Direction direction) {
        Game2048Engine engine = activeGames.get(gameId);
        if (engine == null) {
            return;
        }

        MoveResult result = engine.move(direction);
        if (result.moved()) {
            activeGames.put(gameId, result.nextEngine());
        }
        eventStream.publish(new DomainEventStream.MoveMade(gameId, result));
    }

    public void requestAiSuggestion(GameId gameId) {
        Game2048Engine engine = activeGames.get(gameId);
        if (engine == null) {
            return;
        }
        aiFacade.suggestNextMove(engine.boardState())
                .ifPresent(direction -> eventStream.publish(new DomainEventStream.AiSuggestionProduced(gameId, direction)));
    }

    public void abandonGame(GameId gameId) {
        activeGames.remove(gameId);
    }
}
