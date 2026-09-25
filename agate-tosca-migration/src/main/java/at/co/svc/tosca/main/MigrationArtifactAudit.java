package at.co.svc.tosca.main;

import at.co.svc.tosca.util.FileNameSanitizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tracks artifacts created by one Tosca migration and writes a persistent
 * manifest per migrated root test.
 *
 * Design goals:
 * - does not change Tosca -> AGATE transformation
 * - distinguishes files that existed before the run from files created now
 * - determines dependencies starting from the current root YAML/template
 * - protects files referenced by manifests from earlier migrations
 * - dry-run is the default cleanup validation mode
 * - real cleanup only deletes NEW, unreferenced files below reusable/ or modules/
 * - template/ files are never deleted by this first implementation
 *
 * System property:
 *   -Dagate.migration.cleanup=none
 *   -Dagate.migration.cleanup=dry-run
 *   -Dagate.migration.cleanup=cleanup
 */
public final class MigrationArtifactAudit {

    public static final String CLEANUP_PROPERTY =
            "agate.migration.cleanup";

    private static final String MANIFEST_DIR =
            ".migration-manifests";

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

    private MigrationArtifactAudit() {
    }

    public enum Mode {
        NONE,
        DRY_RUN,
        CLEANUP;

        public static Mode fromSystemProperty() {

            String value =
                    System.getProperty(
                            CLEANUP_PROPERTY,
                            "none"
                    );

            if (value == null) {
                return NONE;
            }

            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "dry-run",
                     "dryrun",
                     "cleanup-dry-run" -> DRY_RUN;

                case "cleanup",
                     "delete",
                     "true" -> CLEANUP;

                default -> NONE;
            };
        }
    }

    /**
     * Used by the existing MainMigrationApp code to suppress its legacy
     * unused-reusable deletion only when the new cleanup workflow is active.
     *
     * Normal "migrate" therefore keeps the old behaviour unchanged.
     */
    public static boolean isManagedCleanupEnabled() {
        return Mode.fromSystemProperty() != Mode.NONE;
    }

    public static Session begin(
            String appId,
            String baseFileName,
            String migrationDir) throws IOException {

        Path appRoot =
                Paths.get(
                        System.getProperty("user.dir"),
                        migrationDir,
                        appId
                )
                .toAbsolutePath()
                .normalize();

        return begin(
                appId,
                baseFileName,
                appRoot
        );
    }

    public static Session begin(
            String appId,
            String baseFileName,
            Path appRoot) throws IOException {

        Path normalizedRoot =
                appRoot.toAbsolutePath()
                        .normalize();

        Map<String, FileSnapshot> before =
                snapshot(normalizedRoot);

        return new Session(
                appId,
                baseFileName,
                normalizedRoot,
                before
        );
    }

    public static final class Session {

        private final String appId;
        private final String baseFileName;
        private final Path appRoot;
        private final Map<String, FileSnapshot> before;
        private final Instant startedAt;

        private Session(
                String appId,
                String baseFileName,
                Path appRoot,
                Map<String, FileSnapshot> before) {

            this.appId = appId;
            this.baseFileName = baseFileName;
            this.appRoot = appRoot;
            this.before = before;
            this.startedAt = Instant.now();
        }

        public void finish(
                boolean migrationSucceeded) {

            finish(
                    Mode.fromSystemProperty(),
                    migrationSucceeded
            );
        }

        public void finish(
                Mode mode,
                boolean migrationSucceeded) {

            try {

                Files.createDirectories(
                        appRoot
                );

                Map<String, FileSnapshot> after =
                        snapshot(appRoot);

                Set<String> previousRequired =
                        readRequiredFilesFromPreviousManifests(
                                appRoot
                        );

                Reachability reachability =
                        determineReachability(
                                appRoot,
                                baseFileName
                        );

                List<ArtifactStatus> statuses =
                        buildStatuses(
                                before,
                                after,
                                reachability.requiredFiles,
                                previousRequired
                        );

                printAudit(
                        mode,
                        migrationSucceeded,
                        reachability,
                        statuses
                );

                if (mode == Mode.CLEANUP) {

                    deleteSafeCandidates(
                            statuses
                    );

                    removeEmptyGeneratedDirectories(
                            appRoot.resolve("reusable")
                    );

                    removeEmptyGeneratedDirectories(
                            appRoot.resolve("modules")
                    );
                }

                writeManifest(
                        mode,
                        migrationSucceeded,
                        reachability,
                        statuses
                );

            } catch (Exception e) {

                /*
                 * Audit/cleanup must never hide the result of the actual
                 * Tosca migration. We report the problem, but do not rethrow
                 * from a finally block.
                 */
                System.err.println();
                System.err.println(
                        "[MIGRATION-AUDIT-ERROR] "
                                + e.getMessage()
                );

                if (Boolean.getBoolean("agate.debug")) {
                    e.printStackTrace();
                }
            }
        }

        private List<ArtifactStatus> buildStatuses(
                Map<String, FileSnapshot> before,
                Map<String, FileSnapshot> after,
                Set<String> requiredCurrent,
                Set<String> requiredPrevious) {

            Set<String> allPaths =
                    new LinkedHashSet<>();

            allPaths.addAll(before.keySet());
            allPaths.addAll(after.keySet());

            List<ArtifactStatus> result =
                    new ArrayList<>();

            for (String relativePath : allPaths) {

                FileSnapshot oldFile =
                        before.get(relativePath);

                FileSnapshot newFile =
                        after.get(relativePath);

                boolean existedBefore =
                        oldFile != null;

                boolean existsAfter =
                        newFile != null;

                boolean createdThisRun =
                        !existedBefore
                                && existsAfter;

                boolean modifiedThisRun =
                        existedBefore
                                && existsAfter
                                && !oldFile.sameContentMetadata(
                                        newFile
                                );

                boolean requiredNow =
                        requiredCurrent.contains(
                                relativePath
                        );

                boolean protectedByPrevious =
                        requiredPrevious.contains(
                                relativePath
                        );

                ArtifactCategory category =
                        categoryOf(relativePath);

                boolean safeCleanupLocation =
                        category == ArtifactCategory.REUSABLE
                                || category == ArtifactCategory.MODULE
                                || category == ArtifactCategory.TEMPLATE;

                boolean cleanupCandidate =
                        createdThisRun
                                && !requiredNow
                                && !protectedByPrevious
                                && safeCleanupLocation;

                result.add(
                        new ArtifactStatus(
                                relativePath,
                                existedBefore,
                                existsAfter,
                                createdThisRun,
                                modifiedThisRun,
                                requiredNow,
                                protectedByPrevious,
                                category,
                                cleanupCandidate
                        )
                );
            }

            result.sort(
                    Comparator.comparing(
                            ArtifactStatus::path,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            return result;
        }

        private void deleteSafeCandidates(
                List<ArtifactStatus> statuses)
                throws IOException {

            for (ArtifactStatus status : statuses) {

                if (!status.cleanupCandidate()) {
                    continue;
                }

                Path target =
                        appRoot.resolve(
                                status.path()
                        )
                        .normalize();

                if (!target.startsWith(appRoot)) {

                    System.err.println(
                            "[CLEANUP-SKIP] Unsafe path: "
                                    + target
                    );

                    continue;
                }

                if (Files.deleteIfExists(target)) {

                    status.markDeleted();

                    System.out.println(
                            "[CLEANUP-DELETE] "
                                    + status.path()
                    );
                }
            }
        }

        private void writeManifest(
                Mode mode,
                boolean migrationSucceeded,
                Reachability reachability,
                List<ArtifactStatus> statuses)
                throws IOException {

            Path manifestDirectory =
                    appRoot.resolve(
                            MANIFEST_DIR
                    );

            Files.createDirectories(
                    manifestDirectory
            );

            Path manifestFile =
                    manifestDirectory.resolve(
                            sanitizeManifestName(
                                    baseFileName
                            )
                                    + ".json"
                    );

            ObjectNode root =
                    MAPPER.createObjectNode();

            root.put(
                    "application",
                    appId
            );

            root.put(
                    "baseFileName",
                    baseFileName
            );

            root.put(
                    "startedAt",
                    startedAt.toString()
            );

            root.put(
                    "finishedAt",
                    Instant.now().toString()
            );

            root.put(
                    "mode",
                    mode.name()
            );

            root.put(
                    "migrationSucceeded",
                    migrationSucceeded
            );

            root.put(
                    "appRoot",
                    appRoot.toString()
            );

            ArrayNode roots =
                    root.putArray(
                            "rootFiles"
                    );

            for (String rootFile :
                    reachability.rootFiles) {

                roots.add(rootFile);
            }

            ArrayNode required =
                    root.putArray(
                            "requiredFiles"
                    );

            for (String file :
                    reachability.requiredFiles) {

                required.add(file);
            }

            ArrayNode files =
                    root.putArray(
                            "files"
                    );

            for (ArtifactStatus status :
                    statuses) {

                ObjectNode item =
                        files.addObject();

                item.put(
                        "path",
                        status.path()
                );

                item.put(
                        "category",
                        status.category()
                                .name()
                );

                item.put(
                        "existedBeforeRun",
                        status.existedBeforeRun()
                );

                item.put(
                        "existsAfterRun",
                        status.existsAfterRun()
                );

                item.put(
                        "createdThisRun",
                        status.createdThisRun()
                );

                item.put(
                        "modifiedThisRun",
                        status.modifiedThisRun()
                );

                item.put(
                        "reachableCurrent",
                        status.reachableCurrent()
                );

                item.put(
                        "protectedByPreviousManifest",
                        status.protectedByPreviousManifest()
                );

                item.put(
                        "cleanupCandidate",
                        status.cleanupCandidate()
                );

                item.put(
                        "deletedByCleanup",
                        status.deletedByCleanup()
                );
            }

            MAPPER.writeValue(
                    manifestFile.toFile(),
                    root
            );

            System.out.println();
            System.out.println(
                    "[MIGRATION-AUDIT] Manifest: "
                            + manifestFile
            );
        }

        private void printAudit(
                Mode mode,
                boolean migrationSucceeded,
                Reachability reachability,
                List<ArtifactStatus> statuses) {

            long newFiles =
                    statuses.stream()
                            .filter(
                                    ArtifactStatus::createdThisRun
                            )
                            .count();

            long existingFiles =
                    statuses.stream()
                            .filter(
                                    s ->
                                            s.existedBeforeRun()
                                                    && s.existsAfterRun()
                            )
                            .count();

            long modifiedFiles =
                    statuses.stream()
                            .filter(
                                    ArtifactStatus::modifiedThisRun
                            )
                            .count();

            long required =
                    statuses.stream()
                            .filter(
                                    ArtifactStatus::reachableCurrent
                            )
                            .count();

            List<ArtifactStatus> candidates =
                    statuses.stream()
                            .filter(
                                    ArtifactStatus::cleanupCandidate
                            )
                            .toList();

            System.out.println();
            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "MIGRATION FILE AUDIT"
            );

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "Application          : "
                            + appId
            );

            System.out.println(
                    "Base file            : "
                            + baseFileName
            );

            System.out.println(
                    "Mode                 : "
                            + mode
            );

            System.out.println(
                    "Migration successful : "
                            + migrationSucceeded
            );

            System.out.println(
                    "Files before run     : "
                            + before.size()
            );

            System.out.println(
                    "Files after run      : "
                            + statuses.stream()
                                    .filter(
                                            ArtifactStatus::existsAfterRun
                                    )
                                    .count()
            );

            System.out.println(
                    "NEW                  : "
                            + newFiles
            );

            System.out.println(
                    "EXISTING             : "
                            + existingFiles
            );

            System.out.println(
                    "MODIFIED             : "
                            + modifiedFiles
            );

            System.out.println(
                    "REACHABLE CURRENT    : "
                            + required
            );

            System.out.println(
                    "CLEANUP CANDIDATES   : "
                            + candidates.size()
            );

            System.out.println();

            System.out.println(
                    "ROOT FILES:"
            );

            if (reachability.rootFiles.isEmpty()) {

                System.out.println(
                        "  [WARNING] No root YAML/template could be resolved."
                );

            } else {

                for (String rootFile :
                        reachability.rootFiles) {

                    System.out.println(
                            "  ROOT -> "
                                    + rootFile
                    );
                }
            }

            System.out.println();

            System.out.println(
                    "NEW FILES:"
            );

            for (ArtifactStatus status :
                    statuses) {

                if (status.createdThisRun()) {

                    System.out.println(
                            "  NEW  -> "
                                    + status.path()
                                    + suffixFor(status)
                    );
                }
            }

            System.out.println();

            System.out.println(
                    "EXISTING FILES TOUCHED:"
            );

            for (ArtifactStatus status :
                    statuses) {

                if (status.modifiedThisRun()) {

                    System.out.println(
                            "  MOD  -> "
                                    + status.path()
                                    + suffixFor(status)
                    );
                }
            }

            System.out.println();

            if (mode == Mode.DRY_RUN) {

                System.out.println(
                        "CLEANUP DRY-RUN:"
                );

            } else if (mode == Mode.CLEANUP) {

                System.out.println(
                        "CLEANUP PLAN:"
                );

            } else {

                System.out.println(
                        "CLEANUP CANDIDATES:"
                );
            }

            if (candidates.isEmpty()) {

                System.out.println(
                        "  none"
                );

            } else {

                for (ArtifactStatus candidate :
                        candidates) {

                    System.out.println(
                            "  "
                                    + (mode == Mode.CLEANUP
                                    ? "DELETE"
                                    : "CANDIDATE")
                                    + " -> "
                                    + candidate.path()
                    );
                }
            }

            System.out.println();

            System.out.println(
                    "SAFETY RULE:"
            );

            System.out.println(
                    "  Only NEW files below reusable/, modules/ or template/ can be deleted."
            );

            System.out.println(
                    "  Existing files, current root/template files, current CSV companions and manifest files are never deleted."
            );

            System.out.println(
                    "  Files reachable now or protected by previous manifests are never deleted."
            );

            System.out.println(
                    "============================================================"
            );
        }

        private String suffixFor(
                ArtifactStatus status) {

            List<String> flags =
                    new ArrayList<>();

            if (status.reachableCurrent()) {
                flags.add("USED");
            }

            if (status.protectedByPreviousManifest()) {
                flags.add("PROTECTED");
            }

            if (status.cleanupCandidate()) {
                flags.add("UNUSED/CANDIDATE");
            }

            if (flags.isEmpty()) {
                return "";
            }

            return " [" + String.join(", ", flags) + "]";
        }
    }

    private static Reachability determineReachability(
            Path appRoot,
            String baseFileName)
            throws IOException {

        LinkedHashSet<String> rootFiles =
                discoverRootFiles(
                        appRoot,
                        baseFileName
                );

        LinkedHashSet<String> required =
                new LinkedHashSet<>();

        Deque<Path> yamlQueue =
                new ArrayDeque<>();

        Set<Path> scannedYaml =
                new HashSet<>();

        for (String rootFile : rootFiles) {

            Path path =
                    appRoot.resolve(
                            rootFile
                    );

            if (Files.isRegularFile(path)) {

                required.add(
                        toRelative(
                                appRoot,
                                path
                        )
                );

                if (isYaml(path)) {
                    yamlQueue.add(path);
                }
            }
        }

        addRootCsvCompanions(
                appRoot,
                baseFileName,
                rootFiles,
                required
        );

        while (!yamlQueue.isEmpty()) {

            Path yaml =
                    yamlQueue.removeFirst()
                            .toAbsolutePath()
                            .normalize();

            if (!scannedYaml.add(yaml)) {
                continue;
            }

            if (!Files.isRegularFile(yaml)) {
                continue;
            }

            List<String> lines =
                    Files.readAllLines(
                            yaml,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                String command =
                        extractCommand(line);

                if (command == null
                        || command.isBlank()) {
                    continue;
                }

                if (command.startsWith("reusable.")) {

                    String reusableName =
                            command.substring(
                                    "reusable.".length()
                            );

                    String cleanName =
                            FileNameSanitizer.sanitize(
                                    reusableName
                            );

                    Path reusable =
                            appRoot.resolve(
                                    "reusable"
                            )
                            .resolve(
                                    cleanName
                                            + ".yaml"
                            )
                            .normalize();

                    if (Files.isRegularFile(reusable)) {

                        String relative =
                                toRelative(
                                        appRoot,
                                        reusable
                                );

                        if (required.add(relative)) {
                            yamlQueue.add(reusable);
                        }
                    }

                    continue;
                }

                if (command.startsWith("soap.")
                        || command.startsWith("rest.")) {

                    addApiModuleArtifacts(
                            appRoot,
                            command,
                            required
                    );
                }
            }
        }

        return new Reachability(
                rootFiles,
                required
        );
    }

    private static LinkedHashSet<String> discoverRootFiles(
            Path appRoot,
            String baseFileName)
            throws IOException {

        LinkedHashSet<String> roots =
                new LinkedHashSet<>();

        Path direct =
                appRoot.resolve(
                        baseFileName
                                + ".yaml"
                );

        if (Files.isRegularFile(direct)) {
            roots.add(
                    toRelative(
                            appRoot,
                            direct
                    )
            );
        }

        Path templateDir =
                appRoot.resolve(
                        "template"
                );

        if (!Files.isDirectory(templateDir)) {
            return roots;
        }

        LinkedHashSet<String> candidateNames =
                new LinkedHashSet<>();

        candidateNames.add(
                baseFileName
        );

        candidateNames.add(
                stripKnownMigrationPrefix(
                        baseFileName
                )
        );

        try (Stream<Path> stream =
                     Files.list(templateDir)) {

            List<Path> yamlFiles =
                    stream.filter(
                                    Files::isRegularFile
                            )
                            .filter(
                                    MigrationArtifactAudit::isYaml
                            )
                            .sorted()
                            .toList();

            for (Path yaml : yamlFiles) {

                String fileName =
                        yaml.getFileName()
                                .toString();

                String stem =
                        fileName.substring(
                                0,
                                fileName.lastIndexOf('.')
                        );

                for (String candidate :
                        candidateNames) {

                    if (stem.equalsIgnoreCase(
                            candidate
                    )) {

                        roots.add(
                                toRelative(
                                        appRoot,
                                        yaml
                                )
                        );

                        break;
                    }
                }
            }
        }

        return roots;
    }

    private static void addRootCsvCompanions(
            Path appRoot,
            String baseFileName,
            Set<String> rootFiles,
            Set<String> required) {

        Path templateDir =
                appRoot.resolve(
                        "template"
                );

        if (!Files.isDirectory(templateDir)) {
            return;
        }

        Path baseCsv =
                templateDir.resolve(
                        baseFileName
                                + ".csv"
                );

        if (Files.isRegularFile(baseCsv)) {
            required.add(
                    toRelative(
                            appRoot,
                            baseCsv
                    )
            );
        }

        for (String rootFile :
                rootFiles) {

            Path root =
                    appRoot.resolve(
                            rootFile
                    );

            if (!root.startsWith(templateDir)
                    || !isYaml(root)) {
                continue;
            }

            String name =
                    root.getFileName()
                            .toString();

            String stem =
                    name.substring(
                            0,
                            name.lastIndexOf('.')
                    );

            Path siblingCsv =
                    root.getParent()
                            .resolve(
                                    stem
                                            + ".csv"
                            );

            if (Files.isRegularFile(
                    siblingCsv
            )) {

                required.add(
                        toRelative(
                                appRoot,
                                siblingCsv
                        )
                );
            }
        }
    }

    private static void addApiModuleArtifacts(
            Path appRoot,
            String command,
            Set<String> required)
            throws IOException {

        String[] parts =
                command.split("\\.");

        if (parts.length < 2) {
            return;
        }

        Path moduleDir =
                appRoot.resolve(
                        "modules"
                );

        for (String part : parts) {

            if (part == null
                    || part.isBlank()) {
                return;
            }

            moduleDir =
                    moduleDir.resolve(
                            part
                    );
        }

        moduleDir =
                moduleDir.normalize();

        if (!moduleDir.startsWith(
                appRoot
        )) {
            return;
        }

        if (!Files.isDirectory(
                moduleDir
        )) {
            return;
        }

        try (Stream<Path> stream =
                     Files.walk(moduleDir)) {

            for (Path file :
                    stream.filter(
                                    Files::isRegularFile
                            )
                            .toList()) {

                required.add(
                        toRelative(
                                appRoot,
                                file
                        )
                );
            }
        }
    }

    private static String extractCommand(
            String rawLine) {

        if (rawLine == null) {
            return null;
        }

        String trimmed =
                rawLine.trim();

        if (!trimmed.startsWith(
                "command:"
        )) {
            return null;
        }

        String value =
                trimmed.substring(
                        "command:".length()
                )
                .trim();

        if (value.isEmpty()) {
            return null;
        }

        int commentIndex =
                value.indexOf(
                        " #"
                );

        if (commentIndex >= 0) {

            value =
                    value.substring(
                            0,
                            commentIndex
                    )
                    .trim();
        }

        if ((value.startsWith("\"")
                && value.endsWith("\""))
                || (value.startsWith("'")
                && value.endsWith("'"))) {

            if (value.length() >= 2) {

                value =
                        value.substring(
                                1,
                                value.length() - 1
                        );
            }
        }

        return value.trim();
    }

    private static Set<String> readRequiredFilesFromPreviousManifests(
            Path appRoot)
            throws IOException {

        Path manifestDir =
                appRoot.resolve(
                        MANIFEST_DIR
                );

        if (!Files.isDirectory(
                manifestDir
        )) {
            return Set.of();
        }

        Set<String> required =
                new LinkedHashSet<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(
                             manifestDir,
                             "*.json"
                     )) {

            for (Path manifest :
                    stream) {

                try {

                    JsonNode root =
                            MAPPER.readTree(
                                    manifest.toFile()
                            );

                    JsonNode files =
                            root.path(
                                    "requiredFiles"
                            );

                    if (files.isArray()) {

                        for (JsonNode file :
                                files) {

                            String value =
                                    file.asText(
                                            ""
                                    );

                            if (!value.isBlank()) {
                                required.add(value);
                            }
                        }
                    }

                } catch (Exception e) {

                    System.err.println(
                            "[MIGRATION-AUDIT-WARNING] "
                                    + "Cannot read previous manifest "
                                    + manifest
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }
        }

        return required;
    }

    private static Map<String, FileSnapshot> snapshot(
            Path appRoot)
            throws IOException {

        Map<String, FileSnapshot> result =
                new LinkedHashMap<>();

        if (!Files.isDirectory(
                appRoot
        )) {
            return result;
        }

        try (Stream<Path> stream =
                     Files.walk(appRoot)) {

            for (Path path :
                    stream.filter(
                                    Files::isRegularFile
                            )
                            .toList()) {

                /*
                 * The manifest itself is bookkeeping, not a migration artifact.
                 */
                if (path.startsWith(
                        appRoot.resolve(
                                MANIFEST_DIR
                        )
                )) {
                    continue;
                }

                String relative =
                        toRelative(
                                appRoot,
                                path
                        );

                result.put(
                        relative,
                        FileSnapshot.read(
                                path
                        )
                );
            }
        }

        return result;
    }

    private static ArtifactCategory categoryOf(
            String relativePath) {

        String normalized =
                relativePath.replace(
                        '\\',
                        '/'
                );

        if (normalized.startsWith(
                "reusable/"
        )) {
            return ArtifactCategory.REUSABLE;
        }

        if (normalized.startsWith(
                "modules/"
        )) {
            return ArtifactCategory.MODULE;
        }

        if (normalized.startsWith(
                "template/"
        )) {
            return ArtifactCategory.TEMPLATE;
        }

        return ArtifactCategory.ROOT_OR_OTHER;
    }

    private static void removeEmptyGeneratedDirectories(
            Path root)
            throws IOException {

        if (!Files.isDirectory(
                root
        )) {
            return;
        }

        List<Path> directories;

        try (Stream<Path> stream =
                     Files.walk(root)) {

            directories =
                    stream.filter(
                                    Files::isDirectory
                            )
                            .sorted(
                                    Comparator.reverseOrder()
                            )
                            .toList();
        }

        for (Path directory :
                directories) {

            if (directory.equals(root)) {
                continue;
            }

            try (Stream<Path> content =
                         Files.list(directory)) {

                if (content.findAny().isEmpty()) {
                    Files.deleteIfExists(directory);
                }
            }
        }
    }

    private static String stripKnownMigrationPrefix(
            String baseFileName) {

        if (baseFileName == null) {
            return "";
        }

        String result =
                baseFileName.trim();

        boolean changed;

        do {

            changed = false;

            for (String prefix :
                    List.of(
                            "TCD_",
                            "T_",
                            "TS_",
                            "Instance_"
                    )) {

                if (result.regionMatches(
                        true,
                        0,
                        prefix,
                        0,
                        prefix.length()
                )) {

                    result =
                            result.substring(
                                    prefix.length()
                            );

                    changed = true;

                    break;
                }
            }

        } while (changed);

        return result;
    }

    private static boolean isYaml(
            Path path) {

        if (path == null
                || path.getFileName() == null) {
            return false;
        }

        String name =
                path.getFileName()
                        .toString()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return name.endsWith(
                ".yaml"
        )
                || name.endsWith(
                ".yml"
        );
    }

    private static String sanitizeManifestName(
            String value) {

        if (value == null
                || value.isBlank()) {
            return "migration";
        }

        String sanitized =
                value.replaceAll(
                        "[^\\p{L}\\p{N}._-]+",
                        "_"
                );

        sanitized =
                sanitized.replaceAll(
                        "_+",
                        "_"
                );

        sanitized =
                sanitized.replaceAll(
                        "^_+|_+$",
                        ""
                );

        return sanitized.isBlank()
                ? "migration"
                : sanitized;
    }

    private static String toRelative(
            Path root,
            Path path) {

        return root.relativize(
                        path.toAbsolutePath()
                                .normalize()
                )
                .toString()
                .replace(
                        '\\',
                        '/'
                );
    }

    private enum ArtifactCategory {
        ROOT_OR_OTHER,
        TEMPLATE,
        REUSABLE,
        MODULE
    }

    private record FileSnapshot(
            long size,
            FileTime modified) {

        private static FileSnapshot read(
                Path path)
                throws IOException {

            return new FileSnapshot(
                    Files.size(path),
                    Files.getLastModifiedTime(
                            path
                    )
            );
        }

        private boolean sameContentMetadata(
                FileSnapshot other) {

            if (other == null) {
                return false;
            }

            return size == other.size
                    && modified.equals(
                            other.modified
                    );
        }
    }

    private static final class ArtifactStatus {

        private final String path;
        private final boolean existedBeforeRun;
        private final boolean existsAfterRun;
        private final boolean createdThisRun;
        private final boolean modifiedThisRun;
        private final boolean reachableCurrent;
        private final boolean protectedByPreviousManifest;
        private final ArtifactCategory category;
        private final boolean cleanupCandidate;

        private boolean deletedByCleanup;

        private ArtifactStatus(
                String path,
                boolean existedBeforeRun,
                boolean existsAfterRun,
                boolean createdThisRun,
                boolean modifiedThisRun,
                boolean reachableCurrent,
                boolean protectedByPreviousManifest,
                ArtifactCategory category,
                boolean cleanupCandidate) {

            this.path = path;
            this.existedBeforeRun = existedBeforeRun;
            this.existsAfterRun = existsAfterRun;
            this.createdThisRun = createdThisRun;
            this.modifiedThisRun = modifiedThisRun;
            this.reachableCurrent = reachableCurrent;
            this.protectedByPreviousManifest = protectedByPreviousManifest;
            this.category = category;
            this.cleanupCandidate = cleanupCandidate;
        }

        private String path() {
            return path;
        }

        private boolean existedBeforeRun() {
            return existedBeforeRun;
        }

        private boolean existsAfterRun() {
            return existsAfterRun
                    && !deletedByCleanup;
        }

        private boolean createdThisRun() {
            return createdThisRun;
        }

        private boolean modifiedThisRun() {
            return modifiedThisRun;
        }

        private boolean reachableCurrent() {
            return reachableCurrent;
        }

        private boolean protectedByPreviousManifest() {
            return protectedByPreviousManifest;
        }

        private ArtifactCategory category() {
            return category;
        }

        private boolean cleanupCandidate() {
            return cleanupCandidate;
        }

        private boolean deletedByCleanup() {
            return deletedByCleanup;
        }

        private void markDeleted() {
            this.deletedByCleanup = true;
        }
    }

    private record Reachability(
            LinkedHashSet<String> rootFiles,
            LinkedHashSet<String> requiredFiles) {
    }
}
