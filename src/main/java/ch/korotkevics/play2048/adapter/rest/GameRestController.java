package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;
import ch.korotkevics.play2048.domain.service.GameId;
import ch.korotkevics.play2048.domain.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public final class GameRestController {

    private static final String CLIENT_ID_HEADER = "X-Client-ID";
    private final GameService gameService;

    public GameRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GameResponse startNewGame(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId) {
        GameId gameId = gameService.startNewGame(clientId);
        return new GameResponse(gameId.value().toString());
    }

    @GetMapping("/current")
    public MoveResult getCurrentGame(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId) {
        return gameService.getActiveGame(clientId)
                .map(engine -> new MoveResult(
                        null, false, 0, engine.score(),
                        engine.isGameOver(), engine.isWon(), engine.boardState(), 
                        java.util.Collections.emptyList(), engine
                ))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "No active game"));
    }

    @PostMapping("/move")
    public MoveResult makeMove(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId, 
                               @RequestBody MoveRequest request) {
        return gameService.makeMove(clientId, request.direction());
    }

    @PostMapping("/ai")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestAiSuggestion(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId) {
        gameService.requestAiSuggestion(clientId);
    }

    @PostMapping("/undo")
    public MoveResult undo(@RequestHeader(value = CLIENT_ID_HEADER, defaultValue = "default") String clientId) {
        return gameService.undo(clientId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "No moves to undo"));
    }

    public record GameResponse(String gameId) {}
    public record MoveRequest(Direction direction) {}
}
