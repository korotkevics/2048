package ch.korotkevics.play2048.domain.engine;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LineProcessorTest {

    private final LineProcessor processor = new LineProcessor();

    @Test
    public void testEmptyLine() {
        int[] input = {0, 0, 0, 0};
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{0, 0, 0, 0});
        assertEquals(result.scoreGained(), 0);
    }

    @Test
    public void testNoMerge() {
        int[] input = {2, 4, 8, 16};
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{2, 4, 8, 16});
        assertEquals(result.scoreGained(), 0);
    }

    @Test
    public void testSingleMerge() {
        int[] input = {2, 2, 0, 0};
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{4, 0, 0, 0});
        assertEquals(result.scoreGained(), 4);
    }

    @Test
    public void testMultipleMerges() {
        int[] input = {2, 2, 4, 4};
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{4, 8, 0, 0});
        assertEquals(result.scoreGained(), 12);
    }

    @Test
    public void testGapsMerges() {
        int[] input = {2, 0, 2, 0};
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{4, 0, 0, 0});
        assertEquals(result.scoreGained(), 4);
    }

    @Test
    public void testCascadeMerge() {
        int[] input = {2, 2, 4, 0};
        // Should merge to 4, 4, 0, 0 in one pass, not 8!
        LineProcessor.ProcessedLine result = processor.process(input);
        assertEquals(result.values(), new int[]{4, 4, 0, 0});
        assertEquals(result.scoreGained(), 4);
    }
}
