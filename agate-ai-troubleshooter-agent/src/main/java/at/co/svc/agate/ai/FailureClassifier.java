package at.co.svc.agate.ai;
public final class FailureClassifier {
    private final AssertionMismatchAnalyzer assertion = new AssertionMismatchAnalyzer();
    public FailureType classify(String log) {
        if(assertion.inspect(log).isPresent())return FailureType.ASSERTION_VALUE_MISMATCH;
        if(log!=null && log.contains("SQL response was not found"))return FailureType.SQL_RESPONSE_NOT_FOUND;
        if(log!=null && log.contains("SOAP XPath was not found"))return FailureType.XPATH_NOT_FOUND;
        return FailureType.OTHER;
    }
}
