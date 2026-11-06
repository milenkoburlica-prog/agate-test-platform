package at.co.svc.agate.core.allure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllureResultRewriter {

    public static void main(String[] args) throws Exception {
        Path resultsPath = Path.of("allure-results");
        if (!Files.exists(resultsPath)) return;

        ObjectMapper mapper = new ObjectMapper();

        Files.list(resultsPath)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        ObjectNode root = (ObjectNode) mapper.readTree(path.toFile());

                        // Processing RESULT files (where tests are located)
                        if (path.getFileName().toString().contains("result")) {
                            String fullName = root.has("name") ? root.get("name").asText() : "";

                            // Regex matching: (Application).(Suite.yaml).(TC_Name)
                            // Group 1: app + suite, Group 2: TC
                            Pattern pattern = Pattern.compile("^(.*?\\..*?\\.yaml)\\.(TC_.*)$");
                            Matcher matcher = pattern.matcher(fullName);

                            if (matcher.find()) {
                                String fullSuitePath = matcher.group(1); // opc-gui.chipkarte_vewaltung.yaml
                                String tcName = matcher.group(2);        // TC_11

                                // 1. Changing test name to just "TC_11"
                                root.put("name", tcName);

                                // 2. Updating labels
                                if (root.has("labels")) {
                                    ArrayNode labels = (ArrayNode) root.get("labels");
                                    for (JsonNode labelNode : labels) {
                                        ObjectNode label = (ObjectNode) labelNode;
                                        String labelName = label.get("name").asText();

                                        // Changing suite, testClass, and subSuite to "opc-gui.chipkarte_vewaltung.yaml"
                                        if ("suite".equals(labelName) || "testClass".equals(labelName) || "subSuite".equals(labelName)) {
                                            label.put("value", fullSuitePath);
                                        }

                                        // Fixing package to avoid GenericYamlTest
                                        if ("package".equals(labelName)) {
                                            String currentPkg = label.get("value").asText();
                                            if (currentPkg.contains("GenericYamlTest")) {
                                                // Setting application name as package (e.g. opc-gui)
                                                label.put("value", fullSuitePath.split("\\.")[0]);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Processing CONTAINER files (to avoid GenericYamlTest in title)
                        if (path.getFileName().toString().contains("container")) {
                            if (root.has("name") && root.get("name").asText().contains("GenericYamlTest")) {
                                // Giving container a general name or cleaning it
                                root.put("name", "YAML Execution Suite");
                            }
                        }

                        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);

                    } catch (Exception e) {
                        System.err.println("Error: " + path.getFileName() + " -> " + e.getMessage());
                    }
                });

        System.out.println("Dynamic rewrite completed.");
    }
}