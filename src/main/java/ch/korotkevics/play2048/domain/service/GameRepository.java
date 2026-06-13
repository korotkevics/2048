package ch.korotkevics.play2048.domain.service;

import ch.korotkevics.play2048.domain.engine.Game2048Engine;

import java.time.Instant;
import java.util.Optional;

public interface GameRepository {
    void save(String clientId, Game2048Engine engine);
    Optional<Game2048Engine> findByClientId(String clientId);
    void deleteByClientId(String clientId);
    void deleteStaleGames(Instant threshold);
}
