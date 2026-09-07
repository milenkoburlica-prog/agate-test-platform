package at.co.svc.aga.transformator.dto;

import java.util.Map;

public class Module {
    public String Surrogate;
    public Map<String, String> Attributes; // Ovde stoje Payload, Headers, ExplicitConnection
    public String getSurrogate() {
        return Surrogate;
    }
    public void setSurrogate(String surrogate) {
        Surrogate = surrogate;
    }
    public Map<String, String> getAttributes() {
        return Attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        Attributes = attributes;
    }
    
    
}
