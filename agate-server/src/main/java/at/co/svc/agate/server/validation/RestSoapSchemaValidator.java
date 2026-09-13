package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RestSoapSchemaValidator
        implements AgateValidator {

    private static final Set<String> EXEC_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "command",
                    "endpoint",
                    "parameters",
                    "response"
            );

    private static final Set<String> ASSERT_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "source",
                    "selector",
                    "path",
                    "action",
                    "expected",
                    "value",
                    "ignore",
                    "unordered"
            );

    /*
     * REST BUFFER supports action/expected.
     *
     * Example:
     *
     * type: REST
     * op: BUFFER
     * source: BODY
     * path: "$.title"
     * action: EQUALS
     * expected: "abc"
     * name: title
     */
    private static final Set<String> REST_BUFFER_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "source",
                    "path",
                    "action",
                    "expected",
                    "value",
                    "name",
                    "constraints"
            );

    /*
     * SOAP BUFFER is extraction-oriented.
     */
    private static final Set<String> SOAP_BUFFER_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "source",
                    "path",
                    "action",
                    "name",
                    "constraints"
            );

    private static final Set<String> SOURCES =
            Set.of(
                    "STATUS",
                    "HEADERS",
                    "BODY"
            );

    /*
     * REST ASSERT
     */
    private static final Set<String> REST_STATUS_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "GREATER_THAN",
                    "GREATER_OR_EQUALS",
                    "GREATER_THAN_OR_EQUAL",
                    "LESS_THAN",
                    "LESS_OR_EQUALS",
                    "LESS_THAN_OR_EQUAL"
            );

    private static final Set<String> REST_HEADER_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "EXISTS",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY"
            );

    private static final Set<String> REST_BODY_ACTIONS =
            Set.of(
                    "EQUALS",
                    "VERIFY",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "EXISTS",
                    "COUNT",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY",
                    "MATCH_REFERENCE"
            );

    /*
     * SOAP ASSERT
     */
    private static final Set<String> SOAP_STATUS_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "GREATER_THAN",
                    "GREATER_OR_EQUALS",
                    "GREATER_THAN_OR_EQUAL",
                    "LESS_THAN",
                    "LESS_OR_EQUALS",
                    "LESS_THAN_OR_EQUAL"
            );

    private static final Set<String> SOAP_HEADER_ACTIONS =
            Set.of(
                    "IS_HEADER_PRESENT",
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY"
            );

    private static final Set<String> SOAP_BODY_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "NOT_EMPTY",
                    "IS_NOT_EMPTY",
                    "IS_EMPTY",
                    "MATCH_REFERENCE"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(c.root())) {

            for (ValidationUtil.StepRef step :
                    ValidationUtil.steps(
                            tc.node(),
                            tc.index())) {

                JsonNode node =
                        step.node();

                String type =
                        ValidationUtil.type(node);

                if (!"REST".equals(type)
                        && !"SOAP".equals(type)) {

                    continue;
                }

                String op =
                        ValidationUtil.op(node);

                switch (op) {

                    case "EXEC" ->
                            validateExec(
                                    c,
                                    node,
                                    type,
                                    issues);

                    case "ASSERT" ->
                            validateAssert(
                                    c,
                                    node,
                                    type,
                                    issues);

                    case "BUFFER" ->
                            validateBuffer(
                                    c,
                                    node,
                                    type,
                                    issues);

                    default ->
                            issues.add(
                                    ValidationIssue.error(
                                            "AGATE-V220",
                                            type
                                                    + " has unsupported op '"
                                                    + op
                                                    + "'. Supported: EXEC, ASSERT, BUFFER.",
                                            c.file(),
                                            ValidationUtil.lineOfProperty(
                                                    c,
                                                    node,
                                                    "op")
                                    )
                            );
                }
            }
        }

        return issues;
    }

    private void validateExec(
            ValidationContext c,
            JsonNode node,
            String type,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                EXEC_FIELDS,
                type + "/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "command",
                "AGATE-V221",
                type + "/EXEC",
                issues);
    }

    private void validateAssert(
            ValidationContext c,
            JsonNode node,
            String type,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                ASSERT_FIELDS,
                type + "/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V222",
                type + "/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "source",
                "AGATE-V223",
                type + "/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V224",
                type + "/ASSERT",
                issues);

        String source =
                ValidationUtil.source(node);

        String action =
                ValidationUtil.action(node);

        if (source == null
                || source.isBlank()) {

            return;
        }

        if (!SOURCES.contains(source)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V225",
                            type
                                    + "/ASSERT has invalid source '"
                                    + source
                                    + "'. Supported: "
                                    + SOURCES,
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "source")
                    )
            );

            return;
        }

        if (action == null
                || action.isBlank()) {

            return;
        }

        Set<String> supportedActions =
                supportedAssertActions(
                        type,
                        source);

        if (!supportedActions.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V229",
                            type
                                    + "/ASSERT has invalid action '"
                                    + action
                                    + "' for source "
                                    + source
                                    + ". Supported: "
                                    + supportedActions,
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "action")
                    )
            );

            return;
        }

        validateAssertRequirements(
                c,
                node,
                type,
                source,
                action,
                issues);
    }

    private void validateAssertRequirements(
            ValidationContext c,
            JsonNode node,
            String type,
            String source,
            String action,
            List<ValidationIssue> issues) {

        if ("MATCH_REFERENCE".equals(action)) {

            if (!"BODY".equals(source)) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V230",
                                type
                                        + "/ASSERT action MATCH_REFERENCE requires source BODY.",
                                c.file(),
                                ValidationUtil.lineOfProperty(
                                        c,
                                        node,
                                        "action")
                        )
                );
            }

            return;
        }

        if ("STATUS".equals(source)) {

            if (requiresExpected(action)) {

                ValidationUtil.require(
                        c,
                        node,
                        "expected",
                        "AGATE-V226",
                        type + "/ASSERT",
                        issues);
            }

            return;
        }

        if ("HEADERS".equals(source)) {

            ValidationUtil.require(
                    c,
                    node,
                    "path",
                    "AGATE-V227",
                    type + "/ASSERT",
                    issues);

            if (requiresExpected(action)) {

                ValidationUtil.require(
                        c,
                        node,
                        "expected",
                        "AGATE-V226",
                        type + "/ASSERT",
                        issues);
            }

            return;
        }

        /*
         * BODY
         */
        ValidationUtil.require(
                c,
                node,
                "path",
                "AGATE-V228",
                type + "/ASSERT",
                issues);

        if (requiresExpected(action)) {

            ValidationUtil.require(
                    c,
                    node,
                    "expected",
                    "AGATE-V226",
                    type + "/ASSERT",
                    issues);
        }
    }

    private void validateBuffer(
            ValidationContext c,
            JsonNode node,
            String type,
            List<ValidationIssue> issues) {

        Set<String> allowedFields =
                "REST".equals(type)
                        ? REST_BUFFER_FIELDS
                        : SOAP_BUFFER_FIELDS;

        ValidationUtil.unknownFields(
                c,
                node,
                allowedFields,
                type + "/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V240",
                type + "/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "source",
                "AGATE-V241",
                type + "/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V242",
                type + "/BUFFER",
                issues);

        String source =
                ValidationUtil.source(node);

        if (source == null
                || source.isBlank()) {

            return;
        }

        if (!SOURCES.contains(source)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V243",
                            type
                                    + "/BUFFER has invalid source '"
                                    + source
                                    + "'. Supported: "
                                    + SOURCES,
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "source")
                    )
            );

            return;
        }

        /*
         * STATUS has no path.
         */
        if ("STATUS".equals(source)) {
            return;
        }

        /*
         * HEADERS and BODY require path.
         */
        ValidationUtil.require(
                c,
                node,
                "path",
                "AGATE-V244",
                type + "/BUFFER",
                issues);

        /*
         * REST BUFFER may additionally validate the extracted value.
         *
         * Example:
         *
         * action: EQUALS
         * expected: "abc"
         */
        if ("REST".equals(type)) {

            String action =
                    ValidationUtil.action(node);

            if (action != null
                    && !action.isBlank()
                    && requiresExpected(action)
                    && !node.hasNonNull("expected")) {

                ValidationUtil.require(
                        c,
                        node,
                        "expected",
                        "AGATE-V245",
                        "REST/BUFFER",
                        issues);
            }
        }
    }

    private Set<String> supportedAssertActions(
            String type,
            String source) {

        if ("SOAP".equals(type)) {

            return switch (source) {

                case "STATUS" ->
                        SOAP_STATUS_ACTIONS;

                case "HEADERS" ->
                        SOAP_HEADER_ACTIONS;

                case "BODY" ->
                        SOAP_BODY_ACTIONS;

                default ->
                        Set.of();
            };
        }

        return switch (source) {

            case "STATUS" ->
                    REST_STATUS_ACTIONS;

            case "HEADERS" ->
                    REST_HEADER_ACTIONS;

            case "BODY" ->
                    REST_BODY_ACTIONS;

            default ->
                    Set.of();
        };
    }

    private boolean requiresExpected(
            String action) {

        if (action == null) {
            return false;
        }

        return switch (
                action.toUpperCase(
                        Locale.ROOT)) {

            case "EQUALS",
                 "VERIFY",
                 "NOT_EQUALS",
                 "CONTAINS",
                 "GREATER_THAN",
                 "GREATER_OR_EQUALS",
                 "GREATER_THAN_OR_EQUAL",
                 "LESS_THAN",
                 "LESS_OR_EQUALS",
                 "LESS_THAN_OR_EQUAL",
                 "COUNT",
                 "IS_HEADER_PRESENT" ->
                    true;

            case "IS_EMPTY",
                 "IS_NOT_EMPTY",
                 "NOT_EMPTY",
                 "EXISTS",
                 "MATCH_REFERENCE" ->
                    false;

            default ->
                    false;
        };
    }
}