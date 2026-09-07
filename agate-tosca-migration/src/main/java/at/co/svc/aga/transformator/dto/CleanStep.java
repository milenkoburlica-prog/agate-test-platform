package at.co.svc.aga.transformator.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CleanStep {
    private int index;
    private String name;
    private String type;
    private String op;
    private String surrogate;
    private String module;
    private String moduleSurrogate;
    private String reusableName; // Ime bloka iz biblioteke
    private String reusableSurrogate; // Ključ za libs.json
    private String condition;
    // 1. DODAJ OVO POLJE
    private String moduleClass; 
//    private String xCondition;

    private List<StepValueDetails> values;
    private String action;

 // Dodaj u klasu koja ti predstavlja Assert korak
    private List<Constraint> constraints;

    public List<Constraint> getConstraints() { return constraints; }
    public void setConstraints(List<Constraint> constraints) { this.constraints = constraints; }
    
    
 // 4. Ako TI u kodu treba mapa za pretragu, napravi metodu koja je pravi "u letu"
    @JsonIgnore
    public Map<String, StepValueDetails> getValuesAsMap() {
        if (values == null) return new LinkedHashMap<>();
        
        // Ovde praviš mapu samo kada ti zatreba
        return values.stream().collect(Collectors.toMap(
            StepValueDetails::getName, 
            v -> v,
            (existing, replacement) -> existing, // Čuva prvo pojavljivanje
            java.util.LinkedHashMap::new
        ));
    }
    
    
    // 2. DODAJ GETTER I SETTER ZA moduleClass
    public String getModuleClass() {
        return moduleClass;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setModuleClass(String moduleClass) {
        this.moduleClass = moduleClass;
    }

    // Proveri da li imaš i ostale settere koji ti trebaju u ToscaParser-u
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSurrogate() { return surrogate; }
    public void setSurrogate(String surrogate) { this.surrogate = surrogate; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getModuleSurrogate() { return moduleSurrogate; }
    public void setModuleSurrogate(String moduleSurrogate) { this.moduleSurrogate = moduleSurrogate; }

//    public Map<String, StepValueDetails> getValues() { return values; }
//    public void setValues(Map<String, StepValueDetails> values) { this.values = values; }

    public String getReusableName() {
        return reusableName;
    }

    public void setReusableName(String reusableName) {
        this.reusableName = reusableName;
    }

    public String getReusableSurrogate() {
        return reusableSurrogate;
    }

    public void setReusableSurrogate(String reusableSurrogate) {
        this.reusableSurrogate = reusableSurrogate;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

// public String getxCondition() {
//        return xCondition;
//    }
//
//    public void setxCondition(String xCondition) {
//        this.xCondition = xCondition;
//    }

    // U klasi CleanStep
    public String getCombinedCondition(String xCondition, String condition) {
        if (xCondition == null || xCondition.isEmpty()) {
            return condition;
        }
        
        String xCondWrapped = "(" + xCondition + ")";
        
        if (condition == null || condition.isEmpty()) {
            return "(" + condition + ")";
        }
        
        // Spreči da se stalno dodaje isti uslov ako se metoda pozove više puta
        if (condition.contains(xCondWrapped)) {
            return condition;
        }
        
        return "((" + condition + ") AND (" + xCondWrapped + "))";
    }
 // 3. Dodaj novi getter i setter koji vraćaju Listu
    @JsonProperty("values")
    public void setValues(List<StepValueDetails> values) {
        this.values = values;
    }

    public List<StepValueDetails> getValues() {
        return this.values;
    }
}