package at.co.svc.agate.core.dsl.register;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

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
                throw definitionError(
                        null,
                        null,
                        "root",
                        loaded,
                        "Root YAML must be a map containing the 'testCases' section.");
            }

            Map<String, Object> root = (Map<String, Object>) loaded;

            validateRootStructure(root);

            List<TestCase> rawList = parseTestCases(root, yamlPath);

            validateUniqueTestCaseIds(rawList, yamlPath);

            return TestCaseFilter.filter(rawList);

        } catch (MarkedYAMLException e) {
            printYamlSyntaxError(yamlPath, e);
            throw e;

        } catch (TestDefinitionException e) {
            printDefinitionError(yamlPath, e);
            throw e;

        } catch (Exception e) {
            printUnexpectedLoaderError(yamlPath, e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestCase> parseTestCases(Map<String, Object> root, String yamlPath) throws Exception {
        List<TestCase> finalTestCases = new ArrayList<>();

        Object testCasesObj = root.get("testCases");

        if (!(testCasesObj instanceof List)) {
            throw definitionError(
                    null,
                    null,
                    "testCases",
                    testCasesObj,
                    "'testCases' must be a YAML list.");
        }

        List<?> rawEntries = (List<?>) testCasesObj;

        for (int tcIndex = 0; tcIndex < rawEntries.size(); tcIndex++) {
            Object entry = rawEntries.get(tcIndex);

            if (!(entry instanceof Map)) {
                throw definitionError(
                        "index " + (tcIndex + 1),
                        null,
                        "testCase",
                        entry,
                        "Every entry below 'testCases' must be a map/object.");
            }

            Map<String, Object> tcMap = (Map<String, Object>) entry;

            validateTestCaseDefinition(tcMap, tcIndex + 1);

            String dataFile = asString(tcMap.get("dataSource"));
            if (dataFile == null) {
                dataFile = asString(tcMap.get("dataFile"));
            }

            if (dataFile != null && !dataFile.isBlank()) {
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
            validateStepDefinition(tc, stepMap, localIndex, yamlPath);

            String type = asString(stepMap.get("type"));
            String currentIdNum = idPrefix.isEmpty() ? String.valueOf(localIndex) : idPrefix + "." + localIndex;

            TestStep step = StepParserFactory.parseStep(tc, stepMap, yamlPath, localIndex, resolver);
            step.setSourceFile(yamlPath);
            step.setSourceStepIndex(localIndex);
            
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

            if ("CALL".equalsIgnoreCase(type)) {
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
             InputStreamReader reader =
                     new InputStreamReader(fis, StandardCharsets.UTF_8)) {

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

            throw definitionError(
                    fragmentPath,
                    tc.getName(),
                    parseParentStepIndex(parentId),
                    "steps",
                    null,
                    "Reusable module must contain a top-level 'steps:' section.");
        }
    }
    
    private static Integer parseParentStepIndex(String parentId) {

        if (parentId == null || parentId.isBlank()) {
            return null;
        }

        try {
            String firstPart = parentId.contains(".")
                    ? parentId.substring(0, parentId.indexOf('.'))
                    : parentId;

            return Integer.parseInt(firstPart);

        } catch (NumberFormatException e) {
            return null;
        }
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
    

    private static void validateRootStructure(Map<String, Object> root) {
        if (!root.containsKey("testCases")) {
            throw definitionError(
                    null,
                    null,
                    "testCases",
                    null,
                    "Add a top-level 'testCases:' section.");
        }

        Object testCases = root.get("testCases");

        if (!(testCases instanceof List)) {
            throw definitionError(
                    null,
                    null,
                    "testCases",
                    testCases,
                    "'testCases' must be a YAML list.");
        }
    }

    private static void validateTestCaseDefinition(
            Map<String, Object> tcMap,
            int testCaseIndex) {

        String id = asString(
                tcMap.get("name") != null
                        ? tcMap.get("name")
                        : tcMap.get("id"));

        if (id == null || id.isBlank()) {
            throw definitionError(
                    "index " + testCaseIndex,
                    null,
                    "id",
                    id,
                    "Every test case must define a non-empty 'id' or 'name'.");
        }

        Object stepsObj = tcMap.get("steps");

        if (stepsObj == null) {
            throw definitionError(
                    id,
                    null,
                    "steps",
                    null,
                    "Every test case must contain a 'steps:' list.");
        }

        if (!(stepsObj instanceof List)) {
            throw definitionError(
                    id,
                    null,
                    "steps",
                    stepsObj,
                    "'steps' must be a YAML list.");
        }
    }

    private static void validateStepDefinition(
            TestCase tc,
            Map<String, Object> stepMap,
            int stepIndex,
            String sourceFile) {

        String testCaseName =
                tc != null ? tc.getName() : null;

        String type =
                asString(stepMap.get("type"));

        if (type == null || type.isBlank()) {
            throw definitionError(
                    sourceFile,
                    testCaseName,
                    stepIndex,
                    "type",
                    type,
                    "Every step must define a non-empty 'type'.");
        }

        String normalizedType =
                type.trim().toUpperCase();

        try {
            StepType.valueOf(normalizedType);

        } catch (IllegalArgumentException e) {
            String suggestion =
                    findClosestStepType(normalizedType);

            String hint =
                    suggestion != null
                            ? "Unknown step type '" + type + "'. Did you mean '" + suggestion + "'?"
                            : "Unknown step type '" + type + "'. Supported types: " + supportedStepTypes();

            throw definitionError(
                    sourceFile,
                    testCaseName,
                    stepIndex,
                    "type",
                    type,
                    hint);
        }
    }

    private static void validateUniqueTestCaseIds(
            List<TestCase> testCases,
            String yamlPath) {

        Set<String> seen =
                new HashSet<>();

        for (TestCase tc : testCases) {
            String id =
                    tc != null ? tc.getName() : null;

            if (id == null || id.isBlank()) {
                throw definitionError(
                        null,
                        null,
                        "id",
                        id,
                        "Every resolved test case must have a non-empty ID/name.");
            }

            String normalized =
                    id.trim().toLowerCase();

            if (!seen.add(normalized)) {
                List<Integer> lines =
                        findTestCaseIdLines(yamlPath, id);

                Integer firstLine =
                        lines.size() > 0 ? lines.get(0) : null;

                Integer duplicateLine =
                        lines.size() > 1 ? lines.get(1) : null;

                throw duplicateTestCaseIdError(
                        id,
                        firstLine,
                        duplicateLine);
            }
        }
    }

    private static List<Integer> findTestCaseIdLines(
            String yamlPath,
            String id) {

        List<Integer> result =
                new ArrayList<>();

        if (yamlPath == null || id == null || id.isBlank()) {
            return result;
        }

        try {
            List<String> lines =
                    Files.readAllLines(
                            new File(yamlPath).toPath(),
                            StandardCharsets.UTF_8);

            Pattern candidatePattern =
                    Pattern.compile(
                            "^\\s*(-\\s*)?(id|name)\\s*:\\s*(.*?)\\s*$",
                            Pattern.CASE_INSENSITIVE);

            List<TestCaseIdOccurrence> candidates =
                    new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher matcher = candidatePattern.matcher(line);

                if (!matcher.matches()) {
                    continue;
                }

                String value =
                        unquoteYamlScalar(matcher.group(3));

                if (!id.equals(value)) {
                    continue;
                }

                candidates.add(
                        new TestCaseIdOccurrence(
                                i + 1,
                                indentationOf(line)));
            }

            if (candidates.isEmpty()) {
                return result;
            }

            int minimumIndent =
                    candidates.stream()
                            .mapToInt(candidate -> candidate.indentation)
                            .min()
                            .orElse(Integer.MAX_VALUE);

            for (TestCaseIdOccurrence candidate : candidates) {
                if (candidate.indentation == minimumIndent) {
                    result.add(candidate.line);
                }
            }

        } catch (Exception ignored) {
            // Line information improves the error message but is not required
            // for duplicate-ID validation itself.
        }

        return result;
    }

    private static String unquoteYamlScalar(String value) {
        if (value == null) {
            return null;
        }

        String result = value.trim();

        int commentIndex = result.indexOf(" #");
        if (commentIndex >= 0) {
            result = result.substring(0, commentIndex).trim();
        }

        if (result.length() >= 2) {
            char first = result.charAt(0);
            char last = result.charAt(result.length() - 1);

            if ((first == '\'' && last == '\'')
                    || (first == '\"' && last == '\"')) {
                result = result.substring(1, result.length() - 1);
            }
        }

        return result.trim();
    }

    private static TestDefinitionException duplicateTestCaseIdError(
            String id,
            Integer firstLine,
            Integer duplicateLine) {

        return new TestDefinitionException(
                "Duplicate test case ID",
                id,
                null,
                "id",
                id,
                "Every test case ID in a suite must be unique.",
                firstLine,
                duplicateLine);
    }

    private static final class TestCaseIdOccurrence {

        private final int line;
        private final int indentation;

        private TestCaseIdOccurrence(
                int line,
                int indentation) {

            this.line = line;
            this.indentation = indentation;
        }
    }

    private static String supportedStepTypes() {
        StringBuilder sb =
                new StringBuilder();

        for (StepType type : StepType.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.name());
        }

        return sb.toString();
    }

    private static String findClosestStepType(String actual) {

        if (actual == null || actual.isBlank()) {
            return null;
        }

        String best =
                null;

        int bestDistance =
                Integer.MAX_VALUE;

        for (StepType type : StepType.values()) {
            String candidate =
                    type.name();

            int distance =
                    levenshtein(
                            actual,
                            candidate);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        return bestDistance <= 2
                ? best
                : null;
    }

    private static int levenshtein(String left, String right) {

        int[] previous =
                new int[right.length() + 1];

        int[] current =
                new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;

            for (int j = 1; j <= right.length(); j++) {
                int cost =
                        left.charAt(i - 1) == right.charAt(j - 1)
                                ? 0
                                : 1;

                current[j] =
                        Math.min(
                                Math.min(
                                        current[j - 1] + 1,
                                        previous[j] + 1),
                                previous[j - 1] + cost);
            }

            int[] tmp =
                    previous;

            previous =
                    current;

            current =
                    tmp;
        }

        return previous[right.length()];
    }

    private static void printYamlSyntaxError(
            String yamlPath,
            MarkedYAMLException e) {

        System.err.println();
        System.err.println("============================================================");
        System.err.println("[ERROR] Invalid YAML syntax");
        System.err.println("============================================================");
        System.err.println("File    : " + new File(yamlPath).getAbsolutePath());

        if (e.getProblemMark() != null) {
            System.err.println("Line    : " + (e.getProblemMark().getLine() + 1));
            System.err.println("Column  : " + (e.getProblemMark().getColumn() + 1));
        }

        String problem =
                e.getProblem();

        if (problem == null || problem.isBlank()) {
            problem = e.getMessage();
        }

        System.err.println("Problem : " + cleanMessage(problem));

        String suggestion =
                yamlSyntaxSuggestion(problem);

        if (suggestion != null) {
            System.err.println("Hint    : " + suggestion);
        }

        System.err.println("============================================================");
        System.err.println();
    }

    private static void printDefinitionError(
            String yamlPath,
            TestDefinitionException e) {

        System.err.println();
        System.err.println("============================================================");
        System.err.println("[ERROR] Invalid test definition");
        System.err.println("============================================================");
        String errorFile =
                e.sourceFile != null && !e.sourceFile.isBlank()
                        ? e.sourceFile
                        : yamlPath;

        System.err.println("File      : " + new File(errorFile).getAbsolutePath());

        if (e.testCase != null && !e.testCase.isBlank()) {
            System.err.println("Test Case : " + e.testCase);
        }

        if (e.stepIndex != null) {
            System.err.println("Step      : " + e.stepIndex);
        }

        if (e.field != null && !e.field.isBlank()) {
            System.err.println("Field     : " + e.field);
        }

        if (e.actual != null && !e.actual.isBlank()) {
            if ("id".equals(e.field)
                    && "Duplicate test case ID".equals(e.getMessage())) {
                System.err.println("ID        : " + e.actual);
            } else {
                System.err.println("Actual    : " + e.actual);
            }
        }

        if (e.firstLine != null) {
            System.err.println("First     : line " + e.firstLine);
        }

        if (e.duplicateLine != null) {
            System.err.println("Duplicate : line " + e.duplicateLine);
        }

        System.err.println("Reason    : " + e.getMessage());

        if (e.hint != null && !e.hint.isBlank()) {
            System.err.println("Hint      : " + e.hint);
        }

        System.err.println("============================================================");
        System.err.println();
    }

    private static void printUnexpectedLoaderError(
            String yamlPath,
            Exception e) {

        System.err.println();
        System.err.println("============================================================");
        System.err.println("[ERROR] Test suite cannot be loaded");
        System.err.println("============================================================");
        System.err.println("File   : " + new File(yamlPath).getAbsolutePath());
        System.err.println("Reason : " + cleanMessage(e.getMessage()));
        System.err.println("============================================================");
        System.err.println();
    }

    private static String yamlSyntaxSuggestion(String problem) {

        if (problem == null) {
            return null;
        }

        String lower =
                problem.toLowerCase();

        if (lower.contains("escape")) {
            return "For Windows paths, prefer single quotes, for example 'C:\\\\temp\\\\file.txt'.";
        }

        if (lower.contains("mapping values are not allowed")) {
            return "Check indentation and ':' placement near the reported line.";
        }

        if (lower.contains("could not find expected ':'")) {
            return "Check whether a YAML key is missing ':' near the reported line.";
        }

        if (lower.contains("expected <block end>")
                || lower.contains("expected <block sequence start>")) {
            return "Check indentation and list markers '-' near the reported line.";
        }

        return "Check YAML indentation, quoting and bracket syntax near the reported line.";
    }

    private static String cleanMessage(String value) {

        if (value == null || value.isBlank()) {
            return "Unknown error";
        }

        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static TestDefinitionException definitionError(
            String testCase,
            Integer stepIndex,
            String field,
            Object actual,
            String hint) {

        return definitionError(
                null,
                testCase,
                stepIndex,
                field,
                actual,
                hint);
    }

    private static TestDefinitionException definitionError(
            String sourceFile,
            String testCase,
            Integer stepIndex,
            String field,
            Object actual,
            String hint) {

        String reason;

        if ("id".equals(field)
                && hint != null
                && hint.startsWith("Duplicate test case ID")) {
            reason = "Duplicate test case ID";

        } else if ("type".equals(field)
                && actual != null
                && hint != null
                && hint.startsWith("Unknown step type")) {
            reason = "Unknown step type";

        } else if ("type".equals(field)
                && (actual == null || String.valueOf(actual).isBlank())) {
            reason = "Missing step type";

        } else if ("steps".equals(field)
                && actual == null) {
            reason = "Missing steps section";

        } else if ("id".equals(field)
                && (actual == null || String.valueOf(actual).isBlank())) {
            reason = "Missing test case ID";

        } else {
            reason = "Invalid test definition";
        }

        return new TestDefinitionException(
                reason,
                sourceFile,
                testCase,
                stepIndex,
                field,
                actual != null ? String.valueOf(actual) : null,
                hint,
                null,
                null);
    }

    private static final class TestDefinitionException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        private final String sourceFile;
        private final String testCase;
        private final Integer stepIndex;
        private final String field;
        private final String actual;
        private final String hint;
        private final Integer firstLine;
        private final Integer duplicateLine;

        private TestDefinitionException(
                String reason,
                String testCase,
                Integer stepIndex,
                String field,
                String actual,
                String hint,
                Integer firstLine,
                Integer duplicateLine) {

            this(
                    reason,
                    null,
                    testCase,
                    stepIndex,
                    field,
                    actual,
                    hint,
                    firstLine,
                    duplicateLine);
        }

        private TestDefinitionException(
                String reason,
                String sourceFile,
                String testCase,
                Integer stepIndex,
                String field,
                String actual,
                String hint,
                Integer firstLine,
                Integer duplicateLine) {

            super(reason);

            this.sourceFile = sourceFile;
            this.testCase = testCase;
            this.stepIndex = stepIndex;
            this.field = field;
            this.actual = actual;
            this.hint = hint;
            this.firstLine = firstLine;
            this.duplicateLine = duplicateLine;
        }
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

            List<String> lines =
                    Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            int startSearchIdx = -1;
            int baseIndentation = 0;

            // ------------------------------------------------------------------
            // 1. Locate the relevant "steps:" section.
            // ------------------------------------------------------------------

            if (!isFragment) {

                int tcIdx = -1;

                String cleanIdentifier =
                        identifier.replace("\"", "").trim();

                for (int i = 0; i < lines.size(); i++) {

                    String line = lines.get(i);
                    String cleanLine =
                            line.replace("\"", "").trim();

                    if (cleanLine.startsWith("id:")
                            || cleanLine.startsWith("- id:")
                            || cleanLine.startsWith("name:")) {

                        String actualValue =
                                cleanLine
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
                            && (trimmed.startsWith("- id:")
                                    || trimmed.startsWith("id:"))
                            && indentationOf(line) <= baseIndentation) {

                        return "";
                    }
                }

            } else {

                // Reusable fragment:
                // locate the top-level "steps:" section.

                for (int i = 0; i < lines.size(); i++) {

                    if (lines.get(i).trim().startsWith("steps:")) {

                        startSearchIdx = i;
                        baseIndentation =
                                indentationOf(lines.get(i));

                        break;
                    }
                }
            }

            if (startSearchIdx == -1
                    || startSearchIdx >= lines.size()) {

                return "";
            }

            // ------------------------------------------------------------------
            // 2. Detect top-level YAML list entries below "steps:".
            //
            // Important:
            //
            // Both YAML styles are valid:
            //
            // steps:
            //   - type: CMD
            //
            // and:
            //
            // steps:
            // - type: CMD
            //
            // Therefore the step indentation may be equal to the indentation
            // of "steps:".
            // ------------------------------------------------------------------

            List<Integer> stepStartIndices =
                    new ArrayList<>();

            int stepIndentation = -1;

            for (int i = startSearchIdx + 1;
                 i < lines.size();
                 i++) {

                String line = lines.get(i);
                String trimmed = line.trim();

                if (trimmed.isEmpty()
                        || trimmed.startsWith("#")) {

                    continue;
                }

                int currentIndent =
                        indentationOf(line);

                // Stop when the next test case begins.
                if (!isFragment
                        && currentIndent <= baseIndentation
                        && (trimmed.startsWith("- id:")
                                || trimmed.startsWith("id:")
                                || trimmed.startsWith("name:"))) {

                    break;
                }

                /*
                 * A step can have the same indentation as "steps:".
                 *
                 * Example:
                 *
                 *   steps:
                 *
                 *   - type: CALL
                 *
                 * Therefore use >= instead of >.
                 */
                if (trimmed.startsWith("- ")
                        && currentIndent >= baseIndentation) {

                    if (stepIndentation == -1) {
                        stepIndentation = currentIndent;
                    }

                    // Only entries on the first detected list level are
                    // considered top-level steps. Nested YAML lists are ignored.
                    if (currentIndent == stepIndentation) {
                        stepStartIndices.add(i);
                    }
                }
            }

            // ------------------------------------------------------------------
            // 3. Locate requested step.
            // ------------------------------------------------------------------

            int targetListIdx =
                    targetStepIndex - 1;

            if (targetListIdx < 0
                    || targetListIdx >= stepStartIndices.size()) {

                return "# Error: Step "
                        + targetStepIndex
                        + " does not exist in this Test Case. "
                        + "(Total steps found: "
                        + stepStartIndices.size()
                        + ")";
            }

            int stepStartIdx =
                    stepStartIndices.get(targetListIdx);

            // ------------------------------------------------------------------
            // 4. Include comments directly above the step, but never content
            //    belonging to the previous step.
            // ------------------------------------------------------------------

            int actualStartIdx =
                    stepStartIdx;

            int previousStepIdx =
                    targetListIdx > 0
                            ? stepStartIndices.get(targetListIdx - 1)
                            : startSearchIdx;

            for (int i = stepStartIdx - 1;
                 i > previousStepIdx;
                 i--) {

                String trimmed =
                        lines.get(i).trim();

                if (trimmed.startsWith("steps:")) {
                    break;
                }

                if (!trimmed.startsWith("#")
                        && !trimmed.isEmpty()) {

                    break;
                }

                actualStartIdx = i;
            }

            // ------------------------------------------------------------------
            // 5. Determine the end of the current step.
            // ------------------------------------------------------------------

            int actualEndIdx =
                    targetListIdx < stepStartIndices.size() - 1
                            ? stepStartIndices.get(targetListIdx + 1)
                            : lines.size();

            /*
             * If this is the final step of a normal test case,
             * stop when the next test case starts.
             */
            if (!isFragment
                    && targetListIdx == stepStartIndices.size() - 1) {

                for (int i = stepStartIdx + 1;
                     i < actualEndIdx;
                     i++) {

                    String trimmed =
                            lines.get(i).trim();

                    if (trimmed.isEmpty()) {
                        continue;
                    }

                    int currentIndent =
                            indentationOf(lines.get(i));

                    if (currentIndent <= baseIndentation
                            && (trimmed.startsWith("- id:")
                                    || trimmed.startsWith("id:")
                                    || trimmed.startsWith("name:"))) {

                        actualEndIdx = i;
                        break;
                    }
                }
            }

            // Remove trailing blank lines and comments that belong after
            // the current step.

            while (actualEndIdx > stepStartIdx + 1) {

                String lastLineTrimmed =
                        lines.get(actualEndIdx - 1).trim();

                if (lastLineTrimmed.startsWith("#")
                        || lastLineTrimmed.isEmpty()) {

                    actualEndIdx--;

                } else {
                    break;
                }
            }

            // ------------------------------------------------------------------
            // 6. Normalize indentation relative to the step itself.
            // ------------------------------------------------------------------

            StringBuilder sb =
                    new StringBuilder();

            int spacesToRemove =
                    indentationOf(lines.get(stepStartIdx));

            for (int i = actualStartIdx;
                 i < actualEndIdx;
                 i++) {

                String currentLine =
                        lines.get(i);

                if (currentLine.trim().isEmpty()) {

                    sb.append("\n");
                    continue;
                }

                if (currentLine.length() >= spacesToRemove
                        && currentLine
                                .substring(0, spacesToRemove)
                                .trim()
                                .isEmpty()) {

                    sb.append(
                            currentLine.substring(spacesToRemove)
                    ).append("\n");

                } else {

                    sb.append(
                            currentLine.stripLeading()
                    ).append("\n");
                }
            }

            return sb.toString().trim();

        } catch (Exception e) {

            return "Error extracting YAML text: "
                    + e.getMessage();
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