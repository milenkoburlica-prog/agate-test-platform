package at.co.svc.agate.core.dsl.register;



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;



import at.co.svc.agate.core.debug.DebugAction;
import at.co.svc.agate.core.debug.DebugController;
import at.co.svc.agate.core.debug.DebugQuitException;

import at.co.svc.agate.core.dsl.model.StepType;

import at.co.svc.agate.core.dsl.model.TestCase;

import at.co.svc.agate.core.dsl.model.TestStep;

import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;

import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

import at.co.svc.agate.core.dsl.utils.ConsoleColors;

import at.co.svc.agate.core.error.AgateStepException;

import at.co.svc.agate.core.interfaces.TestLogger;

import at.co.svc.agate.core.interfaces.TestStepEngine;

import at.co.svc.agate.engine.buffer.BufferEngine;

import at.co.svc.agate.engine.call.CallEngine;

import at.co.svc.agate.engine.cmd.CmdEngine;

import at.co.svc.agate.engine.file.FileEngine;

import at.co.svc.agate.engine.gui.GuiEngine;
import at.co.svc.agate.engine.gui.GuiSessionManager;
import at.co.svc.agate.engine.gui.GuiRunScope;

import at.co.svc.agate.engine.json.JsonEngine;
import at.co.svc.agate.engine.loop.LoopEngine;

import at.co.svc.agate.engine.oc.OcCmdEngine;

import at.co.svc.agate.engine.pdf.PdfEngine;

import at.co.svc.agate.engine.rest.RestEngine;

import at.co.svc.agate.engine.soap.SoapEngine;

import at.co.svc.agate.engine.sql.SqlEngine;

import at.co.svc.agate.engine.wait.WaitEngine;

import io.qameta.allure.Allure;

import io.qameta.allure.model.Status;



public class TestExecutor {

    /** Allure is opt-in; the standard AGATE HTML/JSON reporting remains independent. */
    private static boolean isAllureEnabled() {
        return Boolean.parseBoolean(System.getProperty("allure.enabled", "false"));
    }




    private final List<TestStepEngine> engines =

            new ArrayList<>();



    private final TestLogger logger;



    private static boolean shutdownHookAdded =

            false;



    private final YamlPlaceholderResolver resolver =

            new YamlPlaceholderResolver();


    private final DebugController debugController =

            new DebugController();



    public TestExecutor(

            TestLogger logger) {



        this.logger =

                logger;



        if (!shutdownHookAdded) {



            Runtime.getRuntime()

                    .addShutdownHook(

                            new Thread(

                                    () -> {



                                        GuiEngine.cleanupAll(

                                                null);

                                    }

                            )

                    );



            shutdownHookAdded =

                    true;

        }



        engines.add(

                new SqlEngine());



        engines.add(

                new RestEngine());



        engines.add(

                new CmdEngine());



        engines.add(

                new SoapEngine());



        engines.add(

                new WaitEngine());



        engines.add(

                new OcCmdEngine());



        engines.add(

                new GuiEngine());



        engines.add(

                new JsonEngine());



        engines.add(

                new BufferEngine());



        engines.add(

                new PdfEngine());



        engines.add(

                new FileEngine());



        engines.add(

                new CallEngine(

                        this::executeSingleStep));

        engines.add(new at.co.svc.agate.engine.window.WindowEngine());

        engines.add(
                new LoopEngine(
                        this::executeSingleStep,
                        this::evaluateLoopCondition));

    }



    private GuiRunScope guiRunScope;
    /** Applies to all test cases executed with this executor. */
    public TestExecutor withGuiScope(GuiRunScope scope) {
        this.guiRunScope = scope;
        return this;
    }

    public void executeTestCase(

            TestCase tc,

            String yamlFile)

            throws Exception {



        ExecutionContext context =

                new ExecutionContext(

                        this.logger);

        // A test gets its own ExecutionContext, while the suite owns the browser.
        // Without this binding GuiSessionManager falls back to new GuiSession(),
        // launching and closing a dedicated Playwright/Browser for every test.
        if (guiRunScope != null) {
            GuiSessionManager.bind(context, guiRunScope);
        }




        YamlPlaceholderResolver resolver =

                new YamlPlaceholderResolver();



        String uuid =

                UUID.randomUUID()

                        .toString();



        io.qameta.allure.model.TestResult allureResult =

                new io.qameta.allure.model.TestResult();



        allureResult.setUuid(

                uuid);



        allureResult.setName(

                tc.getName());



        allureResult.setDescription(

                tc.getDescription());



        allureResult.setFullName(

                yamlFile

                        + " : "

                        + tc.getName());



        if (isAllureEnabled()) {
            Allure.getLifecycle()

                .scheduleTestCase(

                        allureResult);
        }



        if (isAllureEnabled()) {
            Allure.getLifecycle()

                .startTestCase(

                        uuid);
        }



        // Variables remain literal declarations; deferred references must not
        // shadow values produced by executable steps in ExecutionContext.
        TestVariableInitializer.copyLiteralValues(tc, context);

        int stepIndex =

                1;



        try {



            for (TestStep step :

                    tc.getSteps()) {



                /*

                 * Top-level steps are always visible.

                 *

                 * For CALL this does NOT mean that reusable

                 * child steps are automatically verbose.

                 *

                 * CallEngine decides separately whether its

                 * children should be shown.

                 */

                executeSingleStep(

                        tc,

                        step,

                        context,

                        yamlFile,

                        stepIndex,

                        true

                );



                stepIndex++;

            }



            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .updateTestCase(

                            uuid,

                            result ->

                                    result.setStatus(

                                            Status.PASSED));
            }



        } catch (DebugQuitException e) {

            if (isAllureEnabled()) {
                Allure.getLifecycle()
                    .updateTestCase(
                            uuid,
                            result ->
                                    result.setStatus(
                                            Status.SKIPPED));
            }

            throw e;

        } catch (Exception e) {

            String errorMsg =

                    e.getMessage() != null

                            ? e.getMessage().trim()

                            : "Unknown Error";



            io.qameta.allure.model.StatusDetails details =

                    new io.qameta.allure.model.StatusDetails();



            details.setMessage(

                    errorMsg);



            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .updateTestCase(

                            uuid,

                            result -> {



                                result.setStatus(

                                        Status.FAILED);



                                result.setStatusDetails(

                                        details);

                            }

                    );
            }



            throw e;



        } finally {
            try { GuiEngine.cleanup(context, logger); }
            finally { GuiSessionManager.unbind(context); }

            if (isAllureEnabled()) {
                Allure.getLifecycle()
                    .stopTestCase(

                            uuid);
            }



            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .writeTestCase(

                            uuid);
            }

        }

    }



    /*

     * =========================================================

     * CENTRAL STEP EXECUTION

     * =========================================================

     *

     * isVerbose means:

     *

     *   true  -> this step is visible

     *   false -> this step belongs to a silent reusable execution

     *

     * Important:

     *

     * parameters.verbose is NOT evaluated here anymore.

     *

     * verbose belongs to CALL and controls visibility of

     * reusable CHILD steps.

     *

     * That decision is therefore made inside CallEngine.

     * =========================================================

     */

    public void executeSingleStep(
            TestCase testCase, TestStep step, ExecutionContext context,
            String yamlFile, int currentStepNumber, boolean isVerbose) throws Exception {
        // Sections are deliberately visible even for silent reusable children.
        logSections(testCase, step, step.getSectionsBefore(), context, yamlFile);
        executeActualStep(testCase, step, context, yamlFile, currentStepNumber, isVerbose);
        // Not reached after a failure or debugger quit.
        logSections(testCase, step, step.getSectionsAfter(), context, yamlFile);
    }

    private void logSections(TestCase tc, TestStep owner,
            List<at.co.svc.agate.core.dsl.model.SectionMetadata> sections,
            ExecutionContext context, String yamlFile) {
        for (var section : sections) {
            TestStep metadata = new TestStep();
            metadata.setCondition(section.condition());
            metadata.setParameters(owner.getParameters());
            metadata.setSourceFile(section.sourceFile());
            metadata.setSourceStepIndex(section.sourceStepIndex());
            metadata.setReferenceCallPath(owner.getReferenceCallPath());
            if (!isConditionMet(tc, metadata, resolver,
                    section.sourceFile() == null ? yamlFile : section.sourceFile(),
                    section.sourceStepIndex(), context, false)) continue;
            logger.info(">>> SECTION    : " + section.displayTitle());
            if (section.description() != null && !section.description().isBlank()) {
                logger.info("    Description: " + section.displayDescription());
            }
        }
    }

    private void executeActualStep(

            TestCase testCase,

            TestStep step,

            ExecutionContext context,

            String yamlFile,

            int currentStepNumber,

            boolean isVerbose)

            throws Exception {



        
        DebugAction debugAction =
                debugController.beforeStep(
                        testCase,
                        step,
                        context,
                        yamlFile,
                        currentStepNumber);

        if (debugAction == DebugAction.QUIT) {
            throw new DebugQuitException();
        }

        if (debugAction == DebugAction.SKIP) {
            return;
        }

        /*
         * If the debugger actually stopped on this step, show the real
         * engine execution even when CALL/LOOP originally passed
         * isVerbose=false for reusable children.
         *
         * This is local to the current invocation. Step Over children
         * remain silent because they never receive a debugger prompt.
         */
        boolean effectiveVerbose =
                debugController.shouldForceVerboseCurrentStep()
                        ? true
                        : isVerbose;

long startTime =

                System.currentTimeMillis();



        boolean lastStepStatusFailed =

                false;



        Exception stepFailure =

                null;



        String allureStepName =

                currentStepNumber

                        + ". "

                        + step.getType()

                        + ": "

                        + (

                        step.getOp() != null

                                ? step.getOp()

                                : ""

                )

                        + " "

                        + (

                        step.getCommand() != null

                                ? step.getCommand()

                                : ""

                );



        String stepUuid =

                UUID.randomUUID()

                        .toString();



        io.qameta.allure.model.StepResult allureStepResult =

                new io.qameta.allure.model.StepResult();



        allureStepResult.setName(

                allureStepName);



        if (isAllureEnabled()) {
            Allure.getLifecycle()

                .startStep(

                        stepUuid,

                        allureStepResult);
        }



        try {



            /*

             * Condition check.

             */

            if (step.getType() != StepType.LOOP
                    && !isConditionMet(
                            testCase,
                            step,
                            resolver,
                            yamlFile,
                            currentStepNumber,
                            context,
                            isVerbose)) {



                if (isAllureEnabled()) {
                    Allure.getLifecycle()

                        .updateStep(

                                stepUuid,

                                s ->

                                        s.setStatus(

                                                Status.SKIPPED));
                }



                return;

            }



            try {



                enrichStepDataIfNeeded(

                        testCase,

                        step);



            } catch (Exception e) {



                lastStepStatusFailed =

                        true;



                /*

                 * Show YAML immediately if preparation itself fails.

                 */

                if (step.getTextYaml() != null

                        && !step.getTextYaml()

                        .isEmpty()) {



                    logger.info(

                            String.format(

                                    "%s%s\n%s",

                                    ConsoleColors.RED,

                                    ConsoleColors.RESET,

                                    step.getTextYaml()));

                }



                logger.info(

                        String.format(

                                "    %s>>> ERROR | %s%s",

                                ConsoleColors.RED,

                                e.getMessage(),

                                ConsoleColors.RESET));



                throw e;

            }



            TestStepEngine engine =

                    engines.stream()

                            .filter(

                                    e ->

                                            e.canExecute(

                                                    step.getType()))

                            .findFirst()

                            .orElseThrow(

                                    () ->

                                            new RuntimeException(

                                                    "No engine found for type: "

                                                            + step.getType()));



            engine.execute(

                    testCase,

                    step,

                    context,

                    yamlFile,

                    currentStepNumber,

                    effectiveVerbose,

                    this.logger,

                    effectiveVerbose

            );

            debugController.observeSuccessfulStep(
                    step,
                    context);




            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .updateStep(

                            stepUuid,

                            s ->

                                    s.setStatus(

                                            Status.PASSED));
            }



        } catch (Exception e) {



            lastStepStatusFailed =

                    true;



            stepFailure =

                    e;



            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .updateStep(

                            stepUuid,

                            s ->

                                    s.setStatus(

                                            Status.FAILED));
            }



            throw e;



        } finally {



            long duration =

                    System.currentTimeMillis()

                            - startTime;



            if (isAllureEnabled()) {
                Allure.addAttachment(

                    "Step Duration",

                    "text/plain",

                    duration + " ms");
            }



            if (isAllureEnabled()) {
                Allure.getLifecycle()

                    .stopStep(

                            stepUuid);
            }



            /*

             * Failed reusable steps must always be visible.

             */

            if (lastStepStatusFailed) {



                /*

                 * If a normally silent reusable step failed,

                 * print its DSL now.

                 */

                if (!effectiveVerbose

                        && step.getType()

                        != StepType.CALL

                        && step.getTextYaml()

                        != null

                        && !step.getTextYaml()

                        .isBlank()) {



                    logger.info("");



                    logger.info(

                            "    >>> REUSABLE STEP "

                                    + formatStepNumber(

                                    step));



                    logger.info("");



                    for (String yamlLine :

                            step.getTextYaml()

                                    .split("\\R")) {



                        logger.info(

                                "    >>> DSL      "

                                        + yamlLine);

                    }



                    logger.info("");

                }



                logFailedStepLocation(step, yamlFile);
                if (step.getCondition() != null && !step.getCondition().isBlank()) {
                    logFailedCondition(testCase, step, context, yamlFile, currentStepNumber);
                }

                logger.info(
                        String.format(
                                "    %s>>> %s STEP FINISHED | Status: FAILED | Duration: %d ms%s",

                                ConsoleColors.RED,

                                step.getType(),

                                duration,

                                ConsoleColors.RESET));



                /*

                 * CALL only propagates inner failure.

                 *

                 * The actual failing reusable child has already

                 * printed its structured failure details.

                 */

                if (stepFailure != null

                        && step.getType()

                        != StepType.CALL) {



                    logFailureDetails(

                            stepFailure);

                }



            } else if (effectiveVerbose) {



                /*

                 * Visible normal step.

                 */

                logger.info(

                        String.format(

                                "    %s>>> %s STEP FINISHED | Status: SUCCESS | Duration: %d ms%s",

                                ConsoleColors.GREEN,

                                step.getType(),

                                duration,

                                ConsoleColors.RESET));

            }



            /*

             * IMPORTANT:

             *

             * There is intentionally NO special

             *

             *   else if (step.getType() == CALL)

             *

             * anymore.

             *

             * A hidden nested CALL must remain completely silent.

             *

             * A top-level CALL is visible because executeTestCase()

             * invokes it with isVerbose=true.

             */

        }

    }



    private String formatStepNumber(

            TestStep step) {



        if (step == null

                || step.getId() == null

                || step.getId()

                .isBlank()) {



            return "?";

        }



        String id =

                step.getId();



        /*

         * Generated hierarchical IDs:

         *

         * step_1     -> 1

         * step_1_1   -> 1.1

         * step_1_2   -> 1.2

         * step_1_2_3 -> 1.2.3

         */

        if (id.startsWith(

                "step_")) {



            return id

                    .substring(

                            "step_".length())

                    .replace(

                            '_',

                            '.');

        }



        /*

         * Explicit user-defined step id.

         */

        return id;

    }



    private void logFailureDetails(

            Exception failure) {



        if (failure

                instanceof AgateStepException) {



            AgateStepException agateFailure =

                    (AgateStepException)

                            failure;



            logger.info(

                    String.format(

                            "        %-8s : %s",

                            "Reason",

                            agateFailure

                                    .getReason()));



            agateFailure

                    .getDetails()

                    .forEach(

                            (label, value) ->

                                    logger.info(

                                            String.format(

                                                    "        %-8s : %s",

                                                    label,

                                                    value)));



            return;

        }



        String message =

                failure.getMessage();



        if (message == null

                || message.isBlank()) {



            message =

                    failure.getClass()

                            .getSimpleName();

        }



        logger.info(

                String.format(

                        "        %-8s : %s",

                        "Reason",

                        message.trim()));

    }



    /** Diagnostics run only for FAILED steps; never change execution semantics. */
    private void logFailedStepLocation(TestStep step, String fallbackYaml) {
        String source = step.getSourceFile();
        if (source == null || source.isBlank()) source = fallbackYaml;
        if (step.getType() == StepType.CALL) {
            logger.info("        Reusable : " + (step.getCommand() == null ? "<unknown>" : step.getCommand()));
            if (step.getSubSteps() != null && !step.getSubSteps().isEmpty()) {
                String calledFile = step.getSubSteps().get(0).getSourceFile();
                if (calledFile != null && !calledFile.isBlank())
                    logger.info("        Called YAML File: " + calledFile);
            }
        }
        if (source != null && !source.isBlank()) {
            logger.info("        YAML File: " + source);
            Integer line = findSourceLine(source, step);
            if (line != null) logger.info("        YAML Line: " + line);
            else if (step.getSourceStepIndex() != null && step.getSourceStepIndex() > 0)
                logger.info("        Step Index: " + step.getSourceStepIndex() + " (YAML line unavailable)");
        }
    }

    private Integer findSourceLine(String source, TestStep step) {
        try {
            Path file = Paths.get(source);
            if (!Files.isRegularFile(file)) return null;
            List<String> lines = Files.readAllLines(file);
            String id = step.getId();
            if (id != null && !id.isBlank()) {
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.matches("-?\\s*id\\s*:.*") && line.contains(id)) {
                        for (int j = i; j >= 0 && j >= i - 5; j--)
                            if (lines.get(j).stripLeading().startsWith("- ")) return j + 1;
                        return i + 1;
                    }
                }
            }
            // A source step index is not a YAML line number; do not pretend otherwise.
        } catch (IOException | RuntimeException ignored) { }
        return null;
    }

    private void logFailedCondition(TestCase tc, TestStep step, ExecutionContext context,
                                    String yamlFile, int stepNumber) {
        String condition = step.getCondition();
        logger.info("        Condition (FAILED step): " + condition);
        Map<String, Object> values = new HashMap<>();
        if (tc.getVariables() != null) values.putAll(tc.getVariables());
        values.putAll(context.getVars());
        values.putAll(context.getBufferMap());
        if (step.getParameters() != null) values.putAll(step.getParameters());
        try {
            String resolved = resolver.resolve(tc, condition, values, yamlFile,
                                               stepNumber, "condition", step);
            logger.info("        Condition Resolved: " + normalizeNullLiterals(resolved));
        } catch (RuntimeException ex) {
            logger.info("        Condition Resolution Error: " + ex.getMessage());
        }
        // Only report referenced variables, never dump all test data.
        Pattern token = Pattern.compile("\\{(?:XL|B|T)\\[([^]]+)]}|[A-Za-z_][A-Za-z0-9_.]*");
        Matcher matcher = token.matcher(condition);
        java.util.Set<String> shown = new java.util.LinkedHashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1) != null ? matcher.group(1) : matcher.group();
            if (!values.containsKey(name) || !shown.add(name)) continue;
            String printable = name.toLowerCase(java.util.Locale.ROOT).matches(".*(password|passwd|secret|token|pin|credential|apikey|api_key).*")
                    ? "***" : String.valueOf(values.get(name));
            logger.info("        Condition Var: " + name + " = " + printable);
        }
    }

    private boolean isConditionMet(

            TestCase tc,

            TestStep step,

            YamlPlaceholderResolver resolver,

            String yamlPath,

            int idx,

            ExecutionContext context,

            boolean isVerbose) {



        String condition =

                step.getCondition();



        if (condition == null

                || condition.trim()

                .isEmpty()) {



            return true;

        }



        /*

         * Collect all available runtime values.

         */

        Map<String, Object> allData =

                new HashMap<>();



        if (tc.getVariables() != null) {



            allData.putAll(

                    tc.getVariables());

        }



        allData.putAll(

                context.getVars());



        allData.putAll(

                context.getBufferMap());



        condition = resolveSqlRowCountsInCondition(condition, context);
        condition = resolveSqlCellsInCondition(condition, context);

        String resolvedCondition =

                resolver.resolve(

                        tc,

                        condition,

                        allData,

                        yamlPath,

                        idx,

                        "condition",

                        step);



        resolvedCondition =

                normalizeNullLiterals(

                        resolvedCondition

                );



        ConditionEvaluator ce =

                new ConditionEvaluator();



        // Reusable parameters take precedence over general runtime variables.
        if (step.getParameters() != null) allData.putAll(step.getParameters());
        Boolean isTrue = ce.evaluate(resolvedCondition, allData);





        if (!isTrue

                && isVerbose) {



            if (step.getTextYaml() != null && !step.getTextYaml().isBlank()) {
                logger.info("    >>> SKIPPED STEP DSL:");
                for (String yamlLine : step.getTextYaml().split("\\R")) {
                    logger.info("    >>> DSL      " + yamlLine);
                }
            }

            logger.info(

                    String.format(

                            "    >>> %sSKIPPED%s (Condition false: %s)",

                            ConsoleColors.YELLOW,

                            ConsoleColors.RESET,

                            resolvedCondition));

        }



        return isTrue;

    }

    /*
     * LOOP conditions use exactly the same condition semantics as normal
     * AGATE steps, including placeholder resolution and {NULL}
     * normalization. isVerbose=false suppresses the normal SKIPPED log;
     * LoopEngine logs a successful loop exit itself.
     */
    /**
     * SQL_ROW_COUNT counts result-set rows (not affected rows of UPDATE/DELETE).
     * An empty SELECT produces a stored empty List and therefore resolves to 0.
     * Missing or non-result-set responses fail explicitly rather than pretending
     * that no rows were returned.
     */
    private String resolveSqlRowCountsInCondition(String expression, ExecutionContext context) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "\\{SQL_ROW_COUNT\\[([^]\\r\\n]+)\\]\\}")
                .matcher(expression);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String responseName = matcher.group(1);
            Object response = context.getResponse(responseName, Object.class);
            if (!(response instanceof java.util.List<?> rows)) {
                throw new IllegalArgumentException(
                        "SQL_ROW_COUNT requires an existing SELECT result set: '" + responseName + "'");
            }
            matcher.appendReplacement(result, String.valueOf(rows.size()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Resolves SQL result cells only in conditions; no Tosca placeholders are introduced. */
    private String resolveSqlCellsInCondition(String expression, ExecutionContext context) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "\\{SQL\\[([^]\\r\\n]+)\\]\\[(\\d+)\\]\\[(\\d+)\\]\\}")
                .matcher(expression);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String responseName = matcher.group(1);
            int row = Integer.parseInt(matcher.group(2));
            int column = Integer.parseInt(matcher.group(3));
            Object raw = context.getResponse(responseName, Object.class);
            if (!(raw instanceof java.util.List<?> rows) || row >= rows.size()
                    || !(rows.get(row) instanceof java.util.Map<?, ?> values)
                    || column >= values.size()) {
                throw new IllegalArgumentException("SQL condition cannot read cell [" + row + "]["
                        + column + "] from response '" + responseName + "'");
            }
            Object cell = new java.util.ArrayList<>(values.values()).get(column);
            if (cell == null) {
                throw new IllegalArgumentException("SQL condition result cell is null: " + responseName);
            }
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(cell.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public boolean evaluateLoopCondition(
            TestCase testCase,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            boolean isVerbose) {

        return isConditionMet(
                testCase,
                step,
                resolver,
                yamlFile,
                stepIndex,
                context,
                false
        );
    }




    @SuppressWarnings("unchecked")

    private void enrichStepDataIfNeeded(

            TestCase tc,

            TestStep step)

            throws Exception {



        if ((step.getType()

                == StepType.REST

                || step.getType()

                == StepType.SOAP)

                && "EXEC".equalsIgnoreCase(

                step.getOp())) {



            Map<String, Object> tempMap =

                    new HashMap<>();



            tempMap.put(

                    "type",

                    step.getType()

                            .toString());



            tempMap.put(

                    "op",

                    step.getOp());



            tempMap.put(

                    "command",

                    step.getCommand());



            YamlTestCaseLoader

                    .handleFileBasedRestCall(

                            tc,

                            tempMap,

                            step);



            if (tempMap.containsKey(

                    "method")) {



                step.setMethod(

                        tempMap.get(

                                        "method")

                                .toString());

            }



            if (tempMap.containsKey(

                    "url")) {



                step.setUrl(

                        tempMap.get(

                                        "url")

                                .toString());

            }



            if (tempMap.containsKey(

                    "op")) {



                step.setOp(

                        tempMap.get(

                                        "op")

                                .toString());

            }



            if (tempMap.containsKey(

                    "body")) {



                step.setBody(

                        tempMap.get(

                                        "body")

                                .toString());

            }



            if (tempMap.containsKey(

                    "headers")) {



                step.setHeaders(

                        (Map<String, String>)

                                tempMap.get(

                                        "headers"));

            }

        }

    }



    private static String normalizeNullLiterals(

            String condition) {



        if (condition == null

                || condition.isBlank()) {



            return condition;

        }



        String normalized =

                condition;



        /*

         * Quoted AGATE NULL literals first.

         */

        normalized =

                normalized.replaceAll(

                        "(?i)'\\s*\\{NULL\\}\\s*'",

                        "NULL"

                );



        normalized =

                normalized.replaceAll(

                        "(?i)\"\\s*\\{NULL\\}\\s*\"",

                        "NULL"

                );



        /*

         * Unquoted AGATE NULL literal.

         */

        normalized =

                normalized.replaceAll(

                        "(?i)\\{NULL\\}",

                        "NULL"

                );



        return normalized;

    }





}
