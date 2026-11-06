package at.co.svc.agate.server.dto;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

public class DebugSession {
    public ExecutionContext context;
    public TestCase testCase;
    public String yamlFile;
    public String sessionId;
    
    public DebugSession(ExecutionContext context, TestCase testCase, String yamlFile, String sessionId) {
        this.context = context;
        this.testCase = testCase;
        this.yamlFile = yamlFile;
        this.sessionId = sessionId;
    }
}