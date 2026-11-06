package at.co.svc.agate.server.rest.resource;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource responsible for managing configuration files, environments, and execution contexts.
 */
@Path("/api/v1/config")
public class ConfigResource {

    /**
     * Retrieves a flattened execution context combining environment settings and user credentials.
     * 
     * @param instance the environment instance identifier
     * @param person the username identifier
     * @return HTTP 200 OK with flattened configuration map, or 404/500 on error
     */
    @GET
    @Path("/execution-context")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExecutionContext(
            @QueryParam("instance") String instance,
            @QueryParam("person") String person) {

        ObjectMapper mapper = new ObjectMapper();
        String dataPath = System.getProperty("user.dir") + File.separator + "env";
        
        try {
            File envFile = new File(dataPath, "env.conf");
            List<Map<String, Object>> envs = mapper.readValue(envFile, new TypeReference<>() {});
            
            File readersFile = new File(dataPath, "users.conf");
            List<Map<String, Object>> readers = mapper.readValue(readersFile, new TypeReference<>() {});

            Map<String, Object> selectedEnv = envs.stream()
                    .filter(e -> String.valueOf(e.get("index")).equalsIgnoreCase(instance))
                    .findFirst()
                    .orElse(null);

            Map<String, Object> selectedReader = readers.stream()
                    .filter(r -> String.valueOf(r.get("username")).equalsIgnoreCase(person))
                    .findFirst()
                    .orElse(null);

            if (selectedEnv == null) {
                return Response.status(404).entity(Map.of("error", "Instance " + instance + " not found.")).build();
            }

            Map<String, Object> result = new LinkedHashMap<>();
            
            flattenMap("environments", selectedEnv, result);
            
            if (selectedReader != null) {
                flattenMap("readers", selectedReader, result);
            }

            return Response.ok(result).build();

        } catch (IOException e) {
            return Response.status(500).entity(Map.of("error", "Config files missing: " + e.getMessage())).build();
        }
    }

    /**
     * Recursively flattens a nested JSON structure into dot-separated key-value pairs.
     * 
     * @param prefix the key prefix built across nesting levels
     * @param source the source map to flatten
     * @param target the target map storing flattened values
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue(), target);
            } else {
                target.put(key, entry.getValue());
            }
        }
    }

    /**
     * Retrieves the raw content of all underlying configuration files.
     * 
     * @return HTTP 200 OK containing raw configuration strings
     */
    @GET
    @Path("/all-config-files")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConfigs() {
        String dataPath = System.getProperty("user.dir") + File.separator + "env";
        Map<String, String> result = new LinkedHashMap<>();

        try {
            result.put("envContent", "");
            result.put("usersContent", "");

            File envFile = new File(dataPath, "env.conf");
            if (envFile.exists()) {
                String content = java.nio.file.Files.readString(envFile.toPath());
                result.put("envContent", content);
            }

            File usersFile = new File(dataPath, "users.conf");
            if (usersFile.exists()) {
                String content = java.nio.file.Files.readString(usersFile.toPath());
                result.put("usersContent", content);
            }

            return Response.ok(result).build();

        } catch (Exception e) {
            return Response.status(500).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }    

    /**
     * Updates and saves the raw configuration content to disk.
     * 
     * @param body raw JSON string containing updated config contents
     * @return HTTP 200 OK on success, or 500 on failure
     */
    @POST
    @Path("/save-all-configs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveAllConfigs(String body) {
        String dataPath = System.getProperty("user.dir") + File.separator + "env";
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            Map<String, String> payload = mapper.readValue(body, new TypeReference<Map<String, String>>() {});
            
            if (payload.containsKey("envContent")) {
                java.nio.file.Files.writeString(new File(dataPath, "env.conf").toPath(), payload.get("envContent"));
            }

            if (payload.containsKey("usersContent")) {
                java.nio.file.Files.writeString(new File(dataPath, "users.conf").toPath(), payload.get("usersContent"));
            }

            return Response.ok("{\"status\":\"success\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }   
}