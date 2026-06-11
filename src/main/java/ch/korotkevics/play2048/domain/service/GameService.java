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

    public GameService(MoveSuggester aiFacade) {
        this.aiFacade = aiFacade;
    }

    public GameId startNewGame() {
        GameId gameId = GameId.generate();
        activeGames.put(gameId, Game2048Engine.newGame());
        return gameId;
    }

    public Optional<MoveResult> makeMove(GameId gameId, Direction direction) {
        Game2048Engine engine = activeGames.get(gameId);
        if (engine == null) {
            return Optional.empty();
        }

        MoveResult result = engine.move(direction);
        if (result.moved()) {
            activeGames.put(gameId, result.nextEngine());
        }
        return Optional.of(result);
    }

    public Optional<Direction> getAiSuggestion(GameId gameId) {
        Game2048Engine engine = activeGames.get(gameId);
        if (engine == null) {
            return Optional.empty();
        }
        return aiFacade.suggestNextMove(engine.boardState());
    }

    public void abandonGame(GameId gameId) {
        activeGames.remove(gameId);
    }
}
