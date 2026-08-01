package at.co.svc.agate.engine.json;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

public class JsonEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.JSON;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, 
                          int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {
        
        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = step.getOp() != null ? step.getOp().toUpperCase() : "";

        switch (op) {
            case "ASSERT":
                handleAssert(tc, step, yamlFile, stepIndex, logger, isVerbose);
                break;
            default:
                throw new RuntimeException("Unsupported operation for JsonEngine: " + op);
        }
    }

    private void handleAssert(TestCase tc, TestStep step, String yamlFile, int stepIndex, TestLogger logger, boolean isVerbose) throws Exception {

        String filePath = step.getFile();
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("JSON ASSERT operation requires 'file' parameter to be defined.");
        }

        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            throw new RuntimeException("JSON file not found at path: " + filePath);
        }

        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        Map<String, Object> parameters = step.getParameters();

        if (parameters == null || parameters.isEmpty()) {
            throw new RuntimeException("JSON ASSERT operation requires 'parameters' (path/value pairs) to be defined.");
        }

        logger.info("    Checking JSON assertions for file: " + filePath);

        JsonElement rootElement = JsonParser.parseString(jsonContent);

        boolean hasErrors = false;
        StringBuilder errorSummary = new StringBuilder();

        int counter = 1;
        while (true) {
            String pathKey = "path" + counter;
            String valueKey = "value" + counter;

            if (!parameters.containsKey(pathKey)) {
                break;
            }

            String jsonPathExpression = String.valueOf(parameters.get(pathKey));
            Object rawExpectedValue = parameters.get(valueKey);
            String expectedStr = (rawExpectedValue != null) ? rawExpectedValue.toString() : "";

            String expectedResolved = resolver.resolve(tc, expectedStr, tc.getVariables(), yamlFile, stepIndex, "URL");

            try {
                String actualResolved = getValueByPath(rootElement, jsonPathExpression);

                boolean isMatch = false;
                if (expectedResolved.contains("*")) {
                    isMatch = actualResolved.matches(expectedResolved);
                } else {
                    isMatch = actualResolved.equals(expectedResolved);
                }

                if (isMatch) {
                    logger.info("        " + ConsoleColors.GREEN + "[PASSED]" + ConsoleColors.RESET + 
                                " " + jsonPathExpression + " == \"" + expectedResolved + "\"");
                } else {
                    hasErrors = true;
                    
                    logger.info("        " + ConsoleColors.RED + "[ERROR] " + ConsoleColors.RESET + 
                                " " + jsonPathExpression + " == \"" + actualResolved + "\", expected \"" + expectedResolved + "\"");
                    
                    errorSummary.append(String.format("\n  - Path [%s] failed! Expected: [%s], Actual: [%s]", 
                            jsonPathExpression, expectedResolved, actualResolved));
                }

            } catch (Exception e) {
                hasErrors = true;
                logger.info("        " + ConsoleColors.RED + "[ERROR] " + ConsoleColors.RESET + 
                            " " + jsonPathExpression + " -> Evaluation failed: " + e.getMessage());
                errorSummary.append(String.format("\n  - Path [%s] crashed during evaluation! Reason: %s", 
                        jsonPathExpression, e.getMessage()));
            }

            counter++;
        }
        
        if (hasErrors) {
            String finalErrorMsg = String.format("JSON Assertion failed for file: %s%s", filePath, errorSummary.toString());
            throw new RuntimeException(finalErrorMsg);
        }
        
        logger.info("    " + ConsoleColors.GREEN + ">>> ALL JSON ASSERTIONS PASSED" + ConsoleColors.RESET);
    }
    
    private String getValueByPath(JsonElement root, String path) {
        if (path == null || path.isEmpty()) return "null";
        
        if (path.startsWith("$")) {
            path = path.substring(1);
        }
        if (path.startsWith(".")) {
            path = path.substring(1);
        }

        JsonElement current = root;
        
        Pattern tokenPattern = Pattern.compile("^([a-zA-Z0-9_]+)|^\\[(\\d+)\\]|^\\[\\s*['\"]([^'\"]+)['\"]\\s*\\]");

        while (!path.isEmpty() && current != null && !current.isJsonNull()) {
            if (path.startsWith(".")) {
                path = path.substring(1);
                continue;
            }

            Matcher matcher = tokenPattern.matcher(path);
            if (matcher.find()) {
                String fieldName = matcher.group(1); 
                String arrayIndexStr = matcher.group(2); 
                String bracketKey = matcher.group(3); 

                if (fieldName != null) {
                    if (current.isJsonObject()) {
                        current = current.getAsJsonObject().get(fieldName);
                    } else {
                        throw new RuntimeException("Očekivan JSON Objekat za polje '" + fieldName + "', ali pronađen: " + current);
                    }
                }
                else if (arrayIndexStr != null) {
                    if (current.isJsonArray()) {
                        int index = Integer.parseInt(arrayIndexStr);
                        JsonArray array = current.getAsJsonArray();
                        if (index >= 0 && index < array.size()) {
                            current = array.get(index);
                        } else {
                            throw new RuntimeException("Indeks niza [" + index + "] je van granica. Veličina niza: " + array.size());
                        }
                    } else {
                        throw new RuntimeException("Očekivan JSON Niz za indeks [" + arrayIndexStr + "], ali pronađen: " + current);
                    }
                }
                else if (bracketKey != null) {
                    if (current.isJsonObject()) {
                        current = current.getAsJsonObject().get(bracketKey);
                    } else {
                        throw new RuntimeException("Očekivan JSON Objekat za ključ ['" + bracketKey + "'], ali pronađen: " + current);
                    }
                }

                path = path.substring(matcher.end());
            } else {
                throw new RuntimeException("Sintaksna greška u JSONPath izrazu na delu: " + path);
            }
        }

        if (current == null || current.isJsonNull()) {
            return "null";
        }

        if (current.isJsonPrimitive()) {
            return current.getAsJsonPrimitive().getAsString();
        }
        
        return current.toString();
    }
}