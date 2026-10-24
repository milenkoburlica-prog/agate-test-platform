package at.co.svc.agate.ai;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AssertionMismatchTest {
    private static String demo() throws Exception {
        return Files.readString(Path.of("examples/demo-logs/10-tf082-assertion-mismatch.log"));
    }
    @Test void distinguishesValueMismatchFromXpathFailure() throws Exception {
        var mismatch=new AssertionMismatchAnalyzer().inspect(demo()).orElseThrow();
        assertTrue(mismatch.path().contains("fotoInformation"));
        assertTrue(mismatch.expected().contains("Der Patient muss"));
        assertTrue(mismatch.actual().contains("kein Foto"));
        assertEquals(FailureType.ASSERTION_VALUE_MISMATCH,new FailureClassifier().classify(demo()));
        assertTrue(mismatch.setupEvidence().contains("SPERRE_AB_OFFSET=31"));
    }
    @Test void noAutomaticExpectedRewrite() throws Exception {
        DiagnosticResult result = new AssertionMismatchDiagnostic().analyze(demo()).orElseThrow();
        assertEquals("URSACHE_EINGEGRENZT",result.verificationStatus());
        assertTrue(result.analysis().contains("Sollwert nicht blind"));
        assertFalse(result.verified());
    }
    @Test void oldXpathScenarioNotIntercepted() throws Exception {
        String data="SOAP ASSERT FAILED: EQUALS | Expected: [0], Actual: [1]";
        assertTrue(new AssertionMismatchDiagnostic().analyze(data).isEmpty());
    }
}
