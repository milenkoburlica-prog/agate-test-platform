package at.co.svc.agate.openapi.model;

import java.util.ArrayList;
import java.util.List;

public class AgateOpenApiModel {

    private String source;
    private String title;
    private String version;

    private List<String> servers =
            new ArrayList<>();

    private List<String> tags =
            new ArrayList<>();

    private List<AgateEndpoint> endpoints =
            new ArrayList<>();

    private List<AgateDiagnostic> diagnostics =
            new ArrayList<>();


    public String getSource() {
        return source;
    }

    public void setSource(
            String source) {

        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title) {

        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(
            String version) {

        this.version = version;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(
            List<String> servers) {

        this.servers =
                servers != null
                        ? servers
                        : new ArrayList<>();
    }

    public void addServer(
            String server) {

        if (server != null) {
            servers.add(server);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(
            List<String> tags) {

        this.tags =
                tags != null
                        ? tags
                        : new ArrayList<>();
    }

    public void addTag(
            String tag) {

        if (tag != null) {
            tags.add(tag);
        }
    }

    public List<AgateEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(
            List<AgateEndpoint> endpoints) {

        this.endpoints =
                endpoints != null
                        ? endpoints
                        : new ArrayList<>();
    }

    public void addEndpoint(
            AgateEndpoint endpoint) {

        if (endpoint != null) {
            endpoints.add(endpoint);
        }
    }

    public List<AgateDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(
            List<AgateDiagnostic> diagnostics) {

        this.diagnostics =
                diagnostics != null
                        ? diagnostics
                        : new ArrayList<>();
    }

    public void addDiagnostic(
            AgateDiagnostic diagnostic) {

        if (diagnostic != null) {
            diagnostics.add(diagnostic);
        }
    }
}