package ch.korotkevics.play2048.domain.service.stages;

import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.engine.Direction;
import ch.korotkevics.play2048.domain.service.DomainEventStream;
import ch.korotkevics.play2048.domain.service.GameId;
import ch.korotkevics.play2048.domain.service.GameService;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class GivenGameService extends Stage<GivenGameService> {

    @ProvidedScenarioState
    private GameService gameService;

    @ProvidedScenarioState
    private DomainEventStream eventStream;

    @ProvidedScenarioState
    private MoveSuggester aiFacade;

    @ProvidedScenarioState
    private ch.korotkevics.play2048.domain.service.GameRepository gameRepository;

    @ProvidedScenarioState
    private ch.korotkevics.play2048.domain.service.SettingsRepository settingsRepository;

    @ProvidedScenarioState
    private String clientId = "test-client";

    public GivenGameService a_game_service() {
        eventStream = mock(DomainEventStream.class);
        aiFacade = mock(MoveSuggester.class);
        gameRepository = mock(ch.korotkevics.play2048.domain.service.GameRepository.class);
        settingsRepository = mock(ch.korotkevics.play2048.domain.service.SettingsRepository.class);

        // Basic stubbing to make gameRepository work in tests
        java.util.Map<String, ch.korotkevics.play2048.domain.engine.Game2048Engine> games = new java.util.HashMap<>();
        java.util.Map<String, java.util.Stack<ch.korotkevics.play2048.domain.engine.Game2048Engine>> history = new java.util.HashMap<>();

        Mockito.doAnswer(invocation -> {
            String cid = invocation.getArgument(0);
            ch.korotkevics.play2048.domain.engine.Game2048Engine eng = invocation.getArgument(1);
            games.put(cid, eng);
            Mockito.when(gameRepository.findByClientId(cid)).thenReturn(Optional.ofNullable(games.get(cid)));
            return null;
        }).when(gameRepository).save(any(), any());

        Mockito.doAnswer(invocation -> {
            String cid = invocation.getArgument(0);
            ch.korotkevics.play2048.domain.engine.Game2048Engine eng = invocation.getArgument(1);
            history.computeIfAbsent(cid, k -> new java.util.Stack<>()).push(eng);
            return null;
        }).when(gameRepository).pushToHistory(any(), any());

        Mockito.doAnswer(invocation -> {
            String cid = invocation.getArgument(0);
            if (history.containsKey(cid) && !history.get(cid).isEmpty()) {
                return Optional.of(history.get(cid).pop());
            }
            return Optional.empty();
        }).when(gameRepository).popFromHistory(any());

        this.gameService = new GameService(gameRepository, settingsRepository, aiFacade, eventStream);
        return this;
    }

    public GivenGameService an_ai_that_suggests(Direction direction) {
        Mockito.when(aiFacade.suggestNextMove(any(), any(), any())).thenReturn(Optional.ofNullable(direction));
        return this;
    }
}
