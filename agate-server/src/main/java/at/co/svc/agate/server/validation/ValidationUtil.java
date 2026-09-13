package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

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
                || c.source() == null
                || node == null
                || field == null
                || field.isBlank()) {

            return -1;
        }

        JsonNode valueNode =
                node.get(field);

        if (valueNode != null
                && !valueNode.isNull()
                && valueNode.isValueNode()) {

            String value =
                    valueNode.asText();

            if (value != null) {

                /*
                 * Unquoted form:
                 *
                 * response: res_demo
                 * value: 1000
                 */
                int line =
                        c.source()
                                .lineOf(
                                        field
                                                + ": "
                                                + value);

                if (line > 0) {
                    return line;
                }

                /*
                 * Double quoted form:
                 *
                 * name: "amount"
                 */
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

                /*
                 * Single quoted form:
                 *
                 * column: '4'
                 */
                line =
                        c.source()
                                .lineOf(
                                        field
                                                + ": '"
                                                + value
                                                + "'");

                if (line > 0) {
                    return line;
                }
            }
        }

        /*
         * Do NOT fall back to field + ":" here.
         *
         * That was exactly the reason why response from another
         * step could be reported.
         */
        return lineOfStep(
                c,
                node);
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