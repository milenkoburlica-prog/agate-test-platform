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
                    "OC"
            );

    private static final Set<String> BUFFER_PRODUCER_ENGINES =
            Set.of(
                    "REST",
                    "SOAP",
                    "SQL",
                    "CMD",
                    "OC"
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
             * This includes:
             *
             *   - direct {B[...]} references
             *   - {B[...]} references inside called reusable modules
             */
            Set<String> usedVariables =
                    new LinkedHashSet<>();

            /*
             * Named engine responses.
             */
            Set<String> responses =
                    new LinkedHashSet<>();

            /*
             * Avoid recursive reusable cycles:
             *
             * reusable.a
             *     -> reusable.b
             *         -> reusable.a
             */
            Set<Path> visitedReusableFiles =
                    new HashSet<>();

            /*
             * If a CALL cannot be analyzed, V420 cannot be determined
             * reliably. In that case we suppress only unused-variable
             * reporting for this testcase.
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
                 * Example:
                 *
                 * - type: CALL
                 *   command: reusable.demo_reusable
                 *
                 * We inspect:
                 *
                 * reusable/demo_reusable.yaml
                 *
                 * and collect all {B[...]} references from it.
                 */
                if ("CALL".equals(type)) {

                    String command =
                            ValidationUtil.text(
                                    node,
                                    "command");

                    if (command != null
                            && command.startsWith("reusable.")) {

                        ReusableUsage usage =
                                collectReusableUsage(
                                        c.file(),
                                        command,
                                        visitedReusableFiles
                                );

                        usedVariables.addAll(
                                usage.bufferRefs());

                        if (!usage.complete()) {

                            reusableAnalysisComplete =
                                    false;
                        }
                    } else {

                        /*
                         * CALL exists but we cannot safely analyze it.
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
                         * another CMD operation using:
                         *
                         * response: <name>
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
             *
             * V420 is only emitted if reusable analysis was complete.
             *
             * That means we now actually know whether the variable is
             * referenced directly OR inside a reusable module.
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
     * Reusable analysis
     * =============================================================
     */

    private ReusableUsage collectReusableUsage(
            Path testSuiteFile,
            String command,
            Set<Path> visited) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith("reusable.")) {

            return ReusableUsage.incomplete();
        }

        Path reusableFile =
                resolveReusableFile(
                        testSuiteFile,
                        command);

        if (reusableFile == null
                || !Files.isRegularFile(reusableFile)) {

            return ReusableUsage.incomplete();
        }

        Path normalized =
                reusableFile
                        .toAbsolutePath()
                        .normalize();

        /*
         * Already visited:
         *
         * Do not recurse forever.
         *
         * This is considered complete because this file has already
         * contributed its references earlier in the traversal.
         */
        if (!visited.add(normalized)) {

            return ReusableUsage.complete(
                    Set.of());
        }

        JsonNode root;

        try {

            root =
                    YAML_MAPPER.readTree(
                            normalized.toFile());

        } catch (IOException e) {

            return ReusableUsage.incomplete();
        }

        if (root == null) {

            return ReusableUsage.incomplete();
        }

        Set<String> refs =
                new LinkedHashSet<>(
                        ValidationUtil.bufferRefs(root)
                );

        boolean complete =
                true;

        /*
         * Reusable files currently have:
         *
         * steps:
         *   - ...
         *
         * Analyze nested CALL operations as well.
         */
        JsonNode steps =
                root.get("steps");

        if (steps != null
                && steps.isArray()) {

            for (JsonNode step : steps) {

                if (step == null
                        || !step.isObject()) {

                    continue;
                }

                String type =
                        ValidationUtil.type(step);

                if (!"CALL".equals(type)) {

                    continue;
                }

                String nestedCommand =
                        ValidationUtil.text(
                                step,
                                "command");

                if (nestedCommand == null
                        || !nestedCommand.startsWith(
                                "reusable.")) {

                    complete =
                            false;

                    continue;
                }

                ReusableUsage nested =
                        collectReusableUsage(
                                testSuiteFile,
                                nestedCommand,
                                visited
                        );

                refs.addAll(
                        nested.bufferRefs());

                if (!nested.complete()) {

                    complete =
                            false;
                }
            }
        }

        return new ReusableUsage(
                refs,
                complete);
    }

    /*
     * command:
     *
     * reusable.demo_reusable
     *
     * test suite:
     *
     * data/demo/reusable_engine_demo.yaml
     *
     * resolves to:
     *
     * data/demo/reusable/demo_reusable.yaml
     */
    private Path resolveReusableFile(
            Path testSuiteFile,
            String command) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith(
                        "reusable.")) {

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
         * Also supports:
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

    /*
     * =============================================================
     * Reusable result
     * =============================================================
     */

    private record ReusableUsage(
            Set<String> bufferRefs,
            boolean complete) {

        private static ReusableUsage complete(
                Set<String> refs) {

            return new ReusableUsage(
                    refs,
                    true);
        }

        private static ReusableUsage incomplete() {

            return new ReusableUsage(
                    Set.of(),
                    false);
        }
    }
}