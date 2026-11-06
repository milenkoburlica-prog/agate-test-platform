package at.co.svc.agate.server.dto;

// Map nam više ne treba ovde ako šaljemo čist tekst
public class StepDefinitionResponse {
    public String stepAction;
    public String metadata; // Promenjeno sa Map na String
    public String request;  // Promenjeno sa Map na String

    // Ažuriran konstruktor da prima Stringove
    public StepDefinitionResponse(String stepAction, String metadata, String request) {
        this.stepAction = stepAction;
        this.metadata = metadata;
        this.request = request;
    }
}