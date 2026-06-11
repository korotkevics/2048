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

    private final GameService gameService = mock(GameService.class);
    private final GameRestController controller = new GameRestController(gameService);

    @Test
    public void startNewGameDelegatesToServiceAndConvertsId() {
        GameId id = GameId.generate();
        when(gameService.startNewGame()).thenReturn(id);

        GameRestController.GameResponse response = controller.startNewGame();

        assertThat(response.gameId()).isEqualTo(id.value().toString());
        verify(gameService).startNewGame();
    }

    @Test
    public void makeMoveDelegatesToService() {
        UUID id = UUID.randomUUID();
        GameRestController.MoveRequest request = new GameRestController.MoveRequest(Direction.LEFT);

        controller.makeMove(id, request);

        verify(gameService).makeMove(new GameId(id), Direction.LEFT);
    }

    @Test
    public void requestAiSuggestionDelegatesToService() {
        UUID id = UUID.randomUUID();

        controller.requestAiSuggestion(id);

        verify(gameService).requestAiSuggestion(new GameId(id));
    }
}
