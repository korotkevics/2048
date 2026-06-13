package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/settings")
public final class SettingsRestController {

    private static final String CLIENT_ID_HEADER = "X-Client-ID";
    private final GameService gameService;

    public SettingsRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public SettingsResponse getSettings(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId) {
        var bundle = gameService.getSettings(clientId);
        return new SettingsResponse(
                "1.1", 
                bundle.userSettings().getAiType().name(), 
                bundle.gameSettings().getInitialTileCount(),
                bundle.gameSettings().getSpawnConfiguration().getProbabilities()
        );
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateSettings(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId,
                               @RequestBody SettingsUpdateRequest request) {
        var bundle = gameService.getSettings(clientId);
        var userSettings = bundle.userSettings();
        var gameSettings = bundle.gameSettings();

        if (request.aiType() != null) {
            userSettings.setAiType(UserSettings.AiType.valueOf(request.aiType()));
        }
        if (request.initialTileCount() != null) {
            gameSettings.setInitialTileCount(request.initialTileCount());
        }
        if (request.tileProbabilities() != null && !request.tileProbabilities().isEmpty()) {
            GameSettings.TileSpawnConfiguration spawnConfig = gameSettings.getSpawnConfiguration();
            
            // Safely parse keys to integers
            java.util.Map<Integer, Double> safeProbs = new java.util.HashMap<>();
            for (Map.Entry<?, Double> entry : request.tileProbabilities().entrySet()) {
                int key = Integer.parseInt(String.valueOf(entry.getKey()));
                safeProbs.put(key, entry.getValue());
            }

            // Clear old ones and update with new
            java.util.Set<Integer> currentKeys = new java.util.HashSet<>(spawnConfig.getProbabilities().keySet());
            for (Integer key : currentKeys) {
                spawnConfig.remove(key);
            }
            
            for (Map.Entry<Integer, Double> entry : safeProbs.entrySet()) {
                spawnConfig.update(entry.getKey(), entry.getValue());
            }
            
            spawnConfig.validate();
        }
        
        gameService.updateSettings(clientId, userSettings, gameSettings);
    }

    public record SettingsResponse(String version, String aiType, int initialTileCount, Map<Integer, Double> tileProbabilities) {}
    public record SettingsUpdateRequest(String aiType, Integer initialTileCount, Map<Integer, Double> tileProbabilities) {}
}
