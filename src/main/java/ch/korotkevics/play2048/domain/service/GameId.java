package ch.korotkevics.play2048.domain.service;

import java.util.UUID;

public record GameId(UUID value) {
    public static GameId generate() {
        return new GameId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
