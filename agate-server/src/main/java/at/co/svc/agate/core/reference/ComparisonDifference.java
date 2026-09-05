package at.co.svc.agate.core.reference;

public record ComparisonDifference(
        DifferenceType type,
        String path,
        String expected,
        String actual) {

    @Override
    public String toString() {
        return """
                %s
                  path     : %s
                  expected : %s
                  actual   : %s
                """.formatted(
                type,
                path,
                expected,
                actual);
    }
}