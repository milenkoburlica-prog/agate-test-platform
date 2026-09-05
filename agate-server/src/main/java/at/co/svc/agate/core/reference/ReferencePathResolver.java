package at.co.svc.agate.core.reference;

import java.nio.file.Path;

import at.co.svc.agate.core.dsl.model.TestCase;

public class ReferencePathResolver {

    private static final String REFERENCES_DIRECTORY = "references";

    public Path resolve(
            String yamlFile,
            TestCase testCase,
            String stepId,
            ResponseFormat format) {

        if (yamlFile == null || yamlFile.isBlank()) {
            throw new IllegalArgumentException(
                    "yamlFile must not be empty");
        }

        if (testCase == null) {
            throw new IllegalArgumentException(
                    "testCase must not be null");
        }

        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException(
                    "stepId is required for MATCH_REFERENCE");
        }

        Path yamlPath = Path.of(yamlFile)
                .toAbsolutePath()
                .normalize();

        Path yamlDirectory = yamlPath.getParent();

        if (yamlDirectory == null) {
            yamlDirectory = Path.of(".")
                    .toAbsolutePath()
                    .normalize();
        }

        String yamlName = removeExtension(
                yamlPath.getFileName().toString());

        String testCaseName = sanitize(
                testCase.getName());

        String safeStepId = sanitize(stepId);

        String fileName =
                testCaseName
                        + "__"
                        + safeStepId
                        + "."
                        + format.getFileExtension();

        return yamlDirectory
                .resolve(REFERENCES_DIRECTORY)
                .resolve(sanitize(yamlName))
                .resolve(fileName)
                .normalize();
    }

    private String removeExtension(String fileName) {

        int index = fileName.lastIndexOf('.');

        if (index <= 0) {
            return fileName;
        }

        return fileName.substring(0, index);
    }

    private String sanitize(String value) {

        if (value == null || value.isBlank()) {
            return "unknown";
        }

        String sanitized = value
                .trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_");

        while (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
        }

        while (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(
                    0,
                    sanitized.length() - 1);
        }

        return sanitized.isBlank()
                ? "unknown"
                : sanitized;
    }
}