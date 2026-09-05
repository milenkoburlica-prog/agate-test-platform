package at.co.svc.agate.core.reference;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import at.co.svc.agate.core.dsl.model.TestCase;

public class ReferenceResponseService {

    private final ReferencePathResolver pathResolver;

    private final ReferenceFileStore fileStore;

    private final Map<ResponseFormat, ResponseComparator> comparators =
            new EnumMap<>(ResponseFormat.class);

    public ReferenceResponseService(
            ReferencePathResolver pathResolver,
            ReferenceFileStore fileStore,
            ResponseComparator... responseComparators) {

        this.pathResolver = pathResolver;
        this.fileStore = fileStore;

        if (responseComparators != null) {
            for (ResponseComparator comparator : responseComparators) {

                if (comparator != null) {
                    comparators.put(
                            comparator.getFormat(),
                            comparator);
                }
            }
        }
    }

    public ReferenceAssertionResult assertResponse(
            ResponseFormat format,
            TestCase testCase,
            String stepId,
            String yamlFile,
            String actualResponse,
            ReferenceCompareConfig config)
            throws Exception {

        if (format == null) {
            throw new IllegalArgumentException(
                    "Response format must not be null");
        }

        if (actualResponse == null) {
            throw new IllegalArgumentException(
                    "Actual response must not be null");
        }

        ResponseComparator comparator =
                comparators.get(format);

        if (comparator == null) {
            throw new IllegalStateException(
                    "No ResponseComparator registered for format: "
                            + format);
        }

        ReferenceCompareConfig effectiveConfig =
                config != null
                        ? config
                        : ReferenceCompareConfig.empty();

        Path referenceFile =
                pathResolver.resolve(
                        yamlFile,
                        testCase,
                        stepId,
                        format);

        /*
         * FIRST EXECUTION:
         *
         * There is no approved reference response yet.
         * Store the current response so that the tester can review it.
         */
        if (!fileStore.exists(referenceFile)) {

            String preparedReference =
                    comparator.prepareReference(actualResponse);

            fileStore.create(
                    referenceFile,
                    preparedReference);

            return ReferenceAssertionResult.created(
                    referenceFile);
        }

        /*
         * NORMAL EXECUTION:
         *
         * Reference already exists and is therefore considered
         * tester-approved.
         */
        String reference =
                fileStore.read(referenceFile);

        ComparisonResult comparison =
                comparator.compare(
                        reference,
                        actualResponse,
                        effectiveConfig);

        if (comparison.isEqual()) {
            return ReferenceAssertionResult.matched(
                    referenceFile);
        }

        return ReferenceAssertionResult.different(
                referenceFile,
                comparison);
    }
}