package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import ch.korotkevics.play2048.domain.service.GameService;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GameRestControllerTest {

    private static final String CLIENT_ID = "test-client";
    private final GameService gameService = mock(GameService.class);
    private final GameRestController controller = new GameRestController(gameService);

    @Test
    public void startNewGameDelegatesToService() {
        MoveResult result = new MoveResult(null, true, 0, 0, 0, false, false, new BoardState(new int[4][4]), Collections.emptyList(), null);
        when(gameService.startNewGame(CLIENT_ID)).thenReturn(result);

        MoveResult response = controller.startNewGame(CLIENT_ID);

        assertThat(response).isEqualTo(result);
        verify(gameService).startNewGame(CLIENT_ID);
    }

    @Test
    public void makeMoveDelegatesToService() {
        GameRestController.MoveRequest request = new GameRestController.MoveRequest(Direction.LEFT);
        MoveResult result = new MoveResult(Direction.LEFT, true, 0, 0, 0, false, false, new BoardState(new int[4][4]), Collections.emptyList(), null);
        when(gameService.makeMove(CLIENT_ID, Direction.LEFT)).thenReturn(result);

        MoveResult response = controller.makeMove(CLIENT_ID, request);

        assertThat(response.direction()).isEqualTo(Direction.LEFT);
        verify(gameService).makeMove(CLIENT_ID, Direction.LEFT);
    }

    @Test
    public void requestAiSuggestionDelegatesToService() {
        controller.requestAiSuggestion(CLIENT_ID);

        verify(gameService).requestAiSuggestion(CLIENT_ID);
    }
}
