package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.dto.CleanStep;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ProcessTBoxEvaluationRuntimeOutputTest {

    @Test
    void reviewForUnsupportedStepNeverPollutesYamlStdout() throws Exception {
        CleanStep step = new CleanStep();
        step.setName("Prüfe Batch-Status '0 STARTED'");
        ByteArrayOutputStream yaml = new ByteArrayOutputStream();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try (PrintStream out = new PrintStream(yaml, true, StandardCharsets.UTF_8);
             PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            System.setErr(err);
            assertFalse(ProcessTBoxEvaluationRuntime.processEvaluation(step, ""));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        assertEquals("", yaml.toString(StandardCharsets.UTF_8),
                "Review diagnostics must not be emitted as raw YAML text");
        assertTrue(diagnostics.toString(StandardCharsets.UTF_8).contains("[Review]"));
    }
}
