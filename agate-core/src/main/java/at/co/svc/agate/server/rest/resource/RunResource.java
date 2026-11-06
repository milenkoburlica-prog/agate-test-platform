package at.co.svc.agate.server.rest.resource;

import java.util.UUID;

import org.eclipse.microprofile.context.ManagedExecutor;

import at.co.svc.agate.server.dto.RunRequest;
import at.co.svc.agate.server.dto.RunResponse;
import at.co.svc.agate.server.service.LogService;
import at.co.svc.agate.server.service.TestRunnerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource responsible for initiating test suite executions.
 */
@Path("/api/v1/run")
public class RunResource {

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    LogService logService;

    @Inject
    TestRunnerService runnerService;

    /**
     * Triggers an asynchronous test execution based on the provided request configuration.
     * 
     * @param request the test run configuration payload
     * @return HTTP 202 Accepted response containing the generated session identifier
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runTest(RunRequest request) {
        String sessionId = "exec_" + UUID.randomUUID().toString().substring(0, 8);
        
        logService.addLog(sessionId, "Initializing execution for test case: " + request.testCaseId);

        managedExecutor.submit(() -> {
            try {
                if (request.steps != null && !request.steps.isEmpty()) {
                    logService.addLog(sessionId, "DEBUG: Executing dynamic steps sent from client.");
                    
                    runnerService.runFullTest(
                        request.appId, 
                        request.suite, 
                        sessionId, 
                        request.testCaseId, 
                        request.steps, 
                        request.variables, 
                        request.environment, 
                        request.user, 
                        true 
                    );
                } else {
                    logService.addLog(sessionId, "DEBUG: No steps in request. Reading file from disk: " + request.suite);
                    runnerService.executeTestSuite(
                        request.appId, 
                        request.suite, 
                        request.testCaseId, 
                        sessionId,
                        request.environment, 
                        request.user
                    );
                }
            } catch (Exception e) {
                logService.addLog(sessionId, "CRITICAL ERROR: " + e.getMessage());
            }
        });

        return Response.status(Response.Status.ACCEPTED)
                       .entity(new RunResponse(sessionId, "STARTED", "N/A"))
                       .build();
    }
}