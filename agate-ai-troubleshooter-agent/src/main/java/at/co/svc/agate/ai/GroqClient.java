package at.co.svc.agate.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class GroqClient implements LlmProvider {
    @Inject ObjectMapper mapper;
    @ConfigProperty(name="agate.groq.url") String url;
    @ConfigProperty(name="agate.groq.model") String model;
    @ConfigProperty(name="agate.groq.timeout-seconds", defaultValue="90") int timeoutSeconds;
    @ConfigProperty(name="agate.groq.api-key-file") java.util.Optional<String> keyFile;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    public String model() { return model; }

    public String ask(String system, String user) throws Exception {
        String key = readKey();
        var payload = Map.of("model", model, "temperature", 0, "max_completion_tokens", 2500,
                "messages", List.of(Map.of("role", "system", "content", system),
                                    Map.of("role", "user", "content", user)));
        var req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload), StandardCharsets.UTF_8)).build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() != 200) {
            if (res.statusCode() == 429) throw new GroqException(429, "Groq-Ratenlimit erreicht (HTTP 429).");
            throw new GroqException(502, "Groq-Anfrage fehlgeschlagen (HTTP " + res.statusCode() + ").");
        }
        JsonNode response = mapper.readTree(res.body());
        String answer = response.path("choices").path(0).path("message").path("content").asText("");
        if (answer.isBlank()) throw new GroqException(502, "Groq hat keine Textantwort geliefert.");
        return answer;
    }

    String readKey() throws Exception {
        // Eine Prozessvariable hat Vorrang; andernfalls wird die externe Datei verwendet.
        String env = System.getenv("GROQ_API_KEY");
        if (env != null && !env.isBlank()) return env.trim();
        String path = System.getenv("GROQ_API_KEY_FILE");
        if (path == null || path.isBlank()) path = keyFile.orElse(null);
        if (path == null || path.isBlank()) throw new IllegalStateException(
                "API-Schlüssel fehlt. GROQ_API_KEY_FILE setzen oder agate.groq.api-key-file konfigurieren.");
        Path file = Path.of(path).toAbsolutePath().normalize();
        if (!Files.isRegularFile(file)) throw new IllegalStateException("Die API-Schlüsseldatei wurde nicht gefunden.");
        String key = Files.readString(file, StandardCharsets.UTF_8).strip();
        if (key.isBlank() || key.contains("\n") || key.contains("\r"))
            throw new IllegalStateException("Die API-Schlüsseldatei muss genau einen Schlüssel enthalten.");
        return key;
    }
    public static final class GroqException extends RuntimeException {
        public final int httpStatus;
        public GroqException(int code, String message) { super(message); httpStatus=code; }
    }
}
