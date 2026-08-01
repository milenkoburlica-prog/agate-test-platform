package at.co.svc.agate.assistant.service;

import java.time.Duration;

import at.co.svc.agate.assistant.langchain.AgateDslGenerator;
import at.co.svc.agate.assistant.langchain.AgateTaskDecomposer;
import at.co.svc.agate.assistant.langchain.IntentClassifier;
import at.co.svc.agate.assistant.langchain.MessageIntent;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgateAiService {
    private AgateDslGenerator dslGenerator;
    
    interface ChatAssistant { String chat(String message); }

    private ChatAssistant assistant;
    private IntentClassifier classifier;
    private AgateTaskDecomposer decomposer; // New component

    @PostConstruct
    void init() {
        // 1. Text Model (For Chat and Intent classification)
        OllamaChatModel textModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .timeout(Duration.ofSeconds(60))
                .build();

        // 2. JSON Model (For the Decomposer only)
        OllamaChatModel jsonModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .format("json") // This forces the model to output valid JSON
                .timeout(Duration.ofSeconds(60))
                .build();

        // Uses textModel
        this.assistant = AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(textModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // Uses textModel
        this.classifier = AiServices.builder(IntentClassifier.class)
                .chatLanguageModel(textModel)
                .build();

        // Uses jsonModel - This prevents the ClassCastException
        this.decomposer = AiServices.builder(AgateTaskDecomposer.class)
                .chatLanguageModel(jsonModel)
                .build();
        
        // Inicijalizacija generatora (može koristiti isti jsonModel ili textModel)
        this.dslGenerator = AiServices.builder(AgateDslGenerator.class)
                .chatLanguageModel(textModel) 
                .build();
    }
    
    public String chat(String message) {
        MessageIntent intent = classifier.classify(message);
        
        if (intent == MessageIntent.TEST_CASE_CREATION) {
            // 1. Dobijamo plan (ono što si video u prošlom koraku)
            String plan = decomposer.decompose(message);
            
            // 2. Šaljemo taj plan generatoru da napravi YAML
            String finalYaml = dslGenerator.generateYaml(plan);
            
            System.out.println("🚀 GENERATED YAML:\n" + finalYaml);
            
            return "### ✅ Generated Agate DSL\n\n```yaml\n" + finalYaml + "\n```";
        }

        return assistant.chat(message);
    }
    
}
