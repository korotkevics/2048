package ch.korotkevics.play2048.adapter.rest;

import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.service.GameId;
import ch.korotkevics.play2048.domain.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public final class GameRestController {

    private final GameService gameService;

    public GameRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GameResponse startNewGame() {
        GameId gameId = gameService.startNewGame();
        return new GameResponse(gameId.value().toString());
    }

    @PostMapping("/{id}/move")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void makeMove(@PathVariable UUID id, @RequestBody MoveRequest request) {
        gameService.makeMove(new GameId(id), request.direction());
    }

    @PostMapping("/{id}/ai")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestAiSuggestion(@PathVariable UUID id) {
        gameService.requestAiSuggestion(new GameId(id));
    }

    public record GameResponse(String gameId) {}
    public record MoveRequest(Direction direction) {}
}
