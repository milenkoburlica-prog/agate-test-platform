package at.co.svc.agate.core.dsl.register;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.utils.CsvLoader;

public class YamlTestCaseLoader {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static List<TestCase> loadTestCases(String yamlPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(yamlPath);
             InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {

            Yaml yaml = new Yaml();
            Object loaded = yaml.load(reader);

            if (!(loaded instanceof Map)) {
                throw new IllegalArgumentException("Root YAML must be a map containing 'testCases' key.");
            }

            Map<String, Object> root = (Map<String, Object>) loaded;
            List<TestCase> rawList = parseTestCases(root, yamlPath);
            
            return TestCaseFilter.filter(rawList);
        } catch (Exception e) {
            System.err.println();
            System.err.println("============================================================");
            System.err.println("[YAML][ERROR] Test suite cannot be loaded");
            System.err.println("============================================================");
            System.err.println("File   : " + new File(yamlPath).getAbsolutePath());
            System.err.println("Reason : " + e.getMessage());
            System.err.println("============================================================");
            System.err.println();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestCase> parseTestCases(Map<String, Object> root, String yamlPath) throws Exception {
        List<TestCase> finalTestCases = new ArrayList<>();
        List<Map<String, Object>> yamlTestCases = (List<Map<String, Object>>) root.get("testCases");

        if (yamlTestCases == null) return finalTestCases;

        for (Map<String, Object> tcMap : yamlTestCases) {
            String dataFile = asString(tcMap.get("dataSource"));
            if (dataFile == null) dataFile = asString(tcMap.get("dataFile"));

            if (dataFile != null) {
                finalTestCases.addAll(createDataDrivenTests(tcMap, yamlPath, dataFile));
            } else {
                finalTestCases.add(createTestCaseFromMap(tcMap, yamlPath, null));
            }
        }
        return finalTestCases;
    }

    private static List<TestCase> createDataDrivenTests(Map<String, Object> tcMap, String yamlPath, String dataFile) throws Exception {
        List<TestCase> iterations = new ArrayList<>();
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        List<Map<String, String>> dataRows = CsvLoader.load(dataFile);
        
        for (Map<String, String> row : dataRows) {
            TestCase tc = createTestCaseFromMap(tcMap, yamlPath, row);
            String rawName = tc.getName();
            // There is no TestStep here because this is the test-case name.
            String resolvedName = resolver.resolve(tc, rawName, tc.getVariables(), yamlPath, 0, rawName, null);
            tc.setName(resolvedName); 
            iterations.add(tc);
        }
        return iterations;
    }

    @SuppressWarnings("unchecked")
    private static TestCase createTestCaseFromMap(Map<String, Object> tcMap, String yamlPath, Map<String, String> csvRow) throws Exception {
        TestCase tc = new TestCase();
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        tc.setName(asString(tcMap.get("name") != null ? tcMap.get("name") : tcMap.get("id")));
        tc.setDescription(asString(tcMap.get("description")));
        tc.setStage(asString(tcMap.get("stage")));
        tc.setPriority(asString(tcMap.get("priority")));

        Map<String, Object> vars = new HashMap<>();
        if (csvRow != null) {
            csvRow.forEach(vars::put);
        }

        Object varObj = tcMap.get("variables");
        if (varObj instanceof Map) {
            Map<?, ?> rawVars = (Map<?, ?>) varObj;
            for (Map.Entry<?, ?> entry : rawVars.entrySet()) {
                vars.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        tc.setVariables(vars);

        Object stepsObj = tcMap.get("steps");
        if (stepsObj instanceof List) {
            List<Map<String, Object>> stepList = (List<Map<String, Object>>) stepsObj;
         // Pass null for inherited parameters because the test-case level has no inherited R variables.
//            List<TestStep> tree = processSteps(tc, stepList, yamlPath, resolver, "", null);  
            List<TestStep> tree = processSteps(tc, stepList, yamlPath, resolver, "", null, false);
            for (TestStep s : tree) {
                tc.addStep(s);
            }
        }

        return tc;
    }

    @SuppressWarnings("unchecked")
    private static List<TestStep> processSteps(TestCase tc, List<Map<String, Object>> stepList, String yamlPath, YamlPlaceholderResolver resolver, String idPrefix, Map<String, Object> inheritedParams, boolean isFragment) throws Exception {
        List<TestStep> resultList = new ArrayList<>();
        int localIndex = 1;

        for (Map<String, Object> stepMap : stepList) {
            String type = asString(stepMap.get("type"));
            String currentIdNum = idPrefix.isEmpty() ? String.valueOf(localIndex) : idPrefix + "." + localIndex;

            TestStep step = StepParserFactory.parseStep(tc, stepMap, yamlPath, localIndex, resolver);

            // Keep an explicit YAML id stable. If no id is provided, generate a
            // deterministic fallback id for backward compatibility.
            if (step.getId() == null || step.getId().isBlank()) {
                step.setId("step_" + currentIdNum.replace(".", "_"));
            }

            // Use the explicit fragment flag instead of inferring fragment context from the path.
            String identifier = isFragment ? yamlPath : tc.getName();

            String originalYamlText = extractOriginalStepYaml(yamlPath, identifier, localIndex, isFragment);
            step.setTextYaml(originalYamlText);
            // ------------------------------------------------------------------
            
            // R-variable context:
            Map<String, Object> effectiveParams = new HashMap<>();
            if (inheritedParams != null) {
                effectiveParams.putAll(inheritedParams);
            }
            if (stepMap.get("parameters") instanceof Map) {
                effectiveParams.putAll((Map<String, Object>) stepMap.get("parameters"));
            }
            step.setParameters(effectiveParams);

            if ("CALL".equals(type)) {
                String action = asString(stepMap.get("command"));
                String fragmentPath = "data" + File.separator + System.getProperty("APPLICATION") + File.separator + action.replace(".", File.separator) + ".yaml";
                
                // Pass the effective parameter context into the reusable fragment.
                List<TestStep> subTree = loadFragmentStepsHierarchical(tc, fragmentPath, resolver, currentIdNum, effectiveParams);
                step.setSubSteps(subTree);
            }

            resolveStepDetails(tc, step, resolver, yamlPath, localIndex);
            resultList.add(step);
            localIndex++;
        }
        return resultList;
    }
    
    
    
    @SuppressWarnings("unchecked")
    private static List<TestStep> loadFragmentStepsHierarchical(
            TestCase tc,
            String fragmentPath,
            YamlPlaceholderResolver resolver,
            String parentId,
            Map<String, Object> paramsToPass) throws Exception {

        File file = new File(fragmentPath);

        if (!file.exists()) {
            throw new java.io.FileNotFoundException(
                    "Reusable fragment not found: " + file.getAbsolutePath()
            );
        }

        System.out.println("[YAML] Loading reusable fragment: " + file.getPath());

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {

            Yaml yaml = new Yaml();

            final Map<String, Object> fragmentRoot;
            try {
                fragmentRoot = yaml.load(reader);
            } catch (Exception e) {
                System.err.println();
                System.err.println("============================================================");
                System.err.println("[YAML][ERROR] Reusable fragment cannot be loaded");
                System.err.println("============================================================");
                System.err.println("File       : " + file.getAbsolutePath());
                System.err.println("Test Case  : " + tc.getName());
                System.err.println("Parent Step: " + parentId);
                System.err.println("Reason     : " + e.getMessage());
                System.err.println("============================================================");
                System.err.println();
                throw e;
            }

            if (fragmentRoot != null && fragmentRoot.containsKey("steps")) {
                List<Map<String, Object>> fragmentSteps =
                        (List<Map<String, Object>>) fragmentRoot.get("steps");

                // Pass true because this method is loading a reusable fragment.
                return processSteps(
                        tc,
                        fragmentSteps,
                        fragmentPath,
                        resolver,
                        parentId,
                        paramsToPass,
                        true
                );
            }

            System.out.println(
                    "[YAML][WARNING] Reusable fragment contains no 'steps' section: "
                            + file.getPath()
            );
        }

        return new ArrayList<>();
    }
    
    
    private static void resolveStepDetails(TestCase tc, TestStep step, YamlPlaceholderResolver resolver, String path, int idx) {
        // Pass the current step to every resolver call so R[...] variables can be resolved.
        if (step.getRow() != null) step.setRow(resolver.resolve(tc, step.getRow(), tc.getVariables(), path, idx, "row", step));
        if (step.getColumn() != null) step.setColumn(resolver.resolve(tc, step.getColumn(), tc.getVariables(), path, idx, "column", step));
        if (step.getUrl() != null) step.setUrl(resolver.resolve(tc, step.getUrl(), tc.getVariables(), path, idx, "url", step));
        if (step.getAction() != null) step.setAction(resolver.resolve(tc, step.getAction(), tc.getVariables(), path, idx, "action", step));
        if (step.getBody() != null) step.setBody(resolver.resolve(tc, step.getBody(), tc.getVariables(), path, idx, "body", step));
       ///// if (step.getValue() != null) step.setValue(resolver.resolve(tc, step.getValue(), tc.getVariables(), path, idx, "value", step));
        if (step.getExpected() != null) step.setExpected(resolver.resolve(tc, step.getExpected(), tc.getVariables(), path, idx, "expected", step));
        if (step.getEndpoint() != null) step.setEndpoint(resolver.resolve(tc, step.getEndpoint(), tc.getVariables(), path, idx, "endpoint", step));
        if (step.getCondition() != null) step.setCondition(resolver.resolve(tc, step.getCondition(), tc.getVariables(), path, idx, "condition", step));
        
     // Resolve step parameters used as R variables.
        if (step.getParameters() != null && !step.getParameters().isEmpty()) {
            Map<String, Object> resolvedParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : step.getParameters().entrySet()) {
                Object rawValue = entry.getValue();
                if (rawValue instanceof String) {
                    // Example: "{B[vpNummer]}" becomes "136099".
                    String resolvedValue = resolver.resolve(tc, (String) rawValue, tc.getVariables(), path, idx, "param-" + entry.getKey(), step);
                    String escapedValue = resolvedValue;
                    if (step.getType().equals(StepType.SOAP) && step.getOp().equals("EXEC")) {
                       escapedValue = escapeXml(resolvedValue);
                    }
                    resolvedParams.put(entry.getKey(), escapedValue);
                } else {
                    resolvedParams.put(entry.getKey(), rawValue);
                }
            }
            // Store the resolved parameters back on the step.
            step.setParameters(resolvedParams);
        }
        
        if (step.getHeaders() != null) {
            Map<String, String> resolvedHeaders = new HashMap<>();
            step.getHeaders().forEach((k, v) -> {
                resolvedHeaders.put(k, resolver.resolve(tc, v, tc.getVariables(), path, idx, "header-" + k, step));
            });
            step.setHeaders(resolvedHeaders);
        }
        if (step.getFrom() != null) step.setFrom(resolver.resolve(tc, step.getFrom(), tc.getVariables(), path, idx, "from", step));
        if (step.getTo() != null) step.setTo(resolver.resolve(tc, step.getTo(), tc.getVariables(), path, idx, "to", step));
    }
    private static String escapeXml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
        //return input;
    }
    private static String asString(Object obj) {
        return obj == null ? null : obj.toString();
    }
    
 // The method receives TestStep so inherited parameters are available.
    @SuppressWarnings("unchecked")
    private void enrichStepDataIfNeeded(TestCase tc, TestStep step) throws Exception {
        if ((step.getType() == StepType.REST || step.getType() == StepType.SOAP) 
            && "EXEC".equalsIgnoreCase(step.getOp())) {
            
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("type", step.getType().toString());
            tempMap.put("op", step.getOp());
            tempMap.put("action", step.getAction());
            
            // Pass the step because it already contains inherited parameters (R variables).
            // These parameters were inherited during processSteps recursion.
            YamlTestCaseLoader.handleFileBasedRestCall(tc, tempMap, step);
            
            if (tempMap.containsKey("url")) step.setUrl(tempMap.get("url").toString());
            if (tempMap.containsKey("body")) step.setBody(tempMap.get("body").toString());
            if (tempMap.containsKey("headers")) {
                step.setHeaders((Map<String, String>) tempMap.get("headers"));
            }
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public static void handleFileBasedRestCall(TestCase tc, Map<String, Object> stepMap, TestStep step) throws Exception {
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        
        // Pre-resolve step parameters.
        // Resolve the step parameters before they are used in the request body.
        if (step.getParameters() != null && !step.getParameters().isEmpty()) {
            Map<String, Object> preResolvedParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : step.getParameters().entrySet()) {
                if (entry.getValue() instanceof String) {
                    String rawVal = (String) entry.getValue();
                    // Resolve the parameter value, for example {B[vpNummer]} -> 136099.
                    String resolvedVal = resolver.resolve(tc, rawVal, tc.getVariables(), "internal", 0, "param-fix", step);
                    preResolvedParams.put(entry.getKey(), resolvedVal);
                } else {
                    preResolvedParams.put(entry.getKey(), entry.getValue());
                }
            }
            // Update the step with the resolved parameter values.
            step.setParameters(preResolvedParams);
        }
        // ------------------------------------
        
        
        String actionValue = String.valueOf(stepMap.get("command"));
        if ((actionValue == null)|| (actionValue.equalsIgnoreCase("null")))  {
            throw new RuntimeException("Parameter command not defined!");
        }
        String modulePath = actionValue.replace(".", "/");
        
        Object appVar = System.getProperty("APPLICATION");
        String app = (appVar != null) ? appVar.toString() : "cps";

        String basePath = "data/" + System.getProperty("APPLICATION") + "/modules/" + modulePath + "/";

        // 1. Read metadata.json.
        File metaFile = new File(basePath + "metadata.json");
        if (metaFile.exists()) {
            Map<String, Object> meta = JSON_MAPPER.readValue(metaFile, Map.class);

            if (meta.get("url") != null) {
                if (meta.get("method") != null) {
                    // Keep "op" unchanged and populate "method".
                    stepMap.put("method", meta.get("method").toString()); 
                }
                // Pass the step so the resolver can access R variables.
                if (step.getEndpoint() == null) {
                    throw new RuntimeException("Endpoint null");
                }
                String metaUrl = meta.get("url").toString().replace("{{endpoint}}", step.getEndpoint());
                String url = resolver.resolve(tc, metaUrl, tc.getVariables(), metaFile.getPath(), 0, "url", step);
                stepMap.put("url", url);
            }

            if (meta.get("headers") != null) {
                Map<String, Object> headers = (Map<String, Object>) meta.get("headers");
                Map<String, String> resolvedHeaders = new HashMap<>();
                for (Map.Entry<String, Object> h : headers.entrySet()) {
                    // Pass the current step to the resolver.
                    String resolvedValue = resolver.resolve(tc, h.getValue().toString(), tc.getVariables(), metaFile.getPath(), 0, "header", step);
                    resolvedHeaders.put(h.getKey(), resolvedValue);
                }
                stepMap.put("headers", resolvedHeaders);
            }
            
         // Parse the optional auth block from metadata.json.
            Object authObj = meta.get("auth");
            if (authObj instanceof Map) {
                Map<String, Object> authMap = (Map<String, Object>) authObj;
                at.co.svc.agate.core.dsl.model.SoapAuth soapAuth = new at.co.svc.agate.core.dsl.model.SoapAuth();
                
                // Resolve auth values as well so placeholders such as {B[username]} are supported.
                String resolvedType = resolver.resolve(tc, asString(authMap.get("type")), tc.getVariables(), metaFile.getPath(), 0, "auth-type", step);
                String resolvedUser = resolver.resolve(tc, asString(authMap.get("username")), tc.getVariables(), metaFile.getPath(), 0, "auth-username", step);
                String resolvedPass = resolver.resolve(tc, asString(authMap.get("password")), tc.getVariables(), metaFile.getPath(), 0, "auth-password", step);
                
                soapAuth.setType(resolvedType);
                soapAuth.setUsername(resolvedUser);
                soapAuth.setPassword(resolvedPass);
                
                step.setAuth(soapAuth);
            }
        } else {
            File file = new File(basePath + "metadata.json");
            if (!file.exists()) {
                // Throw a descriptive error so the caller can report the missing configuration file.
                //PrintDslStepContext.logDslStepContext(logger, step);
                throw new RuntimeException("Missing configuration file: " + basePath + "metadata.json");
            }
            throw new RuntimeException("Unknown");
        }

     // 2. Read the request file (JSON or XML).
        File reqFile = new File(basePath + "request.json");
        
        // Fall back to request.xml when request.json does not exist.
        if (!reqFile.exists()) {
            reqFile = new File(basePath + "request.xml");
        }

        // Process the request when either JSON or XML exists.
        if (reqFile.exists()) {
            String bodyContent = Files.readString(reqFile.toPath(), StandardCharsets.UTF_8);
            
            // Pass the current step to the resolver. kako bi {R[vpNummer]} bio zamenjen vrednošću iz parametara
            // Apply NULL-removal and EMPTY-value handling before placeholder resolution.
            bodyContent = resolveNullableRule(bodyContent, step.getParameters());
            
            // Resolve parameters and variables inside the request body for both JSON and XML.
            // Parameters take precedence over test-case variables.
            String resolvedBody = resolver.resolve(tc, bodyContent, step.getParameters(), reqFile.getPath(), 0, "body", step);
            resolvedBody = resolver.resolve(tc, resolvedBody, tc.getVariables(), reqFile.getPath(), 0, "body", step);            
            // Store the final JSON or XML text under the "body" key.
            stepMap.put("body", resolvedBody);
        }
        
    }
    
    private static String resolveNullableRule(String body, Map<String, Object> parameters) {
        if (parameters == null || body == null) return body;

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());

            if ("{NULL}".equalsIgnoreCase(value)) {
                // JSON regex: remove "key": "{R[key]}" and an optional trailing comma.
                // Supports spacing variants around the key, colon, and placeholder.
                String jsonRegex = "(?i)\"\\s*" + key + "\\s*\"\\s*:\\s*\"\\s*\\{R\\[" + key + "\\]\\}\\s*\"\\s*,?";
                body = body.replaceAll(jsonRegex, "");
                
                // XML regex: remove <key>{R[key]}</key>.
                String xmlRegex = "(?i)<\\s*" + key + "\\s*>\\s*\\{R\\[" + key + "\\]\\}\\s*<\\s*/\\s*" + key + "\\s*>";
                body = body.replaceAll(xmlRegex, "");
            } 
            else if ("{EMPTY}".equalsIgnoreCase(value)) {
                // For EMPTY, replace the placeholder with an empty string.
                body = body.replace("{R[" + key + "]}", "");
            }
        }

        // Clean up trailing commas before closing JSON objects or arrays.
        body = body.replaceAll(",\\s*\\}", " }")
                   .replaceAll(",\\s*\\]", " ]");

        return body;
    }
    
    public static String extractOriginalStepYaml(
            String yamlPath,
            String identifier,
            int targetStepIndex,
            boolean isFragment) {

        try {
            File file = new File(yamlPath);
            if (!file.exists()) {
                return "";
            }

            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            int startSearchIdx = -1;
            int baseIndentation = 0;

            // 1. Locate the relevant steps: section.
            if (!isFragment) {
                int tcIdx = -1;
                String cleanIdentifier = identifier.replace("\"", "").trim();

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String cleanLine = line.replace("\"", "").trim();

                    if (cleanLine.startsWith("id:")
                            || cleanLine.startsWith("- id:")
                            || cleanLine.startsWith("name:")) {

                        String actualValue = cleanLine
                                .substring(cleanLine.indexOf(":") + 1)
                                .replace("\"", "")
                                .trim();

                        if (actualValue.equals(cleanIdentifier)) {
                            tcIdx = i;
                            break;
                        }
                    }
                }

                if (tcIdx == -1) {
                    return "# Error: Test Case with ID '"
                            + identifier
                            + "' was not found in the file.";
                }

                for (int i = tcIdx; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String trimmed = line.trim();

                    if (trimmed.startsWith("steps:")) {
                        startSearchIdx = i;
                        baseIndentation = indentationOf(line);
                        break;
                    }

                    if (i > tcIdx
                            && (trimmed.startsWith("- id:") || trimmed.startsWith("id:"))
                            && indentationOf(line) <= baseIndentation) {
                        return "";
                    }
                }
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().startsWith("steps:")) {
                        startSearchIdx = i;
                        baseIndentation = indentationOf(lines.get(i));
                        break;
                    }
                }
            }

            if (startSearchIdx == -1 || startSearchIdx >= lines.size()) {
                return "";
            }

            // 2. Detect top-level YAML list entries below steps:.
            // This deliberately does NOT require "- type:". A step may now start
            // with "- id:" followed by "type: SOAP". Nested lists such as ignore
            // and unordered have greater indentation and are therefore ignored.
            List<Integer> stepStartIndices = new ArrayList<>();
            int stepIndentation = -1;

            for (int i = startSearchIdx + 1; i < lines.size(); i++) {
                String line = lines.get(i);
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int currentIndent = indentationOf(line);

                if (!isFragment
                        && currentIndent <= baseIndentation
                        && (trimmed.startsWith("- id:")
                                || trimmed.startsWith("id:")
                                || trimmed.startsWith("name:"))) {
                    break;
                }

                if (trimmed.startsWith("- ") && currentIndent > baseIndentation) {
                    if (stepIndentation == -1) {
                        stepIndentation = currentIndent;
                    }

                    if (currentIndent == stepIndentation) {
                        stepStartIndices.add(i);
                    }
                }
            }

            int targetListIdx = targetStepIndex - 1;
            if (targetListIdx < 0 || targetListIdx >= stepStartIndices.size()) {
                return "# Error: Step "
                        + targetStepIndex
                        + " does not exist in this Test Case. (Total steps found: "
                        + stepStartIndices.size()
                        + ")";
            }

            int stepStartIdx = stepStartIndices.get(targetListIdx);

            // 3. Include comments directly above the step, but never content from
            // the previous step.
            int actualStartIdx = stepStartIdx;
            int previousStepIdx = targetListIdx > 0
                    ? stepStartIndices.get(targetListIdx - 1)
                    : startSearchIdx;

            for (int i = stepStartIdx - 1; i > previousStepIdx; i--) {
                String trimmed = lines.get(i).trim();

                if (trimmed.startsWith("steps:")) {
                    break;
                }

                if (!trimmed.startsWith("#") && !trimmed.isEmpty()) {
                    break;
                }

                actualStartIdx = i;
            }

            // 4. The next top-level step starts the boundary of this step.
            int actualEndIdx = targetListIdx < stepStartIndices.size() - 1
                    ? stepStartIndices.get(targetListIdx + 1)
                    : lines.size();

            // If this is the final step of a test case, stop when leaving the
            // current test-case indentation level.
            if (!isFragment && targetListIdx == stepStartIndices.size() - 1) {
                for (int i = stepStartIdx + 1; i < actualEndIdx; i++) {
                    String trimmed = lines.get(i).trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }

                    int currentIndent = indentationOf(lines.get(i));
                    if (currentIndent <= baseIndentation
                            && (trimmed.startsWith("- id:")
                                    || trimmed.startsWith("id:")
                                    || trimmed.startsWith("name:"))) {
                        actualEndIdx = i;
                        break;
                    }
                }
            }

            while (actualEndIdx > stepStartIdx + 1) {
                String lastLineTrimmed = lines.get(actualEndIdx - 1).trim();
                if (lastLineTrimmed.startsWith("#") || lastLineTrimmed.isEmpty()) {
                    actualEndIdx--;
                } else {
                    break;
                }
            }

            // 5. Normalize indentation relative to the top-level step entry.
            StringBuilder sb = new StringBuilder();
            int spacesToRemove = indentationOf(lines.get(stepStartIdx));

            for (int i = actualStartIdx; i < actualEndIdx; i++) {
                String currentLine = lines.get(i);

                if (currentLine.trim().isEmpty()) {
                    sb.append("\n");
                    continue;
                }

                if (currentLine.length() >= spacesToRemove
                        && currentLine.substring(0, spacesToRemove).trim().isEmpty()) {
                    sb.append(currentLine.substring(spacesToRemove)).append("\n");
                } else {
                    sb.append(currentLine.stripLeading()).append("\n");
                }
            }

            return sb.toString().trim();

        } catch (Exception e) {
            return "Error extracting YAML text: " + e.getMessage();
        }
    }

    private static int indentationOf(String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }

        int indentation = 0;
        while (indentation < line.length()
                && Character.isWhitespace(line.charAt(indentation))) {
            indentation++;
        }
        return indentation;
    }
    
    
    
}