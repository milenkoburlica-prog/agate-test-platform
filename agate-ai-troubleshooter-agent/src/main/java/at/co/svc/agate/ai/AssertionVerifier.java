package at.co.svc.agate.ai;

import java.util.*;

/** Confirms discrepancy, not whether a changed business expectation is correct. */
public final class AssertionVerifier {
    public record Result(boolean mismatchConfirmed, String status, String detail) {}
    public Result verify(AssertionMismatchAnalyzer.Mismatch mismatch, FixProposal proposal) {
        boolean confirmed=mismatch!=null && !Objects.equals(mismatch.expected(),mismatch.actual());
        if(!confirmed) return new Result(false,"NICHT_PRÜFBAR","Keine abweichenden ASSERT-Werte nachgewiesen.");
        if(proposal.type()==FixType.EXPECTED_VALUE)
            return new Result(true,"FACHLICHE_PRÜFUNG_ERFORDERLICH",
                "Expected/Actual-Abweichung im Log bestätigt; eine Änderung des Sollwerts ist fachlich nicht verifiziert.");
        return new Result(true,"FEHLER_BESTÄTIGT",
            "Java hat unterschiedliche Expected/Actual-Werte aus dem fehlgeschlagenen SOAP-ASSERT gelesen. Kein AGATE-Testlauf.");
    }
}
