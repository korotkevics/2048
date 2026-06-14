package ch.korotkevics.play2048.config;

import ch.korotkevics.play2048.adapter.llm.OllamaLlmAdapter;
import ch.korotkevics.play2048.domain.ai.AiFacade;
import ch.korotkevics.play2048.domain.ai.MoveSuggester;
import ch.korotkevics.play2048.domain.ai.UserSettings;
import ch.korotkevics.play2048.domain.ai.deterministic.DeterministicAiFacade;
import ch.korotkevics.play2048.domain.ai.llm.LlmAiFacade;
import ch.korotkevics.play2048.domain.service.DomainEventStream;
import ch.korotkevics.play2048.domain.service.GameService;
import ch.korotkevics.play2048.domain.service.GameRepository;
import ch.korotkevics.play2048.domain.service.SettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@Configuration
@EnableScheduling
public class GameConfig {

    private final ApplicationContext applicationContext;

    public GameConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MoveSuggester aiFacade() {
        DeterministicAiFacade deterministic = new DeterministicAiFacade();
        // Default model for Ollama, can be further configured
        LlmAiFacade llm = new LlmAiFacade(new OllamaLlmAdapter("phi3.5"));
        
        return new AiFacade(Map.of(
                UserSettings.AiType.DETERMINISTIC, deterministic,
                UserSettings.AiType.LLM, llm
        ));
    }

    @Bean
    public GameService gameService(GameRepository gameRepository, 
                                   SettingsRepository settingsRepository, 
                                   MoveSuggester aiFacade, 
                                   DomainEventStream eventStream) {
        return new GameService(gameRepository, settingsRepository, aiFacade, eventStream);
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupStaleGames() {
        // Fetch from context to avoid circular dependency during Bean creation
        GameService gameService = applicationContext.getBean(GameService.class);
        gameService.cleanupStaleGames();
    }
}
