package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DataFlowValidator
        implements AgateValidator {

    private static final Set<String> RESPONSE_ENGINES =
            Set.of(
                    "REST",
                    "SOAP",
                    "SQL",
                    "CMD",
                    "OC",
                    "FILE"
            );

    private static final Set<String> BUFFER_PRODUCER_ENGINES =
            Set.of(
                    "REST",
                    "SOAP",
                    "SQL",
                    "CMD",
                    "OC",
                    "FILE"
            );

    private static final ObjectMapper YAML_MAPPER =
            new ObjectMapper(
                    new YAMLFactory()
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(c.root())) {

            /*
             * Variables currently available in this testcase.
             */
            Set<String> variables =
                    new LinkedHashSet<>(
                            ValidationUtil.objectKeys(
                                    tc.node()
                                            .get("variables")
                            )
                    );

            /*
             * Variables actually used by this testcase.
             *
             * Includes direct references and references
             * inside reusable modules.
             */
            Set<String> usedVariables =
                    new LinkedHashSet<>();

            /*
             * Named engine responses.
             */
            Set<String> responses =
                    new LinkedHashSet<>();

            /*
             * If reusable analysis cannot be completed,
             * unused-variable reporting is suppressed.
             */
            boolean reusableAnalysisComplete =
                    true;

            for (ValidationUtil.StepRef step :
                    ValidationUtil.steps(
                            tc.node(),
                            tc.index())) {

                JsonNode node =
                        step.node();

                String type =
                        ValidationUtil.type(node);

                String op =
                        ValidationUtil.op(node);

                String response =
                        ValidationUtil.text(
                                node,
                                "response");

                /*
                 * =========================================================
                 * 1. Direct {B[...]} references
                 * =========================================================
                 */
                for (String ref :
                        ValidationUtil.bufferRefs(node)) {

                    usedVariables.add(ref);

                    if (!variables.contains(ref)) {

                        issues.add(
                                ValidationIssue.error(
                                        "AGATE-V401",
                                        "Variable/buffer '"
                                                + ref
                                                + "' is referenced before initialization in "
                                                + step.path()
                                                + ".",
                                        c.file(),
                                        c.source()
                                                .lineOf(
                                                        "{B["
                                                                + ref
                                                                + "]}")
                                )
                        );
                    }
                }

                /*
                 * =========================================================
                 * 2. CALL / reusable module
                 * =========================================================
                 *
                 * Reusable modules are analyzed step-by-step.
                 *
                 * The current variable state is passed into the reusable
                 * module so references can be checked in execution order.
                 */
                if ("CALL".equals(type)) {

                    String command =
                            ValidationUtil.text(
                                    node,
                                    "command");

                    if (command != null
                            && command.startsWith("reusable.")) {

                        boolean complete =
                                validateReusableFlow(
                                        c.file(),
                                        command,
                                        variables,
                                        usedVariables,
                                        new HashSet<>(),
                                        issues);

                        if (!complete) {

                            reusableAnalysisComplete =
                                    false;
                        }

                    } else {

                        /*
                         * CALL exists but cannot be analyzed safely.
                         */
                        reusableAnalysisComplete =
                                false;
                    }
                }

                /*
                 * =========================================================
                 * 3. Native BUFFER / ASSERT
                 * =========================================================
                 */
                if ("BUFFER".equals(type)
                        && "ASSERT".equals(op)) {

                    String name =
                            ValidationUtil.text(
                                    node,
                                    "name");

                    String action =
                            ValidationUtil.action(node);

                    if (name != null
                            && !name.isBlank()) {

                        usedVariables.add(name);

                        /*
                         * IS_NULL may explicitly test an undefined variable.
                         */
                        if (!"IS_NULL".equals(action)
                                && !variables.contains(name)) {

                            issues.add(
                                    ValidationIssue.error(
                                            "AGATE-V403",
                                            "Buffer variable '"
                                                    + name
                                                    + "' is used before it is initialized.",
                                            c.file(),
                                            ValidationUtil.lineOfProperty(
                                                    c,
                                                    node,
                                                    "name")
                                    )
                            );
                        }
                    }
                }

                /*
                 * =========================================================
                 * 4. Response consumption
                 * =========================================================
                 */
                if (usesResponse(
                        type,
                        op)
                        && response != null
                        && !response.isBlank()
                        && !responses.contains(response)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V402",
                                    "Response '"
                                            + response
                                            + "' is used before it is created.",
                                    c.file(),
                                    ValidationUtil.lineOfProperty(
                                            c,
                                            node,
                                            "response")
                            )
                    );
                }

                /*
                 * =========================================================
                 * 5. EXEC creates response
                 * =========================================================
                 */
                if (createsResponse(
                        type,
                        op)
                        && response != null
                        && !response.isBlank()) {

                    responses.add(response);
                }

                /*
                 * =========================================================
                 * 6. Engine BUFFER creates runtime variable
                 * =========================================================
                 */
                if (createsBufferVariable(
                        type,
                        op)) {

                    String name =
                            ValidationUtil.text(
                                    node,
                                    "name");

                    if (name != null
                            && !name.isBlank()) {

                        variables.add(name);

                        /*
                         * CMD BUFFER result may later be consumed by
                         * another CMD operation using response:<name>.
                         */
                        if ("CMD".equals(type)) {

                            responses.add(name);
                        }
                    }
                }

                /*
                 * =========================================================
                 * 7. Native BUFFER / EXEC creates runtime variable
                 * =========================================================
                 */
                if ("BUFFER".equals(type)
                        && "EXEC".equals(op)) {

                    String name =
                            ValidationUtil.text(
                                    node,
                                    "name");

                    if (name != null
                            && !name.isBlank()) {

                        variables.add(name);
                    }
                }
            }

            /*
             * =============================================================
             * 8. Unused testcase variables
             * =============================================================
             */
            if (c.options()
                    .warnUnusedVariables()
                    && reusableAnalysisComplete) {

                Set<String> declared =
                        ValidationUtil.objectKeys(
                                tc.node()
                                        .get("variables")
                        );

                for (String variable :
                        declared) {

                    if (!usedVariables.contains(variable)) {

                        issues.add(
                                ValidationIssue.info(
                                        "AGATE-V420",
                                        "Test-case variable '"
                                                + variable
                                                + "' is defined but never referenced.",
                                        c.file(),
                                        ValidationUtil.lineOfProperty(
                                                c,
                                                tc.node()
                                                        .get("variables"),
                                                variable)
                                )
                        );
                    }
                }
            }
        }

        return issues;
    }

    /*
     * =============================================================
     * Reusable data-flow analysis
     * =============================================================
     */

    private boolean validateReusableFlow(
            Path testSuiteFile,
            String command,
            Set<String> variables,
            Set<String> usedVariables,
            Set<Path> callStack,
            List<ValidationIssue> issues) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith("reusable.")) {

            return false;
        }

        Path reusableFile =
                resolveReusableFile(
                        testSuiteFile,
                        command);

        if (reusableFile == null
                || !Files.isRegularFile(reusableFile)) {

            return false;
        }

        Path normalized =
                reusableFile
                        .toAbsolutePath()
                        .normalize();

        /*
         * Detect only real recursion in the current call chain.
         *
         * The same reusable module may be called multiple times
         * at different points in a testcase.
         */
        if (!callStack.add(normalized)) {

            return false;
        }

        try {

            JsonNode root;

            try {

                root =
                        YAML_MAPPER.readTree(
                                normalized.toFile());

            } catch (IOException e) {

                return false;
            }

            if (root == null) {

                return false;
            }

            JsonNode steps =
                    root.get("steps");

            if (steps == null
                    || !steps.isArray()) {

                return false;
            }

            return validateReusableSteps(
                    testSuiteFile,
                    normalized,
                    steps,
                    variables,
                    usedVariables,
                    callStack,
                    issues);

        } finally {

            /*
             * callStack describes only the current recursion path.
             */
            callStack.remove(normalized);
        }
    }

    /*
     * =============================================================
     * Reusable step sequence / LOOP analysis
     * =============================================================
     *
     * Important execution-order rule:
     *
     * WHILE:
     *   condition -> body
     *
     * DO_WHILE:
     *   body -> condition
     *
     * This matters for buffers created inside the loop body.
     */
    private boolean validateReusableSteps(
            Path testSuiteFile,
            Path reusableFile,
            JsonNode steps,
            Set<String> variables,
            Set<String> usedVariables,
            Set<Path> callStack,
            List<ValidationIssue> issues) {

        if (steps == null
                || !steps.isArray()) {

            return false;
        }

        int stepIndex =
                0;

        for (JsonNode step :
                steps) {

            stepIndex++;

            if (step == null
                    || !step.isObject()) {

                continue;
            }

            String type =
                    ValidationUtil.type(step);

            String op =
                    ValidationUtil.op(step);

            /*
             * =====================================================
             * LOOP
             * =====================================================
             *
             * ValidationUtil.bufferRefs(step) is recursive.
             * Therefore it must NOT be called for the complete LOOP
             * node because that would inspect condition and body at
             * the same time and lose execution order.
             */
            if ("LOOP".equals(type)) {

                String mode =
                        ValidationUtil.text(
                                step,
                                "mode");

                JsonNode condition =
                        step.get("condition");

                JsonNode loopSteps =
                        step.get("steps");

                if (loopSteps == null
                        || !loopSteps.isArray()) {

                    return false;
                }

                boolean doWhile =
                        mode != null
                                && "DO_WHILE".equalsIgnoreCase(
                                        mode);

                if (!doWhile) {

                    validateBufferReferences(
                            condition,
                            reusableFile,
                            stepIndex,
                            variables,
                            usedVariables,
                            issues);
                }

                boolean loopComplete =
                        validateReusableSteps(
                                testSuiteFile,
                                reusableFile,
                                loopSteps,
                                variables,
                                usedVariables,
                                callStack,
                                issues);

                if (!loopComplete) {

                    return false;
                }

                if (doWhile) {

                    validateBufferReferences(
                            condition,
                            reusableFile,
                            stepIndex,
                            variables,
                            usedVariables,
                            issues);
                }

                continue;
            }

            /*
             * =====================================================
             * 1. Validate {B[...]} references BEFORE producers.
             * =====================================================
             *
             * Example:
             *
             * - type: BUFFER
             *   op: EXEC
             *   name: L_LogDir
             *   value: "{B[G_EC_OpenShift_LogDir]}"
             *
             * G_EC_OpenShift_LogDir must already exist.
             * L_LogDir becomes available only after this step.
             */
            validateBufferReferences(
                    step,
                    reusableFile,
                    stepIndex,
                    variables,
                    usedVariables,
                    issues);

            /*
             * =====================================================
             * 2. BUFFER / ASSERT
             * =====================================================
             */
            if ("BUFFER".equals(type)
                    && "ASSERT".equals(op)) {

                String name =
                        ValidationUtil.text(
                                step,
                                "name");

                String action =
                        ValidationUtil.action(step);

                if (name != null
                        && !name.isBlank()) {

                    usedVariables.add(name);

                    if (!"IS_NULL".equals(action)
                            && !variables.contains(name)) {

                        issues.add(
                                ValidationIssue.error(
                                        "AGATE-V403",
                                        "Buffer variable '"
                                                + name
                                                + "' is used before it is initialized "
                                                + "in reusable step "
                                                + stepIndex
                                                + ".",
                                        reusableFile,
                                        lineOfProperty(
                                                reusableFile,
                                                "name",
                                                name)
                                )
                        );
                    }
                }
            }

            /*
             * =====================================================
             * 3. Nested CALL
             * =====================================================
             */
            if ("CALL".equals(type)) {

                String nestedCommand =
                        ValidationUtil.text(
                                step,
                                "command");

                if (nestedCommand == null
                        || !nestedCommand.startsWith("reusable.")) {

                    return false;
                }

                boolean nestedComplete =
                        validateReusableFlow(
                                testSuiteFile,
                                nestedCommand,
                                variables,
                                usedVariables,
                                callStack,
                                issues);

                if (!nestedComplete) {

                    return false;
                }
            }

            /*
             * =====================================================
             * 4. Engine BUFFER creates variable
             * =====================================================
             */
            if (createsBufferVariable(
                    type,
                    op)) {

                String name =
                        ValidationUtil.text(
                                step,
                                "name");

                if (name != null
                        && !name.isBlank()) {

                    variables.add(name);
                }
            }

            /*
             * =====================================================
             * 5. Native BUFFER / EXEC creates variable
             * =====================================================
             */
            if ("BUFFER".equals(type)
                    && "EXEC".equals(op)) {

                String name =
                        ValidationUtil.text(
                                step,
                                "name");

                if (name != null
                        && !name.isBlank()) {

                    variables.add(name);
                }
            }
        }

        return true;
    }

    private void validateBufferReferences(
            JsonNode node,
            Path reusableFile,
            int stepIndex,
            Set<String> variables,
            Set<String> usedVariables,
            List<ValidationIssue> issues) {

        if (node == null) {

            return;
        }

        for (String ref :
                ValidationUtil.bufferRefs(node)) {

            usedVariables.add(ref);

            if (!variables.contains(ref)) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V401",
                                "Variable/buffer '"
                                        + ref
                                        + "' is referenced before initialization "
                                        + "in reusable step "
                                        + stepIndex
                                        + ".",
                                reusableFile,
                                lineOfBufferReference(
                                        reusableFile,
                                        ref)
                        )
                );
            }
        }
    }

    /*
     * =============================================================
     * Reusable file resolution
     * =============================================================
     */

    private Path resolveReusableFile(
            Path testSuiteFile,
            String command) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith("reusable.")) {

            return null;
        }

        Path parent =
                testSuiteFile
                        .toAbsolutePath()
                        .normalize()
                        .getParent();

        if (parent == null) {

            return null;
        }

        String reusableName =
                command.substring(
                        "reusable.".length());

        if (reusableName.isBlank()) {

            return null;
        }

        /*
         * Supports:
         *
         * reusable.demo
         *
         * ->
         *
         * reusable/demo.yaml
         *
         * and:
         *
         * reusable.subfolder.demo
         *
         * ->
         *
         * reusable/subfolder/demo.yaml
         */
        String relative =
                reusableName.replace(
                        '.',
                        '/');

        return parent
                .resolve("reusable")
                .resolve(relative + ".yaml")
                .normalize();
    }

    /*
     * =============================================================
     * Source-location helpers for reusable files
     * =============================================================
     */

    private Integer lineOfBufferReference(
            Path file,
            String variable) {

        if (file == null
                || variable == null
                || variable.isBlank()) {

            return null;
        }

        String token =
                "{B[" + variable + "]}";

        try {

            List<String> lines =
                    Files.readAllLines(file);

            for (int i = 0;
                 i < lines.size();
                 i++) {

                if (lines.get(i)
                        .contains(token)) {

                    return i + 1;
                }
            }

        } catch (IOException ignored) {

            /*
             * Line information improves diagnostics,
             * but validation itself must still work.
             */
        }

        return null;
    }

    private Integer lineOfProperty(
            Path file,
            String property,
            String value) {

        if (file == null
                || property == null
                || property.isBlank()) {

            return null;
        }

        try {

            List<String> lines =
                    Files.readAllLines(file);

            for (int i = 0;
                 i < lines.size();
                 i++) {

                String line =
                        lines.get(i)
                                .trim();

                if (!line.startsWith(
                        property + ":")) {

                    continue;
                }

                if (value == null
                        || value.isBlank()
                        || line.contains(value)) {

                    return i + 1;
                }
            }

        } catch (IOException ignored) {

            /*
             * Location is optional.
             */
        }

        return null;
    }

    /*
     * =============================================================
     * Response semantics
     * =============================================================
     */

    private boolean usesResponse(
            String type,
            String op) {

        if (type == null
                || op == null) {

            return false;
        }

        if (!RESPONSE_ENGINES.contains(type)) {

            return false;
        }

        return "ASSERT".equals(op)
                || "BUFFER".equals(op);
    }

    private boolean createsResponse(
            String type,
            String op) {

        if (type == null
                || op == null) {

            return false;
        }

        return RESPONSE_ENGINES.contains(type)
                && "EXEC".equals(op);
    }

    private boolean createsBufferVariable(
            String type,
            String op) {

        if (type == null
                || op == null) {

            return false;
        }

        return BUFFER_PRODUCER_ENGINES.contains(type)
                && "BUFFER".equals(op);
    }
}