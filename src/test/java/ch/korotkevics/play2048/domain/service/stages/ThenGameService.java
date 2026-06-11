package ch.korotkevics.play2048.domain.service.stages;

import ch.korotkevics.play2048.domain.service.DomainEventStream;
import ch.korotkevics.play2048.domain.service.GameId;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class ThenGameService extends Stage<ThenGameService> {

    @ExpectedScenarioState
    private GameId gameId;

    @ExpectedScenarioState
    private DomainEventStream eventStream;

    public ThenGameService a_game_id_is_generated() {
        assertThat(gameId).isNotNull();
        return this;
    }

    public ThenGameService a_game_started_event_is_published() {
        verify(eventStream).publish(any(DomainEventStream.GameStarted.class));
        return this;
    }

    public ThenGameService a_move_made_event_is_published() {
        verify(eventStream).publish(any(DomainEventStream.MoveMade.class));
        return this;
    }

    public ThenGameService an_ai_suggestion_event_is_published() {
        verify(eventStream).publish(any(DomainEventStream.AiSuggestionProduced.class));
        return this;
    }
}
