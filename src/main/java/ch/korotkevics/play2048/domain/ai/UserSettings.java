package ch.korotkevics.play2048.domain.ai;

public final class UserSettings {

    private AiType aiType = AiType.DETERMINISTIC;

    public synchronized AiType getAiType() {
        return aiType;
    }

    public synchronized void setAiType(AiType aiType) {
        if (aiType == null) {
            throw new IllegalArgumentException("aiType must not be null");
        }
        this.aiType = aiType;
    }

    public enum AiType {
        DETERMINISTIC,
        LLM
    }
}
