package at.co.svc.agate.core.dsl.resolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.utils.CsvLoader;
import at.co.svc.agate.core.env.EnvironmentManager;
import at.co.svc.agate.core.error.AgateStepException;

/**
 * Resolves placeholders within YAML test definitions.
 *
 * Supported placeholders:
 * {B[var]}              - Base/local variables
 * {T[table.column]}     - Table-driven data from CSV files
 * {E[path]}             - Environment/system configurations
 * {R[var]}              - Reusable parameters (from CALL step)
 * {DATE[][][format]}    - Tosca compatible dynamic dates
 * {DATETIME[][][format]}
 * {RND[n]}
 *
 * Error handling rule:
 * Recognized AGATE placeholder syntax must never silently remain unresolved.
 */
public class YamlPlaceholderResolver {

    private static final Pattern B_PATTERN =
            Pattern.compile("\\{B\\[(.+?)]}");

    private static final Pattern T_PATTERN =
            Pattern.compile("\\{T\\[(.+?)]}");

    private static final Pattern E_PATTERN =
            Pattern.compile("\\{E\\[(.+?)]}");

    private static final Pattern R_PATTERN =
            Pattern.compile("\\{R\\[(.+?)]}");

    private static final Pattern RND_PATTERN =
            Pattern.compile("\\{RND\\[(\\d+)\\]\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATE_EXPRESSION_PATTERN =
            Pattern.compile("\\{(DATE|DATETIME)(\\[[^\\]]*\\])(\\[[^\\]]*\\])(\\[[^\\]]*\\])\\}",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern DATE_PREFIX_PATTERN =
            Pattern.compile("\\{(?:DATE|DATETIME)", Pattern.CASE_INSENSITIVE);

    private static final Pattern B_PREFIX_PATTERN =
            Pattern.compile("\\{B\\[", Pattern.CASE_INSENSITIVE);

    private static final Pattern E_PREFIX_PATTERN =
            Pattern.compile("\\{E\\[", Pattern.CASE_INSENSITIVE);

    private static final Pattern R_PREFIX_PATTERN =
            Pattern.compile("\\{R\\[", Pattern.CASE_INSENSITIVE);

    private static final Pattern T_PREFIX_PATTERN =
            Pattern.compile("\\{T\\[", Pattern.CASE_INSENSITIVE);

    private static final Pattern OFFSET_PART_PATTERN =
            Pattern.compile("[+-]?\\d+[dMyHms]", Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter BASE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private final Map<String, Set<String>> warnedPlaceholders = new HashMap<>();

    /**
     * Extended method which also receives TestStep so R variables can be resolved.
     */
    public String resolve(
            TestCase tc,
            String value,
            Map<String, Object> variables,
            String yamlPath,
            int stepIndex,
            String originalAction,
            TestStep currentStep) {

        if (value == null) {
            return null;
        }

        try {
            Map<String, String> vars = new HashMap<>();

            if (variables != null) {
                variables.forEach((k, v) -> {
                    if (k != null && v != null) {
                        vars.put(k, v.toString());
                    }
                });
            }

            String result = value;

            if (result.isEmpty()) {
                return result;
            }

            validateMalformedKnownPlaceholders(
                    result,
                    tc,
                    stepIndex,
                    originalAction);

            if ("body".equals(originalAction)) {
                result = resolveBodyComplexVariables(
                        result,
                        variables);
            }

            // Order is important: R first, then B/T/E.
            result = resolveR(
                    result,
                    currentStep,
                    tc,
                    stepIndex,
                    originalAction);

            result = resolveB(
                    result,
                    vars,
                    tc,
                    stepIndex,
                    originalAction);

            result = resolveT(
                    tc,
                    result,
                    yamlPath,
                    stepIndex,
                    originalAction);

            result = resolveE(
                    tc,
                    result,
                    stepIndex,
                    originalAction);

            // R parameters may themselves contain B/T/E placeholders.
            if (result != null && result.contains("{")) {
                result = resolveB(
                        result,
                        vars,
                        tc,
                        stepIndex,
                        originalAction);

                result = resolveT(
                        tc,
                        result,
                        yamlPath,
                        stepIndex,
                        originalAction);

                result = resolveE(
                        tc,
                        result,
                        stepIndex,
                        originalAction);
            }

            result = resolveToscaDate(
                    result,
                    tc,
                    stepIndex,
                    originalAction);

            result = resolveD(result);

            validateNoUnresolvedKnownPlaceholders(
                    result,
                    tc,
                    stepIndex,
                    originalAction);

            return result;

        } catch (AgateStepException e) {
            throw e;

        } catch (Exception e) {
            throw AgateStepException.builder("Placeholder resolution failed")
                    .detail("Value", value)
                    .detail("Action", originalAction)
                    .detail("Step", stepIndex > 0 ? stepIndex : null)
                    .detail("Technical error", safeMessage(e))
                    .hint("Check the placeholder syntax and referenced variables.")
                    .cause(e)
                    .build();
        }
    }

    private String resolveBodyComplexVariables(
            String body,
            Map<String, Object> variables) {

        if (body == null || variables == null || variables.isEmpty()) {
            return body;
        }

        String result = body;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof Map) {
                result = resolveNestedMap(
                        result,
                        entry.getKey(),
                        (Map<?, ?>) value);
            }

            if (value instanceof List) {
                result = resolveList(
                        result,
                        entry.getKey(),
                        (List<?>) value);
            }
        }

        return result;
    }

    private String resolveNestedMap(
            String body,
            String rootName,
            Map<?, ?> map) {

        String result = body;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String path =
                    rootName + "." + entry.getKey();

            String placeholder =
                    "{B[" + path + "]}";

            Object value = entry.getValue();

            if (value != null) {
                result = result.replace(
                        placeholder,
                        value.toString());
            }
        }

        return result;
    }

    private String resolveList(
            String body,
            String key,
            List<?> list) {

        String placeholder =
                "{B[" + key + "]}";

        String jsonValue =
                toJsonLikeString(list);

        return body.replace(
                placeholder,
                jsonValue);
    }

    private String toJsonLikeString(List<?> list) {

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);

            if (i > 0) {
                sb.append(",");
            }

            if (item instanceof Map) {
                sb.append(mapToJson((Map<?, ?>) item));
            } else {
                sb.append("\"")
                        .append(item == null ? "" : item.toString())
                        .append("\"");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private String mapToJson(Map<?, ?> map) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int counter = 0;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (counter > 0) {
                sb.append(",");
            }

            sb.append("\"")
                    .append(entry.getKey())
                    .append("\":");

            Object value = entry.getValue();

            if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"")
                        .append(value)
                        .append("\"");
            }

            counter++;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Overload for compatibility with older calls which do not have currentStep.
     */
    public String resolve(
            TestCase tc,
            String value,
            Map<String, Object> variables,
            String yamlPath,
            int stepIndex,
            String originalAction) {

        return resolve(
                tc,
                value,
                variables,
                yamlPath,
                stepIndex,
                originalAction,
                null);
    }

    /**
     * Resolves Tosca dynamic dates.
     */
    private String resolveToscaDate(
            String value,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        if (value == null) {
            return null;
        }

        String upper = value.toUpperCase();

        if (!upper.contains("{DATE")
                && !upper.contains("{DATETIME")) {
            return value;
        }

        validateToscaDateExpressions(
                value,
                tc,
                stepIndex,
                originalAction);

        try {
            return ToscaDateResolver.resolve(value);

        } catch (AgateStepException e) {
            throw e;

        } catch (Exception e) {
            throw AgateStepException.builder("Invalid DATE/DATETIME expression")
                    .actual(extractFirstDateExpression(value))
                    .detail("Action", originalAction)
                    .detail("Step", stepIndex > 0 ? stepIndex : null)
                    .detail("Technical error", safeMessage(e))
                    .hint("Check base date, offset and format. Example: {DATE[22.01.2025][+2d][dd.MM.yyyy]}")
                    .cause(e)
                    .build();
        }
    }

    private String resolveR(
            String value,
            TestStep currentStep,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        if (value == null) {
            return null;
        }
//        System.out.println(
//                "[DEBUG-R] step=" +
//                (currentStep != null ? currentStep.getId() : "null")
//                + ", type=" +
//                (currentStep != null ? currentStep.getType() : "null")
//                + ", params=" +
//                (currentStep != null ? currentStep.getParameters() : "null")
//                + ", value=" + value
//        );
        Matcher matcher = R_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);

            if (paramName == null || paramName.isBlank()) {
                throw missingPlaceholderName(
                        "R",
                        matcher.group(0),
                        tc,
                        stepIndex,
                        originalAction);
            }

            if (currentStep == null
                    || currentStep.getParameters() == null
                    || !currentStep.getParameters().containsKey(paramName)
                    || currentStep.getParameters().get(paramName) == null) {

                throw unresolvedPlaceholder(
                        "Reusable parameter was not found",
                        matcher.group(0),
                        paramName,
                        tc,
                        stepIndex,
                        originalAction,
                        "Make sure the CALL/reusable step provides parameter '" + paramName + "'.");
            }

            Object paramValue =
                    currentStep.getParameters().get(paramName);

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(paramValue.toString()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveB(
            String value,
            Map<String, String> variables,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        if (value == null) {
            return null;
        }

        Matcher matcher =
                B_PATTERN.matcher(value);

        StringBuilder sb =
                new StringBuilder();

        while (matcher.find()) {

            String varName =
                    matcher.group(1);

            if (varName == null
                    || varName.isBlank()) {

                throw missingPlaceholderName(
                        "B",
                        matcher.group(0),
                        tc,
                        stepIndex,
                        originalAction);
            }

            String varValue = null;

            /*
             * 1. Runtime variables have priority.
             */
            if (variables != null
                    && variables.containsKey(varName)
                    && variables.get(varName) != null) {

                varValue =
                        variables.get(varName);
            }

            /*
             * 2. Fallback to variables defined
             *    directly on the TestCase.
             */
            if (varValue == null
                    && tc != null
                    && tc.getVariables() != null
                    && tc.getVariables().containsKey(varName)) {

                Object tcValue =
                        tc.getVariables().get(varName);

                if (tcValue != null) {

                    varValue =
                            String.valueOf(tcValue);
                }
            }

            /*
             * 3. Variable was not found in either source.
             */
            if (varValue == null) {

                throw unresolvedPlaceholder(
                        "Buffer variable was not found",
                        matcher.group(0),
                        varName,
                        tc,
                        stepIndex,
                        originalAction,
                        "Make sure variable '"
                                + varName
                                + "' is defined before it is referenced.");
            }

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(
                            varValue));
        }

        matcher.appendTail(sb);

        return sb.toString();
    }
    
    
    private String resolveT(
            TestCase tc,
            String value,
            String yamlPath,
            int stepIndex,
            String originalAction) {

        if (value == null) {
            return null;
        }

        Matcher matcher = T_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();

        String baseDir = "";

        if (yamlPath != null) {
            int lastSlash =
                    Math.max(
                            yamlPath.lastIndexOf("/"),
                            yamlPath.lastIndexOf("\\"));

            if (lastSlash != -1) {
                baseDir =
                        yamlPath.substring(0, lastSlash);
            }
        }

        while (matcher.find()) {
            String fullPath = matcher.group(1);

            if (fullPath == null || fullPath.isBlank()) {
                throw missingPlaceholderName(
                        "T",
                        matcher.group(0),
                        tc,
                        stepIndex,
                        originalAction);
            }

            String replacement = null;

            try {
                String[] parts =
                        fullPath.split("\\.");

                if (parts.length != 2
                        || parts[0].isBlank()
                        || parts[1].isBlank()) {

                    throw AgateStepException.builder("Invalid table placeholder")
                            .actual(matcher.group(0))
                            .detail("Expected syntax", "{T[table.column]}")
                            .detail("Action", originalAction)
                            .hint("Use exactly one table name and one column name.")
                            .build();
                }

                String tableName = parts[0];
                String columnName = parts[1];

                Object tcIdObj =
                        tc != null && tc.getVariables() != null
                                ? tc.getVariables().get("TC_ID")
                                : null;

                if (tcIdObj == null) {
                    throw AgateStepException.builder("Table placeholder cannot be resolved")
                            .detail("Placeholder", matcher.group(0))
                            .field("TC_ID")
                            .hint("Table-driven placeholders require TC_ID in the test-case variables.")
                            .build();
                }

                String tcId =
                        tcIdObj.toString();

                String csvPath =
                        baseDir + "/variables/" + tableName + ".csv";

                List<Map<String, String>> dataRows =
                        CsvLoader.load(csvPath);

                if (dataRows != null) {
                    replacement =
                            dataRows.stream()
                                    .filter(row -> {
                                        String idValueInRow =
                                                row.entrySet().stream()
                                                        .filter(entry ->
                                                                entry.getKey().equalsIgnoreCase("id")
                                                                        || entry.getKey().equalsIgnoreCase("TC_ID"))
                                                        .map(Map.Entry::getValue)
                                                        .findFirst()
                                                        .orElse(null);

                                        return idValueInRow != null
                                                && tcId.equalsIgnoreCase(idValueInRow);
                                    })
                                    .map(row ->
                                            row.entrySet().stream()
                                                    .filter(entry ->
                                                            entry.getKey().equalsIgnoreCase(columnName))
                                                    .map(Map.Entry::getValue)
                                                    .findFirst()
                                                    .orElse(null))
                                    .findFirst()
                                    .orElse(null);
                }

            } catch (AgateStepException e) {
                throw e;

            } catch (Exception e) {
                throw AgateStepException.builder("Table placeholder could not be resolved")
                        .detail("Placeholder", matcher.group(0))
                        .detail("Technical error", safeMessage(e))
                        .hint("Check the CSV file, TC_ID and requested column.")
                        .cause(e)
                        .build();
            }

            if (replacement != null) {
                replacement =
                        resolveD(replacement);

                matcher.appendReplacement(
                        sb,
                        Matcher.quoteReplacement(replacement));

            } else {
                // Preserve the historical warning as additional console information,
                // but no longer allow unresolved AGATE placeholders to silently pass.
                logWarning(
                        tc != null ? tc.getName() : "",
                        stepIndex,
                        "{T[" + fullPath + "]}",
                        originalAction);

                throw unresolvedPlaceholder(
                        "Table value was not found",
                        matcher.group(0),
                        fullPath,
                        tc,
                        stepIndex,
                        originalAction,
                        "Check the CSV table, TC_ID and column name.");
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveE(
            TestCase tc,
            String value,
            int stepIndex,
            String originalAction) {

        if (value == null) {
            return null;
        }

        Matcher matcher = E_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String path = matcher.group(1);

            if (path == null || path.isBlank()) {
                throw missingPlaceholderName(
                        "E",
                        matcher.group(0),
                        tc,
                        stepIndex,
                        originalAction);
            }

            Object replacement;

            if (path.startsWith("env.")) {
                String key =
                        path.substring(4);

                if (key.isBlank()) {
                    throw AgateStepException.builder("Invalid environment placeholder")
                            .actual(matcher.group(0))
                            .hint("Use syntax {E[env.<property>]}.")
                            .build();
                }

                replacement =
                        EnvironmentManager.getEnvValue(key);

            } else if (path.startsWith("users.")) {
                String key =
                        path.substring(6);

                if (key.isBlank()) {
                    throw AgateStepException.builder("Invalid user placeholder")
                            .actual(matcher.group(0))
                            .hint("Use syntax {E[users.<property>]}.")
                            .build();
                }

                replacement =
                        EnvironmentManager.getReaderValue(key);

            } else {
                throw AgateStepException.builder("Unsupported environment placeholder")
                        .actual(matcher.group(0))
                        .hint("Supported namespaces: env., users.")
                        .build();
            }

            if (replacement == null) {
                throw unresolvedPlaceholder(
                        "Environment value was not found",
                        matcher.group(0),
                        path,
                        tc,
                        stepIndex,
                        originalAction,
                        "Check that the referenced environment/user property exists.");
            }

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(replacement.toString()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveD(String value) {

        if (value == null) {
            return null;
        }

        if (value.contains("{D")) {
            return value;
        }

        Matcher matcher =
                RND_PATTERN.matcher(value);

        StringBuilder resultSb =
                new StringBuilder();

        Random random =
                new Random();

        while (matcher.find()) {
            int length =
                    Integer.parseInt(matcher.group(1));

            if (length > 0) {
                StringBuilder randomNumberStr =
                        new StringBuilder();

                randomNumberStr.append(
                        random.nextInt(9) + 1);

                for (int i = 1; i < length; i++) {
                    randomNumberStr.append(
                            random.nextInt(10));
                }

                matcher.appendReplacement(
                        resultSb,
                        randomNumberStr.toString());

            } else {
                matcher.appendReplacement(
                        resultSb,
                        "");
            }
        }

        matcher.appendTail(resultSb);
        value = resultSb.toString();

        Object parsedValue =
                DataValueParser.parse(value);

        return parsedValue != null
                ? parsedValue.toString()
                : "";
    }

    private void validateMalformedKnownPlaceholders(
            String value,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        if (value == null || value.isEmpty()) {
            return;
        }
        value = normalizeDateExpression(value);
        
        validateSimplePlaceholderSyntax(
                value,
                "B",
                B_PREFIX_PATTERN,
                B_PATTERN,
                tc,
                stepIndex,
                originalAction);

        validateSimplePlaceholderSyntax(
                value,
                "E",
                E_PREFIX_PATTERN,
                E_PATTERN,
                tc,
                stepIndex,
                originalAction);

        validateSimplePlaceholderSyntax(
                value,
                "R",
                R_PREFIX_PATTERN,
                R_PATTERN,
                tc,
                stepIndex,
                originalAction);

        validateSimplePlaceholderSyntax(
                value,
                "T",
                T_PREFIX_PATTERN,
                T_PATTERN,
                tc,
                stepIndex,
                originalAction);

        if (DATE_PREFIX_PATTERN.matcher(value).find()) {
            validateToscaDateExpressions(
                    value,
                    tc,
                    stepIndex,
                    originalAction);
        }
    }

    private void validateSimplePlaceholderSyntax(
            String value,
            String placeholderType,
            Pattern prefixPattern,
            Pattern validPattern,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        Matcher prefixMatcher =
                prefixPattern.matcher(value);

        while (prefixMatcher.find()) {
            int start =
                    prefixMatcher.start();

            Matcher validMatcher =
                    validPattern.matcher(value);

            validMatcher.region(
                    start,
                    value.length());

            if (!validMatcher.lookingAt()) {
                String fragment =
                        extractPlaceholderFragment(
                                value,
                                start);

                throw AgateStepException.builder("Malformed " + placeholderType + " placeholder")
                        .actual(fragment)
                        .detail("Action", originalAction)
                        .detail("Step", stepIndex > 0 ? stepIndex : null)
                        .hint("Check brackets and closing '}'.")
                        .build();
            }
        }
    }

    private String normalizeDateExpression(String value) {

        if (value == null) {
            return null;
        }

        return value
                .replace("{DATETIME}", "{DATETIME[][][]}")
                .replace("{DATE}", "{DATE[][][]}");
    }
    
    
    private void validateToscaDateExpressions(
            String value,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        value = normalizeDateExpression(value);
        
        Matcher prefixMatcher =
                DATE_PREFIX_PATTERN.matcher(value);

        int searchFrom = 0;

        while (prefixMatcher.find(searchFrom)) {
            int start =
                    prefixMatcher.start();

            Matcher expressionMatcher =
                    DATE_EXPRESSION_PATTERN.matcher(value);

            expressionMatcher.region(
                    start,
                    value.length());

            if (!expressionMatcher.lookingAt()) {
                throw AgateStepException.builder("Malformed DATE/DATETIME expression")
                        .actual(extractPlaceholderFragment(value, start))
                        .detail("Action", originalAction)
                        .detail("Step", stepIndex > 0 ? stepIndex : null)
                        .hint("Use syntax {DATE[baseDate][offset][format]} or {DATETIME[baseDate][offset][format]}.")
                        .build();
            }

            String fullExpression =
                    expressionMatcher.group(0);

            String baseDate =
                    stripBrackets(expressionMatcher.group(2));

            String offset =
                    stripBrackets(expressionMatcher.group(3));

            String format =
                    stripBrackets(expressionMatcher.group(4));

            validateBaseDate(
                    fullExpression,
                    baseDate,
                    originalAction,
                    stepIndex);

            validateOffset(
                    fullExpression,
                    offset,
                    originalAction,
                    stepIndex);

            validateDateFormat(
                    fullExpression,
                    format,
                    originalAction,
                    stepIndex);

            searchFrom =
                    expressionMatcher.end();
        }
    }

    private void validateBaseDate(
            String expression,
            String baseDate,
            String originalAction,
            int stepIndex) {

        if (baseDate == null || baseDate.isBlank()) {
            return;
        }

        try {
            LocalDate.parse(
                    baseDate,
                    BASE_DATE_FORMAT);

        } catch (DateTimeParseException e) {
            throw AgateStepException.builder("Invalid DATE/DATETIME base date")
                    .actual(baseDate)
                    .detail("Expression", expression)
                    .detail("Action", originalAction)
                    .detail("Step", stepIndex > 0 ? stepIndex : null)
                    .hint("Use a valid base date in dd.MM.yyyy format, for example 22.01.2025.")
                    .cause(e)
                    .build();
        }
    }

    private void validateOffset(
            String expression,
            String offset,
            String originalAction,
            int stepIndex) {

        if (offset == null || offset.isBlank()) {
            return;
        }

        String normalized =
                offset.replace(" ", "");

        int pos = 0;

        Matcher matcher =
                OFFSET_PART_PATTERN.matcher(normalized);

        while (matcher.find()) {
            if (matcher.start() != pos) {
                throw invalidOffset(
                        expression,
                        offset,
                        originalAction,
                        stepIndex);
            }

            pos = matcher.end();
        }

        if (pos != normalized.length()) {
            throw invalidOffset(
                    expression,
                    offset,
                    originalAction,
                    stepIndex);
        }
    }

    private AgateStepException invalidOffset(
            String expression,
            String offset,
            String originalAction,
            int stepIndex) {

        return AgateStepException.builder("Invalid DATE/DATETIME offset")
                .actual(offset)
                .detail("Expression", expression)
                .detail("Action", originalAction)
                .detail("Step", stepIndex > 0 ? stepIndex : null)
                .hint("Use offsets such as +10d, -2d, +6M+1d, +2H or -30m.")
                .build();
    }

    private void validateDateFormat(
            String expression,
            String format,
            String originalAction,
            int stepIndex) {

        if (format == null || format.isBlank()) {
            return;
        }

        try {
            DateTimeFormatter.ofPattern(format);

        } catch (IllegalArgumentException e) {
            throw AgateStepException.builder("Invalid DATE/DATETIME format")
                    .actual(format)
                    .detail("Expression", expression)
                    .detail("Action", originalAction)
                    .detail("Step", stepIndex > 0 ? stepIndex : null)
                    .hint("Use a valid Java date/time pattern, for example dd.MM.yyyy or dd-MM-yyyy HH:mm:ss.")
                    .cause(e)
                    .build();
        }
    }

    private void validateNoUnresolvedKnownPlaceholders(
            String value,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        if (value == null || value.isEmpty()) {
            return;
        }

        if (B_PREFIX_PATTERN.matcher(value).find()) {
            throw unresolvedAfterResolution(
                    value,
                    "B",
                    originalAction,
                    stepIndex);
        }

        if (E_PREFIX_PATTERN.matcher(value).find()) {
            throw unresolvedAfterResolution(
                    value,
                    "E",
                    originalAction,
                    stepIndex);
        }

        if (R_PREFIX_PATTERN.matcher(value).find()) {
            throw unresolvedAfterResolution(
                    value,
                    "R",
                    originalAction,
                    stepIndex);
        }

        if (T_PREFIX_PATTERN.matcher(value).find()) {
            throw unresolvedAfterResolution(
                    value,
                    "T",
                    originalAction,
                    stepIndex);
        }

        if (DATE_PREFIX_PATTERN.matcher(value).find()) {
            throw unresolvedAfterResolution(
                    value,
                    "DATE/DATETIME",
                    originalAction,
                    stepIndex);
        }
    }

    private AgateStepException unresolvedAfterResolution(
            String value,
            String type,
            String originalAction,
            int stepIndex) {

        return AgateStepException.builder("Placeholder could not be resolved")
                .actual(extractFirstKnownPlaceholder(value))
                .detail("Type", type)
                .detail("Action", originalAction)
                .detail("Step", stepIndex > 0 ? stepIndex : null)
                .hint("Check that the placeholder exists and is syntactically valid.")
                .build();
    }

    private AgateStepException unresolvedPlaceholder(
            String reason,
            String placeholder,
            String name,
            TestCase tc,
            int stepIndex,
            String originalAction,
            String hint) {

        return AgateStepException.builder(reason)
                .detail("Placeholder", placeholder)
                .detail("Name", name)
                .detail("Action", originalAction)
                .detail("Step", stepIndex > 0 ? stepIndex : null)
                .hint(hint)
                .build();
    }

    private AgateStepException missingPlaceholderName(
            String type,
            String placeholder,
            TestCase tc,
            int stepIndex,
            String originalAction) {

        return AgateStepException.builder("Placeholder name is missing")
                .actual(placeholder)
                .detail("Type", type)
                .detail("Action", originalAction)
                .detail("Step", stepIndex > 0 ? stepIndex : null)
                .hint("Provide a non-empty name inside the placeholder brackets.")
                .build();
    }

    private String stripBrackets(String value) {
        if (value == null || value.length() < 2) {
            return "";
        }

        return value.substring(
                1,
                value.length() - 1);
    }

    private String extractPlaceholderFragment(
            String value,
            int start) {

        if (value == null || start < 0 || start >= value.length()) {
            return value;
        }

        int end =
                value.indexOf('}', start);

        if (end >= 0) {
            return value.substring(
                    start,
                    end + 1);
        }

        int max =
                Math.min(
                        value.length(),
                        start + 120);

        return value.substring(
                start,
                max);
    }

    private String extractFirstDateExpression(String value) {
        Matcher matcher =
                DATE_PREFIX_PATTERN.matcher(value);

        if (!matcher.find()) {
            return value;
        }

        return extractPlaceholderFragment(
                value,
                matcher.start());
    }

    private String extractFirstKnownPlaceholder(String value) {

        Pattern combined =
                Pattern.compile(
                        "\\{(?:B\\[|E\\[|R\\[|T\\[|DATE|DATETIME)",
                        Pattern.CASE_INSENSITIVE);

        Matcher matcher =
                combined.matcher(value);

        if (!matcher.find()) {
            return value;
        }

        return extractPlaceholderFragment(
                value,
                matcher.start());
    }

    private static String safeMessage(Throwable throwable) {

        if (throwable == null) {
            return "Unknown error";
        }

        String message =
                throwable.getMessage();

        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }

        return message.trim();
    }

    private void logWarning(
            String testCaseName,
            int stepIndex,
            String placeholder,
            String originalAction) {

        String key =
                testCaseName + "_STEP_" + stepIndex;

        warnedPlaceholders.computeIfAbsent(
                key,
                k -> new HashSet<>());

        Set<String> warned =
                warnedPlaceholders.get(key);

        if (!warned.contains(placeholder)) {
            System.out.printf(
                    "[WARN] TC=%s Step %03d -> Placeholder %s not resolved in action: %s%n",
                    testCaseName,
                    stepIndex,
                    placeholder,
                    originalAction);

            warned.add(placeholder);
        }
    }
}
