package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Standard Expectimax AI for 2048.
 * Evaluates moves by considering the weighted average of random tile spawns.
 * Uses heuristics: Monotonicity, Smoothness, Free Tiles, and Corner Weighting.
 */
public final class DeterministicAiFacade implements MoveSuggester {

    private static final int SEARCH_DEPTH = 3;

    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState, UserSettings settings) {
        return Stream.of(Direction.values())
                .map(direction -> evaluateRootMove(boardState, direction))
                .filter(MoveEvaluation::moved)
                .max(Comparator.comparingDouble(MoveEvaluation::score))
                .map(MoveEvaluation::direction);
    }

    private MoveEvaluation evaluateRootMove(BoardState state, Direction direction) {
        Game2048Engine engine = Game2048Engine.from(state.grid());
        MoveResult result = engine.simulateMove(direction);

        if (!result.moved()) {
            return new MoveEvaluation(direction, false, 0);
        }

        // We use Expectimax to find the value of this move
        double score = expectimax(result.boardState(), SEARCH_DEPTH, false);
        return new MoveEvaluation(direction, true, score);
    }

    /**
     * Expectimax algorithm: 
     * - On Player turn: Maximize the score of the best move.
     * - On Chance turn: Calculate the weighted average of all possible tile spawns (2s and 4s).
     */
    private double expectimax(BoardState board, int depth, boolean isPlayerTurn) {
        if (depth == 0) {
            return heuristicScore(board);
        }

        if (isPlayerTurn) {
            double maxScore = 0;
            boolean canMove = false;
            for (Direction dir : Direction.values()) {
                Game2048Engine sim = Game2048Engine.from(board.grid());
                MoveResult res = sim.simulateMove(dir);
                if (res.moved()) {
                    canMove = true;
                    maxScore = Math.max(maxScore, expectimax(res.boardState(), depth - 1, false));
                }
            }
            return canMove ? maxScore : 0; // If no moves, score is 0 (death)
        } else {
            // Chance turn: Average of all possible spawns
            double totalScore = 0;
            int emptyCells = 0;
            int size = board.size();

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board.getValue(r, c) == 0) {
                        emptyCells++;
                        
                        // Spawn a 2 (90% chance)
                        BoardState boardWith2 = board.withTile(r, c, 2);
                        totalScore += 0.9 * expectimax(boardWith2, depth - 1, true);

                        // Spawn a 4 (10% chance)
                        BoardState boardWith4 = board.withTile(r, c, 4);
                        totalScore += 0.1 * expectimax(boardWith4, depth - 1, true);
                    }
                }
            }
            return emptyCells > 0 ? (totalScore / emptyCells) : 0;
        }
    }

    /**
     * The Brain: Evaluates how "good" a board is.
     */
    private double heuristicScore(BoardState board) {
        return cornerWeight(board) 
             + (monotonicity(board) * 2.0) 
             + (smoothness(board) * 0.5) 
             + (Math.log(countEmpty(board) + 1) * 100);
    }

    private double cornerWeight(BoardState board) {
        double score = 0;
        int size = board.size();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int val = board.getValue(r, c);
                if (val > 0) {
                    // Log-based weight to keep big numbers in the top-left
                    score += (Math.log(val) / Math.log(2)) * weight(r, c, size);
                }
            }
        }
        return score;
    }

    /**
     * Penalizes boards where adjacent tiles have high value differences.
     */
    private double smoothness(BoardState board) {
        double smoothness = 0;
        int size = board.size();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int val = board.getValue(r, c);
                if (val != 0) {
                    double valLog = Math.log(val) / Math.log(2);
                    // Check right
                    if (c + 1 < size && board.getValue(r, c + 1) != 0) {
                        smoothness -= Math.abs(valLog - (Math.log(board.getValue(r, c + 1)) / Math.log(2)));
                    }
                    // Check down
                    if (r + 1 < size && board.getValue(r + 1, c) != 0) {
                        smoothness -= Math.abs(valLog - (Math.log(board.getValue(r + 1, c)) / Math.log(2)));
                    }
                }
            }
        }
        return smoothness;
    }

    /**
     * Rewards boards where values strictly increase/decrease in a direction.
     */
    private double monotonicity(BoardState board) {
        double mono = 0;
        int size = board.size();
        
        // Row monotonicity (Left-Right)
        for (int r = 0; r < size; r++) {
            double currentMono = 0;
            for (int c = 0; c < size - 1; c++) {
                int val1 = board.getValue(r, c);
                int val2 = board.getValue(r, c + 1);
                if (val1 > val2) currentMono += (Math.log(val1) / Math.log(2));
                else if (val1 < val2) currentMono -= (Math.log(val2) / Math.log(2));
            }
            mono += Math.abs(currentMono);
        }

        // Column monotonicity (Up-Down)
        for (int c = 0; c < size; c++) {
            double currentMono = 0;
            for (int r = 0; r < size - 1; r++) {
                int val1 = board.getValue(r, c);
                int val2 = board.getValue(r + 1, c);
                if (val1 > val2) currentMono += (Math.log(val1) / Math.log(2));
                else if (val1 < val2) currentMono -= (Math.log(val2) / Math.log(2));
            }
            mono += Math.abs(currentMono);
        }
        
        return mono;
    }

    private int countEmpty(BoardState board) {
        int count = 0;
        for (int[] row : board.grid()) {
            for (int val : row) {
                if (val == 0) count++;
            }
        }
        return count;
    }

    private int weight(int row, int col, int size) {
        // Snake-like weighting
        if (row % 2 == 0) {
            return (size * size - 1) - (row * size + col);
        } else {
            return (size * size - 1) - (row * size + (size - 1 - col));
        }
    }

    private record MoveEvaluation(Direction direction, boolean moved, double score) {
    }
}
