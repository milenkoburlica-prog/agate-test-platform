package at.co.svc.aga.transformator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {
    private String expected;
    private String action;
    private String path;

    // Geteri i seteri
    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}