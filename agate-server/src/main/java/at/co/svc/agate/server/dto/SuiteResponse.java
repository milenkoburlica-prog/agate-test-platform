package at.co.svc.agate.server.dto;

import java.util.List;

import at.co.svc.agate.core.dsl.model.TestCase;

public class SuiteResponse {
    public String id;
    public List<TestCase> content;

    public SuiteResponse() {
    }
    
    public SuiteResponse(String id, List<TestCase> content) {
        this.id = id;
        this.content = content;
    }
}