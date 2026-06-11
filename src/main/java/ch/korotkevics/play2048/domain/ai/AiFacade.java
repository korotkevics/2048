package ch.korotkevics.play2048.domain.ai;

import ch.korotkevics.play2048.domain.engine.BoardState;
import ch.korotkevics.play2048.domain.engine.Direction;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class AiFacade implements MoveSuggester {

    private final Map<UserSettings.AiType, MoveSuggester> suggesters;
    private final UserSettings userSettings;

    public AiFacade(Map<UserSettings.AiType, MoveSuggester> suggesters, UserSettings userSettings) {
        this.suggesters = Map.copyOf(suggesters);
        this.userSettings = Objects.requireNonNull(userSettings, "userSettings must not be null");
    }

    @Override
    public Optional<Direction> suggestNextMove(BoardState boardState) {
        UserSettings.AiType currentType = userSettings.getAiType();
        MoveSuggester suggester = suggesters.get(currentType);
        
        if (suggester == null) {
            return Optional.empty();
        }
        
        return suggester.suggestNextMove(boardState);
    }
}
