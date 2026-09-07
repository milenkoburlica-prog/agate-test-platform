package at.co.svc.tosca.tsu.cleaner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class ReusableDependencyAnalyzer {

    private final Map<String, String> reusableRegistry = new LinkedHashMap<>();
    private final Set<String> requiredModulesRegistry = new TreeSet<>();
    private final String baseAppPath;

    public static void main(String[] args) {
        if (args.length == 0) {
            String path = "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\migration\\data\\MUHI";
            MigrationLog.debug("[Java - IDE] Running local test for path: " + path);
            runAnalysis(path);
            return;
        }

        String command = args[0];
        
        if ("clean".equalsIgnoreCase(command)) {
            if (args.length < 2) {
                MigrationLog.error("[Java - ERROR] For 'clean' command you must provide the application name (e.g., MUHI)!");
                System.exit(1);
            }
            
            String appName = args[1];
            String path = System.getProperty("user.dir") + File.separator + "migration" + File.separator + "data" + File.separator + appName;
            
            MigrationLog.debug("[Java - BAT] Running cleanup for application: " + appName);
            MigrationLog.debug("[Java - BAT] Calculated path: " + path);
            
            runAnalysis(path);
        } else {
            MigrationLog.debug("[Java] Received command '" + command + "'. Executing existing migration logic...");
        }
    }

    private static void runAnalysis(String path) {
        ReusableDependencyAnalyzer analyzer = new ReusableDependencyAnalyzer(path);
        try {
            analyzer.analyze();
        } catch (Exception e) {
            MigrationLog.error("Analysis aborted due to error: " + e.getMessage());
            MigrationLog.error("Analysis failed: " + e);
        }
    }
    
    public ReusableDependencyAnalyzer(String baseAppPath) {
        this.baseAppPath = baseAppPath;
    }

    public void analyze() throws Exception {
        MigrationLog.debug("=== Starting analysis for location: " + baseAppPath + " ===");

        File templateDir = new File(baseAppPath, "template");
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            throw new IllegalArgumentException("Error: 'template' folder does not exist at: " + templateDir.getAbsolutePath());
        }

        List<File> yamlFiles = Arrays.stream(Optional.ofNullable(templateDir.listFiles()).orElse(new File[0]))
                .filter(f -> f.isFile() && (f.getName().endsWith(".yaml") || f.getName().endsWith(".yml")))
                .collect(Collectors.toList());

        if (yamlFiles.isEmpty()) {
            throw new IllegalStateException("Error: No YAML files found in folder: " + templateDir.getAbsolutePath());
        }
        if (yamlFiles.size() > 1) {
            throw new IllegalStateException("Error: More than one YAML file found in folder: " + templateDir.getAbsolutePath());
        }

        File mainTemplateFile = yamlFiles.get(0);
        preprocessAndSaveYaml(mainTemplateFile);
        MigrationLog.debug("[INFO] Analyzing initial template: " + mainTemplateFile.getName());

        extractCallsFromFile(mainTemplateFile);

        int iteration = 1;
        final int MAX_ITERATIONS = 10;
        boolean dynamicElementsFound = true;

        while (iteration <= MAX_ITERATIONS && dynamicElementsFound) {
            List<String> uncheckedModules = reusableRegistry.entrySet().stream()
                    .filter(entry -> "no".equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (uncheckedModules.isEmpty()) {
                dynamicElementsFound = false;
                break;
            }

            MigrationLog.debug(String.format("[Iteration %d] Processing %d unchecked modules...", iteration, uncheckedModules.size()));

            for (String moduleDotNotation : uncheckedModules) {
                File targetYamlFile = resolveDotNotationToPath(moduleDotNotation);

                if (targetYamlFile != null && targetYamlFile.exists()) {
                    reusableRegistry.put(moduleDotNotation, "yes");
                    extractCallsFromFile(targetYamlFile);
                } else {
                    MigrationLog.warn("File for module '" + moduleDotNotation + "' not found.");
                    reusableRegistry.put(moduleDotNotation, "missing_file");
                }
            }

            iteration++;
        }

        if (iteration > MAX_ITERATIONS) {
            MigrationLog.warn("Reached the maximum limit of 10 iterations.");
        }

        //printFinalRegistry();
        cleanupUnusedReusableFiles();
        collectRequiredModules(mainTemplateFile);
        cleanupUnusedModulesFolders();
    }

    private File resolveDotNotationToPath(String dotNotation) {
        if (!dotNotation.startsWith("reusable.")) {
            return null;
        }
        String fileName = dotNotation.substring("reusable.".length()) + ".yaml";
        return Paths.get(baseAppPath, "reusable", fileName).toFile();
    }

    private void collectRequiredModules(File mainTemplateFile) {
        MigrationLog.section(
                "PHASE 2: COLLECTING REQUIRED SOAP/REST MODULES"
        );

        extractModulesFromFile(mainTemplateFile);

        File reusableDir = new File(baseAppPath, "reusable");
        if (reusableDir.exists() && reusableDir.isDirectory()) {
            File[] files = reusableDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                        extractModulesFromFile(file);
                    }
                }
            }
        }

        printRequiredModules();
    }

    private void cleanupUnusedModulesFolders() {
        MigrationLog.section(
                "PHASE 3: CLEANING UNUSED MODULE FOLDERS"
        );

        File modulesDir = new File(baseAppPath, "modules");
        if (!modulesDir.exists() || !modulesDir.isDirectory()) {
            MigrationLog.info("[INFO] 'modules' folder does not exist. Skipping module cleanup.");
            return;
        }

        List<File> leafFolders = new ArrayList<>();
        findLeafModuleFolders(modulesDir, leafFolders);

        int deletedFoldersCount = 0;

        for (File folder : leafFolders) {
            String relativePath = modulesDir.toURI().relativize(folder.toURI()).getPath();
            
            if (relativePath.endsWith("/")) {
                relativePath = relativePath.substring(0, relativePath.length() - 1);
            }
            String moduleDotNotation = relativePath.replace("/", ".").replace("\\", ".");

            if (!requiredModulesRegistry.contains(moduleDotNotation)) {
                try {
                    deleteDirectoryRecursively(folder);
                    MigrationLog.info(String.format(" [DELETED FOLDER] -> modules%s%s (%s)", File.separator, relativePath, moduleDotNotation));
                    deletedFoldersCount++;
                } catch (Exception e) {
                    MigrationLog.error("  # Error deleting folder " + folder.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

        MigrationLog.info("----------------------------------------------------------------------");
        MigrationLog.info(String.format(" Module cleanup finished! Successfully deleted folders: %d", deletedFoldersCount));
        MigrationLog.info("======================================================================\n");
    }

    private void findLeafModuleFolders(File currentFolder, List<File> leafFolders) {
        File[] subFiles = currentFolder.listFiles();
        if (subFiles == null) return;

        boolean hasSubFolders = false;
        for (File file : subFiles) {
            if (file.isDirectory()) {
                hasSubFolders = true;
                findLeafModuleFolders(file, leafFolders);
            }
        }

        if (!hasSubFolders) {
            leafFolders.add(currentFolder);
        }
    }

    private void deleteDirectoryRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursively(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    // Text-based reusable-call extraction replacing the YAML parser.
    private void extractCallsFromFile(File file) {
        MigrationLog.debug("[ANALYZER] Reading reusable calls from: " + file.getName());
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            boolean isInsideCallBlock = false;

            for (String line : lines) {
                String trimmed = line.trim();
                
                // When a new list item starts, determine its type.
                if (trimmed.startsWith("- type:") || trimmed.startsWith("type:")) {
                    String typeValue = trimmed.substring(trimmed.indexOf(":") + 1).trim().replace("'", "").replace("\"", "");
                    isInsideCallBlock = "CALL".equals(typeValue);
                }
                
                // Inside a CALL block, read the command value.
                if (isInsideCallBlock && trimmed.startsWith("command:")) {
                    String commandValue = trimmed.substring(trimmed.indexOf(":") + 1).trim();
                    commandValue = commandValue.replace("'", "").replace("\"", "");
                    
                    if (!commandValue.isEmpty()) {
                        reusableRegistry.putIfAbsent(commandValue, "no");
                    }
                }
            }
        } catch (Exception e) {
            MigrationLog.error("Reusable text analysis failed for file: " + file.getName() + " -> " + e.getMessage());
        }
    }

    // Text-based SOAP/REST module extraction replacing the YAML parser.
    private void extractModulesFromFile(File file) {
        MigrationLog.debug("[ANALYZER] Reading SOAP/REST modules from: " + file.getName());
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            boolean isInsideModuleBlock = false;

            for (String line : lines) {
                String trimmed = line.trim();

                // Normalize hidden NBSP characters while reading the file.
                if (trimmed.contains("\u00A0")) {
                    trimmed = trimmed.replace("\u00A0", " ");
                }

                if (trimmed.startsWith("- type:") || trimmed.startsWith("type:")) {
                    String typeValue = trimmed.substring(trimmed.indexOf(":") + 1).trim().replace("'", "").replace("\"", "");
                    isInsideModuleBlock = "SOAP".equals(typeValue) || "REST".equals(typeValue);
                }

                if (isInsideModuleBlock && trimmed.startsWith("command:")) {
                    String commandValue = trimmed.substring(trimmed.indexOf(":") + 1).trim();
                    commandValue = commandValue.replace("'", "").replace("\"", "");

                    if (!commandValue.isEmpty()) {
                        requiredModulesRegistry.add(commandValue);
                    }
                }
            }
        } catch (Exception e) {
            MigrationLog.error("Module text analysis failed for file: " + file.getName() + " -> " + e.getMessage());
        }
    }

    private void printRequiredModules() {
        MigrationLog.info("FOUND REQUIRED MODULES:");
        if (requiredModulesRegistry.isEmpty()) {
            MigrationLog.info(" (No SOAP or REST modules found in remaining files)");
        } else {
            for (String module : requiredModulesRegistry) {
                String folderStructure = module.replace(".", File.separator);
                String expectedFolderPath = Paths.get(baseAppPath, "modules", folderStructure).toString();
                MigrationLog.info(String.format(" -> Module: %s", module));
                MigrationLog.info(String.format("    Folder: %s", expectedFolderPath));
                
            }
        }
        MigrationLog.info("======================================================================\n");
    }

    private void cleanupUnusedReusableFiles() {
        MigrationLog.section(
                "CLEANING UNUSED REUSABLE FILES"
        );

        File reusableDir = new File(baseAppPath, "reusable");
        if (!reusableDir.exists() || !reusableDir.isDirectory()) {
            MigrationLog.info("[INFO] 'reusable' folder does not exist. Skipping cleanup.");
            return;
        }

        File[] files = reusableDir.listFiles();
        if (files == null || files.length == 0) return;

        int deletedCount = 0;
        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                String pureName = file.getName().substring(0, file.getName().lastIndexOf("."));
                String expectedDotNotation = "reusable." + pureName;

                if (!reusableRegistry.containsKey(expectedDotNotation)) {
                    if (file.delete()) {
                        MigrationLog.info(String.format(" [DELETED] -> %s", file.getName()));
                        deletedCount++;
                    }
                }
            }
        }
        MigrationLog.info("----------------------------------------------------------------------");
        MigrationLog.info(String.format(" Cleanup finished! Successfully deleted files: %d", deletedCount));
        MigrationLog.info("======================================================================\n");
    }

    private void preprocessAndSaveYaml(File file) {
        MigrationLog.debug("[PRE-SANITY] Reading and sanitizing file: " + file.getName());
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            List<String> sanitizedLines = new ArrayList<>();
            int conditionCounter = 0;
            int removedCounter = 0;
            boolean fileModified = false;

            for (String line : lines) {
                String trimmed = line.trim();
                
                if (trimmed.startsWith("condition:") || trimmed.startsWith("- condition:")) {
                    conditionCounter++;
                    int colonIndex = line.indexOf(":");
                    String prefix = line.substring(0, colonIndex + 1);
                    String value = line.substring(colonIndex + 1).trim();

                    if (value.isEmpty() || "\"\"".equals(value) || "''".equals(value)) {
                        MigrationLog.info(String.format("  -> [REMOVED CONDITION #%d] Empty condition line removed.", conditionCounter));
                        removedCounter++;
                        fileModified = true;
                        continue; 
                    }

                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        sanitizedLines.add(line);
                        continue;
                    }

                    MigrationLog.info(String.format("  -> [UNQUOTED CONDITION #%d]", conditionCounter));
                    MigrationLog.info("     Original line: " + line);

                    String newLine = prefix + " '" + value + "'";
                    MigrationLog.info("     Updated line:  " + newLine);
                    
                    line = newLine;
                    fileModified = true;
                }
                sanitizedLines.add(line);
            }

            if (fileModified) {
                Files.write(file.toPath(), sanitizedLines, StandardCharsets.UTF_8);
                MigrationLog.info("[PRE-SANITY] File " + file.getName() + " was updated on disk.");
            } else {
                MigrationLog.info("[PRE-SANITY] No changes required for file " + file.getName());
            }
            MigrationLog.info(String.format("[PRE-SANITY] Completed. Processed conditions: %d, removed empty conditions: %d\n", 
                    conditionCounter, removedCounter));

        } catch (Exception e) {
            MigrationLog.error("Pre-sanity phase failed for file " + file.getName() + ": " + e.getMessage());
        }
    }    
}