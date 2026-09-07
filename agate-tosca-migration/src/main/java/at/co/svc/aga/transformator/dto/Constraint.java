//package at.co.svc.aga.transformator.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class Constraint {
//    private String path;
//    private String expected;
//    private String action;
//    private String toscaPath;
//
//    // Obavezno dodaj getere i setere
//    public String getPath() { return path; }
//    public void setPath(String path) { this.path = path; }
//
//    public String getExpected() { return expected; }
//    public void setExpected(String expected) { this.expected = expected; }
//
//    public String getAction() { return action; }
//    public void setAction(String action) { this.action = action; }
//
//    public String getToscaPath() { return toscaPath; }
//    public void setToscaPath(String toscaPath) { this.toscaPath = toscaPath; }
//    
// // U klasi Constraint.java dodaj metodu za ekstrakciju polja iz path-a
//    public String getFieldFromPath() {
//        if (this.path == null || !this.path.contains("local-name")) return "";
//        
//        // Izvlači poslednji element iz XPath-a (npr. 'svtCode' iz .../*[local-name()='svtCode'])
//        String[] parts = this.path.split("local-name\\(\\)\\='");
//        if (parts.length > 1) {
//            return parts[parts.length - 1].replace("']", "");
//        }
//        return "";
//    }
//    
//}


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