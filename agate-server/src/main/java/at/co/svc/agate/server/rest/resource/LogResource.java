package at.co.svc.agate.server.rest.resource;

import java.util.List;
import java.util.Map;

import at.co.svc.agate.server.dto.LogResponse;
import at.co.svc.agate.server.service.LogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource responsible for handling execution log retrieval.
 */
@Path("/api/v1/logs")
public class LogResource {

    @Inject
    LogService logService;

    /**
     * Retrieves the current execution logs and status for a given session identifier.
     * 
     * @param sessionId the unique session identifier
     * @return LogResponse containing session status and log entries
     */
    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public LogResponse getLogs(@PathParam("sessionId") String sessionId) {
        Map<String, Object> status = logService.getSessionStatus(sessionId);
        
        return new LogResponse(
            (String) status.get("sessionId"),
            (Boolean) status.get("isFinished"),
            (List<String>) status.get("logs")
        );
    }
}