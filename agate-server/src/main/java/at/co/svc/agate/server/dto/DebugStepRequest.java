package at.co.svc.agate.server.dto;

import java.util.Map;

public class DebugStepRequest {
    public String sessionId;
    public String appId;
    public String suite;
    public String testCaseId;
    public int stepIndex;
    public String environment;
    public String user;
    
    // DODATO: Mapa varijabli koje klijent trenutno ima na ekranu
    public Map<String, String> variables; 
    
    // DODATO: Opciono, ceo objekat koraka ako je klijent nešto dopisao u akciji
    public Map<String, Object> step;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getStep() {
        return step;
    }

    public void setStep(Map<String, Object> step) {
        this.step = step;
    } 
    
    
}


