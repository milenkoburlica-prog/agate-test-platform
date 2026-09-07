package at.co.svc.agate.core.dsl.runtime;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import at.co.svc.agate.core.interfaces.TestLogger;

public class ExecutionContext {

    private final Map<String, Object> vars =
            new ConcurrentHashMap<>();

    private final Map<String, Object> buffer =
            new ConcurrentHashMap<>();

    private final TestLogger logger;

    private boolean inCallMode;

    public ExecutionContext(
            TestLogger logger) {

        this.logger = logger;
    }

    public TestLogger getLogger() {
        return logger;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void setVar(
            String key,
            Object value) {

        if (key != null
                && value != null) {

            vars.put(
                    key,
                    value);
        }
    }

    public Object getVar(
            String key) {

        return vars.get(key);
    }

    public Set<String> getVarKeys() {
        return vars.keySet();
    }

    public void storeBuffer(
            String key,
            Object value) {

        if (key != null
                && value != null) {

            buffer.put(
                    key,
                    value);
        }
    }

    public Object getBuffer(
            String key) {

        return buffer.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getResponse(
            String key,
            Class<T> clazz) {

        Object value =
                buffer.get(key);

        if (value != null
                && clazz.isInstance(value)) {

            return (T) value;
        }

        return null;
    }

    public void clearBuffer() {
        buffer.clear();
    }

    public boolean isInCallMode() {
        return inCallMode;
    }

    public void setInCallMode(
            boolean inCallMode) {

        this.inCallMode =
                inCallMode;
    }

    public String getBufferAsString(
            String key) {

        Object value =
                buffer.get(key);

        return value != null
                ? value.toString()
                : null;
    }

    public Map<String, Object> getBufferMap() {
        return new ConcurrentHashMap<>(
                buffer);
    }
}