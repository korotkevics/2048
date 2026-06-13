package ch.korotkevics.play2048.domain.ai.deterministic;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.MoveResult;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class DeterministicAiFacade implements MoveSuggester {

    /**
     * Suggests the best next move based on a heuristic evaluation of the board.
     * Evaluates all possible moves one step ahead and picks the one with the highest heuristic score.
     * Heuristic considers:
     * 1. Monotonicity and alignment (highest tiles in the top-left corner).
     * 2. Number of empty cells (maximizing flexibility).
     * 3. Total score gained.
     */
    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState, ch.korotkevics.play2048.domain.ai.UserSettings settings) {
        return Stream.of(Direction.values())
                .map(direction -> evaluateMove(boardState, direction))
                .filter(MoveEvaluation::moved)
                .max(Comparator.comparingDouble(MoveEvaluation::score))
                .map(MoveEvaluation::direction);
    }

    private MoveEvaluation evaluateMove(BoardState state, Direction direction) {
        Game2048Engine engine = Game2048Engine.from(state.grid());
        MoveResult result = engine.simulateMove(direction);

        if (!result.moved()) {
            return new MoveEvaluation(direction, false, 0);
        }

        double score = heuristicScore(result.boardState());
        // Bias towards gained score in this move as well
        score += result.scoreGained();

        return new MoveEvaluation(direction, true, score);
    }

    private double heuristicScore(BoardState board) {
        double score = 0;
        int size = board.size();

        // Weighted matrix to encourage keeping highest tiles in the top-left corner
        // 4x4 matrix example:
        // [[15, 14, 13, 12],
        //  [ 8,  9, 10, 11],
        //  [ 7,  6,  5,  4],
        //  [ 0,  1,  2,  3]]
        for (int row = 0; row < size; row++) {
            for (int col = 0; column(row, col, size) < size; col++) {
                int value = board.getValue(row, col);
                if (value > 0) {
                    score += Math.log(value) / Math.log(2) * weight(row, col, size);
                } else {
                    // Bonus for empty cells
                    score += 10;
                }
            }
        }
        return score;
    }

    private int weight(int row, int col, int size) {
        // Snake-like weighting
        if (row % 2 == 0) {
            return (size * size - 1) - (row * size + col);
        } else {
            return (size * size - 1) - (row * size + (size - 1 - col));
        }
    }

    private int column(int row, int col, int size) {
        // Dummy helper for the loop condition
        return col;
    }

    private record MoveEvaluation(Direction direction, boolean moved, double score) {
    }
}
