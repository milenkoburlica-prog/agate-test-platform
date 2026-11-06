package at.co.svc.agate.core.reference;

import java.nio.file.Path;

public class ReferenceAssertionResult {

    private final ReferenceAssertionStatus status;

    private final Path referenceFile;

    private final ComparisonResult comparisonResult;

    private ReferenceAssertionResult(
            ReferenceAssertionStatus status,
            Path referenceFile,
            ComparisonResult comparisonResult) {

        this.status = status;
        this.referenceFile = referenceFile;
        this.comparisonResult = comparisonResult;
    }

    public static ReferenceAssertionResult created(
            Path referenceFile) {

        return new ReferenceAssertionResult(
                ReferenceAssertionStatus.REFERENCE_CREATED,
                referenceFile,
                ComparisonResult.equal());
    }

    public static ReferenceAssertionResult matched(
            Path referenceFile) {

        return new ReferenceAssertionResult(
                ReferenceAssertionStatus.MATCHED,
                referenceFile,
                ComparisonResult.equal());
    }

    public static ReferenceAssertionResult different(
            Path referenceFile,
            ComparisonResult comparisonResult) {

        return new ReferenceAssertionResult(
                ReferenceAssertionStatus.DIFFERENT,
                referenceFile,
                comparisonResult);
    }

    public ReferenceAssertionStatus getStatus() {
        return status;
    }

    public Path getReferenceFile() {
        return referenceFile;
    }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    public boolean isCreated() {
        return status == ReferenceAssertionStatus.REFERENCE_CREATED;
    }

    public boolean isMatched() {
        return status == ReferenceAssertionStatus.MATCHED;
    }

    public boolean isDifferent() {
        return status == ReferenceAssertionStatus.DIFFERENT;
    }
}