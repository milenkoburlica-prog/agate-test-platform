package at.co.svc.aga.transformator.utils;

import java.nio.file.Files;
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

    /** Backward-compatible summary: without export roots all issues remain relevant. */
    public static synchronized void printSummary() {
        printGrouped(new ArrayList<>(UNPROCESSED_MODULES), List.of(), List.of());
    }

    public static synchronized void printSummary(Path applicationRoot, String baseName) {
        UsedYamlGraph.Result graph=UsedYamlGraph.scan(applicationRoot,UsedYamlGraph.roots(applicationRoot,baseName));
        List<UnprocessedModule> relevant=new ArrayList<>(), informational=new ArrayList<>();
        for(UnprocessedModule issue:UNPROCESSED_MODULES) {
            boolean used=true;
            if(graph.complete() && !issue.yamlFile().equals("<unknown>")) {
                try {
                    Path issuePath=Paths.get(issue.yamlFile()).toAbsolutePath().normalize();
                    used=!issuePath.startsWith(applicationRoot.toAbsolutePath().normalize()) || graph.used().contains(issuePath);
                }
                catch(Exception e) { used=true; }
            }
            (used?relevant:informational).add(issue);
        }
        printGrouped(relevant,informational,graph.unresolved());
    }

    private static void printGrouped(List<UnprocessedModule> relevant,List<UnprocessedModule> informational,List<String> unresolved) {
        relevant = existingOrUnknownFiles(relevant);
        informational = existingOrUnknownFiles(informational);
        System.out.println("\n============================================================");
        System.out.println("TOSCA MIGRATION REVIEW");
        System.out.println("============================================================");
        for(String warning:unresolved)System.out.println("[WARNING] Dependency check incomplete: "+warning);
        if(!unresolved.isEmpty())System.out.println("All review items remain relevant until dependencies are resolved.");
        System.out.println("\nGROUP 2 - CURRENT EXPORT (USED): "+relevant.size());
        printItems(relevant,"ERROR");
        System.out.println("\nGROUP 1 - OUTSIDE CURRENT EXPORT: "+informational.size());
        printItems(informational,"INFO");
        System.out.println("\nRelevant review items      : "+relevant.size());
        System.out.println("Informational review items : "+informational.size());
        System.out.println(!relevant.isEmpty()?"Manual correction required for current export.":
                unresolved.isEmpty()?"[SUCCESS] No manual correction required for current export.":
                "[WARNING] Current export review incomplete until dependencies are resolved.");
        System.out.println("============================================================");
    }
    /** Check only when reporting: output files can be written after registration. */
    private static List<UnprocessedModule> existingOrUnknownFiles(List<UnprocessedModule> issues) {
        List<UnprocessedModule> result = new ArrayList<>();
        for (UnprocessedModule issue : issues) {
            boolean missing = false;
            if (!"<unknown>".equals(issue.yamlFile())) {
                try {
                    // Unknown existence (for example denied access) must retain the diagnostic.
                    missing = Files.notExists(Paths.get(issue.yamlFile()).toAbsolutePath().normalize());
                } catch (RuntimeException e) {
                    // An invalid or inaccessible path is not proof that output is absent.
                }
            }
            if (!missing) result.add(issue);
        }
        return result;
    }
    private static void printItems(List<UnprocessedModule> issues,String level) {
        int index=1;
        for(UnprocessedModule issue:issues) {
            System.out.println("\n["+level+"] Tosca module could not be migrated automatically");
            System.out.println("        Item      : "+index++);
            System.out.println("        Module    : "+issue.moduleName());
            System.out.println("        Step      : "+issue.stepName());
            System.out.println("        YAML file : "+issue.yamlFile());
        }
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
