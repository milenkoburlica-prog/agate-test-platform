package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public final class DocumentStructureValidator implements AgateValidator {

    private static final Set<String> TOP_LEVEL =
            Set.of("testCases");

    private static final Set<String> TEST_CASE_FIELDS =
            Set.of(
                    "id",
                    "description",
                    "stage",
                    "priority",
                    "variables",
                    "steps"
            );

    @Override
    public List<ValidationIssue> validate(ValidationContext c) {

        List<ValidationIssue> issues = new ArrayList<>();
        JsonNode root = c.root();

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

        JsonNode testCases = root.get("testCases");

        if (testCases == null) {
            issues.add(ValidationIssue.error(
                    "AGATE-V105",
                    "Missing top-level 'testCases' section.",
                    c.file(),
                    1
            ));
            return issues;
        }

        if (!testCases.isArray()) {
            issues.add(ValidationIssue.error(
                    "AGATE-V106",
                    "'testCases' must be a YAML list.",
                    c.file(),
                    c.source().lineOf("testCases:")
            ));
            return issues;
        }

        Set<String> ids = new HashSet<>();

        for (ValidationUtil.TestCaseRef tc : ValidationUtil.testCases(root)) {

            ValidationUtil.unknownFields(
                    c,
                    tc.node(),
                    TEST_CASE_FIELDS,
                    "test case",
                    issues
            );

            String id = ValidationUtil.text(tc.node(), "id");

            if (id == null || id.isBlank()) {
                issues.add(ValidationIssue.error(
                        "AGATE-V130",
                        tc.path() + " requires property 'id'.",
                        c.file(),
                        c.source().lineOf("- id:")
                ));
            } else if (!ids.add(id)) {
                issues.add(ValidationIssue.error(
                        "AGATE-V131",
                        "Duplicate test case id '" + id + "'.",
                        c.file(),
                        c.source().lineOf("id: " + id)
                ));
            }

            JsonNode variables = tc.node().get("variables");
            if (variables != null && !variables.isObject()) {
                issues.add(ValidationIssue.error(
                        "AGATE-V132",
                        "'variables' must be a map/object.",
                        c.file(),
                        c.source().lineOf("variables:")
                ));
            }

            JsonNode steps = tc.node().get("steps");
            if (steps != null && !steps.isArray()) {
                issues.add(ValidationIssue.error(
                        "AGATE-V133",
                        "'steps' must be a YAML list.",
                        c.file(),
                        c.source().lineOf("steps:")
                ));
            }
        }

        return issues;
    }
}
