package at.co.svc.agate.core.dsl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {
    @JsonProperty("column")
    private String column;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("expected")
    private String expected;
    
    @JsonProperty("path")
    private String path;

    // Getters and Setters
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }
}