package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.Game2048Engine;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class WhenEngine extends Stage<WhenEngine> {

    @ExpectedScenarioState
    private Game2048Engine engine;

    @ProvidedScenarioState
    private MoveResult lastResult;

    public WhenEngine a_move_is_made(Direction direction) {
        lastResult = engine.move(direction);
        return this;
    }
}
