package ch.korotkevics.play2048.domain;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class TileSpawner {

    public void spawnInitialTiles(Board board, GameSettings settings, Random random) {
        for (int i = 0; i < settings.getInitialTileCount(); i++) {
            spawnTile(board, 2, random);
        }
    }

    public void spawnRandomTile(Board board, GameSettings settings, Random random) {
        Map<Integer, Double> probs = settings.getSpawnConfiguration().getProbabilities();
        if (probs.isEmpty()) {
            spawnTile(board, 2, random);
            return;
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
        spawnTile(board, selectedValue, random);
    }

    private void spawnTile(Board board, int value, Random random) {
        List<Board.Cell> emptyCells = board.emptyCells();
        if (emptyCells.isEmpty()) {
            return;
        }

        Board.Cell cell = emptyCells.get(random.nextInt(emptyCells.size()));
        board.setValue(cell.row(), cell.column(), value);
    }
}
