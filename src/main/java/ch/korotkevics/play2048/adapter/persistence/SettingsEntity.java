package ch.korotkevics.play2048.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class SettingsEntity {

    @Id
    private String clientId;

    private String aiType;

    private int initialTileCount;

    @Column(columnDefinition = "TEXT")
    private String tileProbabilitiesJson;

    private String version;

    private int highScore;

    public SettingsEntity() {}

    public SettingsEntity(String clientId, String aiType, int initialTileCount, String tileProbabilitiesJson, String version, int highScore) {
        this.clientId = clientId;
        this.aiType = aiType;
        this.initialTileCount = initialTileCount;
        this.tileProbabilitiesJson = tileProbabilitiesJson;
        this.version = version;
        this.highScore = highScore;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAiType() { return aiType; }
    public void setAiType(String aiType) { this.aiType = aiType; }

    public int getInitialTileCount() { return initialTileCount; }
    public void setInitialTileCount(int initialTileCount) { this.initialTileCount = initialTileCount; }

    public String getTileProbabilitiesJson() { return tileProbabilitiesJson; }
    public void setTileProbabilitiesJson(String tileProbabilitiesJson) { this.tileProbabilitiesJson = tileProbabilitiesJson; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
}
