package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class AiFacade implements MoveSuggester {

    private final Map<UserSettings.AiType, MoveSuggester> suggesters;

    public AiFacade(Map<UserSettings.AiType, MoveSuggester> suggesters) {
        this.suggesters = Map.copyOf(suggesters);
    }

    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState, UserSettings settings, ch.korotkevics.play2048.domain.engine.GameSettings gameSettings) {
        UserSettings.AiType currentType = settings.getAiType();
        MoveSuggester suggester = suggesters.get(currentType);
        
        if (suggester == null) {
            return Optional.empty();
        }
        
        return suggester.suggestNextMove(boardState, settings, gameSettings);
    }
}
