package at.co.svc.agate.ai;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Locale;

/** Deterministic intent and subject router. Model output never grants permissions. */
@ApplicationScoped
public class RequestRouter {
    public enum Intent { CAPABILITIES, CREATE_TEST, FORBIDDEN_EXECUTION,
        ENGINE_DIAGNOSE, UNSUPPORTED_ENGINE, EXPLAIN_PREVIOUS, DIAGNOSE, NEEDS_INPUT }
    public record Route(Intent intent, String engine) {}

    public Route classify(String message, boolean hasPreviousDiagnosis, boolean hasFreshEvidence) {
        String s = message.toLowerCase(Locale.ROOT).replace('‑','-');
        if (containsAny(s, "was kannst du", "welche tools", "welche agate-tools", "fähigkeiten", "capabilities", "deine funktionen"))
            return new Route(Intent.CAPABILITIES, "GENERAL");
        if (containsAny(s, "erstelle", "erstellen", "generiere", "schreibe", "baue", "anlegen", "erzeuge") &&
                containsAny(s, "test", "testsuite", "testfall", "testfälle", "reusable"))
            return new Route(Intent.CREATE_TEST, "GENERAL");
        if (containsAny(s, "starte", "ausführen", "führe aus", "deploye", "lösche", "restart", "neustart", "rollout") &&
                containsAny(s, "test", "deployment", "pod", "cluster", "produktion", "produktiv", "sql", "kommando"))
            return new Route(Intent.FORBIDDEN_EXECUTION, "GENERAL");
        // Subject matching precedes reuse of SOAP evidence. A different engine starts a different investigation.
        if (containsAny(s, "openshift", "crashloopbackoff", "kubectl", "oc get", "pod-logs", "pod logs", "deployment", "pod-events"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "OPENSHIFT");
        if (containsAny(s, "powershell", "cmd-engine", "batch", "exit code", "exitcode", "stderr", "sqlplus"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "CMD");
        if (containsAny(s, "sql", "jdbc", "oracle", "postgresql", "datenbank"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "SQL");
        if (containsAny(s, "playwright", "browser", "locator", "gui-test"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "GUI");
        if (containsAny(s, "rest", "jsonpath", "http 500", "http 503"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "REST");
        if (containsAny(s, "buffer", "missing_buffer", "b[", "variable", "variablen"))
            return new Route(hasFreshEvidence ? Intent.ENGINE_DIAGNOSE : Intent.UNSUPPORTED_ENGINE, "BUFFER");
        if (hasPreviousDiagnosis && !hasFreshEvidence &&
                containsAny(s, "warum", "wieso", "weshalb", "erklär", "erklaer", "genauer", "haupt-xpath", "diesen xpath", "welchen xpath", "was bedeutet", "was hast du geändert", "was wurde geändert"))
            return new Route(Intent.EXPLAIN_PREVIOUS, "SOAP");
        if (!hasFreshEvidence) return new Route(Intent.NEEDS_INPUT, "SOAP");
        // With an attached log, infer the engine only from actual evidence if the question is generic.
        String evidenceNeutral = s;
        if (evidenceNeutral.contains("analys") || evidenceNeutral.contains("fehler") || evidenceNeutral.contains("test"))
            return new Route(Intent.DIAGNOSE, "AUTO");
        return new Route(Intent.DIAGNOSE, "SOAP");
    }
    static boolean containsAny(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }
}
