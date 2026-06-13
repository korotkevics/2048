package ch.korotkevics.play2048.adapter.persistence;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.service.SettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class DatabaseSettingsAdapter implements SettingsRepository {

    private final SpringDataSettingsRepository repository;
    private final ObjectMapper objectMapper;

    public DatabaseSettingsAdapter(SpringDataSettingsRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(String clientId, UserSettings userSettings, GameSettings gameSettings, int highScore) {
        try {
            String probsJson = objectMapper.writeValueAsString(gameSettings.getSpawnConfiguration().getProbabilities());
            SettingsEntity entity = repository.findById(clientId)
                    .orElse(new SettingsEntity());
            
            entity.setClientId(clientId);
            entity.setAiType(userSettings.getAiType().name());
            entity.setInitialTileCount(gameSettings.getInitialTileCount());
            entity.setTileProbabilitiesJson(probsJson);
            entity.setVersion("1.1"); 
            entity.setHighScore(highScore);
            
            repository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize settings", e);
        }
    }

    @Override
    public Optional<SettingsBundle> findByClientId(String clientId) {
        return repository.findById(clientId).map(entity -> {
            try {
                UserSettings us = new UserSettings();
                us.setAiType(UserSettings.AiType.valueOf(entity.getAiType()));
                
                GameSettings gs = new GameSettings();
                gs.setInitialTileCount(entity.getInitialTileCount());
                Map<String, Double> probs = objectMapper.readValue(entity.getTileProbabilitiesJson(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
                GameSettings.TileSpawnConfiguration config = gs.getSpawnConfiguration();
                
                // Track which keys are in the new configuration
                Set<Integer> newKeys = new HashSet<>();
                probs.forEach((k, v) -> {
                    int val = Integer.parseInt(k);
                    config.update(val, v);
                    newKeys.add(val);
                });

                // Remove only the default keys that are NOT in the new configuration
                Set<Integer> currentKeys = new HashSet<>(config.getProbabilities().keySet());
                for (Integer key : currentKeys) {
                    if (!newKeys.contains(key)) {
                        config.remove(key);
                    }
                }
                
                return new SettingsBundle(us, gs, entity.getHighScore());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize settings", e);
            }
        });
    }
}
