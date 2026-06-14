package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.BoardState;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;

public class WhenBoardState extends Stage<WhenBoardState> {

    @ExpectedScenarioState
    @ProvidedScenarioState
    private BoardState boardState;

    @ProvidedScenarioState
    private String toStringResult;

    @ProvidedScenarioState
    private boolean equalsResult;

    @ProvidedScenarioState
    private int hashCodeResult;

    @ProvidedScenarioState
    private int lastValue;

    public WhenBoardState the_string_representation_is_requested() {
        toStringResult = boardState.toString();
        return this;
    }

    public WhenBoardState it_is_compared_to(Object other) {
        equalsResult = boardState.equals(other);
        return this;
    }

    public WhenBoardState the_hash_code_is_requested() {
        hashCodeResult = boardState.hashCode();
        return this;
    }

    public WhenBoardState the_value_at_is_requested(int row, int col) {
        lastValue = boardState.getValue(row, col);
        return this;
    }

    public WhenBoardState a_tile_is_added_at(int row, int col, int value) {
        boardState = boardState.withTile(row, col, value);
        return this;
    }
}
