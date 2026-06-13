package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.service.GameId;
import ch.korotkevics.play2048.domain.service.GameService;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameRestControllerTest {

    private static final String CLIENT_ID = "test-client";
    private final GameService gameService = mock(GameService.class);
    private final GameRestController controller = new GameRestController(gameService);

    @Test
    public void startNewGameDelegatesToServiceAndConvertsId() {
        GameId id = GameId.generate();
        when(gameService.startNewGame(CLIENT_ID)).thenReturn(id);

        GameRestController.GameResponse response = controller.startNewGame(CLIENT_ID);

        assertThat(response.gameId()).isEqualTo(id.value().toString());
        verify(gameService).startNewGame(CLIENT_ID);
    }

    @Test
    public void makeMoveDelegatesToService() {
        GameRestController.MoveRequest request = new GameRestController.MoveRequest(Direction.LEFT);

        controller.makeMove(CLIENT_ID, request);

        verify(gameService).makeMove(CLIENT_ID, Direction.LEFT);
    }

    @Test
    public void requestAiSuggestionDelegatesToService() {
        controller.requestAiSuggestion(CLIENT_ID);

        verify(gameService).requestAiSuggestion(CLIENT_ID);
    }
}
