package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Professional Expectimax AI for 2048.
 * Optimized for depth search with high-precision heuristics and dynamic settings.
 */
public final class DeterministicAiFacade implements MoveSuggester {

    private static final int SEARCH_DEPTH = 6;
    
    private static final double[][] WEIGHT_MATRIX = {
        {2048, 1024, 512, 256},
        {16,   32,   64,  128},
        {8,    4,    2,   1},
        {0.5,  0.25, 0.1, 0.05}
    };

    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState, UserSettings settings, GameSettings gameSettings) {
        return Stream.of(Direction.values())
                .parallel()
                .map(direction -> evaluateRootMove(boardState, direction, gameSettings))
                .filter(MoveEvaluation::moved)
                .max(Comparator.comparingDouble(MoveEvaluation::score))
                .map(MoveEvaluation::direction);
    }

    private MoveEvaluation evaluateRootMove(BoardState state, Direction direction, GameSettings settings) {
        Game2048Engine engine = Game2048Engine.from(state.grid(), 0, new java.util.Random(), settings);
        MoveResult result = engine.simulateMove(direction);

        if (!result.moved()) {
            return new MoveEvaluation(direction, false, 0);
        }

        double score = expectimax(result.boardState().grid(), SEARCH_DEPTH - 1, false, settings);
        return new MoveEvaluation(direction, true, score);
    }

    private double expectimax(int[][] grid, int depth, boolean isPlayerTurn, GameSettings settings) {
        if (depth == 0) {
            return calculateHeuristic(grid);
        }

        if (isPlayerTurn) {
            double maxScore = -1e9;
            boolean moved = false;
            for (Direction dir : Direction.values()) {
                int[][] nextGrid = simulateMoveFast(grid, dir);
                if (nextGrid != null) {
                    moved = true;
                    maxScore = Math.max(maxScore, expectimax(nextGrid, depth - 1, false, settings));
                }
            }
            return moved ? maxScore : -1e6;
        } else {
            double totalScore = 0;
            int emptyCells = 0;
            int[][] currentGrid = copyGrid(grid);
            Map<Integer, Double> probabilities = settings.getSpawnConfiguration().getProbabilities();
            
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (currentGrid[r][c] == 0) {
                        emptyCells++;
                        
                        // Dynamic Weighted average based on settings
                        for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
                            currentGrid[r][c] = entry.getKey();
                            totalScore += entry.getValue() * expectimax(currentGrid, depth - 1, true, settings);
                        }
                        
                        currentGrid[r][c] = 0; // Backtrack
                    }
                }
            }
            return emptyCells > 0 ? (totalScore / emptyCells) : 0;
        }
    }

    private double calculateHeuristic(int[][] grid) {
        double score = 0;
        double penalty = 0;
        int emptyCount = 0;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int val = grid[r][c];
                if (val == 0) {
                    emptyCount++;
                    continue;
                }
                
                double valLog = Math.log(val) / Math.log(2);
                score += valLog * WEIGHT_MATRIX[r][c];

                if (c + 1 < 4 && grid[r][c+1] != 0) {
                    penalty += Math.abs(valLog - (Math.log(grid[r][c+1]) / Math.log(2)));
                }
                if (r + 1 < 4 && grid[r+1][c] != 0) {
                    penalty += Math.abs(valLog - (Math.log(grid[r+1][c]) / Math.log(2)));
                }
            }
        }

        return score - (penalty * 10.0) + (Math.pow(emptyCount, 2) * 50);
    }

    private int[][] simulateMoveFast(int[][] grid, Direction dir) {
        int[][] next = new int[4][4];
        boolean moved = false;
        
        for (int i = 0; i < 4; i++) {
            int[] line = getLine(grid, i, dir);
            int[] processed = processLine(line);
            if (!java.util.Arrays.equals(line, processed)) {
                moved = true;
            }
            setLine(next, i, dir, processed);
        }
        
        return moved ? next : null;
    }

    private int[] getLine(int[][] grid, int index, Direction dir) {
        int[] line = new int[4];
        for (int i = 0; i < 4; i++) {
            line[i] = switch (dir) {
                case LEFT -> grid[index][i];
                case RIGHT -> grid[index][3-i];
                case UP -> grid[i][index];
                case DOWN -> grid[3-i][index];
            };
        }
        return line;
    }

    private void setLine(int[][] grid, int index, Direction dir, int[] line) {
        for (int i = 0; i < 4; i++) {
            switch (dir) {
                case LEFT -> grid[index][i] = line[i];
                case RIGHT -> grid[index][3-i] = line[i];
                case UP -> grid[i][index] = line[i];
                case DOWN -> grid[3-i][index] = line[i];
            }
        }
    }

    private int[] processLine(int[] line) {
        int[] res = new int[4];
        int target = 0;
        for (int i = 0; i < 4; i++) {
            if (line[i] == 0) continue;
            if (res[target] == 0) {
                res[target] = line[i];
            } else if (res[target] == line[i]) {
                res[target] *= 2;
                target++;
            } else {
                target++;
                if (target < 4) {
                    res[target] = line[i];
                }
            }
        }
        return res;
    }
    
    private int[][] copyGrid(int[][] source) {
        int[][] copy = new int[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, 4);
        }
        return copy;
    }

    private record MoveEvaluation(Direction direction, boolean moved, double score) {
    }
}
