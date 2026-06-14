package ch.korotkevics.play2048.domain.engine;

import java.util.List;
import java.util.Map;
import java.util.Random;

final class TileSpawner {

    Board spawnInitialTiles(Board board, GameSettings settings, Random random) {
        Board current = board;
        int count = settings.getInitialTileCount();
        if (count == 0) {
            // "Random number of 2s" - picked a range of 2 to 6 as reasonable for 4x4
            count = 2 + random.nextInt(5); 
        }
        for (int i = 0; i < count; i++) {
            current = spawnTile(current, 2, random);
        }
        return current;
    }

    Board spawnRandomTile(Board board, GameSettings settings, Random random) {
        Map<Integer, Double> probs = settings.getSpawnConfiguration().getProbabilities();
        if (probs.isEmpty()) {
            return spawnTile(board, 2, random);
        }

        double r = random.nextDouble();
        double cumulative = 0.0;
        int selectedValue = 2;
        for (Map.Entry<Integer, Double> entry : probs.entrySet()) {
            cumulative += entry.getValue();
            selectedValue = entry.getKey();
            if (r < cumulative) {
                break;
            }
        }
        return spawnTile(board, selectedValue, random);
    }

    private Board spawnTile(Board board, int value, Random random) {
        List<Board.Cell> emptyCells = board.emptyCells();
        if (emptyCells.isEmpty()) {
            return board;
        }

        Board.Cell cell = emptyCells.get(random.nextInt(emptyCells.size()));
        return board.withValue(cell.row(), cell.column(), value);
    }
}
