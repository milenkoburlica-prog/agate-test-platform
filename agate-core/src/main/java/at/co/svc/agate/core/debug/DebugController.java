package at.co.svc.agate.core.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

public class DebugController {

    public static final String PROPERTY = "agate.interactive.debug";

    private final BufferedReader input =
            new BufferedReader(new InputStreamReader(System.in));

    private final Set<String> responseNames =
            new LinkedHashSet<>();

    /*
     * Deepest scope in which interactive pauses are enabled.
     *
     * 1 -> main test
     * 2 -> one entered CALL/LOOP
     * 3 -> nested CALL/LOOP
     */
    private int activeDepth = 1;

    private boolean continueMode = false;

    /*
     * True only for the step for which a prompt was actually displayed.
     * TestExecutor reads this immediately after beforeStep().
     *
     * This is what makes a stepped-into reusable show the real engine output
     * even if CallEngine originally passed isVerbose=false.
     */
    private boolean forceVerboseCurrentStep = false;

    public boolean isEnabled() {
        return Boolean.getBoolean(PROPERTY);
    }

    public DebugAction beforeStep(
            TestCase testCase,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex) {

        forceVerboseCurrentStep = false;

        if (!isEnabled()) {
            return DebugAction.CONTINUE;
        }

        int depth = determineDepth(step);

        if (continueMode) {
            return DebugAction.CONTINUE;
        }

        /*
         * Child of a Step-Over / Step-Out scope:
         * execute silently, without a prompt.
         */
        if (depth > activeDepth) {
            return DebugAction.CONTINUE;
        }

        /*
         * Execution returned to a parent scope.
         */
        if (depth < activeDepth) {
            activeDepth = depth;
        }

        /*
         * A visible debugger stop should also execute visibly.
         * This fixes REST/SOAP/CMD/etc. inside a stepped-into CALL.
         */
        forceVerboseCurrentStep = true;

        printStepHeader(
                testCase,
                step,
                yamlFile,
                stepIndex,
                depth);

        while (true) {

            System.out.print("debug> ");

            String command = readLine();

            /*
             * ENTER:
             * execute this step and stop on the next visible step.
             *
             * For CALL/LOOP this behaves like Step Over.
             */
            if (command == null || command.isBlank()) {
                return DebugAction.CONTINUE;
            }

            switch (command.trim().toLowerCase()) {

                case "s":
                    System.out.println(
                            ">>> DEBUG: Step "
                                    + displayStep(step, stepIndex)
                                    + " skipped by user.");
                    return DebugAction.SKIP;

                case "i":
                    if (isContainer(step)) {
                        activeDepth = depth + 1;
                        System.out.println(
                                ">>> DEBUG: Step Into -> "
                                        + scopeName(step));
                    } else {
                        System.out.println(
                                ">>> DEBUG: Current step has no child scope. "
                                        + "Continuing normally.");
                    }
                    return DebugAction.CONTINUE;

                case "o":
                    activeDepth = depth;
                    return DebugAction.CONTINUE;

                case "u":
                    activeDepth = Math.max(1, depth - 1);
                    System.out.println(
                            ">>> DEBUG: Step Out -> parent scope");
                    return DebugAction.CONTINUE;

                case "c":
                    continueMode = true;
                    /*
                     * "continue" should restore normal AGATE logging rules.
                     */
                    forceVerboseCurrentStep = false;
                    System.out.println(
                            ">>> DEBUG: Continue to end of test case.");
                    return DebugAction.CONTINUE;

                case "v":
                    printVariables(context);
                    break;

                case "b":
                    printBuffers(context);
                    break;

                case "r":
                    printResponses(context);
                    break;

                case "q":
                    return DebugAction.QUIT;

                case "h":
                case "?":
                    printCommands(step);
                    break;

                default:
                    System.out.println(
                            "Unknown debug command: " + command);
                    printCommands(step);
                    break;
            }
        }
    }

    /**
     * TestExecutor must call this immediately after beforeStep().
     */
    public boolean shouldForceVerboseCurrentStep() {
        return isEnabled() && forceVerboseCurrentStep;
    }

    public void observeSuccessfulStep(
            TestStep step,
            ExecutionContext context) {

        if (!isEnabled() || step == null) {
            return;
        }

        String op =
                step.getOp() != null
                        ? step.getOp().trim().toUpperCase()
                        : "EXEC";

        if (("EXEC".equals(op)
                || "PUT".equals(op)
                || "GET".equals(op))
                && step.getResponse() != null
                && !step.getResponse().isBlank()) {

            responseNames.add(step.getResponse().trim());
        }
    }

    private void printStepHeader(
            TestCase testCase,
            TestStep step,
            String yamlFile,
            int stepIndex,
            int depth) {

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(
                "DEBUG - BEFORE STEP "
                        + displayStep(step, stepIndex));
        System.out.println("=".repeat(70));

        System.out.println(
                "Test Case : "
                        + safe(testCase != null
                        ? testCase.getName()
                        : null));

        System.out.println(
                "YAML      : " + safe(yamlFile));

        System.out.println(
                "Depth     : " + depth);

        System.out.println(
                "Type      : "
                        + safe(step != null
                        && step.getType() != null
                        ? step.getType().toString()
                        : null));

        System.out.println(
                "Op        : "
                        + safe(step != null
                        ? step.getOp()
                        : null));

        if (step != null
                && step.getCommand() != null
                && !step.getCommand().isBlank()) {

            System.out.println(
                    "Command   : " + step.getCommand());
        }

        printYamlStep(step);

        if (isContainer(step)) {

            System.out.println();
            System.out.println("Navigation:");
            System.out.println(
                    "  i = step into " + scopeName(step));
            System.out.println(
                    "  o / [Enter] = step over " + scopeName(step));
        }

        System.out.println();
        printCommands(step);
    }

    /*
     * Loader can attach comments immediately preceding a step to textYaml.
     *
     * Example:
     *
     *   # 3.
     *   #- type: WAIT
     *   #  value: 300
     *   # 4.
     *   - type: REST
     *
     * That is still ONE TestStep. For debugger display we start at the first
     * active "- type:" line so commented-out old steps are not mistaken for
     * additional executable debug steps.
     */
    private void printYamlStep(TestStep step) {

        if (step == null
                || step.getTextYaml() == null
                || step.getTextYaml().isBlank()) {
            return;
        }

        String yaml = step.getTextYaml();

        if (yaml.trim().startsWith("# Error:")) {
            return;
        }

        String[] lines = yaml.split("\\R");

        int firstActiveStep = -1;

        for (int i = 0; i < lines.length; i++) {

            String trimmed = lines[i].trim();

            if (trimmed.startsWith("- type:")) {
                firstActiveStep = i;
                break;
            }
        }

        if (firstActiveStep < 0) {
            firstActiveStep = 0;
        }

        System.out.println();
        System.out.println("YAML STEP:");
        System.out.println();

        for (int i = firstActiveStep; i < lines.length; i++) {
            System.out.println("  " + lines[i]);
        }
    }

    private void printCommands(TestStep step) {

        System.out.println("Commands:");
        System.out.println("  [Enter] next / step over");
        System.out.println("  s       skip current step");

        if (isContainer(step)) {
            System.out.println("  i       step into");
        }

        System.out.println("  o       step over");
        System.out.println("  u       step out");
        System.out.println("  c       continue to end");
        System.out.println("  v       variables");
        System.out.println("  b       buffers");
        System.out.println("  r       responses");
        System.out.println("  q       quit debug execution");
        System.out.println("  h       help");
        System.out.println();
    }

    private boolean isContainer(TestStep step) {

        if (step == null || step.getType() == null) {
            return false;
        }

        return step.getType() == StepType.CALL
                || step.getType() == StepType.LOOP;
    }

    private String scopeName(TestStep step) {

        if (step == null || step.getType() == null) {
            return "scope";
        }

        if (step.getType() == StepType.CALL) {

            return step.getCommand() != null
                    && !step.getCommand().isBlank()
                    ? "CALL " + step.getCommand()
                    : "CALL";
        }

        if (step.getType() == StepType.LOOP) {
            return "LOOP";
        }

        return step.getType().toString();
    }

    private int determineDepth(TestStep step) {

        if (step == null
                || step.getId() == null
                || step.getId().isBlank()) {
            return 1;
        }

        String id = step.getId().trim();

        if (id.startsWith("step_")) {

            String path =
                    id.substring("step_".length());

            if (path.isBlank()) {
                return 1;
            }

            return path.split("_").length;
        }

        return 1;
    }

    private String displayStep(
            TestStep step,
            int fallbackStepIndex) {

        if (step != null
                && step.getId() != null
                && step.getId().startsWith("step_")) {

            return step.getId()
                    .substring("step_".length())
                    .replace('_', '.');
        }

        return String.valueOf(fallbackStepIndex);
    }

    private void printVariables(
            ExecutionContext context) {

        System.out.println();
        System.out.println("CURRENT VARIABLES");
        System.out.println("-".repeat(70));

        if (context == null
                || context.getVars() == null
                || context.getVars().isEmpty()) {

            System.out.println("  <none>");
            System.out.println("-".repeat(70));
            System.out.println();
            return;
        }

        Map<String, Object> sorted =
                new TreeMap<>(context.getVars());

        sorted.forEach(
                (name, value) ->
                        System.out.printf(
                                "  %-28s = %s%n",
                                name,
                                formatValue(value)));

        System.out.println("-".repeat(70));
        System.out.println();
    }

    private void printBuffers(
            ExecutionContext context) {

        System.out.println();
        System.out.println("CURRENT BUFFERS");
        System.out.println("-".repeat(70));

        if (context == null
                || context.getBufferMap() == null
                || context.getBufferMap().isEmpty()) {

            System.out.println("  <none>");
            System.out.println("-".repeat(70));
            System.out.println();
            return;
        }

        Map<String, Object> sorted =
                new TreeMap<>(context.getBufferMap());

        sorted.forEach(
                (name, value) ->
                        System.out.printf(
                                "  %-28s = %s%n",
                                name,
                                formatValue(value)));

        System.out.println("-".repeat(70));
        System.out.println();
    }

    private void printResponses(
            ExecutionContext context) {

        System.out.println();
        System.out.println("CURRENT RESPONSES");
        System.out.println("-".repeat(70));

        if (responseNames.isEmpty()) {

            System.out.println("  <none>");
            System.out.println("-".repeat(70));
            System.out.println();
            return;
        }

        for (String name : responseNames) {

            Object value =
                    context != null
                            ? context.getResponse(
                            name,
                            Object.class)
                            : null;

            String type =
                    value != null
                            ? value.getClass().getSimpleName()
                            : "null";

            System.out.printf(
                    "  %-28s [%s]%n",
                    name,
                    type);
        }

        System.out.println("-".repeat(70));
        System.out.println();
    }

    private String readLine() {

        try {
            return input.readLine();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Cannot read debug command from console.",
                    e);
        }
    }

    private static String safe(String value) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    private static String formatValue(Object value) {

        if (value == null) {
            return "null";
        }

        String text = String.valueOf(value);

        if (text.length() > 500) {
            return text.substring(0, 500) + "...";
        }

        return text;
    }
}
