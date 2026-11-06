package at.co.svc.agate.core.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ProjectConfigValidator {

    private ProjectConfigValidator() {
    }

    public static void validateOrThrow(ProjectContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ProjectContext must not be null");
        }

        List<String> errors = new ArrayList<>();

        if (context.getVersion() != 1) {
            errors.add("Unsupported project-agate.yaml version: " + context.getVersion());
        }

        if (isBlank(context.getProjectName())) {
            errors.add("project.name is required");
        }

        validateDirectory(
                "paths.tests",
                context.getTestsRoot(),
                true,
                errors);

        validateConfiguredPath(
                "paths.responses",
                context.getResponsesRoot(),
                errors);

        validateConfiguredPath(
                "paths.reports",
                context.getReportsRoot(),
                errors);

        validateFile(
                "config.environments",
                context.getEnvironmentConfig(),
                errors);

        validateFile(
                "config.users",
                context.getUsersConfig(),
                errors);

        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder("Invalid AGATE project configuration:");
            for (String error : errors) {
                message.append(System.lineSeparator())
                        .append(" - ")
                        .append(error);
            }
            throw new IllegalArgumentException(message.toString());
        }
    }

    private static void validateDirectory(
            String field,
            Path path,
            boolean mustExist,
            List<String> errors) {

        if (path == null) {
            errors.add(field + " is missing");
            return;
        }

        if (mustExist && (!Files.exists(path) || !Files.isDirectory(path))) {
            errors.add(field + " directory was not found: " + path);
        }
    }

    private static void validateConfiguredPath(
            String field,
            Path path,
            List<String> errors) {

        if (path == null) {
            errors.add(field + " is missing");
        }
    }

    private static void validateFile(
            String field,
            Path path,
            List<String> errors) {

        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            errors.add(field + " file was not found: " + path);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
