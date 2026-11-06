package at.co.svc.agate.server.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LogService {

    // Koristimo ConcurrentHashMap jer više thread-ova pristupa logovima istovremeno
    private final Map<String, SessionData> sessionLogs = new ConcurrentHashMap<>();

    private static class SessionData {
        final List<String> logs = Collections.synchronizedList(new ArrayList<>());
        boolean isFinished = false;
    }

    public void addLog(String sessionId, String message) {
        sessionLogs.computeIfAbsent(sessionId, k -> new SessionData()).logs.add(message);
    }

    public void finishSession(String sessionId) {
        SessionData data = sessionLogs.get(sessionId);
        if (data != null) {
            data.isFinished = true;
        }
    }

    public Map<String, Object> getSessionStatus(String sessionId) {
        SessionData data = sessionLogs.getOrDefault(sessionId, new SessionData());
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        
        // Pravimo kopiju liste u trenutku čitanja da izbegnemo ConcurrentModificationException
        synchronized (data.logs) {
            response.put("logs", new ArrayList<>(data.logs));
        }
        
        response.put("isFinished", data.isFinished);
        return response;
    }
}