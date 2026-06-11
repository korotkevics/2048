package ch.korotkevics.play2048.adapter.llm;

import ch.korotkevics.play2048.domain.engine.BoardState;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaLlmAdapterTest {

    @Test
    public void adapterReturnsEmptyOptionalForNow() {
        OllamaLlmAdapter adapter = new OllamaLlmAdapter("http://localhost:11434", "llama3");
        BoardState state = new BoardState(new int[4][4]);

        assertThat(adapter.askForMove(state)).isEmpty();
    }
}
