package ch.korotkevics.play2048.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "game_history")
public class GameHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String boardJson;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Instant createdAt;

    public GameHistoryEntity() {}

    public GameHistoryEntity(String clientId, String boardJson, int score) {
        this.clientId = clientId;
        this.boardJson = boardJson;
        this.score = score;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getClientId() { return clientId; }
    public String getBoardJson() { return boardJson; }
    public int getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }
}
