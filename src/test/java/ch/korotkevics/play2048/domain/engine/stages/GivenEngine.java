package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import java.util.Random;

public class GivenEngine extends Stage<GivenEngine> {

    @ProvidedScenarioState
    private Game2048Engine engine;

    @ProvidedScenarioState
    private Exception exception;

    public GivenEngine a_new_game() {
        engine = Game2048Engine.newGame();
        return this;
    }

    public GivenEngine a_new_game_with_size(int size) {
        try {
            engine = Game2048Engine.newGame(size, new Random());
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public GivenEngine a_new_game_with_random_seed(int size, long seed) {
        engine = Game2048Engine.newGame(size, new Random(seed));
        return this;
    }

    public GivenEngine a_game_from_grid(int[][] grid) {
        try {
            engine = Game2048Engine.from(grid);
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public GivenEngine a_game_from_grid_with_score(int[][] grid, int score) {
        try {
            engine = Game2048Engine.from(grid, score, new Random());
        } catch (Exception e) {
            exception = e;
        }
        return this;
    }

    public GivenEngine a_game_from_grid_with_settings(int[][] grid, GameSettings settings) {
        engine = Game2048Engine.from(grid, 0, new Random(), settings);
        return this;
    }
    
    public GivenEngine a_game_with_settings(int[][] grid, GameSettings settings) {
        engine = Game2048Engine.newGame(grid.length, new Random(), settings);
        return this;
    }
    
    public GivenEngine a_game_with_random_seed(int[][] grid, long seed) {
        engine = Game2048Engine.from(grid, 0, new Random(seed));
        return this;
    }
}
