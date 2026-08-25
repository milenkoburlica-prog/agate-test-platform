package at.co.svc.agate.openapi.model;

public class AgateDiagnostic {

    private AgateDiagnosticSeverity severity;

    private String code;

    private String location;

    private String message;


    public AgateDiagnostic() {
    }


    public AgateDiagnostic(
            AgateDiagnosticSeverity severity,
            String code,
            String location,
            String message) {

        this.severity = severity;
        this.code = code;
        this.location = location;
        this.message = message;
    }


    public AgateDiagnosticSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(
            AgateDiagnosticSeverity severity) {

        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(
            String code) {

        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location) {

        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message) {

        this.message = message;
    }
}