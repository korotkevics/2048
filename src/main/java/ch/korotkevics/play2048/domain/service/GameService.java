package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The "Game Master" of the domain.
 * Manages active game sessions and coordinates moves and AI suggestions.
 */
public final class GameService {

    private final GameRepository gameRepository;
    private final SettingsRepository settingsRepository;
    private final MoveSuggester aiFacade;
    private final DomainEventStream eventStream;
    
    private final Map<String, Future<?>> activeAutoPlays = new ConcurrentHashMap<>();
    private final Map<String, String> activeAutoPlayTokens = new ConcurrentHashMap<>();
    
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public GameService(GameRepository gameRepository, SettingsRepository settingsRepository, MoveSuggester aiFacade, DomainEventStream eventStream) {
        this.gameRepository = gameRepository;
        this.settingsRepository = settingsRepository;
        this.aiFacade = aiFacade;
        this.eventStream = eventStream;
    }

    public MoveResult startNewGame(String clientId) {
        stopAutoPlay(clientId);
        gameRepository.clearHistory(clientId);
        SettingsRepository.SettingsBundle settingsBundle = getSettings(clientId);
        
        Game2048Engine engine = Game2048Engine.newGame(Game2048Engine.DEFAULT_SIZE, new java.util.Random(), settingsBundle.gameSettings());
        gameRepository.save(clientId, engine);
        
        MoveResult result = new MoveResult(
                null, true, 0, 0, settingsBundle.highScore(),
                false, false, engine.boardState(), java.util.Collections.emptyList(), engine
        );

        eventStream.publish(new DomainEventStream.GameStarted(clientId, engine.boardState(), settingsBundle.highScore()));
        return result;
    }

    public MoveResult makeMove(String clientId, Direction direction) {
        Game2048Engine engine = gameRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("No active game for client: " + clientId));

        gameRepository.pushToHistory(clientId, engine);

        MoveResult result = engine.move(direction);
        
        SettingsRepository.SettingsBundle bundle = getSettings(clientId);
        int currentHighScore = bundle.highScore();
        int newHighScore = Math.max(currentHighScore, result.score());
        
        if (newHighScore > currentHighScore) {
            settingsRepository.save(clientId, bundle.userSettings(), bundle.gameSettings(), newHighScore);
        }

        MoveResult finalResult = new MoveResult(
                result.direction(), result.moved(), result.scoreGained(), result.score(),
                newHighScore, result.gameOver(), result.won(), result.boardState(),
                result.deltas(), result.nextEngine()
        );

        if (result.moved()) {
            gameRepository.save(clientId, result.nextEngine());
        } else {
            gameRepository.popFromHistory(clientId);
        }
        
        eventStream.publish(new DomainEventStream.MoveMade(clientId, finalResult));
        return finalResult;
    }

    public Optional<MoveResult> undo(String clientId) {
        return gameRepository.popFromHistory(clientId).map(previousEngine -> {
            gameRepository.save(clientId, previousEngine);
            
            int highScore = getSettings(clientId).highScore();
            
            MoveResult result = new MoveResult(
                    null, true, 0, previousEngine.score(), highScore,
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

        SettingsRepository.SettingsBundle bundle = getSettings(clientId);
        UserSettings userSettings = bundle.userSettings();
        GameSettings gameSettings = bundle.gameSettings();

        aiFacade.suggestNextMove(engine.boardState(), userSettings, gameSettings)
                .ifPresent(direction -> eventStream.publish(new DomainEventStream.AiSuggestionProduced(clientId, direction)));
    }

    public void abandonGame(String clientId) {
        stopAutoPlay(clientId);
        gameRepository.deleteByClientId(clientId);
    }
    
    public Optional<Game2048Engine> getActiveGame(String clientId) {
        return gameRepository.findByClientId(clientId);
    }

    public SettingsRepository.SettingsBundle getSettings(String clientId) {
        return settingsRepository.findByClientId(clientId)
                .orElseGet(() -> new SettingsRepository.SettingsBundle(new UserSettings(), new GameSettings(), 0));
    }

    public void updateSettings(String clientId, UserSettings userSettings, GameSettings gameSettings) {
        int currentHighScore = getSettings(clientId).highScore();
        settingsRepository.save(clientId, userSettings, gameSettings, currentHighScore);
    }

    public void cleanupStaleGames() {
        gameRepository.deleteStaleGames(java.time.Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS));
    }

    public void startAutoPlay(String clientId) {
        stopAutoPlay(clientId); 

        String sessionToken = UUID.randomUUID().toString();
        activeAutoPlayTokens.put(clientId, sessionToken);

        Future<?> future = executor.submit(() -> {
            try {
                int consecutiveFailedMoves = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    if (!sessionToken.equals(activeAutoPlayTokens.get(clientId))) break;

                    Game2048Engine engine = gameRepository.findByClientId(clientId).orElse(null);
                    if (engine == null || engine.isGameOver() || engine.isWon()) break;

                    SettingsRepository.SettingsBundle bundle = getSettings(clientId);
                    UserSettings userSettings = bundle.userSettings();
                    GameSettings gameSettings = bundle.gameSettings();

                    Optional<Direction> suggestion = aiFacade.suggestNextMove(engine.boardState(), userSettings, gameSettings);

                    if (suggestion.isPresent()) {
                        MoveResult moveResult = makeMove(clientId, suggestion.get());
                        
                        if (moveResult.moved()) {
                            consecutiveFailedMoves = 0;
                            if (userSettings.getAiType() == UserSettings.AiType.DETERMINISTIC) {
                                Thread.sleep(300); 
                            }
                        } else {
                            consecutiveFailedMoves++;
                            if (consecutiveFailedMoves >= 3) {
                                break;
                            }
                            Thread.sleep(100);
                        }
                    } else {
                        break; 
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                activeAutoPlayTokens.remove(clientId, sessionToken);
                activeAutoPlays.remove(clientId);
            }
        });

        activeAutoPlays.put(clientId, future);
    }

    public void stopAutoPlay(String clientId) {
        activeAutoPlayTokens.remove(clientId);
        Future<?> future = activeAutoPlays.remove(clientId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
