package at.co.svc.aga.transformator.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Strict, all-or-nothing removal of the obsolete Tosca Edge session-restore IF block. */
final class LegacyEdgeRestoreWorkaround {
    private LegacyEdgeRestoreWorkaround() {}

    static boolean matches(List<Map<String, Object>> steps) {
        if (steps == null || steps.size() != 11) return false;
        String[] suffixes = {"parent_init", "parent_set", "p0_init", "p1_init",
                "p0_find", "p0_buffer", "p1_find", "p1_buffer",
                "decision_init", "decision_set"};
        String first = str(steps.get(0).get("id"));
        if (!first.matches("agate_cf_[0-9a-f]{32}_parent_init")) return false;
        String prefix = first.substring(0, first.length() - "parent_init".length());
        for (int i = 0; i < suffixes.length; i++) {
            if (!Objects.equals(prefix + suffixes[i], steps.get(i).get("id"))) return false;
        }
        // Do not remove a structure if anything other than the known 3 window probes/actions exists.
        if (!buffer(steps.get(0), prefix + "parent", "false", false)
                || !buffer(steps.get(1), prefix + "parent", "true", true)
                || !buffer(steps.get(2), prefix + "p0", "false", false)
                || !buffer(steps.get(3), prefix + "p1", "false", false)
                || !buffer(steps.get(8), prefix + "decision", "false", false)
                || !buffer(steps.get(9), prefix + "decision", "true", true)) return false;
        String edgeCondition = str(steps.get(1).get("condition"));
        if (!edgeCondition.contains("{E[env.B_TestkonfigBrowser]}") || !edgeCondition.contains("Edge")) return false;
        if (!find(steps.get(4), "PANEL", "Seiten wiederherstellen", prefix + "response0")
                || !existsBuffer(steps.get(5), prefix + "p0", prefix + "response0")
                || !find(steps.get(6), "RESTORE", "Wiederherstellen", prefix + "response1")
                || !existsBuffer(steps.get(7), prefix + "p1", prefix + "response1")) return false;
        Map<String, Object> close = steps.get(10);
        if (!"WINDOW".equals(close.get("type")) || !"EXEC".equals(close.get("op"))
                || !"CLICK".equals(close.get("action"))
                || !"CLOSE".equals(str(map(close.get("target")).get("edgeRestore")))) return false;
        if (!source(close, "Schließen") || !str(close.get("condition")).contains(prefix + "decision")) return false;
        // No unrelated work must be hidden in these generated IF guards.
        String decisionCondition = str(steps.get(9).get("condition"));
        return decisionCondition.contains(prefix + "parent")
                && decisionCondition.contains(prefix + "p0")
                && decisionCondition.contains(prefix + "p1")
                && str(steps.get(10).get("condition")).contains(prefix + "parent")
                && str(steps.get(4).get("condition")).contains(prefix + "parent")
                && str(steps.get(6).get("condition")).contains(prefix + "parent");
    }

    private static boolean buffer(Map<String, Object> step, String name, String value, boolean condition) {
        return "BUFFER".equals(step.get("type")) && "EXEC".equals(step.get("op"))
                && name.equals(step.get("name")) && value.equals(str(step.get("value")))
                && (condition == step.containsKey("condition"));
    }
    private static boolean find(Map<String, Object> step, String role, String business, String response) {
        return "WINDOW".equals(step.get("type")) && "EXEC".equals(step.get("op"))
                && "FIND".equals(step.get("action")) && response.equals(step.get("response"))
                && role.equals(map(step.get("target")).get("edgeRestore")) && source(step, business);
    }
    private static boolean existsBuffer(Map<String, Object> step, String name, String response) {
        return "WINDOW".equals(step.get("type")) && "BUFFER".equals(step.get("op"))
                && "EXISTS".equals(step.get("action")) && name.equals(step.get("name"))
                && response.equals(step.get("response"));
    }
    private static boolean source(Map<String, Object> step, String business) {
        Map<String, Object> src = map(step.get("source"));
        String engine = str(map(src.get("properties")).get("Engine"));
        String caption = str(step.get("caption")).toLowerCase(Locale.ROOT);
        return business.equals(src.get("businessName"))
                && "AnyUI".equals(engine) && caption.contains("microsoft edge");
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object obj) {
        return obj instanceof Map<?, ?> ? (Map<String, Object>) obj : Map.of();
    }
    private static String str(Object obj) { return Objects.toString(obj, ""); }
}
