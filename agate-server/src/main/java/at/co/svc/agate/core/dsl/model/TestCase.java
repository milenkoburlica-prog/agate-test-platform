package at.co.svc.agate.core.dsl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestCase {

    @JsonProperty("id")
    private String name;
    private String description;
    private String stage;
    private String priority;

    private Map<String, Object> variables = new HashMap<>();
    private List<TestStep> steps = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Map<String, Object> getVariables() { return variables; }

    public void setVariables(Map<String, Object> vars) {
        this.variables = (vars == null) ? new HashMap<>() : new HashMap<>(vars);
    }

    public List<TestStep> getSteps() { return steps; }

    public void addStep(TestStep step) {
        this.steps.add(step);
    }
    
 // Dodaj ovo u TestCase.java
    public void addVariable(String key, Object value) {
        if (this.variables == null) {
            this.variables = new java.util.HashMap<>();
        }
        this.variables.put(key, value);
    }

    // Često zatreba i ova metoda za proveru
    public Object getVariable(String key) {
        return this.variables != null ? this.variables.get(key) : null;
    }
    
    
}