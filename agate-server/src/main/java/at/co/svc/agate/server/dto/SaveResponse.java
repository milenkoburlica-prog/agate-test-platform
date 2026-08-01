package at.co.svc.agate.server.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class SaveResponse {
    public String status;
    public String message;
    public String version;
    public String timestamp;

    // Konstruktor za greške (prima 2 parametra)
    public SaveResponse(String status, String message) {
        this(status, message, "n/a", OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    }

    // Pun konstruktor (prima 4 parametra) - OVO TI NEDOSTAJE
    public SaveResponse(String status, String message, String version, String timestamp) {
        this.status = status;
        this.message = message;
        this.version = version;
        this.timestamp = timestamp;
    }
}