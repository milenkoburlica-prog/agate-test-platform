package at.co.svc.agate.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public final class ProjectConfigLoader {

    private ProjectConfigLoader() {
    }

    @SuppressWarnings("unchecked")
    public static ProjectConfig load(Path projectFile) throws IOException {
        if (projectFile == null || !Files.isRegularFile(projectFile)) {
            throw new IOException(
                    "AGATE project configuration was not found | Path: "
                            + projectFile);
        }

        final Object loaded;
        try (InputStream input = Files.newInputStream(projectFile)) {
            loaded = new Yaml().load(input);
        }

        if (!(loaded instanceof Map<?, ?> rawRoot)) {
            throw new IllegalArgumentException(
                    "Invalid AGATE project configuration | Root YAML must be a map | Path: "
                            + projectFile);
        }

        Map<String, Object> root = (Map<String, Object>) rawRoot;
        ProjectConfig result = new ProjectConfig();

        Object version = root.get("version");
        if (version instanceof Number number) {
            result.setVersion(number.intValue());
        } else if (version != null) {
            result.setVersion(Integer.parseInt(String.valueOf(version).trim()));
        }

        Object projectObj = root.get("project");
        if (projectObj instanceof Map<?, ?> projectMap) {
            Object name = projectMap.get("name");
            if (name != null) {
                result.getProject().setName(String.valueOf(name).trim());
            }
        }

        Object pathsObj = root.get("paths");
        if (pathsObj instanceof Map<?, ?> pathsMap) {
            setIfPresent(pathsMap, "tests", result.getPaths()::setTests);
            setIfPresent(pathsMap, "responses", result.getPaths()::setResponses);
            setIfPresent(pathsMap, "reports", result.getPaths()::setReports);
        }
        
        Object configObj = root.get("config");
        if (configObj instanceof Map<?, ?> configMap) {
            setIfPresent(configMap, "environments", result.getConfig()::setEnvironments);
            setIfPresent(configMap, "users", result.getConfig()::setUsers);
        }

        return result;
    }

    private static void setIfPresent(
            Map<?, ?> map,
            String key,
            java.util.function.Consumer<String> setter) {

        Object value = map.get(key);
        if (value != null && !String.valueOf(value).isBlank()) {
            setter.accept(String.valueOf(value).trim());
        }
    }
}
