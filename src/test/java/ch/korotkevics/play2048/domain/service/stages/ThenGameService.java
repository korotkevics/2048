package ch.korotkevics.play2048.domain.service.stages;

import ch.korotkevics.play2048.domain.engine.MoveResult;
import ch.korotkevics.play2048.domain.service.DomainEventStream;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class ThenGameService extends Stage<ThenGameService> {

    @ExpectedScenarioState
    private MoveResult moveResult;

    @ExpectedScenarioState
    private Optional<MoveResult> undoResult;

    @ExpectedScenarioState
    private DomainEventStream eventStream;

    @ExpectedScenarioState
    private ch.korotkevics.play2048.domain.service.GameRepository gameRepository;

    public ThenGameService a_game_id_is_generated() {
        assertThat(moveResult).isNotNull();
        return this;
    }

    public ThenGameService a_game_started_event_is_published() {
        verify(eventStream).publish(any(DomainEventStream.GameStarted.class));
        return this;
    }

    public ThenGameService a_move_made_event_is_published() {
        verify(eventStream, atLeastOnce()).publish(any(DomainEventStream.MoveMade.class));
        return this;
    }

    public ThenGameService a_move_made_event_is_published_with_score(int score) {
        verify(eventStream, atLeastOnce()).publish(argThat(event -> {
            if (event instanceof DomainEventStream.MoveMade m) {
                return m.moveResult().score() == score;
            }
            return false;
        }));
        return this;
    }

    public ThenGameService an_ai_suggestion_event_is_published() {
        verify(eventStream).publish(any(DomainEventStream.AiSuggestionProduced.class));
        return this;
    }

    public ThenGameService an_undo_result_is_present() {
        assertThat(undoResult).isPresent();
        return this;
    }

    public ThenGameService no_exception_is_thrown() {
        return this;
    }

    public ThenGameService the_stale_games_cleanup_was_called_on_repository() {
        verify(gameRepository).deleteStaleGames(any());
        return this;
    }
}
