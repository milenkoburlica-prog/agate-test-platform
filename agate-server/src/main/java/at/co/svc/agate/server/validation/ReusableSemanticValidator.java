package at.co.svc.agate.server.validation;

import at.co.svc.agate.core.dsl.model.StepType;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Validate types and LOOP contracts in every reachable reusable. */
public final class ReusableSemanticValidator implements AgateValidator {
    private final ValidationParser parser = new ValidationParser();

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();
        // No state shared between invocations.
        Set<Path> checked = new HashSet<>();
        Path appRoot = context.file().toAbsolutePath().normalize().getParent();
        if (appRoot == null) return issues;
        for (ValidationUtil.TestCaseRef tc : ValidationUtil.testCases(context.root())) {
            inspect(context, appRoot, tc.node().get("steps"), checked, issues);
        }
        return issues;
    }

    private void inspect(ValidationContext ctx, Path appRoot, JsonNode steps,
                         Set<Path> checked, List<ValidationIssue> issues) {
        if (steps == null || !steps.isArray()) return;
        for (JsonNode step : steps) {
            if (!step.isObject()) continue;
            String type = ValidationUtil.text(step, "type");
            if (type != null && type.equalsIgnoreCase("CALL")) {
                String command = ValidationUtil.text(step, "command");
                if (command != null && command.startsWith("reusable.")) {
                    String name = command.substring("reusable.".length()).replace('.', '/');
                    Path target = appRoot.resolve("reusable").resolve(name + ".yaml").normalize();
                    if (!target.startsWith(appRoot.resolve("reusable").normalize())) {
                        issues.add(ValidationIssue.error("AGATE-V122", "Invalid reusable path: " + command, ctx.file(), null));
                    } else if (!Files.isRegularFile(target)) {
                        issues.add(ValidationIssue.error("AGATE-V123", "Missing reusable: " + command, target, null));
                    } else if (checked.add(target)) {
                        try {
                            ValidationParser.ParsedDocument doc = parser.parse(target);
                            ValidationContext child = new ValidationContext(target, doc.root(), doc.source(), ctx.options());
                            JsonNode childSteps = reusableSteps(doc.root());
                            if (!childSteps.isArray()) {
                                issues.add(ValidationIssue.error("AGATE-V124", "Reusable must contain steps array", target, 1));
                            } else {
                                inspectTypes(child, childSteps, issues, false);
                                inspect(child, appRoot, childSteps, checked, issues);
                            }
                        } catch (IOException e) {
                            issues.add(ValidationIssue.error("AGATE-V125", "Cannot parse reusable: " + e.getMessage(), target, 1));
                        }
                    }
                }
            }
            inspect(ctx, appRoot, step.get("steps"), checked, issues);
        }
    }

    /**
     * ValidationParser normalizes top-level reusable "steps" to
     * "testCases[0].steps" through GuiDocumentAdapter. Accept both shapes,
     * so reusable validation matches the shared parser's actual output.
     */
    private static JsonNode reusableSteps(JsonNode root) {
        if (root == null || !root.isObject()) return null;
        JsonNode direct = root.get("steps");
        if (direct != null && direct.isArray()) return direct;
        JsonNode cases = root.get("testCases");
        if (cases != null && cases.isArray() && cases.size() == 1) {
            JsonNode steps = cases.get(0).get("steps");
            if (steps != null && steps.isArray()) return steps;
        }
        return null;
    }

    private void inspectTypes(ValidationContext ctx, JsonNode steps, List<ValidationIssue> issues, boolean inLoop) {
        for (JsonNode step : steps) {
            String type = ValidationUtil.text(step, "type");
            boolean supported;
            try {
                StepType.valueOf(type == null ? "" : type.trim().toUpperCase(Locale.ROOT));
                supported = true;
            } catch (IllegalArgumentException e) { supported = false; }
            if (!supported) {
                issues.add(ValidationIssue.error("AGATE-V121",
                    "Unknown or unsupported engine type '" + type + "' in reusable.",
                    ctx.file(), ctx.source().lineOf("type: " + type)));
            }
            if ("LOOP".equalsIgnoreCase(type)) {
                for (String required : new String[]{"mode","condition","maxIterations","timeoutMs","steps"}) {
                    if (!step.hasNonNull(required)) {
                        issues.add(ValidationIssue.error("AGATE-V126",
                            "LOOP requires '" + required + "' in reusable.", ctx.file(),
                            ctx.source().lineOf("type: LOOP")));
                    }
                }
                String mode = step.path("mode").asText("");
                if (!mode.isEmpty() && !mode.equals("WHILE") && !mode.equals("DO_WHILE")) {
                    issues.add(ValidationIssue.error("AGATE-V127", "Unsupported LOOP mode: " + mode,
                        ctx.file(), ctx.source().lineOf("mode:")));
                }
            }
            if ("BREAK".equalsIgnoreCase(type)) {
                if (!inLoop) issues.add(ValidationIssue.error("AGATE-V128", "BREAK must be inside LOOP.steps",
                        ctx.file(), ctx.source().lineOf("type: BREAK")));
                String condition = ValidationUtil.text(step, "condition");
                if (condition == null || condition.isBlank()) issues.add(ValidationIssue.error("AGATE-V129", "BREAK requires condition",
                        ctx.file(), ctx.source().lineOf("type: BREAK")));
            }
            JsonNode nested = step.get("steps");
            if (nested != null && nested.isArray()) inspectTypes(ctx, nested, issues,
                    inLoop || "LOOP".equalsIgnoreCase(type));
        }
    }
}
