package ch.korotkevics.play2048.adapter.persistence;

import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.service.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Component
public class DatabaseGameAdapter implements GameRepository {

    private final SpringDataGameRepository repository;
    private final SpringDataSettingsRepository settingsRepository;
    private final SpringDataGameHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public DatabaseGameAdapter(SpringDataGameRepository repository, 
                               SpringDataSettingsRepository settingsRepository, 
                               SpringDataGameHistoryRepository historyRepository,
                               ObjectMapper objectMapper) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(String clientId, Game2048Engine engine) {
        try {
            String boardJson = objectMapper.writeValueAsString(engine.board());
            GameEntity entity = repository.findById(clientId)
                    .orElse(new GameEntity());
            entity.setClientId(clientId);
            entity.setBoardJson(boardJson);
            entity.setScore(engine.score());
            entity.setLastActivityAt(Instant.now());
            repository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize board", e);
        }
    }

    @Override
    public Optional<Game2048Engine> findByClientId(String clientId) {
        return repository.findById(clientId).map(entity -> {
            try {
                int[][] grid = objectMapper.readValue(entity.getBoardJson(), int[][].class);
                GameSettings gameSettings = fetchSettings(clientId);
                return Game2048Engine.from(grid, entity.getScore(), new Random(), gameSettings);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize board", e);
            }
        });
    }

    private GameSettings fetchSettings(String clientId) {
        return settingsRepository.findById(clientId).map(entity -> {
            try {
                GameSettings gs = new GameSettings();
                gs.setInitialTileCount(entity.getInitialTileCount());
                java.util.Map<String, Double> probs = objectMapper.readValue(entity.getTileProbabilitiesJson(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
                GameSettings.TileSpawnConfiguration config = gs.getSpawnConfiguration();
                
                Set<Integer> newKeys = new HashSet<>();
                probs.forEach((k, v) -> {
                    int val = Integer.parseInt(k);
                    config.update(val, v);
                    newKeys.add(val);
                });

                Set<Integer> currentKeys = new HashSet<>(config.getProbabilities().keySet());
                for (Integer key : currentKeys) {
                    if (!newKeys.contains(key)) {
                        config.remove(key);
                    }
                }
                return gs;
            } catch (Exception e) {
                return new GameSettings();
            }
        }).orElse(new GameSettings());
    }

    @Override
    @Transactional
    public void deleteByClientId(String clientId) {
        repository.deleteById(clientId);
        historyRepository.deleteByClientId(clientId);
    }

    @Override
    @Transactional
    public void deleteStaleGames(Instant threshold) {
        repository.deleteByLastActivityAtBefore(threshold);
        historyRepository.deleteByCreatedAtBefore(threshold);
    }

    @Override
    @Transactional
    public void pushToHistory(String clientId, Game2048Engine engine) {
        try {
            String boardJson = objectMapper.writeValueAsString(engine.board());
            GameHistoryEntity entity = new GameHistoryEntity(clientId, boardJson, engine.score());
            historyRepository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize history board", e);
        }
    }

    @Override
    @Transactional
    public Optional<Game2048Engine> popFromHistory(String clientId) {
        return historyRepository.findFirstByClientIdOrderByCreatedAtDesc(clientId).map(entity -> {
            try {
                int[][] grid = objectMapper.readValue(entity.getBoardJson(), int[][].class);
                GameSettings gameSettings = fetchSettings(clientId);
                Game2048Engine engine = Game2048Engine.from(grid, entity.getScore(), new Random(), gameSettings);
                historyRepository.delete(entity);
                return engine;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize history board", e);
            }
        });
    }

    @Override
    @Transactional
    public void clearHistory(String clientId) {
        historyRepository.deleteByClientId(clientId);
    }
}
