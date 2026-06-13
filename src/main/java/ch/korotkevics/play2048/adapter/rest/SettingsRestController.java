package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        return new SettingsResponse("1.0", userSettings.getAiType().name(), gameSettings.getInitialTileCount());
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
    }

    public record SettingsResponse(String version, String aiType, int initialTileCount) {}
    public record SettingsUpdateRequest(String aiType, Integer initialTileCount) {}
}
