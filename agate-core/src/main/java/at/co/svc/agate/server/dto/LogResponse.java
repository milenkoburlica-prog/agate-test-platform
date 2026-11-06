package at.co.svc.agate.server.dto;

import java.util.List;

public class LogResponse {
    public String sessionId;
    public boolean isFinished;
    public List<String> logs;

    public LogResponse(String sessionId, boolean isFinished, List<String> logs) {
        this.sessionId = sessionId;
        this.isFinished = isFinished;
        this.logs = logs;
    }
}