package at.co.svc.agate.ai;
import java.util.*;
/** Evidence-based diagnosis for a value mismatch, without changing the expected value. */
public final class AssertionMismatchDiagnostic {
    private final AssertionMismatchAnalyzer analyzer=new AssertionMismatchAnalyzer();
    private final VerificationRegistry verification=new VerificationRegistry();
    public Optional<DiagnosticResult> analyze(String log) {
        Optional<AssertionMismatchAnalyzer.Mismatch> found=analyzer.inspect(log);
        if(found.isEmpty())return Optional.empty();
        var m=found.get();
        FixProposal p=new FixProposal(FixType.TEST_DATA,m.path(),m.expected(),null,
                "Vorbereitete Testdaten und fachliche Hinweisvariante prüfen; Sollwert nicht blind an Istwert anpassen.",
                m.setupEvidence(),List.of("FOTO_INFO-Datensatz prüfen", "Fachregel für Hinweisvariante klären", "AGATE-Test erneut ausführen"));
        var check=verification.verifyAssertion(m,p);
        StringBuilder b=new StringBuilder();
        b.append("ENGINE: SOAP\nPROBLEMTYP: ASSERTION_VALUE_MISMATCH\n")
         .append("BEFUND: Der EQUALS-ASSERT meldet unterschiedliche Werte für einen vorhandenen SOAP-Body-Pfad.\n")
         .append("XPath: ").append(m.path()).append("\n")
         .append("ERWARTET: ").append(m.expected()).append("\n")
         .append("TATSÄCHLICH: ").append(m.actual()).append("\n");
        if(!p.evidence().isEmpty()){b.append("\nRELEVANTE TESTVORBEREITUNG:\n");for(String s:p.evidence())b.append("- ").append(s).append('\n');}
        b.append("\nKORREKTURVORSCHLAG: ").append(p.explanation()).append("\n")
         .append("VERIFIKATION: ").append(check.detail()).append("\n")
         .append("PRÜFSCHRITTE:\n");
        for(String s:p.verificationSteps())b.append("- ").append(s).append('\n');
        b.append("ERGEBNIS: URSACHE_EINGEGRENZT. AGATE-TESTAUSFÜHRUNG: NICHT DURCHGEFÜHRT.");
        return Optional.of(new DiagnosticResult("JAVA",b.toString(),false,"URSACHE_EINGEGRENZT"));
    }
}
