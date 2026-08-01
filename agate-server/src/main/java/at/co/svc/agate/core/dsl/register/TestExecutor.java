package at.co.svc.agate.core.dsl.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.interfaces.TestStepEngine;
import at.co.svc.agate.engine.buffer.BufferEngine;
import at.co.svc.agate.engine.call.CallEngine;
import at.co.svc.agate.engine.cmd.CmdEngine;
import at.co.svc.agate.engine.gui.GuiEngine;
import at.co.svc.agate.engine.json.JsonEngine;
import at.co.svc.agate.engine.oc.OcCmdEngine;
import at.co.svc.agate.engine.pdf.PdfEngine;
import at.co.svc.agate.engine.rest.RestEngine;
import at.co.svc.agate.engine.soap.SoapEngine;
import at.co.svc.agate.engine.sql.SqlEngine;
import at.co.svc.agate.engine.wait.WaitEngine;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

public class TestExecutor {

    private final List<TestStepEngine> engines = new ArrayList<>();
    private final TestLogger logger;
    private static boolean shutdownHookAdded = false;
    private final YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
    //private Boolean closeDialog = false;

    public TestExecutor(TestLogger logger) {
        this.logger = logger;

        if (!shutdownHookAdded) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                GuiEngine.cleanupAll(null);
            }));
            shutdownHookAdded = true;
        }

        engines.add(new SqlEngine());
        engines.add(new RestEngine());
        engines.add(new CmdEngine());
        engines.add(new SoapEngine());
        engines.add(new WaitEngine());
        engines.add(new OcCmdEngine());
        engines.add(new GuiEngine());
        engines.add(new JsonEngine());
        engines.add(new BufferEngine());
        engines.add(new PdfEngine());
        engines.add(new CallEngine(this::executeSingleStep));
        
    }

    public void executeTestCase(TestCase tc, String yamlFile) throws Exception {
        ExecutionContext context = new ExecutionContext(this.logger);
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        
        String uuid = UUID.randomUUID().toString();

        io.qameta.allure.model.TestResult allureResult = new io.qameta.allure.model.TestResult();
        allureResult.setUuid(uuid);
        allureResult.setName(tc.getName());
        allureResult.setDescription(tc.getDescription());
        allureResult.setFullName(yamlFile + " : " + tc.getName());

        Allure.getLifecycle().scheduleTestCase(allureResult);
        Allure.getLifecycle().startTestCase(uuid);
        Map<String, Object> resolvedVars = new HashMap<>();
        if (tc.getVariables() != null) {
            
            tc.getVariables().forEach((k, v) -> {
                if (k != null && v != null) {
                    String resolvedValue = resolver.resolve(
                            tc, 
                            v.toString(), 
                            tc.getVariables(), // Die Variablen als Kontext
                            null,              // yamlPath (für Header nicht zwingend)
                            0,                 // stepIndex
                            "HEADER_PRINT"     // Action Name
                        );
                        //System.out.printf("  %-15s = %s%n", k, resolvedValue);
                        
                    context.setVar(k, resolvedValue.toString());
                    resolvedVars.put(k, resolvedValue);
                }
            });
            tc.setVariables(resolvedVars);
        }

        int stepIndex = 1;
        try {
            for (TestStep step : tc.getSteps()) {
                // Podrazumevano isVerbose može biti true ili povučeno iz konfiguracije
                executeSingleStep(tc, step, context, yamlFile, stepIndex, true);
                stepIndex++;
            }
            Allure.getLifecycle().updateTestCase(uuid, result -> result.setStatus(Status.PASSED));
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().trim() : "Unknown Error";

            io.qameta.allure.model.StatusDetails details = new io.qameta.allure.model.StatusDetails();
            details.setMessage(errorMsg);

            Allure.getLifecycle().updateTestCase(uuid, result -> {
                result.setStatus(Status.FAILED);
                result.setStatusDetails(details);
            });

            throw e;
        } finally {
            Allure.getLifecycle().stopTestCase(uuid);
            Allure.getLifecycle().writeTestCase(uuid);
        }
    }

    // JEDINSTVENA METODA ZA IZVRŠAVANJE KORAKA (Svi pozivi prolaze kroz Allure)
    public void executeSingleStep(TestCase testCase, TestStep step, ExecutionContext context, String yamlFile,
            int currentStepNumber, boolean isVerbose) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean lastStepStatusFailed = false;
        // =========================================================================
        // NOVO: Logika za selektivni default (CALL = false, izvan CALL-a = true)
        // =========================================================================
        boolean defaultVerbose = (step.getType() == StepType.CALL) ? false : true;

        // Ako je ovo unutrašnji korak, a roditeljski CALL je već utišan (isVerbose je
        // false), utišaj i njega
        if (step.getType() != StepType.CALL && !isVerbose) {
            defaultVerbose = false;
        }

        // Proveri da li je korisnik u samom YAML koraku ručno pregazio vrednost
        if (step.getParameters() != null && step.getParameters().containsKey("verbose")) {
            isVerbose = Boolean.parseBoolean(step.getParameters().get("verbose").toString());
        } else {
            // Ako parametra nema u YAML-u, primeni izračunati default
            isVerbose = defaultVerbose;
        }
        // =========================================================================

        String allureStepName = currentStepNumber + ". " + step.getType() + ": "
                + (step.getOp() != null ? step.getOp() : "") + " "
                + (step.getCommand() != null ? step.getCommand() : "");

        String stepUuid = UUID.randomUUID().toString();
        io.qameta.allure.model.StepResult allureStepResult = new io.qameta.allure.model.StepResult();
        allureStepResult.setName(allureStepName);

        Allure.getLifecycle().startStep(stepUuid, allureStepResult);

        try {
            // Uslov (Condition) provera
            if (!isConditionMet(testCase, step, resolver, yamlFile, currentStepNumber, context, isVerbose)) {
                Allure.getLifecycle().updateStep(stepUuid, s -> s.setStatus(Status.SKIPPED));
                return; // Skačemo na finally blok koji će zatvoriti korak kao SKIPPED
            }

            //enrichStepDataIfNeeded(testCase, step);
            try {
                // ... tvoj kod koji poziva enrichStepDataIfNeeded ...
                enrichStepDataIfNeeded(testCase, step); 
            } catch (Exception e) {
                lastStepStatusFailed = true;
                
                // 1. Ispisujemo YAML korak koji je izazvao grešku
                if (step.getTextYaml() != null && !step.getTextYaml().isEmpty()) {
                    logger.info(String.format("%s%s\n%s", 
                                 ConsoleColors.RED, ConsoleColors.RESET, step.getTextYaml()));
                }
                
                // 2. Ispisujemo formatiranu grešku
                logger.info(String.format("    %s>>> ERROR | %s%s", 
                             ConsoleColors.RED, e.getMessage(), ConsoleColors.RESET));
                
                throw e; // Odlazi u finally blok
            }
            
            TestStepEngine engine = engines.stream().filter(e -> e.canExecute(step.getType())).findFirst()
                    .orElseThrow(() -> new RuntimeException("No engine found for type: " + step.getType()));

            engine.execute(testCase, step, context, yamlFile, currentStepNumber, true, this.logger, isVerbose);

            Allure.getLifecycle().updateStep(stepUuid, s -> s.setStatus(Status.PASSED));

        } catch (Exception e) {
            lastStepStatusFailed = true;
            Allure.getLifecycle().updateStep(stepUuid, s -> s.setStatus(Status.FAILED));
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Allure.addAttachment("Step Duration", "text/plain", duration + " ms");
            Allure.getLifecycle().stopStep(stepUuid);

            // LOGUJ SAMO AKO NIJE CALL STEP (jer CallEngine to radi za sebe)
            // ILI ako je FAILED (jer grešku moramo videti u svakom slučaju)
            if (step.getType() != StepType.CALL || lastStepStatusFailed) {
                if (isVerbose) 
                {
                    String status = lastStepStatusFailed ? "FAILED" : "SUCCESS";
                    String color = lastStepStatusFailed ? ConsoleColors.RED : ConsoleColors.GREEN;

                    logger.info(String.format("    %s>>> %s STEP FINISHED | Status: %s | Duration: %d ms%s", 
                                color, step.getType(), status, duration, ConsoleColors.RESET));
                }
            } else {
                String status = lastStepStatusFailed ? "FAILED" : "SUCCESS";
                String color = lastStepStatusFailed ? ConsoleColors.RED : ConsoleColors.GREEN;
                
                logger.info(String.format("    %s>>> %s STEP FINISHED | Status: %s | Duration: %d ms%s", 
                        color, step.getType(), status, duration, ConsoleColors.RESET));
            }
        }
    }

    private boolean isConditionMet(TestCase tc, TestStep step, YamlPlaceholderResolver resolver, String yamlPath,
            int idx, ExecutionContext context, boolean isVerbose) {
        
        String condition = step.getCondition();
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }

        // 1. Inicijalizuj mapu sa svim mogućim izvorima
        Map<String, Object> allData = new HashMap<>();

        // 2. Dodaj varijable iz TestCase-a
        if (tc.getVariables() != null) {
            allData.putAll(tc.getVariables());
        }

        // 3. Dodaj varijable iz ExecutionContext-a (runtime vars)
        allData.putAll(context.getVars());

        // 4. Dodaj sve buffere iz ExecutionContext-a
        // Pošto tvoj ExecutionContext nema getBufferMap(), koristimo metodu 
        // koju smo definisali ili pristupamo direktno ako možeš
        // Ako ne možeš, moraćemo da dodamo metodu u ExecutionContext
        allData.putAll(context.getBufferMap()); 

        // 5. Sada Resolver radi nad kompletnom slikom
        String resolvedCondition = resolver.resolve(tc, condition, allData, yamlPath, idx, "condition", step);

        ConditionEvaluator ce = new ConditionEvaluator();
        Boolean isTrue = ce.evaluate(resolvedCondition);

        if (!isTrue && isVerbose) {
            logger.info(String.format("    >>> %sSKIPPED%s (Condition false: %s)", ConsoleColors.YELLOW,
                    ConsoleColors.RESET, resolvedCondition));
        }

        return isTrue;
    }
    @SuppressWarnings("unchecked")
    private void enrichStepDataIfNeeded(TestCase tc, TestStep step) throws Exception {
        if ((step.getType() == StepType.REST || step.getType() == StepType.SOAP)
                && "EXEC".equalsIgnoreCase(step.getOp())) {

            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("type", step.getType().toString());
            tempMap.put("op", step.getOp());
            tempMap.put("command", step.getCommand());

//            try {
                YamlTestCaseLoader.handleFileBasedRestCall(tc, tempMap, step);
//            } catch (Exception e) {
//                PrintDslStepContext.logDslStepContext(logger, step);
//                e.printStackTrace();
//            }

            if (tempMap.containsKey("method")) {
                step.setMethod(tempMap.get("method").toString());
            }
            if (tempMap.containsKey("url"))
                step.setUrl(tempMap.get("url").toString());
            if (tempMap.containsKey("op"))
                step.setOp(tempMap.get("op").toString());
            if (tempMap.containsKey("body"))
                step.setBody(tempMap.get("body").toString());
            if (tempMap.containsKey("headers")) {
                step.setHeaders((Map<String, String>) tempMap.get("headers"));
            }
        }
    }
}
