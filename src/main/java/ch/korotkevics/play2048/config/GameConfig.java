package ch.korotkevics.play2048.config;

import ch.korotkevics.play2048.adapter.llm.OllamaLlmAdapter;
import ch.korotkevics.play2048.domain.ai.AiFacade;
import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.ai.deterministic.DeterministicAiFacade;
import ch.korotkevics.play2048.domain.ai.llm.LlmAiFacade;
import ch.korotkevics.play2048.domain.service.DomainEventStream;
import ch.korotkevics.play2048.domain.service.GameService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GameConfig {

    @Bean
    public UserSettings userSettings() {
        return new UserSettings();
    }

    @Bean
    public MoveSuggester aiFacade(UserSettings userSettings) {
        DeterministicAiFacade deterministic = new DeterministicAiFacade();
        // Default model for Ollama, can be further configured
        LlmAiFacade llm = new LlmAiFacade(new OllamaLlmAdapter("llama3"));
        
        return new AiFacade(Map.of(
                UserSettings.AiType.DETERMINISTIC, deterministic,
                UserSettings.AiType.LLM, llm
        ), userSettings);
    }

    @Bean
    public GameService gameService(MoveSuggester aiFacade, DomainEventStream eventStream) {
        return new GameService(aiFacade, eventStream);
    }
}
