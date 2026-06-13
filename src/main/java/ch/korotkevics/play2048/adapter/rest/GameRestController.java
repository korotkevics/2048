package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.engine.MoveResult;
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
    public MoveResult startNewGame(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        return gameService.startNewGame(clientId);
    }

    @GetMapping("/current")
    public MoveResult getCurrentGame(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        int highScore = gameService.getSettings(clientId).highScore();
        return gameService.getActiveGame(clientId)
                .map(engine -> new MoveResult(
                        null, false, 0, engine.score(), highScore,
                        engine.isGameOver(), engine.isWon(), engine.boardState(), 
                        java.util.Collections.emptyList(), null // Do not return engine to REST
                ))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "No active game"));
    }

    @PostMapping("/move")
    public MoveResult makeMove(@RequestHeader(value = CLIENT_ID_HEADER) String clientId, 
                               @RequestBody MoveRequest request) {
        MoveResult result = gameService.makeMove(clientId, request.direction());
        // Strip engine from response
        return new MoveResult(
                result.direction(), result.moved(), result.scoreGained(), result.score(),
                result.highScore(), result.gameOver(), result.won(), result.boardState(),
                result.deltas(), null
        );
    }

    @PostMapping("/ai")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestAiSuggestion(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        gameService.requestAiSuggestion(clientId);
    }

    @PostMapping("/auto-play")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startAutoPlay(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        gameService.startAutoPlay(clientId);
    }

    @DeleteMapping("/auto-play")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopAutoPlay(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        gameService.stopAutoPlay(clientId);
    }

    @PostMapping("/undo")
    public MoveResult undo(@RequestHeader(value = CLIENT_ID_HEADER) String clientId) {
        return gameService.undo(clientId)
                .map(result -> new MoveResult(
                        result.direction(), result.moved(), result.scoreGained(), result.score(),
                        result.highScore(), result.gameOver(), result.won(), result.boardState(),
                        result.deltas(), null
                ))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "No moves to undo"));
    }

    public record MoveRequest(Direction direction) {}
}
