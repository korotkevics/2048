package ch.korotkevics.play2048.domain;

import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import static ch.korotkevics.play2048.domain.Direction.DOWN;
import static ch.korotkevics.play2048.domain.Direction.LEFT;
import static ch.korotkevics.play2048.domain.Direction.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Game2048EngineTest {

    @Test
    public void movingLeftCompactsMergesEachTileOnceScoresAndSpawnsANewTile() {
        Game2048Engine engine = Game2048Engine.from(new int[][]{
                {2, 2, 2, 2},
                {4, 0, 4, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, 0, new PredictableRandom(new int[]{11}, new double[]{0.1}));

        MoveResult result = engine.move(LEFT);

        assertThat(result.moved()).isTrue();
        assertThat(result.scoreGained()).isEqualTo(16);
        assertThat(result.score()).isEqualTo(16);
        assertThat(result.board()).isDeepEqualTo(new int[][]{
                {4, 4, 0, 0},
                {8, 4, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 2}
        });
        assertThat(engine.score()).isEqualTo(16);
    }

    @Test
    public void movingRightUsesRightEdgeAsTheMergeDirection() {
        Game2048Engine engine = Game2048Engine.from(new int[][]{
                {2, 0, 2, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, 10, new PredictableRandom(new int[]{11}, new double[]{0.95}));

        MoveResult result = engine.move(RIGHT);

        assertThat(result.scoreGained()).isEqualTo(4);
        assertThat(result.score()).isEqualTo(14);
        assertThat(result.board()).isDeepEqualTo(new int[][]{
                {0, 0, 4, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 0, 0}
        });
    }

    @Test
    public void movingDownMergesByColumnAndSpawnsOnlyAfterAChange() {
        Game2048Engine engine = Game2048Engine.from(new int[][]{
                {2, 0, 4, 0},
                {2, 0, 4, 0},
                {4, 0, 0, 0},
                {4, 0, 0, 0}
        }, 0, new PredictableRandom(new int[]{0}, new double[]{0.1}));

        MoveResult result = engine.move(DOWN);

        assertThat(result.scoreGained()).isEqualTo(20);
        assertThat(result.board()).isDeepEqualTo(new int[][]{
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {8, 0, 8, 0}
        });
    }

    @Test
    public void moveThatDoesNotChangeTheBoardDoesNotSpawnATile() {
        int[][] board = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        };
        Game2048Engine engine = Game2048Engine.from(board, 40, new PredictableRandom(new int[]{0}, new double[]{0.1}));

        MoveResult result = engine.move(LEFT);

        assertThat(result.moved()).isFalse();
        assertThat(result.scoreGained()).isZero();
        assertThat(result.score()).isEqualTo(40);
        assertThat(result.gameOver()).isTrue();
        assertThat(result.board()).isDeepEqualTo(board);
    }

    @Test
    public void fullBoardIsNotGameOverWhenAMergeIsStillAvailable() {
        Game2048Engine engine = Game2048Engine.from(new int[][]{
                {2, 2, 4, 8},
                {16, 32, 64, 128},
                {256, 512, 1024, 2},
                {4, 8, 16, 32}
        });

        assertThat(engine.isGameOver()).isFalse();
    }

    @Test
    public void reachingTargetTileMarksTheGameAsWon() {
        Game2048Engine engine = Game2048Engine.from(new int[][]{
                {1024, 1024, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        }, 0, new PredictableRandom(new int[]{0}, new double[]{0.1}));

        MoveResult result = engine.move(LEFT);

        assertThat(result.won()).isTrue();
        assertThat(engine.isWon()).isTrue();
    }

    @Test
    public void newGamePlacesInitialTiles() {
        Game2048Engine engine = Game2048Engine.newGame(4, new PredictableRandom(
                new int[]{0, 0, 0, 0},
                new double[]{}
        ));

        assertThat(engine.board()).isDeepEqualTo(new int[][]{
                {2, 2, 2, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Test
    public void boardInputAndOutputAreDefensiveCopies() {
        int[][] board = {
                {2, 0},
                {0, 0}
        };
        Game2048Engine engine = Game2048Engine.from(board);

        board[0][0] = 4;
        int[][] exportedBoard = engine.board();
        exportedBoard[0][0] = 8;

        assertThat(engine.board()).isDeepEqualTo(new int[][]{
                {2, 0},
                {0, 0}
        });
    }

    @Test
    public void invalidBoardsAreRejected() {
        assertThatThrownBy(() -> Game2048Engine.from(new int[][]{
                {2, 4},
                {8}
        })).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("square");

        assertThatThrownBy(() -> Game2048Engine.from(new int[][]{
                {2, 3},
                {0, 4}
        })).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("powers of two");
    }

    @Test
    public void customTileSpawnConfigurationCanBeUsed() {
        GameSettings settings = new GameSettings();
        GameSettings.TileSpawnConfiguration config = settings.getSpawnConfiguration();
        config.update(8, 1.0);
        config.remove(2);
        config.remove(4);
        config.validate();

        Game2048Engine engine = Game2048Engine.newGame(4, new PredictableRandom(
                new int[]{0, 0, 0, 0, 0},
                new double[]{0.5}
        ), settings);

        // Initial 4 tiles are always 2
        assertThat(engine.board()[0][0]).isEqualTo(2);
        assertThat(engine.board()[0][1]).isEqualTo(2);
        assertThat(engine.board()[0][2]).isEqualTo(2);
        assertThat(engine.board()[0][3]).isEqualTo(2);

        engine.move(LEFT);
        // Row 0: [4, 4, 0, 0]. NextInt(14) with value 0 picks (0,2).
        assertThat(engine.board()[0][2]).isEqualTo(8);
    }

    @Test
    public void tileSpawnConfigurationValidation() {
        GameSettings settings = new GameSettings();
        GameSettings.TileSpawnConfiguration config = settings.getSpawnConfiguration();

        assertThatThrownBy(() -> config.update(3, 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("power of two");

        assertThatThrownBy(() -> config.update(4096, 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 2048");

        config.update(2, 0.5);
        config.update(4, 0.4);
        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sum of probabilities must be 1.0");

        config.update(8, 0.1);
        config.validate(); // Should not throw
    }

    @Test
    public void customInitialTileCount() {
        GameSettings settings = new GameSettings();
        settings.setInitialTileCount(1);
        Game2048Engine engine = Game2048Engine.newGame(4, new PredictableRandom(new int[]{5}, new double[]{}), settings);

        int count = 0;
        for (int[] row : engine.board()) {
            for (int val : row) {
                if (val != 0) count++;
            }
        }
        assertThat(count).isEqualTo(1);
    }

    private static final class PredictableRandom extends Random {
        private final Queue<Integer> ints = new ArrayDeque<>();
        private final Queue<Double> doubles = new ArrayDeque<>();

        private PredictableRandom(int[] ints, double[] doubles) {
            for (int value : ints) {
                this.ints.add(value);
            }
            for (double value : doubles) {
                this.doubles.add(value);
            }
        }

        @Override
        public int nextInt(int bound) {
            assertThat(ints).as("nextInt values").isNotEmpty();
            int value = ints.remove();
            assertThat(value).isBetween(0, bound - 1);
            return value;
        }

        @Override
        public double nextDouble() {
            assertThat(doubles).as("nextDouble values").isNotEmpty();
            return doubles.remove();
        }
    }
}
