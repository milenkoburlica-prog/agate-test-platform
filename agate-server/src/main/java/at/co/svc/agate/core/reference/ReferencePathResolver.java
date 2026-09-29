package at.co.svc.agate.core.reference;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.project.ProjectContext;

public class ReferencePathResolver {

    private static volatile ProjectContext projectContext;

    public static void setProjectContext(ProjectContext context) {
        projectContext = context;
    }

    /*
     * Leave some safety margin below the traditional
     * Windows MAX_PATH limit.
     */
    private static final int SAFE_WINDOWS_PATH_LENGTH =
            240;

    /*
     * 6 SHA-256 bytes = 12 hexadecimal characters.
     * This is short enough for filenames and strong enough
     * to avoid practical collisions in the reference store.
     */
    private static final int HASH_BYTES =
            6;

    private static final int MIN_READABLE_PREFIX_LENGTH =
            12;

    public Path resolve(
            String yamlFile,
            TestCase testCase,
            TestStep step,
            ResponseFormat format) {

        if (yamlFile == null
                || yamlFile.isBlank()) {

            throw new IllegalArgumentException(
                    "yamlFile must not be empty");
        }

        if (testCase == null) {

            throw new IllegalArgumentException(
                    "testCase must not be null");
        }

        if (step == null) {

            throw new IllegalArgumentException(
                    "step must not be null");
        }

        String stepId =
                step.getId();

        if (stepId == null
                || stepId.isBlank()) {

            throw new IllegalArgumentException(
                    "stepId is required for MATCH_REFERENCE");
        }

        if (format == null) {

            throw new IllegalArgumentException(
                    "format must not be null");
        }

        Path yamlPath =
                Path.of(yamlFile)
                        .toAbsolutePath()
                        .normalize();

        Path yamlDirectory =
                yamlPath.getParent();

        if (yamlDirectory == null) {

            yamlDirectory =
                    Path.of(".")
                            .toAbsolutePath()
                            .normalize();
        }

        String yamlName =
                removeExtension(
                        yamlPath.getFileName()
                                .toString());

        String safeYamlName =
                sanitize(
                        yamlName);

        String originalTestCaseName =
                testCase.getName();

        String safeTestCaseName =
                sanitize(
                        originalTestCaseName);

        String referenceIdentity =
                buildReferenceIdentity(
                        step.getReferenceCallPath(),
                        stepId);

        String safeReferenceIdentity =
                sanitize(
                        referenceIdentity);

        String extension =
                format.getFileExtension();

        Path referenceDirectory;

        ProjectContext context = projectContext;

        if (context != null) {
            String application =
                    System.getProperty(
                                    "APPLICATION",
                                    "unknown")
                            .trim()
                            .toLowerCase();

            referenceDirectory =
                    context.getResponsesRoot()
                            .resolve(application)
                            .resolve(safeYamlName)
                            .normalize();
        } else {
            // Legacy fallback for direct executions without ProjectContext.
            referenceDirectory =
                    yamlDirectory
                            .resolve("references")
                            .resolve(safeYamlName)
                            .normalize();
        }

        String readableBaseName =
                safeTestCaseName
                        + "__"
                        + safeReferenceIdentity;

        String fileName =
                readableBaseName
                        + "."
                        + extension;

        Path referencePath =
                referenceDirectory
                        .resolve(
                                fileName)
                        .normalize();

        /*
         * Normal case:
         * keep the complete human-readable accumulated path.
         */
        if (referencePath
                .toString()
                .length()
                <= SAFE_WINDOWS_PATH_LENGTH) {

            return referencePath;
        }

        /*
         * Long-path case.
         *
         * Hash the complete logical identity, not the shortened form.
         * Therefore the generated filename remains deterministic and
         * unique even when the readable prefix has to be truncated.
         */
        String hashSource =
                nullSafe(yamlName)
                        + "|"
                        + nullSafe(originalTestCaseName)
                        + "|"
                        + nullSafe(referenceIdentity)
                        + "|"
                        + format.name();

        String hash =
                shortHash(
                        hashSource);

        String suffix =
                "__"
                        + hash
                        + "."
                        + extension;

        int availableBaseLength =
                SAFE_WINDOWS_PATH_LENGTH
                        - referenceDirectory
                                .toString()
                                .length()
                        - 1
                        - suffix.length();

        if (availableBaseLength
                < MIN_READABLE_PREFIX_LENGTH) {

            throw new IllegalStateException(
                    "Reference directory path is too long to create a safe reference filename: "
                            + referenceDirectory);
        }

        String shortenedBase =
                abbreviate(
                        readableBaseName,
                        availableBaseLength);

        String shortenedFileName =
                shortenedBase
                        + suffix;

        Path shortenedPath =
                referenceDirectory
                        .resolve(
                                shortenedFileName)
                        .normalize();

        if (shortenedPath
                .toString()
                .length()
                > SAFE_WINDOWS_PATH_LENGTH) {

            throw new IllegalStateException(
                    "Unable to create reference path within "
                            + SAFE_WINDOWS_PATH_LENGTH
                            + " characters: "
                            + shortenedPath);
        }

        return shortenedPath;
    }

    private String buildReferenceIdentity(
            String referenceCallPath,
            String stepId) {

        if (referenceCallPath == null
                || referenceCallPath.isBlank()) {

            return stepId;
        }

        return referenceCallPath
                + "__"
                + stepId;
    }

    private String removeExtension(
            String fileName) {

        int index =
                fileName.lastIndexOf('.');

        if (index <= 0) {

            return fileName;
        }

        return fileName.substring(
                0,
                index);
    }

    private String sanitize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "unknown";
        }

        String sanitized =
                value.trim();

        /*
         * Keep common German characters readable.
         */
        sanitized =
                sanitized
                        .replace(
                                "ä",
                                "ae")
                        .replace(
                                "ö",
                                "oe")
                        .replace(
                                "ü",
                                "ue")
                        .replace(
                                "Ä",
                                "Ae")
                        .replace(
                                "Ö",
                                "Oe")
                        .replace(
                                "Ü",
                                "Ue")
                        .replace(
                                "ß",
                                "ss");

        sanitized =
                sanitized
                        .replaceAll(
                                "[^a-zA-Z0-9._-]",
                                "_")
                        .replaceAll(
                                "_+",
                                "_");

        while (sanitized.startsWith("_")) {

            sanitized =
                    sanitized.substring(
                            1);
        }

        while (sanitized.endsWith("_")) {

            sanitized =
                    sanitized.substring(
                            0,
                            sanitized.length() - 1);
        }

        return sanitized.isBlank()
                ? "unknown"
                : sanitized;
    }

    private String abbreviate(
            String value,
            int maxLength) {

        if (value == null) {

            return "unknown";
        }

        if (value.length()
                <= maxLength) {

            return value;
        }

        return value.substring(
                0,
                maxLength);
    }

    private String shortHash(
            String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder result =
                    new StringBuilder();

            for (int i = 0;
                 i < HASH_BYTES;
                 i++) {

                result.append(
                        String.format(
                                "%02x",
                                hash[i]));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    e);
        }
    }

    private String nullSafe(
            String value) {

        return value == null
                ? ""
                : value;
    }
}
