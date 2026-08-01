package at.co.svc.agate.server.dto;

//Sadržaj samog modula
public class ModuleContentResponse {
    public String name;
    public String metadata;
    public String request;

    public ModuleContentResponse(String name, String metadata, String request) {
        this.name = name;
        this.metadata = metadata;
        this.request = request;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
    
    
}