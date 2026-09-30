package at.co.svc.agate.core.dsl.register;



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;



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

        engines.add(
                new LoopEngine(
                        this::executeSingleStep,
                        this::evaluateLoopCondition));

    }



    public void executeTestCase(

            TestCase tc,

            String yamlFile)

            throws Exception {



        ExecutionContext context =

                new ExecutionContext(

                        this.logger);



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



        Allure.getLifecycle()

                .scheduleTestCase(

                        allureResult);



        Allure.getLifecycle()

                .startTestCase(

                        uuid);



        Map<String, Object> resolvedVars =

                new HashMap<>();



        if (tc.getVariables() != null) {



            tc.getVariables()

                    .forEach(

                            (k, v) -> {



                                if (k != null

                                        && v != null) {



                                    String resolvedValue =

                                            resolver.resolve(

                                                    tc,

                                                    v.toString(),

                                                    tc.getVariables(),

                                                    null,

                                                    0,

                                                    "HEADER_PRINT"

                                            );



                                    context.setVar(

                                            k,

                                            resolvedValue

                                                    .toString());



                                    resolvedVars.put(

                                            k,

                                            resolvedValue);

                                }

                            }

                    );



            tc.setVariables(

                    resolvedVars);

        }



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



            Allure.getLifecycle()

                    .updateTestCase(

                            uuid,

                            result ->

                                    result.setStatus(

                                            Status.PASSED));



        } catch (DebugQuitException e) {

            Allure.getLifecycle()
                    .updateTestCase(
                            uuid,
                            result ->
                                    result.setStatus(
                                            Status.SKIPPED));

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



            throw e;



        } finally {



            Allure.getLifecycle()

                    .stopTestCase(

                            uuid);



            Allure.getLifecycle()

                    .writeTestCase(

                            uuid);

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



        Allure.getLifecycle()

                .startStep(

                        stepUuid,

                        allureStepResult);



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



                Allure.getLifecycle()

                        .updateStep(

                                stepUuid,

                                s ->

                                        s.setStatus(

                                                Status.SKIPPED));



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

                    true,

                    this.logger,

                    effectiveVerbose

            );

            debugController.observeSuccessfulStep(
                    step,
                    context);




            Allure.getLifecycle()

                    .updateStep(

                            stepUuid,

                            s ->

                                    s.setStatus(

                                            Status.PASSED));



        } catch (Exception e) {



            lastStepStatusFailed =

                    true;



            stepFailure =

                    e;



            Allure.getLifecycle()

                    .updateStep(

                            stepUuid,

                            s ->

                                    s.setStatus(

                                            Status.FAILED));



            throw e;



        } finally {



            long duration =

                    System.currentTimeMillis()

                            - startTime;



            Allure.addAttachment(

                    "Step Duration",

                    "text/plain",

                    duration + " ms");



            Allure.getLifecycle()

                    .stopStep(

                            stepUuid);



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



        Boolean isTrue =

                ce.evaluate(

                        resolvedCondition

                );





        if (!isTrue

                && isVerbose) {



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
