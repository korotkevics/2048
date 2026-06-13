package ch.korotkevics.play2048.domain.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        Board emptyBoard = new Board(size);
        TileSpawner spawner = new TileSpawner();
        Board seededBoard = spawner.spawnInitialTiles(emptyBoard, settings, random);
        return new Game2048Engine(seededBoard, 0, random, settings);
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
        List<GlobalTileMove> deltas = new ArrayList<>();
        
        MoveResult simResult = simulateMove(direction);
        if (simResult.moved()) {
            deltas.addAll(simResult.deltas());
            
            Board boardWithTile = tileSpawner.spawnRandomTile(simResult.nextEngine().board, settings, random);
            
            // Track the new random tile
            Board oldBoard = simResult.nextEngine().board;
            for (int r = 0; r < boardWithTile.size(); r++) {
                for (int c = 0; c < boardWithTile.size(); c++) {
                    if (oldBoard.getValue(r, c) == 0 && boardWithTile.getValue(r, c) != 0) {
                        deltas.add(new GlobalTileMove(r, c, r, c, boardWithTile.getValue(r, c), false, true));
                    }
                }
            }

            Game2048Engine finalEngine = new Game2048Engine(boardWithTile, simResult.score(), random, settings);
            return new MoveResult(
                    direction,
                    true,
                    simResult.scoreGained(),
                    simResult.score(),
                    simResult.score(), // Temp high score, will be updated by GameService
                    finalEngine.isGameOver(),
                    finalEngine.isWon(),
                    finalEngine.boardState(),
                    deltas,
                    finalEngine
            );
        }
        return simResult;
    }

    public MoveResult simulateMove(Direction direction) {
        Objects.requireNonNull(direction, "direction must not be null");

        int scoreGained = 0;
        boolean moved = false;
        int size = board.size();
        Board currentBoard = board;
        List<GlobalTileMove> deltas = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            int[] originalLine = currentBoard.getLine(index, direction);
            LineProcessor.ProcessedLine processedLine = lineProcessor.process(originalLine);
            
            if (!Arrays.equals(originalLine, processedLine.values())) {
                moved = true;
                currentBoard = currentBoard.withLine(index, direction, processedLine.values());
                
                // Convert line moves to global moves
                for (TileMove move : processedLine.moves()) {
                    Board.Cell fromCell = cellFor(index, move.fromIndex(), direction, size);
                    Board.Cell toCell = cellFor(index, move.toIndex(), direction, size);
                    deltas.add(new GlobalTileMove(
                            fromCell.row(), fromCell.column(),
                            toCell.row(), toCell.column(),
                            move.value(), move.merged(), false
                    ));
                }
            }
            scoreGained += processedLine.scoreGained();
        }

        int finalScore = score + scoreGained;
        Game2048Engine nextEngine = new Game2048Engine(currentBoard, finalScore, random, settings);

        return new MoveResult(
                direction,
                moved,
                scoreGained,
                finalScore,
                finalScore, // Temp high score
                nextEngine.isGameOver(),
                nextEngine.isWon(),
                nextEngine.boardState(),
                deltas,
                nextEngine
        );
    }

    private Board.Cell cellFor(int index, int offset, Direction direction, int size) {
        return switch (direction) {
            case LEFT -> new Board.Cell(index, offset);
            case RIGHT -> new Board.Cell(index, size - 1 - offset);
            case UP -> new Board.Cell(offset, index);
            case DOWN -> new Board.Cell(size - 1 - offset, index);
        };
    }

    public int[][] board() {
        return board.grid();
    }

    public BoardState boardState() {
        return new BoardState(board.grid());
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
