package ch.korotkevics.play2048.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public final class Game2048Engine {

    public static final int DEFAULT_SIZE = 4;
    public static final int TARGET_TILE = 2048;

    private final Board board;
    private final Random random;
    private final GameSettings settings;
    private final LineProcessor lineProcessor;
    private final TileSpawner tileSpawner;
    private int score;

    private Game2048Engine(Board board, int score, Random random, GameSettings settings) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.score = score;
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.lineProcessor = new LineProcessor();
        this.tileSpawner = new TileSpawner();
        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative");
        }
    }

    public static Game2048Engine newGame() {
        return newGame(DEFAULT_SIZE, new Random(), new GameSettings());
    }

    public static Game2048Engine newGame(int size, Random random) {
        return newGame(size, random, new GameSettings());
    }

    public static Game2048Engine newGame(int size, Random random, GameSettings settings) {
        Game2048Engine engine = new Game2048Engine(new Board(size), 0, random, settings);
        engine.tileSpawner.spawnInitialTiles(engine.board, settings, random);
        return engine;
    }

    public static Game2048Engine from(int[][] grid) {
        return from(grid, 0, new Random(), new GameSettings());
    }

    public static Game2048Engine from(int[][] grid, int score, Random random) {
        return from(grid, score, random, new GameSettings());
    }

    public static Game2048Engine from(int[][] grid, int score, Random random, GameSettings settings) {
        return new Game2048Engine(new Board(grid), score, random, settings);
    }

    public MoveResult move(Direction direction) {
        Objects.requireNonNull(direction, "direction must not be null");

        int scoreGained = 0;
        boolean moved = false;
        int size = board.size();

        for (int index = 0; index < size; index++) {
            int[] originalLine = board.getLine(index, direction);
            LineProcessor.ProcessedLine processedLine = lineProcessor.process(originalLine);
            if (!Arrays.equals(originalLine, processedLine.values())) {
                moved = true;
                board.setLine(index, direction, processedLine.values());
            }
            scoreGained += processedLine.scoreGained();
        }

        if (moved) {
            score += scoreGained;
            tileSpawner.spawnRandomTile(board, settings, random);
        }

        return new MoveResult(
                direction,
                moved,
                scoreGained,
                score,
                isGameOver(),
                isWon(),
                board()
        );
    }

    public int[][] board() {
        return board.grid();
    }

    public int score() {
        return score;
    }

    public GameSettings settings() {
        return settings;
    }

    public boolean isWon() {
        return board.hasTileWithValue(TARGET_TILE);
    }

    public boolean isGameOver() {
        return board.isGameOver();
    }
}
