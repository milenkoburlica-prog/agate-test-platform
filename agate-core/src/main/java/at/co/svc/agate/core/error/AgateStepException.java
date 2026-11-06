package at.co.svc.agate.core.error;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgateStepException extends RuntimeException {

    private final String reason;
    private final LinkedHashMap<String, String> details;

    private AgateStepException(Builder builder) {
        super(builder.buildMessage(), builder.cause);
        this.reason = builder.reason;
        this.details = new LinkedHashMap<>(builder.details);
    }

    public String getReason() {
        return reason;
    }

    public Map<String, String> getDetails() {
        return new LinkedHashMap<>(details);
    }

    public static Builder builder(String reason) {
        return new Builder(reason);
    }

    public static final class Builder {

        private final String reason;
        private final LinkedHashMap<String, String> details = new LinkedHashMap<>();
        private Throwable cause;

        private Builder(String reason) {
            this.reason = normalize(reason, "AGATE step failed");
        }

        public Builder expected(Object value) {
            return detail("Expected", value);
        }

        public Builder actual(Object value) {
            return detail("Actual", value);
        }

        public Builder path(Object value) {
            return detail("Path", value);
        }

        public Builder field(Object value) {
            return detail("Field", value);
        }

        public Builder command(Object value) {
            return detail("Command", value);
        }

        public Builder hint(Object value) {
            return detail("Hint", value);
        }

        public Builder detail(String label, Object value) {
            if (label != null && !label.isBlank() && value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) {
                    details.put(label.trim(), text);
                }
            }
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public AgateStepException build() {
            return new AgateStepException(this);
        }

        private String buildMessage() {
            StringBuilder sb = new StringBuilder(reason);
            appendSummary(sb, "Expected");
            appendSummary(sb, "Actual");
            appendSummary(sb, "Path");
            appendSummary(sb, "Field");
            return sb.toString();
        }

        private void appendSummary(StringBuilder sb, String key) {
            String value = details.get(key);
            if (value != null && !value.isBlank()) {
                sb.append(" | ").append(key).append(": ").append(value);
            }
        }

        private static String normalize(String value, String fallback) {
            if (value == null || value.isBlank()) {
                return fallback;
            }
            return value.trim();
        }
    }
}
