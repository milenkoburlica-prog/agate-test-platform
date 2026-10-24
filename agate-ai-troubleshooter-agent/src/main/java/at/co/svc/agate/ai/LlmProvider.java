package at.co.svc.agate.ai;
/** Austauschbarer LLM-Vertrag; aktuell ist nur Groq aktiv. */
public interface LlmProvider {
    String model();
    String ask(String system, String user) throws Exception;
}
