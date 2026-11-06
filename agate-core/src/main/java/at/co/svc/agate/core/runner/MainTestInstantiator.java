package at.co.svc.agate.core.runner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import at.co.svc.agate.core.dsl.register.YamlTestInstantiator;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.project.ProjectConfig;
import at.co.svc.agate.core.project.ProjectConfigLoader;
import at.co.svc.agate.core.project.ProjectConfigValidator;
import at.co.svc.agate.core.project.ProjectContext;
import at.co.svc.agate.core.project.ProjectLocator;
import at.co.svc.agate.core.project.ProjectRuntime;

public class MainTestInstantiator {

    public static void main(String[] args) {

        String appName =
                "MUHI1";

        String templateFile =
                "anspruchPruefen.yaml";

        String dataFile =
                "MUHI1_T_anspruchPruefen.csv";

        start(
                appName,
                templateFile,
                dataFile);
    }

    private static void start(
            String appName,
            String templateFile,
            String dataFile) {

        System.out.println(
                "=".repeat(80));

        System.out.println(
                "             BATCH TEST CASE INSTANTIATION");

        System.out.println(
                "=".repeat(80));

        try {

            ProjectLocator.LocatedProject locatedProject =
                    ProjectLocator.locate(null);

            ProjectConfig projectConfig =
                    ProjectConfigLoader.load(
                            locatedProject.projectFile());

            ProjectContext projectContext =
                    new ProjectContext(
                            locatedProject.projectRoot(),
                            locatedProject.projectFile(),
                            projectConfig);

            ProjectConfigValidator.validateOrThrow(
                    projectContext);

            ProjectRuntime.initialize(
                    projectContext);

            Path appPath =
                    ProjectRuntime
                            .applicationRoot(appName)
                            .normalize();

            cleanupOldInstances(
                    appPath);

            YamlTestInstantiator instantiator =
                    new YamlTestInstantiator();

            instantiator.instantiate(
                    appName,
                    templateFile,
                    dataFile);

            sanitizeAgateServerInstances(
                    appPath);

            System.out.println(
                    "=".repeat(80));

            System.out.println(
                    "  STATUS : "
                            + ConsoleColors.GREEN
                            + "ALL CASES GENERATED & CLEANED IN AGATE-SERVER"
                            + ConsoleColors.RESET);

        } catch (Exception e) {

            System.err.println(
                    "\n"
                            + ConsoleColors.RED
                            + "FATAL ERROR:"
                            + ConsoleColors.RESET);

            e.printStackTrace();

            System.exit(1);
        }
    }

    private static void sanitizeAgateServerInstances(
            Path appPath) {

        if (!Files.exists(appPath)) {

            System.out.println(
                    "[WARNING] AGATE application path does not exist: "
                            + appPath);

            return;
        }

        try (var stream =
                     Files.walk(
                             appPath,
                             1)) {

            List<Path> instances =
                    stream
                            .filter(
                                    Files::isRegularFile)
                            .filter(p ->
                                    p.getFileName()
                                            .toString()
                                            .startsWith("Instance_")
                                            && p.getFileName()
                                                    .toString()
                                                    .endsWith(".yaml"))
                            .toList();

            for (Path instancePath :
                    instances) {

                sanitizeYamlFile(
                        instancePath);
            }

            System.out.println(
                    "[INFO] Agate-server instance cleanup finished successfully.");

        } catch (Exception e) {

            System.err.println(
                    "[ERROR] Failed to clean up agate-server files: "
                            + e.getMessage());
        }
    }

    private static void sanitizeYamlFile(
            Path filePath) {

        try {

            List<String> lines =
                    Files.readAllLines(
                            filePath,
                            StandardCharsets.UTF_8);

            List<String> sanitizedLines =
                    new ArrayList<>();

            boolean fileModified =
                    false;

            for (String line :
                    lines) {

                String trimmed =
                        line.trim();

                if (trimmed.startsWith("condition:")) {

                    int colonIndex =
                            line.indexOf(":");

                    String prefix =
                            line.substring(
                                    0,
                                    colonIndex + 1);

                    String value =
                            line.substring(
                                            colonIndex + 1)
                                    .trim();

                    if (value.isEmpty()
                            || "\"\"".equals(value)
                            || "''".equals(value)) {

                        fileModified =
                                true;

                        continue;
                    }

                    if (value.startsWith("\"")
                            && value.endsWith("\"")
                            && !value.contains("'''")) {

                        sanitizedLines.add(
                                line);

                        continue;
                    }

                    if (value.startsWith("'''")
                            && value.endsWith("'''")
                            && value.length() > 3) {

                        value =
                                value.substring(
                                        3,
                                        value.length() - 3);

                    } else if (value.startsWith("'")
                            && value.endsWith("'")
                            && value.length() > 1) {

                        value =
                                value.substring(
                                        1,
                                        value.length() - 1);
                    }

                    value =
                            value.replace(
                                    "''",
                                    "'");

                    line =
                            prefix
                                    + " \""
                                    + value
                                    + "\"";

                    fileModified =
                            true;
                }

                sanitizedLines.add(
                        line);
            }

            if (fileModified) {

                Files.write(
                        filePath,
                        sanitizedLines,
                        StandardCharsets.UTF_8);

                System.out.println(
                        "[SANITY] Successfully sanitized file: "
                                + filePath.getFileName());
            }

        } catch (Exception e) {

            System.err.println(
                    "[WARNING] Error sanitizing file "
                            + filePath.getFileName()
                            + ": "
                            + e.getMessage());
        }
    }

    private static void cleanupOldInstances(
            Path appPath)
            throws Exception {

        if (!Files.exists(appPath)) {
            return;
        }

        System.out.println(
                "[INFO] Cleaning up old instances in: "
                        + appPath);

        try (var stream =
                     Files.walk(
                             appPath,
                             1)) {

            List<Path> instances =
                    stream
                            .filter(
                                    Files::isRegularFile)
                            .filter(p ->
                                    p.getFileName()
                                            .toString()
                                            .startsWith("Instance_")
                                            && p.getFileName()
                                                    .toString()
                                                    .endsWith(".yaml"))
                            .toList();

            for (Path instance :
                    instances) {

                Files.deleteIfExists(
                        instance);
            }
        }

        System.out.println(
                "[INFO] Cleanup finished.");
    }
}
