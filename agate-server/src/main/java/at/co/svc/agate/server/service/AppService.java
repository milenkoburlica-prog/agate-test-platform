package at.co.svc.agate.server.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import at.co.svc.agate.core.dsl.model.SuiteDetails;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.YamlTestCaseLoader;
import at.co.svc.agate.server.dto.AppInfo;
import at.co.svc.agate.server.dto.ModuleContentResponse;
import at.co.svc.agate.server.dto.ModuleNode;
import at.co.svc.agate.server.dto.SaveResponse;
import at.co.svc.agate.server.dto.SuiteInfo;
import at.co.svc.agate.server.dto.SuiteResponse;
import at.co.svc.agate.server.dto.ValidationResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppService {

//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String getDataPath() {
        return System.getProperty("user.dir") + File.separator + "data";
    }

    public List<AppInfo> getApps() {
        List<AppInfo> apps = new ArrayList<>();
        File dataFolder = new File(getDataPath());

        if (dataFolder.exists() && dataFolder.isDirectory()) {
            File[] folders = dataFolder.listFiles(File::isDirectory);
            if (folders != null) {
                for (File folder : folders) {
                    String appId = folder.getName();
                    String appName = getAppNameFromDescription(folder).orElse(appId);
                    apps.add(new AppInfo(appId, appName));
                }
            }
        }
        return apps;
    }

    private Optional<String> getAppNameFromDescription(File folder) {
        File descFile = new File(folder, ".description");
        if (descFile.exists() && descFile.isFile()) {
            try (Stream<String> lines = Files.lines(descFile.toPath())) {
                return lines
                        .filter(line -> line.toLowerCase().startsWith("description:"))
                        .map(line -> line.substring(line.indexOf(":") + 1).trim())
                        .findFirst();
            } catch (IOException e) {
                System.err.println("Could not read .description for " + folder.getName());
            }
        }
        return Optional.empty();
    }

    public AppInfo createApp(AppInfo appInfo) throws IOException {
        File appFolder = new File(getDataPath(), appInfo.getName().trim());
        if (appFolder.exists()) {
            throw new IllegalArgumentException("Application already exists");
        }

        appFolder.mkdirs();
        new File(appFolder, "modules" + File.separator + "rest").mkdirs();
        new File(appFolder, "modules" + File.separator + "soap").mkdirs();
        new File(appFolder, "template").mkdirs();
        
        File descFile = new File(appFolder, ".description");
        Files.writeString(descFile.toPath(), "description: " + appInfo.getName());

        return new AppInfo(appInfo.getName(), appInfo.getName());
    }

    public AppInfo renameApp(String appId, AppInfo newAppInfo) throws IOException {
        File oldFolder = new File(getDataPath(), appId);
        File newFolder = new File(getDataPath(), newAppInfo.getName().trim());

        if (!oldFolder.exists() || !oldFolder.isDirectory()) {
            throw new IllegalArgumentException("App not found");
        }
        if (newFolder.exists()) {
            throw new IllegalArgumentException("New name already in use");
        }

        if (oldFolder.renameTo(newFolder)) {
            File descFile = new File(newFolder, ".description");
            Files.writeString(descFile.toPath(), "description: " + newAppInfo.getName());
            return new AppInfo(newAppInfo.getName(), newAppInfo.getName());
        }
        throw new RuntimeException("Failed to rename folder");
    }

    public void deleteApp(String appId) throws IOException {
        File appFolder = new File(getDataPath(), appId);
        if (!appFolder.exists()) {
            throw new IllegalArgumentException("App folder does not exist");
        }

        Files.walk(appFolder.toPath())
            .sorted(java.util.Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(file -> {
                if (!file.delete()) {
                    throw new RuntimeException("Failed to delete: " + file.getAbsolutePath());
                }
            });
    }

    public List<SuiteInfo> getSuites(String appId) {
        List<SuiteInfo> suites = new ArrayList<>();
        File appFolder = new File(getDataPath(), appId);

        if (appFolder.exists() && appFolder.isDirectory()) {
            File[] yamlFiles = appFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yaml"));
            if (yamlFiles != null) {
                for (File file : yamlFiles) {
                    suites.add(new SuiteInfo(file.getName()));
                }
            }
        }
        return suites;
    }

    public SuiteDetails getSuiteDetails(String appId, String fileName) throws Exception {
        System.setProperty("APPLICATION", appId);
        String yamlFilePath = "data" + File.separator + appId + File.separator + fileName;
        List<TestCase> testCases = YamlTestCaseLoader.loadTestCases(yamlFilePath);
        return new SuiteDetails(fileName, testCases);
    }

    public SuiteInfo createSuite(String appId, SuiteInfo suiteInfo) throws IOException {
        String name = suiteInfo.getId().trim();
        if (!name.endsWith(".yaml")) {
            name += ".yaml";
        }

        File targetFile = new File(getDataPath() + File.separator + appId, name);
        if (targetFile.exists()) {
            throw new IllegalArgumentException("Suite already exists");
        }

        String initialYaml = 
            "# CLI Command Execution Demo\n" +
            "testCases:\n" +
            "  - id: TC_NEW_01\n" +
            "    description: New TC Created with Client\n" +
            "    stage: INT\n" +
            "    priority: HIGH\n" +
            "    variables:\n" +
            "      instance: \"{E[env.index]}\"\n" +
            "    steps:\n" +
            "      - type: CMD\n" +
            "        op: EXEC\n" +
            "        command: echo \"{B[instance]}\"\n" +
            "        response: test1";

        Files.writeString(targetFile.toPath(), initialYaml);
        return new SuiteInfo(name);
    }

    public SuiteInfo renameSuite(String appId, String oldFileName, SuiteInfo newSuiteInfo) {
        String appDirPath = getDataPath() + File.separator + appId;
        File oldFile = new File(appDirPath, oldFileName);
        
        String newName = newSuiteInfo.getId().trim();
        if (!newName.endsWith(".yaml")) newName += ".yaml";
        File newFile = new File(appDirPath, newName);

        if (!oldFile.exists()) throw new IllegalArgumentException("File not found");
        if (newFile.exists()) throw new IllegalArgumentException("Name already in use");

        if (oldFile.renameTo(newFile)) {
            return new SuiteInfo(newName);
        }
        throw new RuntimeException("Failed to rename file");
    }

    public void deleteSuite(String appId, String fileName) {
        File fileToDelete = new File(getDataPath() + File.separator + appId, fileName);
        if (!fileToDelete.exists()) throw new IllegalArgumentException("File not found");

        if (!fileToDelete.delete()) {
            throw new RuntimeException("Could not delete file");
        }
    }

    public SaveResponse saveSuite(String appId, String fileName, SuiteResponse suiteRequest) throws IOException {
        File directory = new File(getDataPath() + File.separator + appId);
        if (!directory.exists()) directory.mkdirs();

        File targetFile = new File(directory, fileName);
        List<Map<String, Object>> orderedTestCases = convertToOrderedMap(suiteRequest.content);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(targetFile)) {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("testCases", orderedTestCases);
            yaml.dump(root, writer);
        }

        return new SaveResponse("SUCCESS", "Suite " + fileName + " saved successfully", "v1", OffsetDateTime.now().toString());
    }

    public ValidationResponse validateSuite(String appId, SuiteResponse suiteRequest) {
        ValidationResponse vr = new ValidationResponse(true);
        if (suiteRequest.content == null || suiteRequest.content.isEmpty()) {
            vr.addError("content", "Suite content is missing.");
            return vr;
        }

        File tempYaml = null;
        try {
            tempYaml = File.createTempFile("validate_" + appId + "_", ".yaml");
            List<Map<String, Object>> orderedTestCases = convertToOrderedMap(suiteRequest.content);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);

            try (FileWriter writer = new FileWriter(tempYaml)) {
                Map<String, Object> root = new LinkedHashMap<>();
                root.put("testCases", orderedTestCases);
                yaml.dump(root, writer);
            }

            YamlTestCaseLoader.loadTestCases(tempYaml.getAbsolutePath());
        } catch (Exception e) {
            vr.addError("yaml.parsing", "Loader Error: " + e.getMessage());
        } finally {
            if (tempYaml != null && tempYaml.exists()) {
                tempYaml.delete();
            }
        }
        return vr;
    }

    public List<ModuleNode> getModulesExplorer(String appId) {
        String modulesPath = getDataPath() + File.separator + appId + File.separator + "modules";
        File modulesFolder = new File(modulesPath);
        
        List<ModuleNode> nodes = new ArrayList<>();
        if (modulesFolder.exists() && modulesFolder.isDirectory()) {
            scanDirectory(modulesFolder, nodes, "");
        }
        return nodes;
    }

    private void scanDirectory(File folder, List<ModuleNode> nodeList, String prefix) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                String fullName = prefix.isEmpty() ? f.getName() : prefix + "." + f.getName();
                boolean isModule = new File(f, "metadata.json").exists();
                ModuleNode node = new ModuleNode(f.getName(), fullName, isModule ? "module" : "folder");
                node.setFullName(fullName);
                nodeList.add(node);

                if (!isModule) {
                    scanDirectory(f, node.children, fullName);
                }
            }
        }
    }

    public ModuleContentResponse getModuleDetails(String appId, String moduleName) {
        String folderPathStructure = moduleName.replace(".", File.separator);
        Path modulePath = Paths.get(getDataPath(), appId, "modules", folderPathStructure);
        File folder = modulePath.toFile();

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Module not found");
        }

        String metadata = getFileContent(folder, "metadata");
        String request = getFileContent(folder, "request");

        return new ModuleContentResponse(moduleName, metadata, request);
    }

    public void createModule(String appId, String path) throws IOException {
        String systemPath = path.replace(".", File.separator).replace("/", File.separator);
        File targetFolder = new File(getDataPath() + File.separator + appId + File.separator + "modules", systemPath);

        if (targetFolder.exists()) {
            throw new IllegalArgumentException("Module already exists");
        }

        targetFolder.mkdirs();
        Files.writeString(new File(targetFolder, "metadata.json").toPath(), "{\n  \"url\": \"\",\n  \"method\": \"POST\"\n}");
        Files.writeString(new File(targetFolder, "request.json").toPath(), "{}");
    }

    public void deleteModule(String appId, String modulePath) throws IOException {
        String systemPath = modulePath.replace(".", File.separator);
        Path pathToBeDeleted = Paths.get(getDataPath(), appId, "modules", systemPath);
        File folder = pathToBeDeleted.toFile();

        if (!folder.exists()) throw new IllegalArgumentException("Module not found");

        Files.walk(pathToBeDeleted)
            .sorted(java.util.Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    public void renameModule(String appId, String oldPath, String newPath) throws IOException {
        String modulesDataPath = getDataPath() + File.separator + appId + File.separator + "modules";
        File oldFolder = new File(modulesDataPath, oldPath.replace(".", File.separator));
        File newFolder = new File(modulesDataPath, newPath.replace(".", File.separator));

        if (!oldFolder.exists()) throw new IllegalArgumentException("Source module/folder not found");
        if (newFolder.exists()) throw new IllegalArgumentException("Target name already exists");

        File parent = newFolder.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!oldFolder.renameTo(newFolder)) {
            throw new RuntimeException("Failed to rename module");
        }
    }

    // Pomoćne metode za mapiranje
    private List<Map<String, Object>> convertToOrderedMap(List<TestCase> testCases) {
        List<Map<String, Object>> orderedTestCases = new ArrayList<>();
        for (TestCase tc : testCases) {
            Map<String, Object> tcMap = new LinkedHashMap<>();
            if (tc.getName() != null) tcMap.put("id", tc.getName()); 
            if (tc.getDescription() != null) tcMap.put("description", tc.getDescription());
            if (tc.getStage() != null) tcMap.put("stage", tc.getStage());
            if (tc.getPriority() != null) tcMap.put("priority", tc.getPriority());
            if (tc.getVariables() != null && !tc.getVariables().isEmpty()) {
                tcMap.put("variables", tc.getVariables());
            }
            if (tc.getSteps() != null && !tc.getSteps().isEmpty()) {
                tcMap.put("steps", processSteps(tc.getSteps()));
            }
            orderedTestCases.add(tcMap);
        }
        return orderedTestCases;
    }

    private List<Map<String, Object>> processSteps(List<TestStep> steps) {
        List<Map<String, Object>> orderedSteps = new ArrayList<>();
        for (TestStep step : steps) {
            Map<String, Object> stepMap = new LinkedHashMap<>();
            if (step.getType() != null) stepMap.put("type", step.getType().name());
            if (step.getOp() != null) stepMap.put("op", step.getOp());
            if (step.getAction() != null) stepMap.put("action", step.getAction());
            if (step.getParameters() != null && !step.getParameters().isEmpty()) {
                stepMap.put("parameters", step.getParameters());
            }
            if (step.getSubSteps() != null && !step.getSubSteps().isEmpty()) {
                stepMap.put("subSteps", processSteps(step.getSubSteps()));
            }
            if (step.getCommand() != null) stepMap.put("command", step.getCommand());
            if (step.getResponse() != null) stepMap.put("response", step.getResponse());
            if (step.getValue() != null) stepMap.put("value", step.getValue());
            if (step.getName() != null) stepMap.put("name", step.getName());
            if (step.getCondition() != null) stepMap.put("condition", step.getCondition());
            if (step.getSource() != null) stepMap.put("source", step.getSource());
            if (step.getSelector() != null) stepMap.put("selector", step.getSelector());
            if (step.getRow() != null) stepMap.put("row", step.getRow());
            if (step.getColumn() != null) stepMap.put("column", step.getColumn());
            if (step.getExpected() != null) stepMap.put("expected", step.getExpected());
            if (step.getPod() != null) stepMap.put("pod", step.getPod());
            if (step.getNamespace() != null) stepMap.put("namespace", step.getNamespace());
            if (step.getFrom() != null) stepMap.put("from", step.getFrom());
            if (step.getTo() != null) stepMap.put("to", step.getTo());
            if (step.getPath() != null) stepMap.put("path", step.getPath());
            if (step.getRequired() != null) stepMap.put("required", step.getRequired());

            orderedSteps.add(stepMap);
        }
        return orderedSteps;
    }

    private String getFileContent(File folder, String fileNameWithoutExtension) {
        try {
            Path jsonPath = folder.toPath().resolve(fileNameWithoutExtension + ".json");
            if (Files.exists(jsonPath)) return Files.readString(jsonPath);
            Path xmlPath = folder.toPath().resolve(fileNameWithoutExtension + ".xml");
            if (Files.exists(xmlPath)) return Files.readString(xmlPath);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "";
    }
}