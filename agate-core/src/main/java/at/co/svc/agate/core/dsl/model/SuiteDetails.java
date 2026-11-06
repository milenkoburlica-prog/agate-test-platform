package at.co.svc.agate.core.dsl.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SuiteDetails {
    // This is the file name (e.g. "002_windows_cmd_basic_test.yaml")
    private String id;

    // This is the list of tests within that file
    // Seen as "content" in curl
    private List<TestCase> content;

    public SuiteDetails() {}

    public SuiteDetails(String id, List<TestCase> content) {
        this.id = id;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<TestCase> getContent() { return content; }
    public void setContent(List<TestCase> content) { this.content = content; }
}