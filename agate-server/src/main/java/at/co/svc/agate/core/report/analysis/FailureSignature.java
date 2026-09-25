package at.co.svc.agate.core.report.analysis;

import java.util.Objects;

public class FailureSignature {

    private final String stepType;
    private final String reason;
    private final String field;
    private final String action;
    private final String source;

    public FailureSignature(
            String stepType,
            String reason,
            String field,
            String action,
            String source) {

        this.stepType = normalize(stepType);
        this.reason = normalize(reason);
        this.field = normalize(field);
        this.action = normalize(action);
        this.source = normalize(source);
    }

    public String getStepType() {
        return stepType;
    }

    public String getReason() {
        return reason;
    }

    public String getField() {
        return field;
    }

    public String getAction() {
        return action;
    }

    public String getSource() {
        return source;
    }

    private static String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof FailureSignature)) {
            return false;
        }

        FailureSignature that =
                (FailureSignature) o;

        return Objects.equals(stepType, that.stepType)
                && Objects.equals(reason, that.reason)
                && Objects.equals(field, that.field)
                && Objects.equals(action, that.action)
                && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                stepType,
                reason,
                field,
                action,
                source
        );
    }

    @Override
    public String toString() {

        return "FailureSignature{" +
                "stepType='" + stepType + '\'' +
                ", reason='" + reason + '\'' +
                ", field='" + field + '\'' +
                ", action='" + action + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}