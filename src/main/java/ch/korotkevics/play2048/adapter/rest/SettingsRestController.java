package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/settings")
public final class SettingsRestController {

    private final UserSettings userSettings;
    private final GameSettings gameSettings;

    public SettingsRestController(UserSettings userSettings, GameSettings gameSettings) {
        this.userSettings = userSettings;
        this.gameSettings = gameSettings;
    }

    @GetMapping
    public SettingsResponse getSettings() {
        return new SettingsResponse(
                "1.1", 
                userSettings.getAiType().name(), 
                gameSettings.getInitialTileCount(),
                gameSettings.getSpawnConfiguration().getProbabilities()
        );
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateSettings(@RequestBody SettingsUpdateRequest request) {
        if (request.aiType() != null) {
            userSettings.setAiType(UserSettings.AiType.valueOf(request.aiType()));
        }
        if (request.initialTileCount() != null) {
            gameSettings.setInitialTileCount(request.initialTileCount());
        }
        if (request.tileProbabilities() != null && !request.tileProbabilities().isEmpty()) {
            GameSettings.TileSpawnConfiguration spawnConfig = gameSettings.getSpawnConfiguration();
            
            // Add or update probabilities
            for (Map.Entry<Integer, Double> entry : request.tileProbabilities().entrySet()) {
                spawnConfig.update(entry.getKey(), entry.getValue());
            }
            
            // Remove old ones not in the new request
            Set<Integer> currentKeys = spawnConfig.getProbabilities().keySet();
            for (Integer key : currentKeys) {
                if (!request.tileProbabilities().containsKey(key)) {
                    spawnConfig.remove(key);
                }
            }
            
            spawnConfig.validate();
        }
    }

    public record SettingsResponse(String version, String aiType, int initialTileCount, Map<Integer, Double> tileProbabilities) {}
    public record SettingsUpdateRequest(String aiType, Integer initialTileCount, Map<Integer, Double> tileProbabilities) {}
}
