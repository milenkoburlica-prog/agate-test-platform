package at.co.svc.agate.core.dsl.model;

import java.util.Map;
import java.util.Set;

/** Section labels are literal display metadata; conditions use the normal evaluator. */
public record SectionMetadata(String title, String description, String condition,
                              String sourceFile, int sourceStepIndex) {
    public static SectionMetadata from(Map<String, Object> raw, String file, int index) {
        if (!Set.of("section", "description", "condition").containsAll(raw.keySet()))
            throw new IllegalArgumentException("Unsupported section field in " + file + " at source step " + index);
        String title = string(raw.get("section"), "section", file, index);
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Section requires a nonempty title in " + file + " at source step " + index);
        return new SectionMetadata(title, string(raw.get("description"), "description", file, index),
                string(raw.get("condition"), "condition", file, index), file, index);
    }
    private static String string(Object value, String field, String file, int index) {
        if (value == null) return null;
        if (!(value instanceof String))
            throw new IllegalArgumentException("Section " + field + " must be text in " + file + " at source step " + index);
        return (String) value;
    }
    private static String display(String value) {
        // Prevent multiline labels from looking like independent log entries.
        return value.replaceAll("[\r\n\t]+", " ").trim();
    }
    public String displayTitle() { return display(title); }
    public String displayDescription() { return description == null ? "" : display(description); }
}
