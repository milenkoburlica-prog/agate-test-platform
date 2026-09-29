package at.co.svc.agate.core.dsl.register;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import at.co.svc.agate.core.project.ProjectRuntime;
public class YamlTestInstantiator {
 private static final boolean CONSOLE_PRINT = true;
 private static class TCObject {
 private final String testId;
 private final Map<String, String> variables =
 new HashMap<>();
 public TCObject(
                String testId) {
 this.testId =
                    testId;
        }
 public void addVariable(
                String key,
                String value) {
            variables.put(
                    key,
                    value);
        }
 public String getTestId() {
 return testId;
        }
 public Map<String, String> getVariables() {
 return variables;
        }
    }
 public void instantiate(
            String appName,
            String templateFileName,
            String csvFileName)
 throws Exception {
        Path appPath =
                ProjectRuntime
                        .applicationRoot(appName)
                        .normalize();

        Path templatePath =
                appPath
                        .resolve("template")
                        .resolve(templateFileName)
                        .normalize();

        Path csvPath =
                appPath
                        .resolve("template")
                        .resolve(csvFileName)
                        .normalize();
        /*
         * ==============================================================
         * Load CSV
         * ==============================================================
         */
        List<List<String>> table =
 new ArrayList<>();
 int expectedColumnCount =
                -1;
 try (BufferedReader br =
                     Files.newBufferedReader(
                             csvPath,
                             StandardCharsets.UTF_8)) {
            String line;
 while ((line = br.readLine()) != null) {
 if (line.trim().isEmpty()) {
 continue;
                }
                String[] values =
                        line.split(
                                ";",
                                -1);
                List<String> row =
 new ArrayList<>();
 for (String val : values) {
                    String cleanedVal =
                            val;
                    /*
                     * Excel CSV escaping.
                     */
                    cleanedVal =
                            cleanedVal.replace(
                                    "\"\"",
                                    "\"");
                    /*
                     * Remove surrounding Excel quotes.
                     */
                    cleanedVal =
                            cleanedVal.replaceAll(
                                    "^\"|\"$",
                                    "");
                    row.add(
                            cleanedVal);
                }
 if (expectedColumnCount == -1) {
                    expectedColumnCount =
                            row.size();
                }
 while (row.size()
                        < expectedColumnCount) {
                    row.add("");
                }
                table.add(
                        row);
            }
        }
 if (table.isEmpty()) {
 throw new IllegalArgumentException(
                    "CSV file contains no data: "
                            + csvPath);
        }
        /*
         * ==============================================================
         * Legacy CSV cleanup
         * ==============================================================
         */
 int expectedColumnCountX =
                table.get(0)
                        .size();
        List<Integer> columnsToRemove =
 new ArrayList<>();
        List<Integer> rowsToRemove =
 new ArrayList<>();
 for (int col = 0;
             col < table.get(0).size();
             col++) {
 int junkRow =
                    -1;
 boolean hasJunk =
 false;
 for (int row = 0;
                 row < table.size();
                 row++) {
                String val =
                        table.get(row)
                                .get(col);
 if ("-\\??=[]{}@áä".equals(val)) {
                    hasJunk =
 true;
                    junkRow =
                            row;
 break;
                }
            }
 if (hasJunk
                    && table.get(junkRow).size()
                    > expectedColumnCountX) {
                columnsToRemove.add(
                        col);
                rowsToRemove.add(
                        junkRow);
            }
        }
 for (int i = 0;
             i < columnsToRemove.size();
             i++) {
 int col =
                    columnsToRemove.get(i);
 int row =
                    rowsToRemove.get(i);
 if (row >= 0
                    && row < table.size()
                    && col >= 0
                    && col < table.get(row).size()) {
                table.get(row)
                        .remove(col);
            }
        }
        /*
         * ==============================================================
         * Convert CSV columns into test-case data objects
         * ==============================================================
         */
        List<TCObject> tcs =
 new ArrayList<>();
 for (int i = 1;
             i < table.get(0).size();
             i++) {
            TCObject tc =
 new TCObject(
                            table.get(0)
                                    .get(i));
 for (int j = 1;
                 j < table.size();
                 j++) {
                String key =
                        table.get(j)
                                .get(0);
                String rawVal =
                        i < table.get(j).size()
                                ? table.get(j).get(i)
                                : "";
                String sanitizedVal =
                        sanitizeValue(
                                rawVal);
                tc.addVariable(
                        key,
                        sanitizedVal);
            }
            tcs.add(
                    tc);
        }
        /*
         * ==============================================================
         * Load template YAML
         * ==============================================================
         */
        Yaml yaml =
 new Yaml();
        Object loaded;
 try (var inputStream =
                     Files.newInputStream(
                             templatePath)) {
            loaded =
                    yaml.load(
                            inputStream);
        }
        List<Map<String, Object>> templateTestCases;
 if (loaded instanceof Map<?, ?> loadedMap) {
            Object testCasesObject =
                    loadedMap.get(
                            "testCases");
 if (!(testCasesObject
 instanceof List<?>)) {
 throw new IllegalArgumentException(
                        "Template contains no valid 'testCases' list: "
                                + templatePath);
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cases =
                    (List<Map<String, Object>>)
                            testCasesObject;
            templateTestCases =
                    cases;
        } else if (loaded instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cases =
                    (List<Map<String, Object>>)
                            loaded;
            templateTestCases =
                    cases;
        } else {
 throw new IllegalArgumentException(
                    "Unsupported template structure: "
                            + templatePath);
        }
 if (templateTestCases == null
                || templateTestCases.isEmpty()) {
 throw new IllegalArgumentException(
                    "Template contains no test cases: "
                            + templatePath);
        }
        /*
         * Currently one template test case is instantiated
         * once for every CSV test-data column.
         */
        Map<String, Object> templateTestCase =
                templateTestCases.get(0);
        Set<String> globalMissingKeys =
 new TreeSet<>();
        List<Map<String, Object>> finalTestCasesList =
 new ArrayList<>();
        /*
         * ==============================================================
         * Instantiate all CSV test cases
         * ==============================================================
         */
 for (TCObject tc : tcs) {
            /*
             * Deep copy template via YAML.
             *
             * Section entries survive this because they are real
             * YAML data:
             *
             * - section: Test data
             *   description: ...
             */
            String tempString =
                    yaml.dump(
                            templateTestCase);
            @SuppressWarnings("unchecked")
            Map<String, Object> newTestCase =
                    yaml.load(
                            tempString);
            newTestCase.put(
                    "id",
                    tc.getTestId());
            newTestCase.put(
                    "stage",
                    "*");
            Set<String> tcMissingKeys =
 new TreeSet<>();
            /*
             * Replace:
             *
             * {XL[...]}
             */
            replaceXLBuffer(
                    newTestCase,
                    tc,
                    tcMissingKeys);
            /*
             * Evaluate template conditions and remove
             * non-applicable steps.
             */
            processStepsAfterInstanciation(
                    newTestCase,
                    tc,
                    tcMissingKeys);

            removeEmptySections(
                    newTestCase);

 if (newTestCase.containsKey(
                    "parameters")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params =
                        (Map<String, Object>)
                                newTestCase.get(
                                        "parameters");
 for (String key :
                        tc.getVariables()
                                .keySet()) {
 if (!params.containsKey(
                            key)) {
                        params.put(
                                key,
                                "{NULL}");
                    }
                }
            }
            globalMissingKeys.addAll(
                    tcMissingKeys);
            finalTestCasesList.add(
                    newTestCase);
        }
        /*
         * ==============================================================
         * Build final YAML
         * ==============================================================
         */
        Map<String, Object> finalYamlOutput =
 new LinkedHashMap<>();
        finalYamlOutput.put(
                "testCases",
                finalTestCasesList);
        DumperOptions options =
 new DumperOptions();
        options.setDefaultFlowStyle(
                DumperOptions.FlowStyle.BLOCK);
        options.setExplicitStart(
 false);
        options.setWidth(
                Integer.MAX_VALUE);
        options.setSplitLines(
 false);
        options.setIndent(
                2);
        options.setIndicatorIndent(
                0);
        Yaml outputYaml =
 new Yaml(
                        options);
        String rawYaml =
                outputYaml.dump(
                        finalYamlOutput);
        /*
         * ==============================================================
         * IMPORTANT:
         *
         * Convert template metadata
         *
         *   - section: Test data
         *     description: Set test data
         *
         * into normal YAML comments BEFORE saving the executable suite.
         *
         * The executable YAML must NEVER contain "section" pseudo-steps.
         * ==============================================================
         */
        String formattedYaml =
                renderSectionMarkers(
                        rawYaml);
        /*
         * Add readable empty lines before normal AGATE steps
         * and before new test cases.
         */
        formattedYaml =
                formattedYaml.replaceAll(
                        "(?m)^  - (?=type:|id:)",
                        "\n  - ");

        formattedYaml =
                formattedYaml.replaceAll(
                        "(?m)^- id:",
                        "\n- id:");
        /*
         * Avoid excessive blank lines.
         */
        formattedYaml =
                formattedYaml.replaceAll(
                        "(\\R){3,}",
                        System.lineSeparator()
                                + System.lineSeparator());
        formattedYaml =
                formattedYaml.replaceFirst(
                        "^\\R+",
                        "");
        String templateNameOnly =
                templateFileName.contains(".")
                        ? templateFileName.substring(
                                0,
                                templateFileName
                                        .lastIndexOf('.'))
                        : templateFileName;
        String newFileName =
                "Instance_"
                        + templateNameOnly
                        + ".yaml";
        Path outputPath =
                appPath
                        .resolve(newFileName)
                        .normalize();
        Files.writeString(
                outputPath,
                formattedYaml,
                StandardCharsets.UTF_8);
        System.out.println(
                "[SUCCESS] Created file: "
                        + outputPath);
 if (!globalMissingKeys.isEmpty()) {
            System.out.println(
                    "[INFO] Missing/empty CSV references detected: "
                            + globalMissingKeys);
        }
    }
    /**
     * Converts an AGATE template section:
     *
     * - section: Preparation
     *   description: Create Dialog
     *
     * into:
     *
     * # --- Preparation: Create Dialog ---
     *
     * Section entries are template metadata only and must never
     * remain executable steps in the generated YAML.
     */
    private String renderSectionMarkers(
            String yaml) {

        if (yaml == null
                || yaml.isEmpty()) {

            return yaml;
        }

        StringBuilder result =
                new StringBuilder();

        String[] lines =
                yaml.split(
                        "\\R",
                        -1);

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i];

            String trimmed =
                    line.trim();

            if (!trimmed.startsWith(
                    "- section:")) {

                result.append(
                        line);

                if (i < lines.length - 1) {

                    result.append(
                            System.lineSeparator());
                }

                continue;
            }

            int dashIndex =
                    line.indexOf('-');

            String indent =
                    dashIndex > 0
                            ? line.substring(
                            0,
                            dashIndex)
                            : "";

            String sectionName =
                    trimmed.substring(
                                    "- section:"
                                            .length())
                            .trim();

            sectionName =
                    removeOuterQuotes(
                            sectionName);

            String description =
                    null;

            if (i + 1 < lines.length) {

                String nextLine =
                        lines[i + 1];

                String nextTrimmed =
                        nextLine.trim();

                if (nextTrimmed.startsWith(
                        "description:")) {

                    description =
                            nextTrimmed.substring(
                                            "description:"
                                                    .length())
                                    .trim();

                    description =
                            removeOuterQuotes(
                                    description);

                    i++;
                }
            }

            if (result.length() > 0
                    && !endsWithBlankLine(
                    result)) {

                result.append(
                        System.lineSeparator());
            }

            result.append(
                            indent)
                    .append(
                            "# --- ")
                    .append(
                            sectionName);

            if (description != null
                    && !description.isBlank()) {

                result.append(
                                ": ")
                        .append(
                                description);
            }

            result.append(
                    " ---");

            if (i < lines.length - 1) {

                result.append(
                        System.lineSeparator());
            }
        }

        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private void removeEmptySections(
            Map<String, Object> testCase) {

        if (testCase == null
                || !testCase.containsKey(
                "steps")) {

            return;
        }

        Object stepsObject =
                testCase.get(
                        "steps");

        if (!(stepsObject
                instanceof List<?>)) {

            return;
        }

        List<Map<String, Object>> steps =
                (List<Map<String, Object>>)
                        stepsObject;

        for (int i = steps.size() - 1;
             i >= 0;
             i--) {

            Map<String, Object> step =
                    steps.get(i);

            if (step == null
                    || !step.containsKey(
                    "section")) {

                continue;
            }

            boolean hasExecutableStep =
                    false;

            for (int j = i + 1;
                 j < steps.size();
                 j++) {

                Map<String, Object> followingStep =
                        steps.get(j);

                if (followingStep == null) {

                    continue;
                }

                if (followingStep.containsKey(
                        "section")) {

                    break;
                }

                if (!followingStep.isEmpty()) {

                    hasExecutableStep =
                            true;

                    break;
                }
            }

            if (!hasExecutableStep) {

                if (CONSOLE_PRINT) {

                    System.out.println(
                            "[SECTION] Removing empty section: "
                                    + step.get(
                                    "section"));
                }

                steps.remove(i);
            }
        }
    }

 private boolean endsWithBlankLine(
            StringBuilder value) {
        String separator =
                System.lineSeparator();
        String doubleSeparator =
                separator
                        + separator;
 return value.toString()
                .endsWith(
                        doubleSeparator);
    }
 private String removeOuterQuotes(
            String value) {
 if (value == null) {
 return null;
        }
        String trimmed =
                value.trim();
 if (trimmed.length() < 2) {
 return trimmed;
        }
 if ((trimmed.startsWith("'")
                && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"")
                && trimmed.endsWith("\""))) {
 return trimmed.substring(
                    1,
                    trimmed.length() - 1);
        }
 return trimmed;
    }
 private String sanitizeValue(
            String val) {
 if (val == null) {
 return "";
        }
 if (val.contains("&")
                || val.contains("<")
                || val.contains(">")
                || val.contains("\"")
                || val.contains("'")) {
 return val;
        }
 if ((val.contains("?")
                || val.contains("[]")
                || val.contains("{}")
                || val.contains("@"))
                && !val.contains(
                "DATE")) {
 return val;
        }
 return val;
    }
 private String xmlEscaping(
            String val) {
 if (val == null) {
 return "";
        }
 return val;
    }
 private void processStepsAfterInstanciation(
            Map<String, Object> testCase,
            TCObject tc,
            Set<String> tcMissingKeys) {
 if (!testCase.containsKey(
                "steps")) {
 return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> steps =
                (List<Map<String, Object>>)
                        testCase.get(
                                "steps");
 for (int i = steps.size() - 1;
             i >= 0;
             i--) {
            Map<String, Object> step =
                    steps.get(i);
            /*
             * ----------------------------------------------------------
             * SECTION
             * ----------------------------------------------------------
             *
             * A section is template metadata, NOT an executable
             * AGATE step.
             *
             * A section may define its own condition:
             *
             * - section: CardToken Preparation SVSV
             *   description: Define CardToken SVSV
             *   condition: 'CardToken.Tokentyp == "SVSV"'
             *
             * The condition controls only whether the section marker
             * itself is kept. It does NOT implicitly control the steps
             * that follow the section.
             *
             * If the condition evaluates to false, the section is
             * removed. If it evaluates to true, the already evaluated
             * condition is removed from the generated executable YAML.
             *
             * The remaining section is later converted into a YAML
             * comment by renderSectionMarkers().
             */
            if (step.containsKey(
                    "section")) {

                Object sectionConditionObject =
                        step.get(
                                "condition");

                String sectionCondition =
                        sectionConditionObject == null
                                ? null
                                : sectionConditionObject.toString();

                if (sectionCondition != null
                        && !sectionCondition.trim().isEmpty()) {

                    boolean sectionEnabled =
                            isConditionSatisfied(
                                    sectionCondition,
                                    tc,
                                    step,
                                    CONSOLE_PRINT);

                    if (!sectionEnabled) {

                        if (CONSOLE_PRINT) {
                            System.out.println(
                                    "[SECTION] Removing section because condition is false: "
                                            + step.get("section")
                                            + " | condition="
                                            + sectionCondition);
                        }

                        steps.remove(i);
                        continue;
                    }

                    /*
                     * The condition has already been resolved during
                     * instantiation and must not remain in the generated
                     * executable YAML.
                     */
                    step.remove(
                            "condition");
                }

                continue;
            }
            String type =
                    String.valueOf(
                            step.getOrDefault(
                                    "type",
                                    "UNKNOWN"));
            String op =
                    String.valueOf(
                            step.getOrDefault(
                                    "op",
                                    "UNKNOWN"));
            /*
             * ==========================================================
             * ASSERT cleanup
             * ==========================================================
             */
 if ("ASSERT".equalsIgnoreCase(type)
                    || "ASSERT".equalsIgnoreCase(op)) {
 boolean shouldRemove =
 false;
                String stepString =
 new Yaml().dump(
                                step);
                String reason =
                        "";
                /*
                 * 1.
                 * Check if an explicit missing key is
                 * referenced by this assertion.
                 */
 for (String missingKey :
                        tcMissingKeys) {
                    Pattern exactPattern =
                            Pattern.compile(
                                    "\\b"
                                            + Pattern.quote(
                                            missingKey)
                                            + "\\b");
 if (exactPattern.matcher(
                                    stepString)
                            .find()) {
                        shouldRemove =
 true;
                        reason =
                                "Missing or empty CSV reference column: ["
                                        + missingKey
                                        + "]";
 break;
                    }
                }
                /*
                 * 2.
                 * Check whether expected was resolved to {NULL}.
                 */
 if (!shouldRemove
                        && step.containsKey(
                        "expected")) {
                    Object expectedVal =
                            step.get(
                                    "expected");
 if (expectedVal
 instanceof String
                            && "{NULL}".equals(
                            ((String) expectedVal)
                                    .trim())) {
                        shouldRemove =
 true;
                        reason =
                                "Expected value resolved to {NULL} from data provider.";
                    }
                }
                /*
                 * 3.
                 * Check whether the condition references
                 * a missing CSV field.
                 */
 if (!shouldRemove
                        && step.containsKey(
                        "condition")) {
                    String condition =
                            String.valueOf(
                                    step.get(
                                            "condition"));
 for (String missingKey :
                            tcMissingKeys) {
                        Pattern exactPattern =
                                Pattern.compile(
                                        "\\b"
                                                + Pattern.quote(
                                                missingKey)
                                                + "\\b");
 if (exactPattern.matcher(
                                        condition)
                                .find()) {
                            shouldRemove =
 true;
                            reason =
                                    "Condition relies on missing or empty CSV column: ["
                                            + missingKey
                                            + "]";
 break;
                        }
                    }
                }
 if (shouldRemove) {
 if (CONSOLE_PRINT) {
                        System.out.println(
                                "[INFO] Removing ASSERT step "
                                        + (i + 1)
                                        + " from '"
                                        + testCase.get(
                                        "id")
                                        + "'");
                        System.out.println(
                                "[REASON] "
                                        + reason);
                        System.out.println(
                                "[REASON] Affected step condition was: "
                                        + step.get(
                                        "condition"));
                    }
                    steps.remove(i);
 continue;
                }
            }
            /*
             * ==========================================================
             * SOAP conditional parameters
             * ==========================================================
             */
 if ("SOAP".equalsIgnoreCase(type)
                    && "EXEC".equalsIgnoreCase(op)) {
                Object parametersObject =
                        step.get(
                                "parameters");
 if (parametersObject
 instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> params =
                            (Map<String, Object>)
                                    parametersObject;
                    Map<String, Object> updatedParams =
 new LinkedHashMap<>();
 for (Map.Entry<String, Object> entry :
                            params.entrySet()) {
                        String key =
                                entry.getKey();
                        Object value =
                                entry.getValue();
                        /*
                         * Conditional value list:
                         *
                         * parameter:
                         *   - condition: ...
                         *     value: ...
                         */
 if (value instanceof List<?>) {
                            List<?> conditionList =
                                    (List<?>) value;
                            Object resolvedValue =
                                    "{NULL}";
 if (CONSOLE_PRINT) {
                                System.out.println(
                                        "\n[DEBUG-Klip] Provera liste uslova za parametar: "
                                                + key);
                            }
 for (Object conditionObject :
                                    conditionList) {
 if (!(conditionObject
 instanceof Map<?, ?>)) {
 continue;
                                }
                                Map<?, ?> conditionMap =
                                        (Map<?, ?>)
                                                conditionObject;
                                Object conditionValue =
                                        conditionMap.get(
                                                "condition");
                                String condition =
                                        conditionValue == null
                                                ? null
                                                : conditionValue
                                                .toString();
                                Object rawValue =
                                        conditionMap.get(
                                                "value");
 if (CONSOLE_PRINT) {
                                    System.out.println(
                                            "  -> Evaluiram uslov: ["
                                                    + condition
                                                    + "] | Trenutna vrednost 'value' u šablonu: ["
                                                    + rawValue
                                                    + "]");
                                }
 if (isConditionSatisfied(
                                        condition,
                                        tc,
                                        step,
 CONSOLE_PRINT)) {
                                    resolvedValue =
                                            rawValue;
 if (CONSOLE_PRINT) {
                                        System.out.println(
                                                "  ✔ USLOV PROŠAO! Izabrana vrednost za "
                                                        + key
                                                        + " je: ["
                                                        + resolvedValue
                                                        + "]");
                                    }
 break;
                                } else if (CONSOLE_PRINT) {
                                    System.out.println(
                                            "  ❌ Uslov nije ispunjen.");
                                }
                            }
 if (CONSOLE_PRINT) {
                                System.out.println(
                                        "[DEBUG-Klip] Finalno dodeljen "
                                                + key
                                                + " u updatedParams: ["
                                                + resolvedValue
                                                + "]\n");
                            }
                            updatedParams.put(
                                    key,
                                    resolvedValue);
                        } else if (value
 instanceof String) {
                            String escapedValue =
                                    xmlEscaping(
                                            (String) value);
                            updatedParams.put(
                                    key,
                                    escapedValue);
                        } else {
                            updatedParams.put(
                                    key,
                                    value);
                        }
                    }
                    step.put(
                            "parameters",
                            updatedParams);
                }
            }
            /*
             * ==========================================================
             * Generic step condition
             * ==========================================================
             */
            Object conditionObject =
                    step.get(
                            "condition");
            String condition =
                    conditionObject == null
                            ? null
                            : conditionObject.toString();
 if (CONSOLE_PRINT) {
                System.out.println(
                        "condition="
                                + condition);
            }
 if (condition != null
                    && !condition.trim().isEmpty()) {
 if (!isConditionSatisfied(
                        condition,
                        tc,
                        step,
 CONSOLE_PRINT)) {
                    steps.remove(i);
                } else {
                    /*
                     * Condition was already evaluated during
                     * instantiation.
                     *
                     * The generated executable test does not
                     * need it anymore.
                     */
                    step.remove(
                            "condition");
                }
            }
        }
    }
 private boolean isConditionSatisfied(
            String condition,
            TCObject tc,
            Map<String, Object> step,
            Boolean consolePrint) {
 if (condition == null
                || condition.trim().isEmpty()) {
 return true;
        }
        condition =
                condition.trim();
        /*
         * Remove only outer quoting introduced by
         * YAML / Tosca conversion.
         */
 if (condition.startsWith("'''")
                && condition.endsWith("'''")
                && condition.length() > 6) {
            condition =
                    condition.substring(
                            3,
                            condition.length() - 3);
        } else if (condition.startsWith("'")
                && condition.endsWith("'")
                && condition.length() > 2) {
            condition =
                    condition.substring(
                            1,
                            condition.length() - 1);
        } else if (condition.startsWith("\"")
                && condition.endsWith("\"")
                && condition.length() > 2) {
            condition =
                    condition.substring(
                            1,
                            condition.length() - 1);
        }
        /*
         * Tosca sometimes quotes CSV parameter names:
         *
         * 'CardTokenDMP.TokenWert' != NULL
         *
         * They represent variable names, not string values.
         */
        condition =
                condition.replaceAll(
                        "'([a-zA-Z0-9_.-]+)'",
                        "$1");
        /*
         * Normalize Tosca syntax to JEXL.
         */
        String processedCondition =
                condition
                        .replaceAll(
                                "(?i)'\\{NULL\\}'",
                                "null")
                        .replaceAll(
                                "(?i)\"\\{NULL\\}\"",
                                "null")
                        .replaceAll(
                                "(?i)\\{NULL\\}",
                                "null")
                        .replaceAll(
                                "(?i)\\bNULL\\b",
                                "null")
                        .replaceAll(
                                "(?i)\\bTRUE\\b",
                                "true")
                        .replaceAll(
                                "(?i)\\bFALSE\\b",
                                "false")
                        .replaceAll(
                                "(?i)\\s+AND\\s+",
                                " && ")
                        .replaceAll(
                                "(?i)\\s+OR\\s+",
                                " || ")
                        .trim();
        JexlContext context =
 new MapContext();
        context.set(
                "null",
 null);
        context.set(
                "true",
                Boolean.TRUE);
        context.set(
                "false",
                Boolean.FALSE);
        /*
         * Longest keys first.
         *
         * Example:
         *
         * DialogException
         * DialogException.InfoCode
         */
        List<String> keys =
 new ArrayList<>(
                        tc.getVariables()
                                .keySet());
        keys.sort(
                (a, b) ->
                        Integer.compare(
                                b.length(),
                                a.length()));
        Map<String, Object> mappedVariablesForLog =
 new LinkedHashMap<>();
 int variableCounter =
                0;
 for (String originalKey :
                keys) {
            Pattern keyPattern =
                    Pattern.compile(
                            "(?<![a-zA-Z0-9_.-])"
                                    + Pattern.quote(
                                    originalKey)
                                    + "(?![a-zA-Z0-9_.-])");
            java.util.regex.Matcher matcher =
                    keyPattern.matcher(
                            processedCondition);
 if (!matcher.find()) {
 continue;
            }
            String safeJexlVariable =
                    "VAR_"
                            + variableCounter++;
            String value =
                    tc.getVariables()
                            .get(
                                    originalKey);
            Object contextValue;
 if (value == null
                    || value.trim().isEmpty()
                    || value.equalsIgnoreCase(
                    "NULL")
                    || value.equalsIgnoreCase(
                    "{NULL}")) {
                contextValue =
 null;
            } else if (value.equalsIgnoreCase(
                    "{EMPTY}")) {
                contextValue =
                        "";
            } else {
                contextValue =
                        value;
            }
            context.set(
                    safeJexlVariable,
                    contextValue);
            processedCondition =
                    matcher.replaceAll(
                            java.util.regex.Matcher
                                    .quoteReplacement(
                                            safeJexlVariable));
            mappedVariablesForLog.put(
                    originalKey
                            + " ("
                            + safeJexlVariable
                            + ")",
                    contextValue);
        }
 if (Boolean.TRUE.equals(
                consolePrint)) {
            System.out.println(
                    "\n--- [CONDITION EVALUATION DETAIL] ---");
            System.out.println(
                    "  [RAW TEMPLATE COND] : "
                            + condition);
            System.out.println(
                    "  [JEXL ENGINE EXEC]  : "
                            + processedCondition);
 if (!mappedVariablesForLog.isEmpty()) {
                System.out.println(
                        "  [CSV VARIABLES]");
 for (Map.Entry<String, Object> entry :
                        mappedVariablesForLog.entrySet()) {
                    String displayValue =
                            entry.getValue() == null
                                    ? "null"
                                    : "'"
                                    + entry.getValue()
                                    + "'";
                    System.out.println(
                            "    "
                                    + entry.getKey()
                                    + " = "
                                    + displayValue);
                }
            }
        }
 try {
            JexlEngine jexl =
 new JexlBuilder()
                            .silent(false)
                            .strict(false)
                            .create();
            JexlExpression expression =
                    jexl.createExpression(
                            processedCondition);
            Object result =
                    expression.evaluate(
                            context);
 boolean finalResult =
                    result instanceof Boolean
                            ? (Boolean) result
                            : result != null;
 if (Boolean.TRUE.equals(
                    consolePrint)) {
                System.out.println(
                        "  [EVALUATION RESULT] : "
                                + (
                                finalResult
                                        ? "TRUE (Step will be KEPT)"
                                        : "FALSE (Step will be REMOVED)"
                        ));
                System.out.println(
                        "-------------------------------------\n");
            }
 return finalResult;
        } catch (Exception e) {
 if (Boolean.TRUE.equals(
                    consolePrint)) {
                System.err.println(
                        "  [EVALUATION ERROR]  : "
                                + e.getMessage());
                System.err.println(
                        "  [EXPRESSION]        : "
                                + processedCondition);
                System.err.println(
                        "-------------------------------------\n");
            }
 return false;
        }
    }
 private void replaceXLBuffer(
            Object object,
            TCObject tc,
            Set<String> missingKeys) {
 if (object instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map =
                    (Map<String, Object>)
                            object;
 for (Map.Entry<String, Object> entry :
                    map.entrySet()) {
                Object value =
                        entry.getValue();
 if (value instanceof String) {
                    String stringValue =
                            (String) value;
                    /*
                     * Replace ONLY explicit XL references:
                     *
                     * {XL[id]}
                     * {XL[name]}
                     * {XL[SVPerson.SVNR]}
                     */
 if (stringValue.contains(
                            "{XL[")) {
                        entry.setValue(
                                processString(
                                        stringValue,
                                        tc,
                                        missingKeys));
                    }
                } else {
                    replaceXLBuffer(
                            value,
                            tc,
                            missingKeys);
                }
            }
        } else if (object instanceof List<?>) {
 for (Object item :
                    (List<?>) object) {
                replaceXLBuffer(
                        item,
                        tc,
                        missingKeys);
            }
        }
    }
 private String processString(
            String value,
            TCObject tc,
            Set<String> missingKeys) {
        Pattern pattern =
                Pattern.compile(
                        "\\{XL\\[(.*?)\\]\\}");
        java.util.regex.Matcher matcher =
                pattern.matcher(
                        value);
        StringBuilder result =
 new StringBuilder();
 int lastEnd =
                0;
 while (matcher.find()) {
            result.append(
                    value,
                    lastEnd,
                    matcher.start());
            String key =
                    matcher.group(1);
 if (!tc.getVariables()
                    .containsKey(
                            key)) {
                missingKeys.add(
                        key);
            }
            String resolvedValue =
                    tc.getVariables()
                            .get(
                                    key);
 if (resolvedValue == null
                    || resolvedValue.isEmpty()
                    || resolvedValue.equalsIgnoreCase(
                    "NULL")) {
                result.append(
                        "{NULL}");
            } else if (resolvedValue.equalsIgnoreCase(
                    "{EMPTY}")) {
                result.append(
                        "{EMPTY}");
            } else {
                result.append(
                        resolvedValue);
            }
            lastEnd =
                    matcher.end();
        }
        result.append(
                value.substring(
                        lastEnd));
 return result.length() == 0
                ? "{NULL}"
                : result.toString();
    }
    @SuppressWarnings("unused")
 private String processValue(
            String value) {
 if (value == null
                || value.isEmpty()
                || value.equalsIgnoreCase(
                "NULL")) {
 return "{NULL}";
        }
 if (value.equalsIgnoreCase(
                "{EMPTY}")) {
 return "{EMPTY}";
        }
 return value;
    }
}
