package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ValidationUtil {

    private ValidationUtil() {
    }

    static final Pattern BUFFER_REF =
            Pattern.compile(
                    "\\{B\\[([A-Za-z_][A-Za-z0-9_.-]*)]}");

    static List<TestCaseRef> testCases(
            JsonNode root) {

        List<TestCaseRef> result =
                new ArrayList<>();

        if (root == null) {
            return result;
        }

        JsonNode testCases =
                root.get("testCases");

        if (testCases == null
                || !testCases.isArray()) {

            return result;
        }

        int i = 0;

        for (JsonNode tc : testCases) {

            if (tc.isObject()) {

                result.add(
                        new TestCaseRef(
                                tc,
                                i,
                                "testCases[" + i + "]"
                        )
                );
            }

            i++;
        }

        return result;
    }

    static List<StepRef> steps(
            JsonNode testCase,
            int testCaseIndex) {

        List<StepRef> result =
                new ArrayList<>();

        if (testCase == null) {
            return result;
        }

        JsonNode steps =
                testCase.get("steps");

        if (steps == null
                || !steps.isArray()) {

            return result;
        }

        int i = 0;

        for (JsonNode step : steps) {

            if (step.isObject()) {

                result.add(
                        new StepRef(
                                step,
                                i,
                                "testCases["
                                        + testCaseIndex
                                        + "].steps["
                                        + i
                                        + "]"
                        )
                );
            }

            i++;
        }

        return result;
    }

    static String text(
            JsonNode node,
            String field) {

        if (node == null
                || field == null) {

            return null;
        }

        JsonNode n =
                node.get(field);

        return n != null
                && !n.isNull()
                ? n.asText(null)
                : null;
    }

    static String op(
            JsonNode step) {

        String op =
                text(
                        step,
                        "op");

        return op == null
                || op.isBlank()
                ? "EXEC"
                : op.toUpperCase(
                        Locale.ROOT);
    }

    static String type(
            JsonNode step) {

        String type =
                text(
                        step,
                        "type");

        return type == null
                ? null
                : type.toUpperCase(
                        Locale.ROOT);
    }

    static String action(
            JsonNode step) {

        String action =
                text(
                        step,
                        "action");

        return action == null
                ? null
                : action.toUpperCase(
                        Locale.ROOT);
    }

    static String source(
            JsonNode step) {

        String source =
                text(
                        step,
                        "source");

        return source == null
                ? null
                : source.toUpperCase(
                        Locale.ROOT);
    }

    static Set<String> objectKeys(
            JsonNode node) {

        Set<String> result =
                new LinkedHashSet<>();

        if (node != null
                && node.isObject()) {

            node.fieldNames()
                    .forEachRemaining(
                            result::add);
        }

        return result;
    }

    static Set<String> bufferRefs(
            JsonNode node) {

        Set<String> result =
                new LinkedHashSet<>();

        collectText(
                node,
                result);

        return result;
    }

    private static void collectText(
            JsonNode node,
            Set<String> out) {

        if (node == null) {
            return;
        }

        if (node.isTextual()) {

            Matcher matcher =
                    BUFFER_REF.matcher(
                            node.asText());

            while (matcher.find()) {

                out.add(
                        matcher.group(1));
            }

            return;
        }

        if (node.isArray()) {

            node.forEach(
                    n -> collectText(
                            n,
                            out));

            return;
        }

        if (node.isObject()) {

            node.fields()
                    .forEachRemaining(
                            e -> collectText(
                                    e.getValue(),
                                    out));
        }
    }

    static void require(
            ValidationContext c,
            JsonNode node,
            String field,
            String code,
            String contextText,
            List<ValidationIssue> issues) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()
                || (value.isTextual()
                && value.asText().isBlank())) {

            issues.add(
                    ValidationIssue.error(
                            code,
                            contextText
                                    + " requires property '"
                                    + field
                                    + "'.",
                            c.file(),
                            lineOfStep(
                                    c,
                                    node)
                    )
            );
        }
    }

    static void unknownFields(
            ValidationContext c,
            JsonNode node,
            Set<String> allowed,
            String contextText,
            List<ValidationIssue> issues) {

        if (!c.options()
                .strictUnknownFields()) {

            return;
        }

        if (node == null
                || !node.isObject()) {

            return;
        }

        node.fieldNames()
                .forEachRemaining(
                        field -> {

                            if (allowed.contains(
                                    field)) {

                                return;
                            }

                            issues.add(
                                    ValidationIssue.error(
                                            "AGATE-V123",
                                            "Unknown property '"
                                                    + field
                                                    + "' for "
                                                    + contextText
                                                    + ".",
                                            c.file(),
                                            lineOfProperty(
                                                    c,
                                                    node,
                                                    field)
                                    )
                            );
                        });
    }

    /**
     * Tries to locate one concrete property of one concrete node.
     *
     * Example:
     *
     *     response: res_verify
     *
     * instead of searching only for:
     *
     *     response:
     *
     * This prevents the validator from reporting the first unrelated
     * occurrence of the same property in the YAML file.
     */
    static int lineOfProperty(
            ValidationContext c,
            JsonNode node,
            String field) {

        if (c == null
                || c.file() == null
                || node == null
                || field == null
                || field.isBlank()) {

            return -1;
        }

        int contextualLine =
                lineOfPropertyInMatchingStep(
                        c,
                        node,
                        field);

        if (contextualLine > 0) {

            return contextualLine;
        }

        JsonNode valueNode =
                node.get(field);

        if (valueNode != null
                && !valueNode.isNull()
                && valueNode.isValueNode()) {

            String value =
                    valueNode.asText();

            if (value != null) {

                int line =
                        findScalarLine(
                                c,
                                field,
                                value);

                if (line > 0) {

                    return line;
                }
            }
        }

        return lineOfStep(
                c,
                node);
    }

    private static int lineOfPropertyInMatchingStep(
            ValidationContext c,
            JsonNode node,
            String field) {

        try {

            List<String> lines =
                    Files.readAllLines(
                            c.file());

            int bestStart =
                    -1;

            int bestEnd =
                    -1;

            int bestScore =
                    0;

            for (int i = 0;
                 i < lines.size();
                 i++) {

                String raw =
                        lines.get(i);

                String trimmed =
                        raw.trim();

                if (!trimmed.startsWith("- ")) {

                    continue;
                }

                int indent =
                        leadingSpaces(raw);

                if (!isStepCandidate(
                        lines,
                        i,
                        indent)) {

                    continue;
                }

                int end =
                        findListItemEnd(
                                lines,
                                i,
                                indent);

                int score =
                        scoreNodeAgainstBlock(
                                node,
                                lines,
                                i,
                                end,
                                indent);

                if (score > bestScore) {

                    bestScore =
                            score;

                    bestStart =
                            i;

                    bestEnd =
                            end;
                }
            }

            if (bestStart < 0
                    || bestScore < 2) {

                return -1;
            }

            int itemIndent =
                    leadingSpaces(
                            lines.get(bestStart));

            for (int i = bestStart;
                 i < bestEnd;
                 i++) {

                String direct =
                        directPropertyText(
                                lines.get(i),
                                i == bestStart,
                                itemIndent);

                if (direct == null) {

                    continue;
                }

                if (propertyName(direct)
                        .equals(field)) {

                    return i + 1;
                }
            }

        } catch (Exception ignored) {

            /*
             * Line lookup is diagnostic only.
             * Validation itself must still continue.
             */
        }

        return -1;
    }

    private static boolean isStepCandidate(
            List<String> lines,
            int start,
            int indent) {

        String first =
                lines.get(start)
                        .trim()
                        .substring(2)
                        .trim();

        if (first.startsWith("type:")) {

            return true;
        }

        if (!first.startsWith("id:")) {

            return false;
        }

        int end =
                findListItemEnd(
                        lines,
                        start,
                        indent);

        for (int i = start + 1;
             i < end;
             i++) {

            String raw =
                    lines.get(i);

            if (raw.isBlank()) {

                continue;
            }

            if (leadingSpaces(raw)
                    != indent + 2) {

                continue;
            }

            if (raw.trim()
                    .startsWith("type:")) {

                return true;
            }
        }

        return false;
    }

    private static int findListItemEnd(
            List<String> lines,
            int start,
            int indent) {

        for (int i = start + 1;
             i < lines.size();
             i++) {

            String raw =
                    lines.get(i);

            if (raw.isBlank()) {

                continue;
            }

            int currentIndent =
                    leadingSpaces(raw);

            String trimmed =
                    raw.trim();

            if (currentIndent < indent) {

                return i;
            }

            if (currentIndent == indent
                    && trimmed.startsWith("- ")) {

                return i;
            }
        }

        return lines.size();
    }

    private static int scoreNodeAgainstBlock(
            JsonNode node,
            List<String> lines,
            int start,
            int end,
            int indent) {

        int score =
                0;

        var fields =
                node.fields();

        while (fields.hasNext()) {

            var entry =
                    fields.next();

            JsonNode value =
                    entry.getValue();

            if (value == null
                    || value.isNull()
                    || !value.isValueNode()) {

                continue;
            }

            String expectedField =
                    entry.getKey();

            String expectedValue =
                    value.asText();

            if (expectedValue == null) {

                continue;
            }

            for (int i = start;
                 i < end;
                 i++) {

                String direct =
                        directPropertyText(
                                lines.get(i),
                                i == start,
                                indent);

                if (direct == null) {

                    continue;
                }

                if (scalarPropertyMatches(
                        direct,
                        expectedField,
                        expectedValue)) {

                    score++;

                    break;
                }
            }
        }

        return score;
    }

    private static String directPropertyText(
            String raw,
            boolean firstLine,
            int itemIndent) {

        if (raw == null
                || raw.isBlank()) {

            return null;
        }

        int indent =
                leadingSpaces(raw);

        String trimmed =
                raw.trim();

        if (firstLine) {

            if (indent != itemIndent
                    || !trimmed.startsWith("- ")) {

                return null;
            }

            return trimmed.substring(2)
                    .trim();
        }

        if (indent != itemIndent + 2) {

            return null;
        }

        if (trimmed.startsWith("- ")) {

            return null;
        }

        return trimmed;
    }

    private static boolean scalarPropertyMatches(
            String propertyText,
            String field,
            String expectedValue) {

        if (!propertyName(propertyText)
                .equals(field)) {

            return false;
        }

        int colon =
                propertyText.indexOf(':');

        if (colon < 0) {

            return false;
        }

        String actual =
                propertyText.substring(
                                colon + 1)
                        .trim();

        actual =
                unquote(actual);

        return actual.equals(
                expectedValue);
    }

    private static String propertyName(
            String propertyText) {

        if (propertyText == null) {

            return "";
        }

        int colon =
                propertyText.indexOf(':');

        if (colon < 0) {

            return "";
        }

        return propertyText.substring(
                        0,
                        colon)
                .trim();
    }

    private static String unquote(
            String value) {

        if (value == null) {

            return "";
        }

        if (value.length() >= 2
                && ((value.startsWith("\"")
                && value.endsWith("\""))
                || (value.startsWith("'")
                && value.endsWith("'")))) {

            return value.substring(
                    1,
                    value.length() - 1);
        }

        return value;
    }

    private static int leadingSpaces(
            String line) {

        int count =
                0;

        while (count < line.length()
                && line.charAt(count) == ' ') {

            count++;
        }

        return count;
    }

    /**
     * Best-effort location of a complete step.
     *
     * Uses concrete values in this order:
     *
     * id -> name -> response -> command -> type
     */
    static int lineOfStep(
            ValidationContext c,
            JsonNode node) {

        if (c == null
                || c.source() == null
                || node == null) {

            return -1;
        }

        int line;

        /*
         * 1. Explicit step id is normally the best locator.
         */
        String id =
                text(
                        node,
                        "id");

        if (id != null
                && !id.isBlank()) {

            line =
                    findScalarLine(
                            c,
                            "id",
                            id);

            if (line > 0) {
                return line;
            }
        }

        /*
         * 2. name
         */
        String name =
                text(
                        node,
                        "name");

        if (name != null
                && !name.isBlank()) {

            line =
                    findScalarLine(
                            c,
                            "name",
                            name);

            if (line > 0) {
                return line;
            }
        }

        /*
         * 3. response
         */
        String response =
                text(
                        node,
                        "response");

        if (response != null
                && !response.isBlank()) {

            line =
                    findScalarLine(
                            c,
                            "response",
                            response);

            if (line > 0) {
                return line;
            }
        }

        /*
         * 4. command
         *
         * Useful for many EXEC steps.
         */
        String command =
                text(
                        node,
                        "command");

        if (command != null
                && !command.isBlank()
                && !command.contains("\n")) {

            line =
                    findScalarLine(
                            c,
                            "command",
                            command);

            if (line > 0) {
                return line;
            }
        }

        /*
         * 5. Final fallback: type.
         *
         * This may not be unique, but is still better than returning
         * a completely unrelated property such as the first response.
         */
        String type =
                text(
                        node,
                        "type");

        if (type != null
                && !type.isBlank()) {

            line =
                    c.source()
                            .lineOf(
                                    "type: "
                                            + type);

            if (line > 0) {
                return line;
            }
        }

        return -1;
    }

    /**
     * Locate a scalar YAML property using the common YAML forms:
     *
     * key: value
     * key: "value"
     * key: 'value'
     */
    private static int findScalarLine(
            ValidationContext c,
            String field,
            String value) {

        if (c == null
                || c.source() == null
                || field == null
                || value == null) {

            return -1;
        }

        int line =
                c.source()
                        .lineOf(
                                field
                                        + ": "
                                        + value);

        if (line > 0) {
            return line;
        }

        line =
                c.source()
                        .lineOf(
                                field
                                        + ": \""
                                        + value
                                        + "\"");

        if (line > 0) {
            return line;
        }

        line =
                c.source()
                        .lineOf(
                                field
                                        + ": '"
                                        + value
                                        + "'");

        return line;
    }

    record TestCaseRef(
            JsonNode node,
            int index,
            String path) {
    }

    record StepRef(
            JsonNode node,
            int index,
            String path) {
    }
}