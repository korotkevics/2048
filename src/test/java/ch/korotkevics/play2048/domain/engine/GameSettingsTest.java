package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import java.util.Map;
import static org.testng.Assert.*;

public class GameSettingsTest {

    @Test
    public void testDefaultSettings() {
        GameSettings settings = new GameSettings();
        assertEquals(settings.getInitialTileCount(), 4);
        Map<Integer, Double> probs = settings.getSpawnConfiguration().getProbabilities();
        assertEquals(probs.get(2), 0.9);
        assertEquals(probs.get(4), 0.1);
    }

    @Test
    public void testSetInitialTileCount() {
        GameSettings settings = new GameSettings();
        settings.setInitialTileCount(2);
        assertEquals(settings.getInitialTileCount(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetInitialTileCountInvalid() {
        new GameSettings().setInitialTileCount(0);
    }

    @Test
    public void testUpdateSpawnConfiguration() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().update(8, 0.5);
        assertEquals(settings.getSpawnConfiguration().getProbabilities().get(8), 0.5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateSpawnConfigurationInvalidProbabilityLow() {
        new GameSettings().getSpawnConfiguration().update(2, -0.1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateSpawnConfigurationInvalidProbabilityHigh() {
        new GameSettings().getSpawnConfiguration().update(2, 1.1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateSpawnConfigurationInvalidValueNotPowerOfTwo() {
        new GameSettings().getSpawnConfiguration().update(3, 0.5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateSpawnConfigurationInvalidValueTooSmall() {
        new GameSettings().getSpawnConfiguration().update(1, 0.5);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateSpawnConfigurationInvalidValueTooLarge() {
        new GameSettings().getSpawnConfiguration().update(4096, 0.5);
    }

    @Test
    public void testRemoveSpawnConfiguration() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().remove(4);
        assertFalse(settings.getSpawnConfiguration().getProbabilities().containsKey(4));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRemoveLastSpawnConfiguration() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().remove(4);
        settings.getSpawnConfiguration().remove(2);
    }

    @Test
    public void testValidateValidConfiguration() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().validate(); // Should not throw
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testValidateInvalidSum() {
        GameSettings settings = new GameSettings();
        settings.getSpawnConfiguration().update(2, 0.5);
        // Sum is now 0.5 + 0.1 = 0.6
        settings.getSpawnConfiguration().validate();
    }
}
