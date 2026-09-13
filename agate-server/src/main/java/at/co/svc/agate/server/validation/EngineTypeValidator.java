package at.co.svc.agate.server.validation;

import at.co.svc.agate.core.dsl.model.StepType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EngineTypeValidator
        implements AgateValidator {

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

                String rawType =
                        ValidationUtil.text(
                                node,
                                "type");

                /*
                 * Missing type.
                 */
                if (rawType == null
                        || rawType.isBlank()) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V120",
                                    step.path()
                                            + " requires property 'type'.",
                                    c.file(),
                                    lineOfStep(
                                            c,
                                            node)
                            )
                    );

                    continue;
                }

                /*
                 * Check against the central DSL StepType enum.
                 *
                 * This avoids maintaining a second list
                 * of supported engine types in the validator.
                 */
                if (!isKnownStepType(rawType)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V121",
                                    "Unknown or unsupported engine type '"
                                            + rawType
                                            + "'.",
                                    c.file(),
                                    c.source()
                                            .lineOf(
                                                    "type: "
                                                            + rawType)
                            )
                    );
                }
            }
        }

        return issues;
    }

    private boolean isKnownStepType(
            String rawType) {

        try {

            StepType.valueOf(
                    rawType.trim()
                            .toUpperCase(
                                    Locale.ROOT));

            return true;

        } catch (IllegalArgumentException e) {

            return false;
        }
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

        String type =
                ValidationUtil.text(
                        node,
                        "type");

        if (type != null
                && !type.isBlank()) {

            return c.source()
                    .lineOf(
                            "type: "
                                    + type);
        }

        return 1;
    }
}