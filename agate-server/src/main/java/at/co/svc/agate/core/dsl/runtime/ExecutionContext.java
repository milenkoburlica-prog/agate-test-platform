package at.co.svc.agate.core.dsl.runtime;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import at.co.svc.agate.core.interfaces.TestLogger;

public class ExecutionContext {

    // runtime variables (merged from YAML + CSV + runtime)
    private final Map<String, Object> vars = new ConcurrentHashMap<>();

    // buffer (CMD, REST, SQL responses...)
    private final Map<String, Object> buffer = new ConcurrentHashMap<>();

    private final TestLogger logger;

    public ExecutionContext(TestLogger logger) {
        this.logger = logger;
    }

    public TestLogger getLogger() {
        return logger;
    }

    // =========================
    // VARIABLES (FIX FOR YOUR ERROR)
    // =========================
    public Map<String, Object> getVars() {
        return vars;
    }

    public void setVar(String key, Object value) {
        if (key != null && value != null) {
            vars.put(key, value);
        }
    }

    public Object getVar(String key) {
        return vars.get(key);
    }

    public Set<String> getVarKeys() {
        return vars.keySet();
    }

    // =========================
    // BUFFER (CMD / REST / SQL)
    // =========================
    public void storeBuffer(String key, Object value) {
        if (key != null && value != null) {
            buffer.put(key, value);
        }
    }

    public Object getBuffer(String key) {
        return buffer.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getResponse(String key, Class<T> clazz) {
        Object val = buffer.get(key);
        if (val != null && clazz.isInstance(val)) {
            return (T) val;
        }
        return null;
    }

    public void clearBuffer() {
        buffer.clear();
    }
    
    private boolean inCallMode = false; // Dodaj ovo polje

    // Dodaj getter i setter
    public boolean isInCallMode() {
        return inCallMode;
    }

    public void setInCallMode(boolean inCallMode) {
        this.inCallMode = inCallMode;
    }
 
    public String getBufferAsString(String key) {
        Object val = buffer.get(key);
        return (val != null) ? val.toString() : null;
    }
    public Map<String, Object> getBufferMap() {
        return new ConcurrentHashMap<>(this.buffer);
    }    
}