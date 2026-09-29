package at.co.svc.agate.core.dsl.register;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import at.co.svc.agate.core.dsl.resolver.ReusablePathResolver;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.utils.CsvLoader;
import at.co.svc.agate.core.project.ProjectRuntime;

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
            List<TestStep> tree = processSteps(tc, stepList, yamlPath, resolver, "", null, false, "");
            for (TestStep s : tree) {
                tc.addStep(s);
            }
        }

        return tc;
    }

    private static String resolveInheritedRParameters(
            String value,
            Map<String, Object> inheritedParams) {

        if (value == null
                || inheritedParams == null
                || inheritedParams.isEmpty()) {

            return value;
        }

        String result = value;

        Pattern pattern =
                Pattern.compile("\\{R\\[([^]]+)]}");

        Matcher matcher =
                pattern.matcher(result);

        StringBuffer sb =
                new StringBuffer();

        while (matcher.find()) {

            String parameterName =
                    matcher.group(1);

            if (!inheritedParams.containsKey(parameterName)) {

                // Leave unresolved here.
                // Normal runtime/error handling can deal with it later.
                continue;
            }

            Object inheritedValue =
                    inheritedParams.get(parameterName);

            String replacement =
                    inheritedValue == null
                            ? ""
                            : String.valueOf(inheritedValue);

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(sb);

        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    private static List<TestStep> processSteps(
            TestCase tc,
            List<Map<String, Object>> stepList,
            String yamlPath,
            YamlPlaceholderResolver resolver,
            String idPrefix,
            Map<String, Object> inheritedParams,
            boolean isFragment,
            String referenceCallPath) throws Exception {

        List<TestStep> resultList =
                new ArrayList<>();

        int localIndex = 1;

        for (Map<String, Object> stepMap : stepList) {

            /*
             * SECTION is metadata only.
             *
             * It intentionally has no type and must never be parsed
             * as an executable TestStep. Keep localIndex aligned with
             * the YAML list position so source-step references remain
             * stable for later executable steps.
             */
            if (stepMap != null
                    && stepMap.containsKey("section")) {

                localIndex++;
                continue;
            }

            validateStepDefinition(
                    tc,
                    stepMap,
                    localIndex,
                    yamlPath
            );

            String type =
                    asString(
                            stepMap.get("type")
                    );

            String currentIdNum =
                    idPrefix.isEmpty()
                            ? String.valueOf(localIndex)
                            : idPrefix + "." + localIndex;

            TestStep step =
                    StepParserFactory.parseStep(
                            tc,
                            stepMap,
                            yamlPath,
                            localIndex,
                            resolver
                    );

            step.setSourceFile(
                    yamlPath
            );

            step.setSourceStepIndex(
                    localIndex
            );

            /*
             * Accumulated CALL/reusable ancestry for MATCH_REFERENCE.
             * Top-level steps receive an empty path.
             */
            step.setReferenceCallPath(
                    referenceCallPath
            );

            /*
             * Keep explicit YAML IDs unchanged.
             * Generate a deterministic fallback ID only when no ID exists.
             */
            if (step.getId() == null
                    || step.getId().isBlank()) {

                step.setId(
                        "step_"
                                + currentIdNum.replace(
                                        ".",
                                        "_"
                                )
                );
            }

            /*
             * Store original YAML text for logging/debugging.
             */
            String identifier =
                    isFragment
                            ? yamlPath
                            : tc.getName();

            String originalYamlText =
                    extractOriginalStepYaml(
                            yamlPath,
                            identifier,
                            localIndex,
                            isFragment
                    );

            step.setTextYaml(
                    originalYamlText
            );

            /*
             * ---------------------------------------------------------
             * Build effective parameter context.
             * ---------------------------------------------------------
             *
             * inheritedParams:
             *     parameters passed by the parent CALL.
             *
             * local parameters:
             *     parameters defined on the current child step.
             *
             * Important:
             *
             * Local R[...] references must be resolved against the
             * inherited parent context BEFORE the local parameter
             * replaces the inherited value.
             *
             * Example:
             *
             * Parent CALL:
             *
             *   parameters:
             *     bevid: "100"
             *
             * Child SOAP:
             *
             *   parameters:
             *     bevid: "{R[bevid]}"
             *
             * Result:
             *
             *   bevid = "100"
             *
             *
             * Runtime placeholders must NOT be resolved here.
             *
             * Example:
             *
             * Parent CALL:
             *
             *   parameters:
             *     vpNummer: "{B[B_Karte]}"
             *
             * Child:
             *
             *   parameters:
             *     vpNummer: "{R[vpNummer]}"
             *
             * Result after this method:
             *
             *   vpNummer = "{B[B_Karte]}"
             *
             * The B[...] placeholder remains untouched and will be
             * resolved later during runtime.
             * ---------------------------------------------------------
             */

            /*
             * ---------------------------------------------------------
             * Parameter contexts
             * ---------------------------------------------------------
             *
             * inheritedParams:
             *     Context received from the parent CALL. It remains
             *     visible to the current step so conditions and direct
             *     {R[...]} references continue to work.
             *
             * localParams:
             *     Only parameters explicitly declared on the current step.
             *     Their {R[...]} values are resolved against inheritedParams.
             *
             * effectiveParams:
             *     Runtime context of the current step: inherited + local.
             *
             * paramsToPass:
             *     Context forwarded into a nested reusable. When a CALL
             *     explicitly declares a parameters block, ONLY those local
             *     parameters are forwarded. This prevents unrelated parent
             *     parameters from leaking into deeper reusable levels.
             *
             *     A CALL without a parameters block keeps the previous
             *     transparent pass-through behaviour for backwards
             *     compatibility.
             * ---------------------------------------------------------
             */

            Map<String, Object> localParams =
                    new HashMap<>();

            Object parametersObj =
                    stepMap.get("parameters");

            boolean hasDeclaredParameters =
                    parametersObj instanceof Map<?, ?>;

            if (parametersObj instanceof Map<?, ?> rawLocalParams) {

                for (Map.Entry<?, ?> entry :
                        rawLocalParams.entrySet()) {

                    String parameterName =
                            String.valueOf(
                                    entry.getKey()
                            );

                    Object parameterValue =
                            entry.getValue();

                    if (parameterValue instanceof String stringValue
                            && inheritedParams != null
                            && !inheritedParams.isEmpty()) {

                        String resolvedValue =
                                resolveInheritedRParameters(
                                        stringValue,
                                        inheritedParams
                                );

                        localParams.put(
                                parameterName,
                                resolvedValue
                        );

                    } else {

                        localParams.put(
                                parameterName,
                                parameterValue
                        );
                    }
                }
            }

            Map<String, Object> effectiveParams =
                    new HashMap<>();

            if (inheritedParams != null) {

                effectiveParams.putAll(
                        inheritedParams
                );
            }

            effectiveParams.putAll(
                    localParams
            );

            /*
             * The current step keeps the complete execution context.
             * This is required for conditions such as:
             *
             *   {R[AusschreibeStatus]} != NULL
             *
             * even when AusschreibeStatus belongs to the parent CALL.
             */
            step.setParameters(
                    effectiveParams
            );

            Map<String, Object> paramsToPass =
                    hasDeclaredParameters
                            ? new HashMap<>(localParams)
                            : new HashMap<>(effectiveParams);

            /*
             * ---------------------------------------------------------
             * CALL / reusable handling
             * ---------------------------------------------------------
             */
            if ("CALL".equalsIgnoreCase(type)) {

                String action =
                        asString(
                                stepMap.get("command")
                        );

                String fragmentPath =
                        ReusablePathResolver.resolve(
                                action,
                                System.getProperty("APPLICATION")
                        );

                /*
                 * Pass the effective parameter context into the reusable.
                 *
                 * These parameters become the R-context of the child
                 * reusable steps.
                 */
                String childReferenceCallPath =
                        appendReferenceCallPath(
                                referenceCallPath,
                                step.getId(),
                                reusableFileName(action)
                        );

                List<TestStep> subTree =
                        loadFragmentStepsHierarchical(
                                tc,
                                fragmentPath,
                                resolver,
                                currentIdNum,
                                paramsToPass,
                                childReferenceCallPath
                        );

                step.setSubSteps(
                        subTree
                );
            }

            /*
             * ---------------------------------------------------------
             * LOOP / inline child-step handling
             * ---------------------------------------------------------
             *
             * LOOP owns an inline "steps:" list. Reuse the existing
             * processSteps(...) pipeline so nested steps keep the same
             * parsing, parameter context and runtime behaviour as normal
             * AGATE steps.
             */
            if ("LOOP".equalsIgnoreCase(type)) {

                Object nestedStepsObj =
                        stepMap.get("steps");

                if (nestedStepsObj instanceof List<?> rawNestedSteps) {

                    List<Map<String, Object>> nestedSteps =
                            new ArrayList<>();

                    for (Object nestedStepObj : rawNestedSteps) {

                        if (!(nestedStepObj instanceof Map<?, ?> rawNestedStepMap)) {
                            continue;
                        }

                        Map<String, Object> nestedStepMap =
                                new HashMap<>();

                        for (Map.Entry<?, ?> entry : rawNestedStepMap.entrySet()) {

                            nestedStepMap.put(
                                    String.valueOf(entry.getKey()),
                                    entry.getValue()
                            );
                        }

                        nestedSteps.add(nestedStepMap);
                    }

                    List<TestStep> subTree =
                            processSteps(
                                    tc,
                                    nestedSteps,
                                    yamlPath,
                                    resolver,
                                    currentIdNum,
                                    effectiveParams,
                                    isFragment,
                                    referenceCallPath
                            );

                    /*
                     * extractOriginalStepYaml(...) is based on the outer
                     * steps list. For inline LOOP children keep logging
                     * deterministic by rendering the concrete child map.
                     */
                    Yaml nestedYaml = new Yaml();

                    for (int nestedIndex = 0;
                         nestedIndex < subTree.size()
                                 && nestedIndex < nestedSteps.size();
                         nestedIndex++) {

                        String renderedYaml =
                                nestedYaml.dump(nestedSteps.get(nestedIndex));

                        if (renderedYaml != null) {
                            renderedYaml = renderedYaml.trim();
                        }

                        subTree.get(nestedIndex).setTextYaml(renderedYaml);
                    }

                    step.setSubSteps(subTree);
                }
            }

            /*
             * Resolve values that are safe to resolve during load time.
             *
             * Runtime B[...] and R[...] placeholders are intentionally
             * kept unresolved by resolveStepDetails where necessary.
             */
            resolveStepDetails(
                    tc,
                    step,
                    resolver,
                    yamlPath,
                    localIndex
            );

            resultList.add(
                    step
            );

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
            Map<String, Object> paramsToPass,
            String referenceCallPath) throws Exception {

        File file = new File(fragmentPath);

        if (!file.exists()) {
            throw new java.io.FileNotFoundException(
                    "Reusable fragment not found: " + file.getAbsolutePath()
            );
        }

        if (Boolean.getBoolean("agate.debug")) {
            System.out.println("[YAML] Loading reusable fragment: " + file.getPath());
        }

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
                        true,
                        referenceCallPath
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
    

    private static String appendReferenceCallPath(
            String parentPath,
            String callId,
            String reusableName) {

        String segment =
                nullSafePathPart(callId)
                        + "__"
                        + nullSafePathPart(reusableName);

        if (parentPath == null
                || parentPath.isBlank()) {

            return segment;
        }

        return parentPath
                + "__"
                + segment;
    }

    private static String reusableFileName(
            String action) {

        if (action == null
                || action.isBlank()) {

            return "unknown_reusable";
        }

        String normalized =
                action.replace('\\', '.')
                        .replace('/', '.');

        int lastDot =
                normalized.lastIndexOf('.');

        String name =
                lastDot >= 0
                        ? normalized.substring(lastDot + 1)
                        : normalized;

        if (name.endsWith(".yaml")) {
            name = name.substring(
                    0,
                    name.length() - 5);
        }

        return nullSafePathPart(name);
    }

    private static String nullSafePathPart(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "unknown";
        }

        return value.trim();
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


    private static void resolveStepDetails(
            TestCase tc,
            TestStep step,
            YamlPlaceholderResolver resolver,
            String path,
            int idx) {

        // Resolve only values that are safe at load time.
        // Runtime-dependent placeholders B[...] and R[...] must remain unresolved
        // until the step is actually executed.

        if (step.getRow() != null) {
            step.setRow(resolveLoadTime(
                    tc, step, resolver,
                    step.getRow(),
                    path, idx, "row"));
        }

        if (step.getColumn() != null) {
            step.setColumn(resolveLoadTime(
                    tc, step, resolver,
                    step.getColumn(),
                    path, idx, "column"));
        }

        if (step.getUrl() != null) {
            step.setUrl(resolveLoadTime(
                    tc, step, resolver,
                    step.getUrl(),
                    path, idx, "url"));
        }

        if (step.getAction() != null) {
            step.setAction(resolveLoadTime(
                    tc, step, resolver,
                    step.getAction(),
                    path, idx, "action"));
        }

        if (step.getBody() != null) {
            step.setBody(resolveLoadTime(
                    tc, step, resolver,
                    step.getBody(),
                    path, idx, "body"));
        }

        if (step.getValue() != null) {
            step.setValue(resolveLoadTime(
                    tc, step, resolver,
                    step.getValue(),
                    path, idx, "value"));
        }

        if (step.getExpected() != null) {
            step.setExpected(resolveLoadTime(
                    tc, step, resolver,
                    step.getExpected(),
                    path, idx, "expected"));
        }

        if (step.getEndpoint() != null) {
            step.setEndpoint(resolveLoadTime(
                    tc, step, resolver,
                    step.getEndpoint(),
                    path, idx, "endpoint"));
        }

        /*
         * condition is intentionally NOT resolved here.
         * It is evaluated during runtime when all previous step buffers exist.
         */

        // Parameters
        if (step.getParameters() != null
                && !step.getParameters().isEmpty()) {

            Map<String, Object> resolvedParams =
                    new HashMap<>();

            for (Map.Entry<String, Object> entry :
                    step.getParameters().entrySet()) {

                Object rawValue =
                        entry.getValue();

                if (rawValue instanceof String rawString) {

                    String resolvedValue =
                            resolveLoadTime(
                                    tc,
                                    step,
                                    resolver,
                                    rawString,
                                    path,
                                    idx,
                                    "param-" + entry.getKey());

                    /*
                     * Do NOT XML-escape unresolved runtime expressions.
                     *
                     * Example:
                     * {B[token]}
                     *
                     * must first be resolved during runtime and only then escaped.
                     */
                    if (!containsRuntimePlaceholder(resolvedValue)
                            && step.getType() == StepType.SOAP
                            && "EXEC".equalsIgnoreCase(step.getOp())) {

                        resolvedValue =
                                escapeXml(resolvedValue);
                    }

                    resolvedParams.put(
                            entry.getKey(),
                            resolvedValue);

                } else {

                    resolvedParams.put(
                            entry.getKey(),
                            rawValue);
                }
            }

            step.setParameters(
                    resolvedParams);
        }

        // Headers
        if (step.getHeaders() != null
                && !step.getHeaders().isEmpty()) {

            Map<String, String> resolvedHeaders =
                    new HashMap<>();

            step.getHeaders().forEach(
                    (key, value) -> {

                        String resolvedValue =
                                resolveLoadTime(
                                        tc,
                                        step,
                                        resolver,
                                        value,
                                        path,
                                        idx,
                                        "header-" + key);

                        resolvedHeaders.put(
                                key,
                                resolvedValue);
                    });

            step.setHeaders(
                    resolvedHeaders);
        }

        if (step.getFrom() != null) {
            step.setFrom(resolveLoadTime(
                    tc, step, resolver,
                    step.getFrom(),
                    path, idx, "from"));
        }

        if (step.getTo() != null) {
            step.setTo(resolveLoadTime(
                    tc, step, resolver,
                    step.getTo(),
                    path, idx, "to"));
        }
    }

    private static String resolveLoadTime(
            TestCase tc,
            TestStep step,
            YamlPlaceholderResolver resolver,
            String value,
            String path,
            int idx,
            String field) {

        if (value == null) {
            return null;
        }

        /*
         * B[...] and R[...] may depend on previous runtime steps.
         * Leave the complete value untouched for runtime resolution.
         */
        if (containsRuntimePlaceholder(value)) {
            return value;
        }

        return resolver.resolve(
                tc,
                value,
                tc.getVariables(),
                path,
                idx,
                field,
                step);
    }

    private static boolean containsRuntimePlaceholder(
            String value) {

        if (value == null) {
            return false;
        }

        return value.contains("{B[")
                || value.contains("{R[");
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

        String application = System.getProperty("APPLICATION");

        final String basePath;

        if (ProjectRuntime.isInitialized()) {
            Path moduleDirectory =
                    ProjectRuntime
                            .modulesRoot(application)
                            .resolve(modulePath)
                            .normalize();

            basePath =
                    moduleDirectory.toString()
                            + File.separator;
        } else {
            basePath =
                    "data/"
                            + application
                            + "/modules/"
                            + modulePath
                            + "/";
        }

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

            bodyContent =
                    preferStepParametersOverBufferPlaceholders(
                            bodyContent,
                            step.getParameters());

            // Apply NULL-removal and EMPTY-value handling after B->R compatibility mapping.
            bodyContent = resolveNullableRule(bodyContent, step.getParameters());

            String resolvedBody = resolver.resolve(
                    tc,
                    bodyContent,
                    step.getParameters(),
                    reqFile.getPath(),
                    0,
                    "body",
                    step);

            resolvedBody = resolver.resolve(
                    tc,
                    resolvedBody,
                    tc.getVariables(),
                    reqFile.getPath(),
                    0,
                    "body",
                    step);

            // Store the final JSON or XML text under the "body" key.
            stepMap.put("body", resolvedBody);
        }
        
    }
    

    private static String preferStepParametersOverBufferPlaceholders(
            String body,
            Map<String, Object> parameters) {

        if (body == null
                || parameters == null
                || parameters.isEmpty()) {

            return body;
        }

        Pattern pattern =
                Pattern.compile("\\{B\\[([^\\]]+)]}");

        Matcher matcher =
                pattern.matcher(body);

        StringBuffer sb =
                new StringBuffer();

        boolean changed =
                false;

        while (matcher.find()) {

            String name =
                    matcher.group(1);

            if (!parameters.containsKey(name)) {
                continue;
            }

            String replacement =
                    "{R[" + name + "]}";

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(
                            replacement
                    )
            );

            changed =
                    true;
        }

        if (!changed) {
            return body;
        }

        matcher.appendTail(sb);

        return sb.toString();
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

        System.err.println("File      : " + new File(yamlPath).getAbsolutePath());

        if (e instanceof at.co.svc.agate.core.error.AgateStepException agateError) {

            System.err.println("Reason    : " + agateError.getReason());

            for (Map.Entry<String, String> entry : agateError.getDetails().entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null
                        && value != null
                        && !value.isBlank()) {

                    System.err.printf(
                            "%-10s: %s%n",
                            key,
                            value);
                }
            }

            Throwable cause = agateError.getCause();

            if (cause != null
                    && cause.getMessage() != null
                    && !cause.getMessage().isBlank()) {

                System.err.println(
                        "Why       : " + cleanMessage(cause.getMessage()));
            }

        } else {

            System.err.println(
                    "Reason    : " + cleanMessage(e.getMessage()));
        }

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
                        unquoteYamlScalar(identifier);

                for (int i = 0; i < lines.size(); i++) {

                    String line = lines.get(i);
                    String cleanLine =
                            line.trim();

                    if (cleanLine.startsWith("id:")
                            || cleanLine.startsWith("- id:")
                            || cleanLine.startsWith("name:")) {

                        String actualValue =
                                cleanLine
                                        .substring(cleanLine.indexOf(":") + 1);

                        actualValue =
                                unquoteYamlScalar(actualValue);

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