package ch.korotkevics.play2048.domain.engine.stages;

import ch.korotkevics.play2048.domain.engine.BoardState;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class GivenBoardState extends Stage<GivenBoardState> {

    @ProvidedScenarioState
    private BoardState boardState;

    public GivenBoardState a_board_state_from_grid(int[][] grid) {
        boardState = new BoardState(grid);
        return this;
    }
}
