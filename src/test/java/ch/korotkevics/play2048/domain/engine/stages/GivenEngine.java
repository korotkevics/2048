package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class GivenEngine extends Stage<GivenEngine> {

    @ProvidedScenarioState
    private Game2048Engine engine;

    public GivenEngine a_new_game() {
        engine = Game2048Engine.newGame();
        return this;
    }

    public GivenEngine a_game_from_grid(int[][] grid) {
        engine = Game2048Engine.from(grid);
        return this;
    }
}
