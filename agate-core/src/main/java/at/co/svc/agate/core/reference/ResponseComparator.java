package at.co.svc.agate.core.reference;

public interface ResponseComparator {

    ResponseFormat getFormat();

    ComparisonResult compare(
            String reference,
            String actual,
            ReferenceCompareConfig config) throws Exception;

    /**
     * Converts the first actual response into the representation that will
     * be stored permanently as reference.
     *
     * XML implementation can pretty-print XML.
     * JSON implementation can pretty-print JSON.
     */
    default String prepareReference(String content) throws Exception {
        return content;
    }
}