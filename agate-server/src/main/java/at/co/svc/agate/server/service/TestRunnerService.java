package at.co.svc.agate.server.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.TestExecutor;
import at.co.svc.agate.core.dsl.register.YamlTestCaseLoader;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.engine.gui.GuiEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class TestRunnerService {

    @Inject
    public LogService logService;

    @Inject
    public ObjectMapper objectMapper;

    // Mapa koja čuva stanje (kontekst) za svaku aktivnu debug sesiju
    private final Map<String, ExecutionContext> debugContexts = new ConcurrentHashMap<>();

    private void enrichStepIfNeeded(TestCase tc, Map<String, Object> stepData) {
        String type = String.valueOf(stepData.get("type"));
        String op = String.valueOf(stepData.get("op"));

        if (("REST".equals(type) || "SOAP".equals(type)) && "EXEC".equalsIgnoreCase(op)) {
            try {
                // 1. Konvertujemo mapu u TestStep objekat da bi imali pristup parametrima
                // {R[...]}
                TestStep currentStep = objectMapper.convertValue(stepData, TestStep.class);

                // 2. Pozivamo loader koji će napuniti 'stepData' (mapu) podacima iz fajlova
                YamlTestCaseLoader.handleFileBasedRestCall(tc, stepData, currentStep);

                // 3. KLJUČNO: Mapiramo HTTP metodu iz fajla u naš objekt, ali NE diramo 'op'
                // Pretpostavljamo da je handleFileBasedRestCall stavio metodu (npr. "POST") u
                // stepData pod ključem "method"
                if (stepData.containsKey("method")) {
                    currentStep.setMethod(String.valueOf(stepData.get("method")));

                    // Opciono: Vrati ovo nazad u mapu ako ti treba za dalju obradu,
                    // mada je currentStep onaj koji ide u Executor
                    stepData.put("method", currentStep.getMethod());
                }

                // Napomena: stepData.get("op") ostaje "EXEC" jer ga nismo menjali,
                // što je upravo ono što si želeo da postigneš.

            } catch (Exception e) {
                logService.addLog(tc.getName(), "ERROR during step enrichment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Izvršava samo jedan specifičan korak iz Test Case-a uz podršku za dinamičke
     * varijable sa klijenta.
     */
    public void executeSingleStep(String appId, String fileName, String testCaseId, String sessionId, int stepIndex,
            String env, String user, Map<String, String> variables, Map<String, Object> stepData, Boolean isVerbose) {

        TestLogger frameworkLogger = msg -> logService.addLog(sessionId, msg);
        ExecutionContext context = debugContexts.computeIfAbsent(sessionId, k -> new ExecutionContext(frameworkLogger));

        try {
            TestCase virtualTc = new TestCase();
            virtualTc.setName(testCaseId);

            // --- KORAK A: PRENOS IZ KONTEKSTA U TEST CASE (Dugoročna -> Kratkoročna
            // memorija) ---
            if (context.getVars() != null) {
                context.getVars().forEach((k, v) -> {
                    virtualTc.addVariable(k, String.valueOf(v));
                    // Dodajemo i B[verziju] za resolver
                    virtualTc.addVariable("B[" + k + "]", String.valueOf(v));
                });
            }

            // Dodajemo statičke varijable koje šalje Angular (readers.serverip, itd.)
            if (variables != null) {
                variables.forEach((k, v) -> {
                    virtualTc.addVariable(k, v);
                    context.setVar(k, v);
                });
            }

            // Standardni enrichment
            System.setProperty("PERSON", user);
            System.setProperty("INSTANCE", env);
            enrichStepIfNeeded(virtualTc, stepData);

            TestStep virtualStep = objectMapper.convertValue(stepData, TestStep.class);
            TestExecutor executor = new TestExecutor(frameworkLogger);

            // --- IZVRŠENJE KORAKA ---
            executor.executeSingleStep(virtualTc, virtualStep, context, "DEBUG", stepIndex + 1, isVerbose);

            // --- KORAK B: SINHRONIZACIJA NAZAD (Kratkoročna -> Dugoročna memorija) ---
            // Uzimamo SVE varijable koje su sada u virtualTc (uključujući one koje je
            // BUFFER upravo napravio)
            // i prepisujemo ih u context sesiju.
            if (virtualTc.getVariables() != null) {
                virtualTc.getVariables().forEach((k, v) -> {
                    // Preskačemo varijable koje već imaju prefiks B[ ili E[ da ne dupliramo smeće
                    if (!k.contains("[") && !k.contains("]")) {
                        context.setVar(k, v);
                    }
                });
            }

        } catch (Exception e) {
            logService.addLog(sessionId, "DEBUG ERROR: " + e.getMessage());
            logService.addLog(sessionId, "[[TEST_STATUS:FAILED]]");
            e.printStackTrace();
        }
    }

    /**
     * Briše sačuvani kontekst nakon što se debug sesija završi.
     */
    public void removeDebugContext(String sessionId) {
        debugContexts.remove(sessionId);
    }

    public void executeTestSuite(String appId, String fileName, String testCaseId, String sessionId, String env,
            String user) {
        // 1. Provera šta stiže sa frontenda
        logService.addLog(sessionId, "DEBUG: Primljeno -> ENV: " + env + ", USER: " + user);

        if (env != null)
            System.setProperty("INSTANCE", env);
        if (user != null)
            System.setProperty("PERSON", user);

        // 2. Provera radnog direktorijuma (Gde Quarkus misli da se nalazi?)
        logService.addLog(sessionId, "DEBUG: Working Dir: " + System.getProperty("user.dir"));

        String yamlFile = "data/" + appId + "/" + fileName;
        logService.addLog(sessionId, "DEBUG: Tražim YAML na: " + yamlFile);
        TestLogger frameworkLogger = msg -> logService.addLog(sessionId, msg);
        TestExecutor executor = new TestExecutor(frameworkLogger);

        try {
            List<TestCase> allTestCases = YamlTestCaseLoader.loadTestCases(yamlFile);
            Optional<TestCase> targetTcOpt = allTestCases.stream().filter(tc -> tc.getName().equals(testCaseId))
                    .findFirst();

            if (targetTcOpt.isEmpty()) {
                logService.addLog(sessionId, "ERROR: Test Case " + testCaseId + " not found.");
                return;
            }

            TestCase targetTc = targetTcOpt.get();
            executor.executeTestCase(targetTc, yamlFile);

        } catch (Exception e) {
            logService.addLog(sessionId, "FATAL ERROR: " + e.getMessage());
        } finally {
            logService.addLog(sessionId, "Finalizing suite and cleaning up resources...");
            GuiEngine.cleanup(sessionId, frameworkLogger);
            logService.finishSession(sessionId);
            // Osiguravamo da i ovde obrišemo context ako postoji
            removeDebugContext(sessionId);
        }
    }

    /**
     * Pokreće kompletan test case koristeći korake i varijable dostavljene sa
     * frontenda.
     */
    public void runFullTest(String appId, String fileName, String sessionId, String testCaseId,
            List<Map<String, Object>> stepsData, Map<String, String> variables, String env, String user,
            Boolean isVerbose) {

        TestLogger frameworkLogger = msg -> logService.addLog(sessionId, msg);
        ExecutionContext context = new ExecutionContext(frameworkLogger);

        if (env != null)
            System.setProperty("INSTANCE", env);
        if (user != null)
            System.setProperty("PERSON", user);

        try {
            // 1. Učitavamo originalni fajl sa diska da pokupimo CSV tabele i definicije
            String yamlFile = "data/" + appId + "/" + fileName;
            List<TestCase> allTestCases = YamlTestCaseLoader.loadTestCases(yamlFile);
            
            Optional<TestCase> originalTcOpt = allTestCases.stream()
                    .filter(tc -> tc.getName().equals(testCaseId))
                    .findFirst();

            TestCase finalTc;

            if (originalTcOpt.isPresent()) {
                finalTc = originalTcOpt.get();
                finalTc.getSteps().clear(); // Brišemo stare korake sa diska
                //logService.addLog(sessionId, "DEBUG: Uspešno učitan kontekst tabela sa diska za " + testCaseId);
            } else {
                finalTc = new TestCase();
                finalTc.setName(testCaseId);
            }

            // 2. Dodajemo/prebrisujemo varijable iz Angulara
            if (variables != null) {
                variables.forEach((key, value) -> {
                    finalTc.addVariable(key, value);
                });
            }

            // 3. Ubacujemo modifikovane korake sa klijenta u finalTc
            for (int i = 0; i < stepsData.size(); i++) {
                Map<String, Object> stepMap = stepsData.get(i);

                // Radimo enrichment za REST/SOAP ako ima potrebe
                enrichStepIfNeeded(finalTc, stepMap);

                TestStep currentStep = objectMapper.convertValue(stepMap, TestStep.class);
                finalTc.addStep(currentStep); // Dodajemo korak u listu koraka test case-a
            }

            // 4. KLJUČNI PREOKRET: Umesto executeSingleStep, puštamo ceo engine da vodi test!
            TestExecutor executor = new TestExecutor(frameworkLogger);
            
            // Ova metoda unutar sebe kreira ispravan kontekst i puni ga tabelama iz finalTc!
            executor.executeTestCase(finalTc, yamlFile);

        } catch (Exception e) {
            logService.addLog(sessionId, "CRITICAL RUN ERROR: " + e.getMessage());
         // 2. Šalješ SPECIJALNI TAG koji Angular lako prepoznaje
            logService.addLog(sessionId, "[[TEST_STATUS:FAILED]]");
            e.printStackTrace();
        } finally {
            logService.finishSession(sessionId);
            System.clearProperty("INSTANCE");
            System.clearProperty("PERSON");
        }
    }

}