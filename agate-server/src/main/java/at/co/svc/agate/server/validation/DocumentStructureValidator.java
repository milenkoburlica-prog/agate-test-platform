package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DocumentStructureValidator implements AgateValidator {

    private static final Set<String> TOP_LEVEL =
            Set.of(
                    "testCases"
            );

    private static final Set<String> TEST_CASE_FIELDS =
            Set.of(
                    "id",
                    "description",
                    "stage",
                    "priority",
                    "variables",
                    "steps"
            );

    /**
     * Allowed fields for a section pseudo-step.
     *
     * Example:
     *
     * - section: Preparation
     *   description: Create Dialog
     *   condition: 'CardToken.Tokentyp == "SVSV"'
     */
    private static final Set<String> SECTION_FIELDS =
            Set.of(
                    "section",
                    "description",
                    "condition"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        JsonNode root =
                c.root();

        /*
         * =========================================================
         * ROOT
         * =========================================================
         */
        if (!root.isObject()) {

            return List.of(
                    ValidationIssue.error(
                            "AGATE-V100",
                            "AGATE YAML root must be an object/map.",
                            c.file(),
                            1
                    )
            );
        }

        ValidationUtil.unknownFields(
                c,
                root,
                TOP_LEVEL,
                "AGATE document",
                issues
        );

        /*
         * =========================================================
         * TEST CASES
         * =========================================================
         */
        JsonNode testCases =
                root.get(
                        "testCases");

        if (testCases == null) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V105",
                            "Missing top-level 'testCases' section.",
                            c.file(),
                            1
                    )
            );

            return issues;
        }

        if (!testCases.isArray()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V106",
                            "'testCases' must be a YAML list.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "testCases:")
                    )
            );

            return issues;
        }

        Set<String> ids =
                new HashSet<>();

        /*
         * =========================================================
         * TEST CASE VALIDATION
         * =========================================================
         */
        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        root)) {

            validateTestCase(
                    c,
                    tc,
                    ids,
                    issues
            );
        }

        return issues;
    }

    private void validateTestCase(
            ValidationContext c,
            ValidationUtil.TestCaseRef tc,
            Set<String> ids,
            List<ValidationIssue> issues) {

        /*
         * Unknown test-case properties.
         */
        ValidationUtil.unknownFields(
                c,
                tc.node(),
                TEST_CASE_FIELDS,
                "test case",
                issues
        );

        /*
         * =========================================================
         * ID
         * =========================================================
         */
        String id =
                ValidationUtil.text(
                        tc.node(),
                        "id");

        if (id == null
                || id.isBlank()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V130",
                            tc.path()
                                    + " requires property 'id'.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "- id:")
                    )
            );

        } else if (!ids.add(
                id)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V131",
                            "Duplicate test case id '"
                                    + id
                                    + "'.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "id: "
                                                    + id)
                    )
            );
        }

        /*
         * =========================================================
         * VARIABLES
         * =========================================================
         */
        JsonNode variables =
                tc.node()
                        .get(
                                "variables");

        if (variables != null
                && !variables.isObject()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V132",
                            "'variables' must be a map/object.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "variables:")
                    )
            );
        }

        /*
         * =========================================================
         * STEPS
         * =========================================================
         */
        JsonNode steps =
                tc.node()
                        .get(
                                "steps");

        if (steps == null) {
            return;
        }

        if (!steps.isArray()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V133",
                            "'steps' must be a YAML list.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "steps:")
                    )
            );

            return;
        }

        validateSteps(
                c,
                tc,
                steps,
                issues
        );
    }

    private void validateSteps(
            ValidationContext c,
            ValidationUtil.TestCaseRef tc,
            JsonNode steps,
            List<ValidationIssue> issues) {

        int stepIndex =
                0;

        for (JsonNode step :
                steps) {

            stepIndex++;

            /*
             * Every list item under steps must be an object/map.
             */
            if (!step.isObject()) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V134",
                                tc.path()
                                        + ".steps["
                                        + stepIndex
                                        + "] must be an object/map.",
                                c.file(),
                                findStepLine(
                                        c,
                                        stepIndex)
                        )
                );

                continue;
            }

            /*
             * =====================================================
             * SECTION PSEUDO-STEP
             * =====================================================
             *
             * A section is metadata only.
             *
             * It intentionally has no "type".
             */
            if (isSection(
                    step)) {

                validateSection(
                        c,
                        tc,
                        step,
                        stepIndex,
                        issues
                );

                continue;
            }

            /*
             * =====================================================
             * EXECUTABLE STEP
             * =====================================================
             *
             * Every normal AGATE step must define a type.
             */
            validateExecutableStep(
                    c,
                    tc,
                    step,
                    stepIndex,
                    issues
            );
        }
    }

    private void validateSection(
            ValidationContext c,
            ValidationUtil.TestCaseRef tc,
            JsonNode step,
            int stepIndex,
            List<ValidationIssue> issues) {

        /*
         * Section name must exist and must not be blank.
         */
        JsonNode sectionNode =
                step.get(
                        "section");

        if (sectionNode == null
                || sectionNode.isNull()
                || !sectionNode.isValueNode()
                || sectionNode.asText()
                .isBlank()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V135",
                            tc.path()
                                    + ".steps["
                                    + stepIndex
                                    + "] requires a non-empty 'section' value.",
                            c.file(),
                            findStepLine(
                                    c,
                                    stepIndex)
                    )
            );
        }

        /*
         * A section may contain only section metadata.
         *
         * Supported:
         *
         * section
         * description
         * condition
         */
        Iterator<Map.Entry<String, JsonNode>> fields =
                step.fields();

        while (fields.hasNext()) {

            Map.Entry<String, JsonNode> field =
                    fields.next();

            String fieldName =
                    field.getKey();

            if (!SECTION_FIELDS.contains(
                    fieldName)) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V136",
                                "Unsupported field '"
                                        + fieldName
                                        + "' in section step. "
                                        + "Allowed fields are: "
                                        + SECTION_FIELDS
                                        + ".",
                                c.file(),
                                findFieldLine(
                                        c,
                                        fieldName,
                                        stepIndex)
                        )
                );
            }
        }

        /*
         * Description, if present, should be scalar.
         */
        JsonNode description =
                step.get(
                        "description");

        if (description != null
                && !description.isNull()
                && !description.isValueNode()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V137",
                            "'description' in a section must be a scalar value.",
                            c.file(),
                            findFieldLine(
                                    c,
                                    "description",
                                    stepIndex)
                    )
            );
        }

        /*
         * Condition, if present, should be scalar.
         *
         * Expression syntax itself belongs to the dedicated
         * condition/expression validation logic.
         */
        JsonNode condition =
                step.get(
                        "condition");

        if (condition != null
                && !condition.isNull()
                && !condition.isValueNode()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V138",
                            "'condition' in a section must be a scalar expression.",
                            c.file(),
                            findFieldLine(
                                    c,
                                    "condition",
                                    stepIndex)
                    )
            );
        }
    }

    private void validateExecutableStep(
            ValidationContext c,
            ValidationUtil.TestCaseRef tc,
            JsonNode step,
            int stepIndex,
            List<ValidationIssue> issues) {

        JsonNode typeNode =
                step.get(
                        "type");

        if (typeNode == null
                || typeNode.isNull()
                || !typeNode.isValueNode()
                || typeNode.asText()
                .isBlank()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V139",
                            tc.path()
                                    + ".steps["
                                    + stepIndex
                                    + "] requires property 'type'.",
                            c.file(),
                            findStepLine(
                                    c,
                                    stepIndex)
                    )
            );
        }
    }

    /**
     * Returns true if the YAML step represents an AGATE section
     * pseudo-step.
     */
    private boolean isSection(
            JsonNode step) {

        return step != null
                && step.isObject()
                && step.has(
                        "section");
    }

    /**
     * Best-effort source line lookup.
     *
     * Exact source-position handling can later be centralized
     * inside ValidationUtil / ValidationSource if desired.
     */
    private int findStepLine(
            ValidationContext c,
            int stepIndex) {

        int line =
                c.source()
                        .lineOf(
                                "- section:");

        if (line > 0) {
            return line;
        }

        line =
                c.source()
                        .lineOf(
                                "- type:");

        if (line > 0) {
            return line;
        }

        line =
                c.source()
                        .lineOf(
                                "steps:");

        if (line > 0) {
            return line;
        }

        return 1;
    }

    private int findFieldLine(
            ValidationContext c,
            String field,
            int stepIndex) {

        int line =
                c.source()
                        .lineOf(
                                field + ":");

        if (line > 0) {
            return line;
        }

        return findStepLine(
                c,
                stepIndex);
    }
}