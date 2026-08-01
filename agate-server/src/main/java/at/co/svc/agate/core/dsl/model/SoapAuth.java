package at.co.svc.agate.core.dsl.model;

/**
 * Represents WS-Security or other authentication metadata for SOAP requests.
 */
public class SoapAuth {
    private String type;
    private String username;
    private String password;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}