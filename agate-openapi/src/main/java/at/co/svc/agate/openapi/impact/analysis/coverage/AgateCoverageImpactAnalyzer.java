package at.co.svc.agate.openapi.impact.analysis.coverage;

import at.co.svc.agate.openapi.change.detection.AgateApiChange;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import at.co.svc.agate.openapi.impact.analysis.artifact.AgateOperationArtifactResolver;
import at.co.svc.agate.openapi.impact.analysis.artifact.AgateOperationArtifactSet;

import at.co.svc.agate.openapi.impact.analysis.contract.AgateContractSchemaResolver;
import at.co.svc.agate.openapi.impact.analysis.contract.AgateContractSchemaResolver.ResolvedContractField;
import at.co.svc.agate.openapi.impact.analysis.contract.AgateNewContractIndex;

import at.co.svc.agate.openapi.impact.analysis.csv.AgateCsvArtifactReader;
import at.co.svc.agate.openapi.impact.analysis.csv.AgateCsvSnapshot;

import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.ConstraintType;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.IntentType;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntentResolver;

import at.co.svc.agate.openapi.impact.analysis.model.AgateArtifactType;
import at.co.svc.agate.openapi.impact.analysis.model.AgateImpactReport;
import at.co.svc.agate.openapi.impact.analysis.model.AgateRecommendedAction;
import at.co.svc.agate.openapi.impact.analysis.model.AgateTestCaseImpact;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class AgateCoverageImpactAnalyzer {


    private final AgateOperationArtifactResolver artifactResolver =
            new AgateOperationArtifactResolver();


    private final AgateCsvArtifactReader csvReader =
            new AgateCsvArtifactReader();


    private final AgateContractSchemaResolver schemaResolver =
            new AgateContractSchemaResolver();


    private final AgateTestCaseIntentResolver intentResolver =
            new AgateTestCaseIntentResolver();




    public void analyze(
            AgateOpenApiChangeSet changeSet,
            AgateOpenApiModel newContract,
            Path appDirectory,
            AgateImpactReport report)
            throws Exception {

        if (changeSet == null ||
                newContract == null ||
                appDirectory == null ||
                report == null) {

            return;
        }


        AgateNewContractIndex contractIndex =
                new AgateNewContractIndex(
                        newContract
                );


        for (AgateApiChange change :
                changeSet.getChanges()) {

            analyzeChange(
                    change,
                    contractIndex,
                    appDirectory,
                    report
            );
        }
    }




    private void analyzeChange(
            AgateApiChange change,
            AgateNewContractIndex contractIndex,
            Path appDirectory,
            AgateImpactReport report)
            throws Exception {

        if (change == null) {

            return;
        }


        String location =
                change.getLocation();


        String property =
                change.getProperty();


        if (location == null ||
                property == null) {

            return;
        }


        if (!location.startsWith(
                "request."
        )) {

            return;
        }


        if (!isCoverageRelevantProperty(
                property
        )) {

            return;
        }


        AgateOperationModel operation =
                contractIndex.getOperation(
                        change.getOperationIdentity()
                );


        if (operation == null) {

            return;
        }


        AgateOperationArtifactSet artifacts =
                artifactResolver.resolve(
                        operation,
                        appDirectory
                );


        Path csvFile =
                artifacts.getCsvFile();


        if (csvFile == null ||
                !Files.exists(
                        csvFile
                )) {

            return;
        }


        AgateCsvSnapshot csv =
                csvReader.read(
                        csvFile
                );


        ResolvedContractField resolvedField =
                schemaResolver.resolve(
                        operation,
                        location
                );


        if (resolvedField == null ||
                resolvedField.getSchema() == null) {

            return;
        }


        String field =
                fieldName(
                        location
                );


        if (field == null) {

            return;
        }


        List<AgateCoverageRequirement> requirements =
                buildRequirements(
                        change,
                        field,
                        resolvedField.getSchema()
                );


        for (AgateCoverageRequirement requirement :
                requirements) {

            CoverageCheckResult check =
                    checkCoverage(
                            csv,
                            requirement
                    );


            if (check.getStatus()
                    == CoverageStatus.COVERED) {

                continue;
            }


            /*
             * Existing testcase with correct semantic intent,
             * but still containing the old value.
             *
             * The normal impact pass already reports
             * REGENERATE_BOUNDARY_VALUE / CHANGE_CSV_VALUE.
             *
             * Do not report ADD_TEST_CASE again.
             */

            if (check.getStatus()
                    == CoverageStatus.STALE) {

                continue;
            }


            /*
             * No testcase with the required semantic coverage
             * exists.
             */

            addMissingCoverageImpact(
                    change,
                    artifacts,
                    requirement,
                    report
            );
        }
    }




    private List<AgateCoverageRequirement> buildRequirements(
            AgateApiChange change,
            String field,
            AgateSchema schema) {

        List<AgateCoverageRequirement> result =
                new ArrayList<>();


        String property =
                change.getProperty();


        switch (property) {

            case "minimum" -> {

                if (schema.getMinimum() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MINIMUM,
                                    schema.getMinimum(),
                                    "Boundary testcase for new minimum is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.BELOW_MINIMUM,
                                    below(
                                            schema.getMinimum()
                                    ),
                                    "Negative testcase below new minimum is missing"
                            )
                    );
                }
            }


            case "maximum" -> {

                if (schema.getMaximum() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MAXIMUM,
                                    schema.getMaximum(),
                                    "Boundary testcase for new maximum is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.ABOVE_MAXIMUM,
                                    above(
                                            schema.getMaximum()
                                    ),
                                    "Negative testcase above new maximum is missing"
                            )
                    );
                }
            }


            case "minLength" -> {

                if (schema.getMinLength() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MIN_LENGTH,
                                    schema.getMinLength(),
                                    "Boundary testcase for new minLength is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.BELOW_MIN_LENGTH,
                                    Math.max(
                                            0,
                                            schema.getMinLength() - 1
                                    ),
                                    "Negative testcase below new minLength is missing"
                            )
                    );
                }
            }


            case "maxLength" -> {

                if (schema.getMaxLength() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MAX_LENGTH,
                                    schema.getMaxLength(),
                                    "Boundary testcase for new maxLength is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.ABOVE_MAX_LENGTH,
                                    schema.getMaxLength() + 1,
                                    "Negative testcase above new maxLength is missing"
                            )
                    );
                }
            }


            case "minItems" -> {

                if (schema.getMinItems() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MIN_ITEMS,
                                    schema.getMinItems(),
                                    "Boundary testcase for new minItems is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.BELOW_MIN_ITEMS,
                                    Math.max(
                                            0,
                                            schema.getMinItems() - 1
                                    ),
                                    "Negative testcase below new minItems is missing"
                            )
                    );
                }
            }


            case "maxItems" -> {

                if (schema.getMaxItems() != null) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.MAX_ITEMS,
                                    schema.getMaxItems(),
                                    "Boundary testcase for new maxItems is missing"
                            )
                    );


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.ABOVE_MAX_ITEMS,
                                    schema.getMaxItems() + 1,
                                    "Negative testcase above new maxItems is missing"
                            )
                    );
                }
            }


            case "enum" -> {

                /*
                 * Only genuinely new enum values should create
                 * new test coverage.
                 *
                 * Existing enum values are already represented
                 * by existing old tests.
                 */

                Set<String> oldEnumValues =
                        enumValues(
                                change.getOldValue()
                        );


                Set<String> newEnumValues =
                        enumValues(
                                schema.getEnumValues()
                        );


                for (String newEnumValue :
                        newEnumValues) {

                    if (oldEnumValues.contains(
                            newEnumValue
                    )) {

                        continue;
                    }


                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.ENUM_VALUE,
                                    newEnumValue,
                                    "New enum value "
                                            + newEnumValue
                                            + " is not covered"
                            )
                    );
                }


                /*
                 * The invalid-enum testcase is required only
                 * if no invalid-enum testcase already exists.
                 *
                 * It is not tied to a specific legal enum value.
                 */

                if (schema.getEnumValues() != null &&
                        !schema.getEnumValues().isEmpty()) {

                    result.add(
                            requirement(
                                    change,
                                    field,
                                    AgateCoverageRequirementType.INVALID_ENUM,
                                    "__AGATE_INVALID_ENUM__",
                                    "Invalid enum testcase is missing"
                            )
                    );
                }
            }


            default -> {
            }
        }


        return result;
    }




    private CoverageCheckResult checkCoverage(
            AgateCsvSnapshot csv,
            AgateCoverageRequirement requirement) {

        boolean semanticTestExists =
                false;


        String staleTestcase =
                null;


        String staleValue =
                null;


        for (int i = 0;
             i < csv.getTestcaseCount();
             i++) {

            String testcase =
                    csv.getTestcaseName(
                            i
                    );


            AgateTestCaseIntent intent =
                    intentResolver.resolve(
                            testcase
                    );


            if (!matchesRequirementIntent(
                    intent,
                    requirement
            )) {

                continue;
            }


            semanticTestExists =
                    true;


            String value =
                    csv.getValue(
                            requirement.getField(),
                            i
                    );


            /*
             * ENUM_VALUE is special:
             *
             * Multiple valid enum testcases may exist.
             *
             * Example:
             * USER
             * ADMIN
             * AUDITOR
             *
             * Therefore we must inspect all matching enum
             * testcases before declaring the required value
             * stale/missing.
             */

            if (requirement.getType()
                    == AgateCoverageRequirementType.ENUM_VALUE) {

                if (matchesExpectedValue(
                        value,
                        requirement
                )) {

                    return CoverageCheckResult.covered(
                            testcase,
                            value
                    );
                }


                continue;
            }


            /*
             * INVALID_ENUM is semantic coverage.
             *
             * The concrete invalid token does not have to be
             * compared to a legal enum value.
             */

            if (requirement.getType()
                    == AgateCoverageRequirementType.INVALID_ENUM) {

                return CoverageCheckResult.covered(
                        testcase,
                        value
                );
            }


            if (matchesExpectedValue(
                    value,
                    requirement
            )) {

                return CoverageCheckResult.covered(
                        testcase,
                        value
                );
            }


            staleTestcase =
                    testcase;


            staleValue =
                    value;
        }


        /*
         * For ENUM_VALUE:
         *
         * Existing tests for USER / ADMIN do NOT mean that
         * a missing new enum value IT is "stale".
         *
         * IT represents additional required coverage.
         *
         * Therefore an uncovered enum value is MISSING.
         */

        if (requirement.getType()
                == AgateCoverageRequirementType.ENUM_VALUE) {

            return CoverageCheckResult.missing();
        }


        if (semanticTestExists) {

            return CoverageCheckResult.stale(
                    staleTestcase,
                    staleValue
            );
        }


        return CoverageCheckResult.missing();
    }




    private boolean matchesRequirementIntent(
            AgateTestCaseIntent intent,
            AgateCoverageRequirement requirement) {

        if (intent == null) {

            return false;
        }


        if (!intent.targetsField(
                requirement.getField()
        )) {

            return false;
        }


        return switch (requirement.getType()) {

            case MINIMUM ->
                    intent.getConstraintType()
                            == ConstraintType.MINIMUM
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case BELOW_MINIMUM ->
                    intent.getConstraintType()
                            == ConstraintType.MINIMUM
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case MAXIMUM ->
                    intent.getConstraintType()
                            == ConstraintType.MAXIMUM
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case ABOVE_MAXIMUM ->
                    intent.getConstraintType()
                            == ConstraintType.MAXIMUM
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case MIN_LENGTH ->
                    intent.getConstraintType()
                            == ConstraintType.MIN_LENGTH
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case BELOW_MIN_LENGTH ->
                    intent.getConstraintType()
                            == ConstraintType.MIN_LENGTH
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case MAX_LENGTH ->
                    intent.getConstraintType()
                            == ConstraintType.MAX_LENGTH
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case ABOVE_MAX_LENGTH ->
                    intent.getConstraintType()
                            == ConstraintType.MAX_LENGTH
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case MIN_ITEMS ->
                    intent.getConstraintType()
                            == ConstraintType.MIN_ITEMS
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case BELOW_MIN_ITEMS ->
                    intent.getConstraintType()
                            == ConstraintType.MIN_ITEMS
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case MAX_ITEMS ->
                    intent.getConstraintType()
                            == ConstraintType.MAX_ITEMS
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_VALID;


            case ABOVE_MAX_ITEMS ->
                    intent.getConstraintType()
                            == ConstraintType.MAX_ITEMS
                            &&
                            intent.getIntentType()
                                    == IntentType.BOUNDARY_INVALID;


            case ENUM_VALUE ->
                    intent.getConstraintType()
                            == ConstraintType.ENUM
                            &&
                            intent.getIntentType()
                                    == IntentType.ENUM_VALID;


            case INVALID_ENUM ->
                    intent.getConstraintType()
                            == ConstraintType.ENUM
                            &&
                            intent.isNegativeTest();
        };
    }




    private boolean matchesExpectedValue(
            String current,
            AgateCoverageRequirement requirement) {

        if (current == null) {

            return false;
        }


        return switch (requirement.getType()) {

            case ENUM_VALUE ->
                    sameTextValue(
                            current,
                            requirement.getExpectedValue()
                    );


            case INVALID_ENUM ->
                    true;


            case MINIMUM,
                 BELOW_MINIMUM,
                 MAXIMUM,
                 ABOVE_MAXIMUM ->

                    sameNumericOrTextValue(
                            current,
                            requirement.getExpectedValue()
                    );


            case MIN_LENGTH,
                 BELOW_MIN_LENGTH,
                 MAX_LENGTH,
                 ABOVE_MAX_LENGTH ->

                    !isNullValue(
                            current
                    )
                            &&
                            current.length()
                                    == asInt(
                                    requirement.getExpectedValue()
                            );


            case MIN_ITEMS,
                 BELOW_MIN_ITEMS,
                 MAX_ITEMS,
                 ABOVE_MAX_ITEMS ->

                    !isNullValue(
                            current
                    )
                            &&
                            arraySize(
                                    current
                            )
                                    == asInt(
                                    requirement.getExpectedValue()
                            );
        };
    }




    private void addMissingCoverageImpact(
            AgateApiChange change,
            AgateOperationArtifactSet artifacts,
            AgateCoverageRequirement requirement,
            AgateImpactReport report) {

        AgateTestCaseImpact impact =
                new AgateTestCaseImpact();


        impact.setOperationIdentity(
                change.getOperationIdentity()
        );


        impact.setChangeLocation(
                change.getLocation()
        );


        /*
         * Important for impact deduplication:
         *
         * coverage.enum_value
         * coverage.min_length
         * coverage.below_min_length
         *
         * are intentionally different keys.
         */

        impact.setChangeProperty(
                "coverage."
                        + requirement
                                .getType()
                                .name()
                                .toLowerCase()
        );


        impact.setArtifactType(
                AgateArtifactType.CSV
        );


        impact.setArtifact(
                artifacts.getCsvFile()
        );


        impact.setArtifactLocation(
                "test coverage: "
                        + requirement.getField()
                        + "."
                        + requirement
                                .getType()
                                .name()
                                .toLowerCase()
        );


        impact.setTestcaseName(
                suggestedTestcaseName(
                        requirement
                )
        );


        impact.setCurrentValue(
                null
        );


        impact.setExpectedValue(
                String.valueOf(
                        requirement.getExpectedValue()
                )
        );


        impact.setAction(
                AgateRecommendedAction.ADD_TEST_CASE
        );


        impact.setReason(
                requirement.getDescription()
        );


        report.addImpact(
                impact
        );
    }




    private String suggestedTestcaseName(
            AgateCoverageRequirement requirement) {

        String field =
                requirement
                        .getField()
                        .replace(
                                '.',
                                '_'
                        );


        return switch (requirement.getType()) {

            case MINIMUM ->
                    field
                            + "_minimum";


            case BELOW_MINIMUM ->
                    field
                            + "_below_minimum";


            case MAXIMUM ->
                    field
                            + "_maximum";


            case ABOVE_MAXIMUM ->
                    field
                            + "_above_maximum";


            case MIN_LENGTH ->
                    field
                            + "_minimum_string_length";


            case BELOW_MIN_LENGTH ->
                    field
                            + "_below_minimum_string_length";


            case MAX_LENGTH ->
                    field
                            + "_maximum_string_length";


            case ABOVE_MAX_LENGTH ->
                    field
                            + "_above_maximum_string_length";


            case MIN_ITEMS ->
                    field
                            + "_minimum_array_size";


            case BELOW_MIN_ITEMS ->
                    field
                            + "_below_minimum_array_size";


            case MAX_ITEMS ->
                    field
                            + "_maximum_array_size";


            case ABOVE_MAX_ITEMS ->
                    field
                            + "_above_maximum_array_size";


            case ENUM_VALUE ->
                    field
                            + "_valid_enum_value_"
                            + normalizeValueForName(
                                    requirement.getExpectedValue()
                            );


            case INVALID_ENUM ->
                    field
                            + "_invalid_enum_value";
        };
    }




    private String normalizeValueForName(
            Object value) {

        if (value == null) {

            return "null";
        }


        String result =
                value
                        .toString()
                        .trim()
                        .replaceAll(
                                "[^a-zA-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "_+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );


        if (result.isBlank()) {

            return "value";
        }


        return result;
    }




    private AgateCoverageRequirement requirement(
            AgateApiChange change,
            String field,
            AgateCoverageRequirementType type,
            Object expectedValue,
            String description) {

        AgateCoverageRequirement result =
                new AgateCoverageRequirement();


        result.setOperationIdentity(
                change.getOperationIdentity()
        );


        result.setField(
                field
        );


        result.setType(
                type
        );


        result.setExpectedValue(
                expectedValue
        );


        result.setDescription(
                description
        );


        return result;
    }




    private boolean isCoverageRelevantProperty(
            String property) {

        return "minimum".equals(
                property
        )
                ||
                "maximum".equals(
                        property
                )
                ||
                "minLength".equals(
                        property
                )
                ||
                "maxLength".equals(
                        property
                )
                ||
                "minItems".equals(
                        property
                )
                ||
                "maxItems".equals(
                        property
                )
                ||
                "enum".equals(
                        property
                );
    }




    private Set<String> enumValues(
            Object value) {

        Set<String> result =
                new LinkedHashSet<>();


        if (value == null) {

            return result;
        }


        if (value instanceof Iterable<?> iterable) {

            for (Object item :
                    iterable) {

                if (item != null) {

                    result.add(
                            item
                                    .toString()
                                    .trim()
                    );
                }
            }


            return result;
        }


        /*
         * Change detector may expose old enum through
         * toString() such as:
         *
         * [de, en, fr]
         */

        String text =
                value
                        .toString()
                        .trim();


        if (text.startsWith("[") &&
                text.endsWith("]")) {

            text =
                    text.substring(
                            1,
                            text.length() - 1
                    );
        }


        if (text.isBlank()) {

            return result;
        }


        String[] parts =
                text.split(
                        ","
                );


        for (String part :
                parts) {

            String item =
                    part.trim();


            if (!item.isBlank()) {

                result.add(
                        item
                );
            }
        }


        return result;
    }




    private Object below(
            Object value) {

        if (value == null) {

            return null;
        }


        BigDecimal number =
                new BigDecimal(
                        value.toString()
                );


        return number
                .subtract(
                        BigDecimal.ONE
                )
                .stripTrailingZeros()
                .toPlainString();
    }




    private Object above(
            Object value) {

        if (value == null) {

            return null;
        }


        BigDecimal number =
                new BigDecimal(
                        value.toString()
                );


        return number
                .add(
                        BigDecimal.ONE
                )
                .stripTrailingZeros()
                .toPlainString();
    }




    private boolean sameTextValue(
            String current,
            Object expected) {

        if (current == null ||
                expected == null) {

            return false;
        }


        if (isNullValue(
                current
        )) {

            return false;
        }


        return current
                .trim()
                .equals(
                        expected
                                .toString()
                                .trim()
                );
    }




    private boolean sameNumericOrTextValue(
            String current,
            Object expected) {

        if (current == null ||
                expected == null) {

            return false;
        }


        if (isNullValue(
                current
        )) {

            return false;
        }


        String left =
                current.trim();


        String right =
                expected
                        .toString()
                        .trim();


        try {

            BigDecimal leftNumber =
                    new BigDecimal(
                            left
                    );


            BigDecimal rightNumber =
                    new BigDecimal(
                            right
                    );


            return leftNumber.compareTo(
                    rightNumber
            ) == 0;

        } catch (NumberFormatException ignored) {
        }


        return left.equals(
                right
        );
    }




    private int asInt(
            Object value) {

        if (value == null) {

            return -1;
        }


        return new BigDecimal(
                value.toString()
        )
                .intValue();
    }




    private int arraySize(
            String value) {

        if (value == null ||
                isNullValue(
                        value
                )) {

            return -1;
        }


        String current =
                value.trim();


        if ("[]".equals(
                current
        )) {

            return 0;
        }


        if (!current.startsWith("[") ||
                !current.endsWith("]")) {

            return 1;
        }


        current =
                current.substring(
                        1,
                        current.length() - 1
                )
                        .trim();


        if (current.isEmpty()) {

            return 0;
        }


        return current
                .split(
                        ","
                )
                .length;
    }




    private boolean isNullValue(
            String value) {

        if (value == null) {

            return true;
        }


        return "{NULL}".equalsIgnoreCase(
                value.trim()
        );
    }




    private String fieldName(
            String location) {

        if (location == null) {

            return null;
        }


        if (location.startsWith(
                "request.body."
        )) {

            return location.substring(
                    "request.body."
                            .length()
            );
        }


        int index =
                location.lastIndexOf(
                        '.'
                );


        return index >= 0
                ? location.substring(
                        index + 1
                )
                : location;
    }




    private enum CoverageStatus {

        COVERED,

        STALE,

        MISSING
    }




    private static class CoverageCheckResult {


        private final CoverageStatus status;

        private final String testcaseName;

        private final String currentValue;




        private CoverageCheckResult(
                CoverageStatus status,
                String testcaseName,
                String currentValue) {

            this.status =
                    status;


            this.testcaseName =
                    testcaseName;


            this.currentValue =
                    currentValue;
        }




        public CoverageStatus getStatus() {

            return status;
        }




        public String getTestcaseName() {

            return testcaseName;
        }




        public String getCurrentValue() {

            return currentValue;
        }




        private static CoverageCheckResult covered(
                String testcaseName,
                String currentValue) {

            return new CoverageCheckResult(
                    CoverageStatus.COVERED,
                    testcaseName,
                    currentValue
            );
        }




        private static CoverageCheckResult stale(
                String testcaseName,
                String currentValue) {

            return new CoverageCheckResult(
                    CoverageStatus.STALE,
                    testcaseName,
                    currentValue
            );
        }




        private static CoverageCheckResult missing() {

            return new CoverageCheckResult(
                    CoverageStatus.MISSING,
                    null,
                    null
            );
        }
    }
}