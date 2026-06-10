package ch.korotkevics.play2048.domain;

import java.util.Map;
import java.util.TreeMap;

public final class GameSettings {
    private final TileSpawnConfiguration spawnConfiguration;
    private int initialTileCount;

    public GameSettings() {
        this.spawnConfiguration = new TileSpawnConfiguration();
        this.initialTileCount = 4;
    }

    public TileSpawnConfiguration getSpawnConfiguration() {
        return spawnConfiguration;
    }

    public synchronized int getInitialTileCount() {
        return initialTileCount;
    }

    public synchronized void setInitialTileCount(int initialTileCount) {
        if (initialTileCount < 1) {
            throw new IllegalArgumentException("initialTileCount must be at least 1");
        }
        this.initialTileCount = initialTileCount;
    }

    public static final class TileSpawnConfiguration {
        private final Map<Integer, Double> probabilities = new TreeMap<>();

        public TileSpawnConfiguration() {
            probabilities.put(2, 0.9);
            probabilities.put(4, 0.1);
        }

        public synchronized Map<Integer, Double> getProbabilities() {
            return new TreeMap<>(probabilities);
        }

        public synchronized void update(int value, double probability) {
            validateValue(value);
            if (probability < 0 || probability > 1.0) {
                throw new IllegalArgumentException("probability must be between 0 and 1");
            }
            probabilities.put(value, probability);
        }

        public synchronized void remove(int value) {
            if (probabilities.size() <= 1 && probabilities.containsKey(value)) {
                throw new IllegalStateException("At least one setting must remain");
            }
            probabilities.remove(value);
        }

        public synchronized void validate() {
            if (probabilities.isEmpty()) {
                throw new IllegalStateException("At least one setting must remain");
            }
            double sum = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sum - 1.0) > 1e-9) {
                throw new IllegalStateException("Sum of probabilities must be 1.0, currently: " + sum);
            }
        }

        private void validateValue(int value) {
            if (value < 2 || value > 2048 || (value & (value - 1)) != 0) {
                throw new IllegalArgumentException("value must be a power of two between 2 and 2048");
            }
        }
    }
}
