package at.co.svc.agate.server.rest.resource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.context.ManagedExecutor;

import at.co.svc.agate.server.dto.RunRequest;
import at.co.svc.agate.server.dto.RunResponse;
import at.co.svc.agate.server.service.LogService;
import at.co.svc.agate.server.service.TestRunnerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource responsible for handling debugging sessions, step-by-step executions, and full test runs.
 */
@Path("/api/v1/debug")
public class DebugResource {

    @Inject
    LogService logService;

    @Inject
    TestRunnerService runnerService;

    @Inject
    ManagedExecutor managedExecutor;
    
    /**
     * Initializes a new debugging session.
     * 
     * @param request the test run configuration payload
     * @return HTTP 200 OK response containing the initialized session details
     */
    @POST
    @Path("/init")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response initDebug(RunRequest request) {
        String sessionId = "DEBUG-" + UUID.randomUUID().toString().substring(0, 8);
        
        logService.addLog(sessionId, "--- DEBUG SESSION INITIALIZED ---");
        logService.addLog(sessionId, "Target TC: " + request.testCaseId);

        return Response.ok(new RunResponse(sessionId, "READY", "N/A")).build();
    }

    /**
     * Executes a single test step asynchronously within an active debugging session.
     * 
     * @param payload the execution payload containing step definitions and session metadata
     * @return HTTP 202 Accepted response
     */
    @POST
    @Path("/step")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeStep(Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");

        try {
            final String fAppId = (String) payload.get("appId");
            final String fSuite = (String) payload.get("suite");
            final String fTestCaseId = (String) payload.get("testCaseId");
            final String fEnv = (String) payload.get("environment");
            final String fUser = (String) payload.get("user");
            final String fSessionId = sessionId;
            
            @SuppressWarnings("unchecked")
            final Map<String, String> fVariables = (Map<String, String>) payload.get("variables");
            
            @SuppressWarnings("unchecked")
            final Map<String, Object> fStepData = (Map<String, Object>) payload.get("step");
            
            Integer rawIndex = (Integer) payload.get("stepIndex");
            final int fStepIdx = (rawIndex != null) ? rawIndex : 0;

            if (fAppId == null || fSuite == null || fSessionId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required parameters").build();
            }

            boolean isVerbose = true;
            
            managedExecutor.runAsync(() -> {
                try {
                    logService.addLog(fSessionId, "[[STEP_STATUS:" + fStepIdx + ":RUNNING]]");

                    runnerService.executeSingleStep(
                        fAppId, 
                        fSuite, 
                        fTestCaseId, 
                        fSessionId, 
                        fStepIdx, 
                        fEnv, 
                        fUser, 
                        fVariables, 
                        fStepData,
                        isVerbose
                    );
                    
                    logService.addLog(fSessionId, "[[STEP_STATUS:" + fStepIdx + ":SUCCESS]]");
                    logService.addLog(fSessionId, "--- STEP_FINISHED_ASYNC ---");
                } catch (Exception e) {
                    logService.addLog(fSessionId, "[[STEP_STATUS:" + fStepIdx + ":FAILED]]");
                    logService.addLog(fSessionId, "ASYNC EXECUTION ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            return Response.accepted().build();

        } catch (Exception e) {
            if (sessionId != null) {
                logService.addLog(sessionId, "CRITICAL ERROR: " + e.getMessage());
            }
            return Response.serverError().entity(e.getMessage()).build();
        }
    }   
    
    /**
     * Terminates an active debugging session and clears its execution context.
     * 
     * @param sessionId the unique session identifier
     * @return HTTP 200 OK response
     */
    @DELETE
    @Path("/{sessionId}")
    public Response stopDebug(@PathParam("sessionId") String sessionId) {
        logService.finishSession(sessionId);
        runnerService.removeDebugContext(sessionId);
        return Response.ok().build();
    }
    
    /**
     * Executes a full sequence of test steps asynchronously.
     * 
     * @param payload the execution payload containing all test steps and variables
     * @return HTTP 200 OK response containing session tracking data
     */
    @POST
    @Path("/run-all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeFullTest(Map<String, Object> payload) {
        final String sessionId = "RUN-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            final String fAppId = (String) payload.get("appId");
            final String fSuite = (String) payload.get("suite");
            final String fTestCaseId = (String) payload.get("testCaseId");
            final String fEnv = (String) payload.get("environment");
            final String fUser = (String) payload.get("user");

            @SuppressWarnings("unchecked")
            final Map<String, String> fVariables = (Map<String, String>) payload.get("variables");

            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> fSteps = (List<Map<String, Object>>) payload.get("steps");

            if (fSteps == null || fSteps.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No steps provided for execution").build();
            }

            logService.addLog(sessionId, "--- FULL RUN INITIALIZED ---");

            final boolean isVerbose = true;
            
            managedExecutor.runAsync(() -> {
                try {
                    runnerService.runFullTest(
                        fAppId,          
                        fSuite,          
                        sessionId,       
                        fTestCaseId,     
                        fSteps,          
                        fVariables,      
                        fEnv,            
                        fUser,           
                        isVerbose        
                    );
                } catch (Exception e) {
                    logService.addLog(sessionId, "ASYNC RUN ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return Response.ok(new RunResponse(sessionId, "RUNNING", "N/A")).build();

        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}