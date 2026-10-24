package at.co.svc.agate.ai;
/** Separate verification strategies: no blanket XPath-only veto for value mismatches. */
public final class VerificationRegistry {
    private final AssertionVerifier assertionVerifier = new AssertionVerifier();
    public AssertionVerifier.Result verifyAssertion(AssertionMismatchAnalyzer.Mismatch mismatch, FixProposal proposal) {
        return assertionVerifier.verify(mismatch,proposal);
    }
}
