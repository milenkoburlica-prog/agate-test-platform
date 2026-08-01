package at.co.svc.agate.core.dsl.register;

import java.util.Map;
import java.util.HashMap;

public class TCObject {
    private String testId;
    private Map<String, String> variables = new HashMap<>();

    public TCObject(String testId) {
        this.testId = testId;
    }

    public void addVariable(String key, String value) {
        variables.put(key, value);
    }

    public String getTestId() { return testId; }
    public Map<String, String> getVariables() { return variables; }
}