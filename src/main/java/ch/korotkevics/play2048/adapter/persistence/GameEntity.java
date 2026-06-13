package ch.korotkevics.play2048.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private String clientId;

    @Column(columnDefinition = "TEXT")
    private String boardJson;

    private int score;

    private Instant lastActivityAt;

    public GameEntity() {}

    public GameEntity(String clientId, String boardJson, int score, Instant lastActivityAt) {
        this.clientId = clientId;
        this.boardJson = boardJson;
        this.score = score;
        this.lastActivityAt = lastActivityAt;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getBoardJson() { return boardJson; }
    public void setBoardJson(String boardJson) { this.boardJson = boardJson; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
}
