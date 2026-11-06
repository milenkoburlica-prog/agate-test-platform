package at.co.svc.agate.server.dto;

import java.util.List;
import java.util.Map;

public class RunRequest {
    public String appId;
    public String suite;
    public String testCaseId;
    public String environment;
    public String user;
    
    // 1. DODAJ OVA DVA POLJA:
    public Map<String, String> variables;
    public List<Map<String, Object>> steps;

    // Geteri i seteri za postojeća polja...
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getSuite() { return suite; }
    public void setSuite(String suite) { this.suite = suite; }
    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    // 2. DODAJ OVE GETERE I SETERE:
    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }
    public List<Map<String, Object>> getSteps() { return steps; }
    public void setSteps(List<Map<String, Object>> steps) { this.steps = steps; }
}