package at.co.svc.agate.engine.pdf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import at.co.svc.agate.core.dsl.model.PdfAssertion;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

public class PdfEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.PDF;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "ASSERT";
        long startTime = System.currentTimeMillis();

        try {
            switch (op) {
                case "ASSERT" -> executeAssert(tc, step, context, yamlFile, stepIndex, logger, printExecution, isVerbose);
                default -> throw new IllegalArgumentException("[PDF Engine] Unsupported operation: " + op);
            }

            if (Boolean.TRUE.equals(printExecution)) {
                long duration = System.currentTimeMillis() - startTime;
                if (isVerbose) {
                    logger.info(String.format("    %s>>> RESULT    %s: SUCCESS (Duration: %d ms)", ConsoleColors.GREEN, ConsoleColors.RESET, duration));
                }
            }

        } catch (Exception e) {
            if (isVerbose) {
                logger.info(String.format("    %s>>> STEP FAILED | ERROR: %s%s", ConsoleColors.RED, e.getMessage(), ConsoleColors.RESET));
            }
            throw e;
        }
    }

    private void executeAssert(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            TestLogger logger, Boolean printExecution, boolean isVerbose) throws Exception {

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        Map<String, Object> combinedVars = new HashMap<>(
                tc.getVariables() != null ? tc.getVariables() : new HashMap<>());
        if (context != null && context.getVars() != null) {
            combinedVars.putAll(context.getVars());
        }

        String rawTargetPDF = step.getTargetPDF();
        String rawPassword = step.getPdfPassword();

        if (rawTargetPDF == null) {
            throw new IllegalArgumentException("[PDF Engine] 'targetPDF' is missing in step configuration.");
        }

        String pdfFileName = resolver.resolve(tc, rawTargetPDF, combinedVars, yamlFile, stepIndex, "targetPDF");
        String password = (rawPassword != null && !rawPassword.trim().isEmpty())
                ? resolver.resolve(tc, rawPassword, combinedVars, yamlFile, stepIndex, "pdfPassword")
                : "";

        File pdfFile = new File(pdfFileName);

        if (!pdfFile.exists()) {
            throw new IOException("[PDF Engine] PDF file not found at: " + pdfFile.getAbsolutePath());
        }

        List<PdfAssertion> assertions = step.getPdfAssertions();
        if (assertions == null || assertions.isEmpty()) {
            throw new IllegalArgumentException("[PDF Engine] Missing or empty 'assertions' list in PDF step.");
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> OPEN PDF  %s: %s (%d assertions)", ConsoleColors.GREEN, ConsoleColors.RESET, pdfFile.getName(), assertions.size()));
        }

        try (PDDocument document = Loader.loadPDF(pdfFile, password)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);

            for (int i = 0; i < assertions.size(); i++) {
                PdfAssertion assertion = assertions.get(i);
                String rawValue = assertion.getValue();
                String action = (assertion.getAction() != null) ? assertion.getAction().toUpperCase() : "EXIST";

                if (rawValue == null) {
                    throw new IllegalArgumentException("[PDF Engine] Assertion at index " + i + " is missing 'value'.");
                }

                String textToFind = resolver.resolve(tc, rawValue, combinedVars, yamlFile, stepIndex, "value");

                boolean passed = false;
                String logDetail = "";

                switch (action) {
                    case "EXIST":
                        passed = pdfText.contains(textToFind);
                        logDetail = passed ? String.format("Found text [%s]", textToFind) : String.format("Text [%s] does NOT exist in PDF", textToFind);
                        break;

                    case "NO_EXIST":
                        passed = !pdfText.contains(textToFind);
                        logDetail = passed ? String.format("Text [%s] absent as expected", textToFind) : String.format("Text [%s] was found, but it should NOT exist", textToFind);
                        break;

                    case "COUNT":
                        int expectedCount = (assertion.getExpected() != null) ? assertion.getExpected() : 1;
                        int actualCount = countMatches(pdfText, textToFind);
                        passed = (actualCount == expectedCount);
                        logDetail = String.format("String [%s] count = %d (expected: %d)", textToFind, actualCount, expectedCount);
                        break;

                    default:
                        throw new IllegalArgumentException("[PDF Engine] Unknown action: " + action);
                }

                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    String statusColor = passed ? ConsoleColors.GREEN : ConsoleColors.RED;
                    logger.info(String.format("    %s>>> ASSERT    %s: [%s] %s \"%s\" -> %s", statusColor, ConsoleColors.RESET, action, textToFind, logDetail));
                }

                if (!passed) {
                    throw new AssertionError("[PDF Engine] ASSERT FAILED: " + logDetail);
                }
            }
        }
    }

    private int countMatches(String text, String subText) {
        if (text == null || subText == null || subText.isEmpty()) {
            return 0;
        }
        int count = 0;
        Pattern pattern = Pattern.compile(Pattern.quote(subText));
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}