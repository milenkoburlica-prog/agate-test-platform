package at.co.svc.agate.core.dsl.register;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** GUI-only normalization of Tosca NULL markers. Other engines retain their own semantics. */
public final class GuiNullSemantics {
    private GuiNullSemantics() {}

    /** Apply to the complete instantiated YAML tree, including nested reusable-like steps. */
    public static void normalizeTree(Object node) {
        if (node instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked") Map<String, Object> step = (Map<String, Object>) map;
            normalizeStep(step);
            for (Object child : step.values()) normalizeTree(child);
        } else if (node instanceof List<?> list) {
            for (Object child : list) normalizeTree(child);
        }
    }

    /** Mutates a GUI step mapping only, never SQL/REST/SOAP data. */
    public static void normalizeStep(Map<String, Object> step) {
        if (!"GUI".equalsIgnoreCase(Objects.toString(step.get("type"), ""))) return;
        String action = Objects.toString(step.get("action"), Objects.toString(step.get("op"), ""))
                .toUpperCase(Locale.ROOT);
        if ("FILL".equals(action) || "INPUT".equals(action)) {
            if (step.get("value") instanceof String value) step.put("value", emptyFill(value));
            if (step.get("textbox") instanceof Map<?, ?> textbox) {
                @SuppressWarnings("unchecked") Map<String,Object> box = (Map<String,Object>) textbox;
                if (box.get("value") instanceof String value) box.put("value", emptyFill(value));
            }
        }
        if ("TEXT_GLOB".equals(action)) {
            if (step.get("expected") instanceof String expected)
                step.put("expected", stripNull(expected));
        }
        if ("ASSERT".equals(action) || "ASSERT".equalsIgnoreCase(Objects.toString(step.get("op"), ""))) {
            if (step.get("assertion") instanceof Map<?,?> assertion) {
                @SuppressWarnings("unchecked") Map<String,Object> a = (Map<String,Object>) assertion;
                if ("TEXT_GLOB".equalsIgnoreCase(Objects.toString(a.get("type"), ""))
                        && a.get("expected") instanceof String expected)
                    a.put("expected", stripNull(expected));
            }
        }
    }

    public static String emptyFill(String text) {
        return "{NULL}".equals(text) || "\"{NULL}\"".equals(text) ? "" : text;
    }

    public static String stripNull(String text) {
        return text.replace("{NULL}", "");
    }
}
