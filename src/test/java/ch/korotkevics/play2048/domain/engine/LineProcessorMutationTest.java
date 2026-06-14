package ch.korotkevics.play2048.domain.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LineProcessorMutationTest {

    @Test
    public void process_exhaustive_scenarios() {
        LineProcessor lp = new LineProcessor();
        
        // 2, 2 -> 4, 0
        assertArrayEquals(new int[]{4, 0, 0, 0}, lp.process(new int[]{2, 2, 0, 0}).values());
        
        // 4, 4 -> 8, 0
        assertArrayEquals(new int[]{8, 0, 0, 0}, lp.process(new int[]{4, 4, 0, 0}).values());
        
        // 2, 0, 2 -> 4, 0
        assertArrayEquals(new int[]{4, 0, 0, 0}, lp.process(new int[]{2, 0, 2, 0}).values());
        
        // 2, 2, 2 -> 4, 2
        assertArrayEquals(new int[]{4, 2, 0, 0}, lp.process(new int[]{2, 2, 2, 0}).values());
    }
}
