package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.GameSettings;

import java.util.Optional;

public interface SettingsRepository {
    void save(String clientId, UserSettings userSettings, GameSettings gameSettings);
    Optional<SettingsBundle> findByClientId(String clientId);

    record SettingsBundle(UserSettings userSettings, GameSettings gameSettings) {}
}
