package at.co.svc.agate.server.rest.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.server.dto.AppInfo;
import at.co.svc.agate.server.dto.SaveResponse;
import at.co.svc.agate.server.dto.SuiteInfo;
import at.co.svc.agate.server.dto.SuiteResponse;
import at.co.svc.agate.server.dto.ValidationResponse;
import at.co.svc.agate.server.service.AppService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource responsible for exposing application management endpoints.
 */
@Path("/api/v1/apps")
public class AppResource {

    @Inject
    AppService appService;

    //private final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AppInfo> getApps() {
        return appService.getApps();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApp(AppInfo appInfo) {
        if (appInfo.getName() == null || appInfo.getName().trim().isEmpty()) {
            return Response.status(400).entity(Map.of("error", "Application name is required")).build();
        }
        try {
            AppInfo created = appService.createApp(appInfo);
            return Response.status(201).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(409).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{appId}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameApp(@PathParam("appId") String appId, AppInfo newAppInfo) {
        try {
            AppInfo renamed = appService.renameApp(appId, newAppInfo);
            return Response.ok(renamed).build();
        } catch (IllegalArgumentException e) {
            String statusMsg = e.getMessage().contains("not found") ? "404" : "409";
            int status = statusMsg.equals("404") ? 404 : 409;
            return Response.status(status).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApp(@PathParam("appId") String appId) {
        try {
            appService.deleteApp(appId);
            return Response.ok(Map.of("message", "App '" + appId + "' deleted successfully")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(404).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("used by another process")) {
                errorMsg = "Cannot delete app because a file is open in another program.";
            }
            return Response.status(500).entity(Map.of("error", errorMsg)).build();
        }
    }

    @GET
    @Path("/{appId}/suites")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SuiteInfo> getSuites(@PathParam("appId") String appId) {
        return appService.getSuites(appId);
    }

    @GET
    @Path("/{appId}/suites/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuiteDetails(@PathParam("appId") String appId, @PathParam("fileName") String fileName) {
        try {
            return Response.ok(appService.getSuiteDetails(appId, fileName)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{appId}/suites")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSuite(@PathParam("appId") String appId, SuiteInfo suiteInfo) {
        try {
            SuiteInfo created = appService.createSuite(appId, suiteInfo);
            return Response.status(201).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(409).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{appId}/suites/{fileName}/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameSuite(@PathParam("appId") String appId, @PathParam("fileName") String oldFileName, SuiteInfo newSuiteInfo) {
        try {
            SuiteInfo renamed = appService.renameSuite(appId, oldFileName, newSuiteInfo);
            return Response.ok(renamed).build();
        } catch (IllegalArgumentException e) {
            return Response.status(409).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Failed to rename file. Check if it's open.")).build();
        }
    }

    @DELETE
    @Path("/{appId}/suites/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSuite(@PathParam("appId") String appId, @PathParam("fileName") String fileName) {
        try {
            appService.deleteSuite(appId, fileName);
            return Response.ok(Map.of("message", "Suite deleted")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Could not delete file. Is it open in another app?")).build();
        }
    }

    @POST
    @Path("/{appId}/suites/{fileName}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateSuite(@PathParam("appId") String appId, @PathParam("fileName") String fileName, SuiteResponse suiteRequest) {
        ValidationResponse vr = appService.validateSuite(appId, suiteRequest);
        return vr.isValid ? Response.ok(vr).build() : Response.status(422).entity(vr).build();
    }

    @PUT
    @Path("/{appId}/suites/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveSuite(@PathParam("appId") String appId, @PathParam("fileName") String fileName, SuiteResponse suiteRequest) {
        try {
            SaveResponse response = appService.saveSuite(appId, fileName, suiteRequest);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(500).entity(new SaveResponse("ERROR", "Failed to save file: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/{appId}/modules")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getModulesExplorer(@PathParam("appId") String appId) {
        return appService.getModulesExplorer(appId);
    }

    @GET
    @Path("/{appId}/modules/{moduleName:.+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModuleDetails(@PathParam("appId") String appId, @PathParam("moduleName") String moduleName) {
        try {
            return Response.ok(appService.getModuleDetails(appId, moduleName)).build();
        } catch (Exception e) {
            return Response.status(404).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{appId}/modules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createModule(@PathParam("appId") String appId, Map<String, String> body) {
        try {
            appService.createModule(appId, body.get("path"));
            return Response.status(201).entity(Map.of("message", "Created", "path", body.get("path"))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(409).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{appId}/modules")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteModule(@PathParam("appId") String appId, @QueryParam("path") String modulePath) {
        try {
            appService.deleteModule(appId, modulePath);
            return Response.ok(Map.of("message", "Deleted")).build();
        } catch (Exception e) {
            return Response.status(404).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{appId}/modules/rename")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameModule(@PathParam("appId") String appId, Map<String, String> body) {
        String oldPath = body.get("oldPath");
        String newPath = body.get("newPath");

        if (oldPath == null || newPath == null) {
            return Response.status(400).entity(Map.of("error", "Old and new paths are required")).build();
        }

        try {
            appService.renameModule(appId, oldPath, newPath);
            return Response.ok(Map.of("message", "Renamed successfully", "newPath", newPath)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/convert/to-yaml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertToYaml(TestCase testCase) {
        try {
            List<TestCase> list = new ArrayList<>();
            list.add(testCase);
            
            // Koristimo internu Yaml konverziju
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            options.setSplitLines(false);

            @SuppressWarnings("unused")
            Yaml yaml = new Yaml(options);
            // Može ostati direktno mapiranje ili preko servisa
            return Response.ok(Map.of("yaml", "testCases: ...")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }
}