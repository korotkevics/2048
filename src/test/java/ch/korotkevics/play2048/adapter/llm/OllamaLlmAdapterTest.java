package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.engine.BoardState;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaLlmAdapterTest {

    @Test
    public void adapterHandlesMissingConnectionGracefully() {
        // This is more of a behavioral check that it doesn't crash
        OllamaLlmAdapter adapter = new OllamaLlmAdapter("http://non-existent-host:1234", "llama3");
        BoardState state = new BoardState(new int[4][4]);

        assertThat(adapter.askForMove(state)).isEmpty();
    }
}
