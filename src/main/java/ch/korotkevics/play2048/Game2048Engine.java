package ch.korotkevics.play2048;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

public final class Game2048Engine {

    public static final int DEFAULT_SIZE = 4;
    public static final int TARGET_TILE = 2048;

    private final int size;
    private final int[][] board;
    private final Random random;
    private final GameSettings settings;
    private int score;

    private Game2048Engine(int[][] board, int score, Random random, GameSettings settings) {
        this.size = board.length;
        this.board = copyOf(board);
        this.score = score;
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        validateBoard(this.board);
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
        if (size < 2) {
            throw new IllegalArgumentException("size must be at least 2");
        }

        Game2048Engine engine = new Game2048Engine(new int[size][size], 0, random, settings);
        for (int i = 0; i < settings.getInitialTileCount(); i++) {
            engine.addTile(2);
        }
        return engine;
    }

    public static Game2048Engine from(int[][] board) {
        return from(board, 0, new Random(), new GameSettings());
    }

    public static Game2048Engine from(int[][] board, int score, Random random) {
        return from(board, score, random, new GameSettings());
    }

    public static Game2048Engine from(int[][] board, int score, Random random, GameSettings settings) {
        return new Game2048Engine(board, score, random, settings);
    }

    public MoveResult move(Direction direction) {
        Objects.requireNonNull(direction, "direction must not be null");

        int scoreGained = 0;
        boolean moved = false;
        for (int index = 0; index < size; index++) {
            int[] originalLine = line(index, direction);
            ProcessedLine processedLine = process(originalLine);
            if (!Arrays.equals(originalLine, processedLine.values())) {
                moved = true;
                setLine(index, direction, processedLine.values());
            }
            scoreGained += processedLine.scoreGained();
        }

        if (moved) {
            score += scoreGained;
            addRandomTile();
        }

        return new MoveResult(direction, moved, scoreGained, score, isGameOver(), isWon(), board());
    }

    public int[][] board() {
        return copyOf(board);
    }

    public int score() {
        return score;
    }

    public GameSettings settings() {
        return settings;
    }

    public boolean isWon() {
        for (int[] row : board) {
            for (int value : row) {
                if (value >= TARGET_TILE) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGameOver() {
        if (emptyCells().size() > 0) {
            return false;
        }

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (row + 1 < size && board[row][column] == board[row + 1][column]) {
                    return false;
                }
                if (column + 1 < size && board[row][column] == board[row][column + 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addRandomTile() {
        Map<Integer, Double> probs = settings.getSpawnConfiguration().getProbabilities();
        if (probs.isEmpty()) {
            addTile(2);
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
        addTile(selectedValue);
    }

    private void addTile(int value) {
        List<Cell> emptyCells = emptyCells();
        if (emptyCells.isEmpty()) {
            return;
        }

        Cell cell = emptyCells.get(random.nextInt(emptyCells.size()));
        board[cell.row()][cell.column()] = value;
    }

    private List<Cell> emptyCells() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (board[row][column] == 0) {
                    emptyCells.add(new Cell(row, column));
                }
            }
        }
        return emptyCells;
    }

    private int[] line(int index, Direction direction) {
        int[] line = new int[size];
        for (int offset = 0; offset < size; offset++) {
            Cell cell = cellFor(index, offset, direction);
            line[offset] = board[cell.row()][cell.column()];
        }
        return line;
    }

    private void setLine(int index, Direction direction, int[] line) {
        for (int offset = 0; offset < size; offset++) {
            Cell cell = cellFor(index, offset, direction);
            board[cell.row()][cell.column()] = line[offset];
        }
    }

    private Cell cellFor(int index, int offset, Direction direction) {
        return switch (direction) {
            case LEFT -> new Cell(index, offset);
            case RIGHT -> new Cell(index, size - 1 - offset);
            case UP -> new Cell(offset, index);
            case DOWN -> new Cell(size - 1 - offset, index);
        };
    }

    private ProcessedLine process(int[] line) {
        int[] result = new int[size];
        int resultIndex = 0;
        int scoreGained = 0;

        for (int index = 0; index < line.length; index++) {
            if (line[index] == 0) {
                continue;
            }

            if (index + 1 < line.length) {
                int nextIndex = nextNonZeroIndex(line, index + 1);
                if (nextIndex >= 0 && line[index] == line[nextIndex]) {
                    result[resultIndex++] = line[index] * 2;
                    scoreGained += line[index] * 2;
                    index = nextIndex;
                    continue;
                }
            }

            result[resultIndex++] = line[index];
        }

        return new ProcessedLine(result, scoreGained);
    }

    private int nextNonZeroIndex(int[] line, int startIndex) {
        for (int index = startIndex; index < line.length; index++) {
            if (line[index] != 0) {
                return index;
            }
        }
        return -1;
    }

    private static int[][] copyOf(int[][] board) {
        Objects.requireNonNull(board, "board must not be null");
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = Arrays.copyOf(board[row], board[row].length);
        }
        return copy;
    }

    private static void validateBoard(int[][] board) {
        if (board.length < 2) {
            throw new IllegalArgumentException("board must be at least 2x2");
        }

        for (int[] row : board) {
            if (row == null || row.length != board.length) {
                throw new IllegalArgumentException("board must be square");
            }
            for (int value : row) {
                if (value < 0 || (value != 0 && (value & (value - 1)) != 0)) {
                    throw new IllegalArgumentException("board values must be zero or powers of two");
                }
            }
        }
    }

    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    public record MoveResult(
            Direction direction,
            boolean moved,
            int scoreGained,
            int score,
            boolean gameOver,
            boolean won,
            int[][] board
    ) {
        public MoveResult {
            board = copyOf(board);
        }
    }

    private record Cell(int row, int column) {
    }

    private record ProcessedLine(int[] values, int scoreGained) {
    }

    public static final class GameSettings {
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
}
