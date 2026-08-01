package at.co.svc.agate.server.dto;

public class RunResponse {
    public String sessionId;
    public String status;
    public String estimatedDuration;

    public RunResponse(String sessionId, String status, String estimatedDuration) {
        this.sessionId = sessionId;
        this.status = status;
        this.estimatedDuration = estimatedDuration;
    }
}