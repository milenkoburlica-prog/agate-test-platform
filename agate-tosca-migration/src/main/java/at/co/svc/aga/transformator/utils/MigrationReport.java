package at.co.svc.aga.transformator.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MigrationReport {

    private static final Set<UnprocessedModule> UNPROCESSED_MODULES =
            new LinkedHashSet<>();

    private MigrationReport() {
    }

    public static synchronized void reset() {
        UNPROCESSED_MODULES.clear();
    }

    public static synchronized void registerUnprocessedModule(
            String moduleName,
            String stepName,
            String yamlFile) {

        UNPROCESSED_MODULES.add(
                new UnprocessedModule(
                        safe(moduleName),
                        safe(stepName),
                        normalizePath(yamlFile)
                )
        );
    }

    public static synchronized boolean hasUnprocessedModules() {
        return !UNPROCESSED_MODULES.isEmpty();
    }

    public static synchronized int getUnprocessedModuleCount() {
        return UNPROCESSED_MODULES.size();
    }

    public static synchronized List<UnprocessedModule> getUnprocessedModules() {
        return new ArrayList<>(UNPROCESSED_MODULES);
    }

    public static synchronized void printSummary() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("TOSCA MIGRATION REVIEW");
        System.out.println("============================================================");

        if (UNPROCESSED_MODULES.isEmpty()) {
            System.out.println("[SUCCESS] No unprocessed Tosca modules found.");
            System.out.println("============================================================");
            return;
        }

        int index = 1;

        for (UnprocessedModule issue : UNPROCESSED_MODULES) {

            System.out.println();
            System.out.println(
                    "[ERROR] Tosca module could not be migrated automatically"
            );
            System.out.println("        Item      : " + index);
            System.out.println("        Module    : " + issue.moduleName());
            System.out.println("        Step      : " + issue.stepName());
            System.out.println("        YAML file : " + issue.yamlFile());

            index++;
        }

        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println(
                "Unprocessed Tosca modules : " + UNPROCESSED_MODULES.size()
        );
        System.out.println("Manual correction required.");
        System.out.println("============================================================");
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "<unknown>";
        }
        return value;
    }

    private static String normalizePath(String yamlFile) {

        if (yamlFile == null || yamlFile.isBlank()) {
            return "<unknown>";
        }

        try {
            Path filePath =
                    Paths.get(yamlFile)
                            .normalize()
                            .toAbsolutePath();

            Path userDir =
                    Paths.get(System.getProperty("user.dir"))
                            .normalize()
                            .toAbsolutePath();

            if (filePath.startsWith(userDir)) {
                return userDir
                        .relativize(filePath)
                        .toString()
                        .replace("\\", "/");
            }

            return filePath
                    .toString()
                    .replace("\\", "/");

        } catch (Exception e) {
            return yamlFile.replace("\\", "/");
        }
    }

    public record UnprocessedModule(
            String moduleName,
            String stepName,
            String yamlFile) {
    }
}
