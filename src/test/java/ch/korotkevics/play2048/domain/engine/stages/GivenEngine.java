package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.GameSettings;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import java.util.Random;

public class GivenEngine extends Stage<GivenEngine> {

    @ProvidedScenarioState
    private Game2048Engine engine;

    public GivenEngine a_new_game() {
        engine = Game2048Engine.newGame();
        return this;
    }

    public GivenEngine a_new_game_with_size(int size) {
        engine = Game2048Engine.newGame(size, new Random());
        return this;
    }

    public GivenEngine a_game_from_grid(int[][] grid) {
        engine = Game2048Engine.from(grid);
        return this;
    }

    public GivenEngine a_game_from_grid_with_score(int[][] grid, int score) {
        engine = Game2048Engine.from(grid, score, new Random());
        return this;
    }

    public GivenEngine a_game_from_grid_with_settings(int[][] grid, GameSettings settings) {
        engine = Game2048Engine.from(grid, 0, new Random(), settings);
        return this;
    }
    
    public GivenEngine a_game_with_random_seed(int[][] grid, long seed) {
        engine = Game2048Engine.from(grid, 0, new Random(seed));
        return this;
    }
}
