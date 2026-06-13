package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Game Master" of the domain.
 * Manages active game sessions and coordinates moves and AI suggestions.
 */
public final class GameService {

    private final GameRepository gameRepository;
    private final SettingsRepository settingsRepository;
    private final MoveSuggester aiFacade;
    private final DomainEventStream eventStream;

    public GameService(GameRepository gameRepository, SettingsRepository settingsRepository, MoveSuggester aiFacade, DomainEventStream eventStream) {
        this.gameRepository = gameRepository;
        this.settingsRepository = settingsRepository;
        this.aiFacade = aiFacade;
        this.eventStream = eventStream;
    }

    public GameId startNewGame(String clientId) {
        gameRepository.clearHistory(clientId);
        GameSettings settings = settingsRepository.findByClientId(clientId)
                .map(SettingsRepository.SettingsBundle::gameSettings)
                .orElse(new GameSettings());
        
        Game2048Engine engine = Game2048Engine.newGame(Game2048Engine.DEFAULT_SIZE, new java.util.Random(), settings);
        gameRepository.save(clientId, engine);
        
        eventStream.publish(new DomainEventStream.GameStarted(clientId, engine.boardState()));
        return GameId.generate(); 
    }

    public MoveResult makeMove(String clientId, Direction direction) {
        Game2048Engine engine = gameRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("No active game for client: " + clientId));

        // Push current state to history before making move
        gameRepository.pushToHistory(clientId, engine);

        MoveResult result = engine.move(direction);
        if (result.moved()) {
            gameRepository.save(clientId, result.nextEngine());
        } else {
            // If move didn't happen, remove from history to keep it clean
            gameRepository.popFromHistory(clientId);
        }
        
        eventStream.publish(new DomainEventStream.MoveMade(clientId, result));
        return result;
    }

    public Optional<MoveResult> undo(String clientId) {
        return gameRepository.popFromHistory(clientId).map(previousEngine -> {
            gameRepository.save(clientId, previousEngine);
            
            MoveResult result = new MoveResult(
                    null, true, 0, previousEngine.score(),
                    previousEngine.isGameOver(), previousEngine.isWon(),
                    previousEngine.boardState(), java.util.Collections.emptyList(), previousEngine
            );
            
            eventStream.publish(new DomainEventStream.MoveMade(clientId, result));
            return result;
        });
    }

    public void requestAiSuggestion(String clientId) {
        Game2048Engine engine = gameRepository.findByClientId(clientId)
                .orElse(null);
        if (engine == null) return;

        UserSettings userSettings = settingsRepository.findByClientId(clientId)
                .map(SettingsRepository.SettingsBundle::userSettings)
                .orElse(new UserSettings());

        aiFacade.suggestNextMove(engine.boardState(), userSettings)
                .ifPresent(direction -> eventStream.publish(new DomainEventStream.AiSuggestionProduced(clientId, direction)));
    }

    public void abandonGame(String clientId) {
        gameRepository.deleteByClientId(clientId);
    }
    
    public Optional<Game2048Engine> getActiveGame(String clientId) {
        return gameRepository.findByClientId(clientId);
    }

    public SettingsRepository.SettingsBundle getSettings(String clientId) {
        return settingsRepository.findByClientId(clientId)
                .orElseGet(() -> new SettingsRepository.SettingsBundle(new UserSettings(), new GameSettings()));
    }

    public void updateSettings(String clientId, UserSettings userSettings, GameSettings gameSettings) {
        settingsRepository.save(clientId, userSettings, gameSettings);
    }

    public void cleanupStaleGames() {
        gameRepository.deleteStaleGames(java.time.Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS));
    }
}
