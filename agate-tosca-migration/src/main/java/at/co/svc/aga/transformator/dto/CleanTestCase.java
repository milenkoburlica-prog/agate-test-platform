package at.co.svc.aga.transformator.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CleanTestCase {
    public String name;
    public String surrogate;
    @JsonProperty("steps")
    public List<CleanStep> steps = new ArrayList<>();
    private String condition;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurrogate() {
        return surrogate;
    }
    public void setSurrogate(String surrogate) {
        this.surrogate = surrogate;
    }
    public List<CleanStep> getSteps() {
        return steps;
    }
    public void setSteps(List<CleanStep> steps) {
        this.steps = steps;
    }
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
}