package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FileSchemaValidator
        implements AgateValidator {

    private static final Set<String> FILE_READ =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "action",
                    "path",
                    "response",
                    "encoding"
            );

    private static final Set<String> FILE_WRITE =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "action",
                    "path",
                    "text",
                    "encoding",
                    "overwrite",
                    "response"
            );

    private static final Set<String> FILE_COPY =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "action",
                    "source",
                    "target",
                    "overwrite"
            );

    private static final Set<String> FILE_EXEC_ACTIONS =
            Set.of(
                    "READ",
                    "WRITE",
                    "APPEND",
                    "COPY",
                    "MOVE",
                    "DELETE",
                    "EXISTS"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        c.root())) {

            for (ValidationUtil.StepRef step :
                    ValidationUtil.steps(
                            tc.node(),
                            tc.index())) {

                JsonNode node =
                        step.node();

                String rawType =
                        ValidationUtil.text(
                                node,
                                "type");

                if (rawType == null
                        || !"FILE".equalsIgnoreCase(
                                rawType)) {

                    continue;
                }

                String op =
                        ValidationUtil.op(
                                node);

                /*
                 * FILE schema validation is currently
                 * implemented incrementally.
                 *
                 * At this stage we validate FILE/EXEC.
                 * BUFFER and ASSERT will be added separately.
                 */
                if (!"EXEC".equals(op)) {
                    continue;
                }

                String rawAction =
                        ValidationUtil.text(
                                node,
                                "action");

                if (rawAction == null
                        || rawAction.isBlank()) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V600",
                                    "FILE/EXEC requires property 'action'.",
                                    c.file(),
                                    lineOfStep(
                                            c,
                                            node)
                            )
                    );

                    continue;
                }

                String action =
                        rawAction.toUpperCase(
                                Locale.ROOT);

                /*
                 * First validate whether the action itself
                 * is a supported FILE/EXEC action.
                 */
                if (!FILE_EXEC_ACTIONS.contains(
                        action)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V620",
                                    "FILE/EXEC has invalid action '"
                                            + rawAction
                                            + "'. Supported: READ, WRITE, APPEND, "
                                            + "COPY, MOVE, DELETE, EXISTS.",
                                    c.file(),
                                    c.source()
                                            .lineOf(
                                                    "action: "
                                                            + rawAction)
                            )
                    );

                    continue;
                }

                /*
                 * Action-specific schema validation.
                 *
                 * Other valid FILE actions are currently
                 * accepted but will receive their detailed
                 * schema rules later.
                 */
                switch (action) {

                    case "READ" ->
                            validateRead(
                                    c,
                                    node,
                                    issues);

                    case "WRITE" ->
                            validateWrite(
                                    c,
                                    node,
                                    issues);

                    case "COPY" ->
                            validateCopy(
                                    c,
                                    node,
                                    issues);

                    case "APPEND",
                         "MOVE",
                         "DELETE",
                         "EXISTS" -> {
                        /*
                         * Valid FILE actions.
                         *
                         * Detailed schema validation will
                         * be added step by step.
                         */
                    }

                    default -> {
                        /*
                         * Cannot normally be reached because
                         * FILE_EXEC_ACTIONS was checked above.
                         */
                    }
                }
            }
        }

        return issues;
    }

    private void validateRead(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                FILE_READ,
                "FILE/EXEC READ",
                issues);

        ValidationUtil.require(
                c,
                node,
                "path",
                "AGATE-V601",
                "FILE/EXEC READ",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V602",
                "FILE/EXEC READ",
                issues);
    }

    private void validateWrite(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                FILE_WRITE,
                "FILE/EXEC WRITE",
                issues);

        ValidationUtil.require(
                c,
                node,
                "path",
                "AGATE-V603",
                "FILE/EXEC WRITE",
                issues);

        ValidationUtil.require(
                c,
                node,
                "text",
                "AGATE-V604",
                "FILE/EXEC WRITE",
                issues);
    }

    private void validateCopy(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                FILE_COPY,
                "FILE/EXEC COPY",
                issues);

        ValidationUtil.require(
                c,
                node,
                "source",
                "AGATE-V610",
                "FILE/EXEC COPY",
                issues);

        ValidationUtil.require(
                c,
                node,
                "target",
                "AGATE-V611",
                "FILE/EXEC COPY",
                issues);
    }

    private int lineOfStep(
            ValidationContext c,
            JsonNode node) {

        String id =
                ValidationUtil.text(
                        node,
                        "id");

        if (id != null
                && !id.isBlank()) {

            return c.source()
                    .lineOf(
                            "id: "
                                    + id);
        }

        String action =
                ValidationUtil.text(
                        node,
                        "action");

        if (action != null
                && !action.isBlank()) {

            return c.source()
                    .lineOf(
                            "action: "
                                    + action);
        }

        return c.source()
                .lineOf(
                        "type: FILE");
    }
}