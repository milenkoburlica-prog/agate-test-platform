package at.co.svc.agate.openapi.impact.analysis.intent;

import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.ConstraintType;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.IntentType;


public class AgateTestCaseIntentResolver {


    public AgateTestCaseIntent resolve(
            String testcaseName) {

        if (testcaseName == null ||
                testcaseName.isBlank()) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    null,
                    ConstraintType.NONE,
                    IntentType.OTHER
            );
        }


        String name =
                normalize(
                        testcaseName
                );


        /*
         * Remove technical testcase prefix.
         *
         * TC001_Baseline_valid_request
         *
         * ->
         *
         * Baseline_valid_request
         */

        String semanticName =
                removeTechnicalPrefix(
                        name
                );


        /*
         * =====================================================
         * GENERAL TESTS
         * =====================================================
         */

        if ("baseline_valid_request".equals(
                semanticName
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    null,
                    ConstraintType.NONE,
                    IntentType.BASELINE
            );
        }


        if ("full_request".equals(
                semanticName
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    null,
                    ConstraintType.NONE,
                    IntentType.FULL_REQUEST
            );
        }


        /*
         * =====================================================
         * OPTIONAL PARAMETER
         * =====================================================
         */

        String prefix =
                "optional_parameter_";


        if (semanticName.startsWith(
                prefix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    semanticName.substring(
                            prefix.length()
                    ),
                    ConstraintType.NONE,
                    IntentType.OPTIONAL_PARAMETER
            );
        }


        /*
         * =====================================================
         * ENUM
         * =====================================================
         */

        String suffix =
                "_valid_enum_value";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.ENUM,
                    IntentType.ENUM_VALID
            );
        }


        suffix =
                "_invalid_enum_value";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.ENUM,
                    IntentType.INVALID
            );
        }


        /*
         * =====================================================
         * MINIMUM / MAXIMUM
         * =====================================================
         */

        suffix =
                "_below_minimum";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MINIMUM,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_above_maximum";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAXIMUM,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_minimum";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MINIMUM,
                    IntentType.BOUNDARY_VALID
            );
        }


        suffix =
                "_maximum";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAXIMUM,
                    IntentType.BOUNDARY_VALID
            );
        }


        /*
         * =====================================================
         * STRING LENGTH
         * =====================================================
         */

        suffix =
                "_below_minimum_string_length";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MIN_LENGTH,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_above_maximum_string_length";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAX_LENGTH,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_minimum_string_length";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MIN_LENGTH,
                    IntentType.BOUNDARY_VALID
            );
        }


        suffix =
                "_maximum_string_length";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAX_LENGTH,
                    IntentType.BOUNDARY_VALID
            );
        }


        /*
         * =====================================================
         * ARRAY SIZE
         * =====================================================
         */

        suffix =
                "_below_minimum_array_size";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MIN_ITEMS,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_above_maximum_array_size";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAX_ITEMS,
                    IntentType.BOUNDARY_INVALID
            );
        }


        suffix =
                "_minimum_array_size";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MIN_ITEMS,
                    IntentType.BOUNDARY_VALID
            );
        }


        suffix =
                "_maximum_array_size";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.MAX_ITEMS,
                    IntentType.BOUNDARY_VALID
            );
        }


        /*
         * =====================================================
         * PATTERN
         * =====================================================
         */

        suffix =
                "_value_matches_pattern";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.PATTERN,
                    IntentType.BOUNDARY_VALID
            );
        }


        suffix =
                "_value_violates_pattern";


        if (semanticName.endsWith(
                suffix
        )) {

            return new AgateTestCaseIntent(
                    testcaseName,
                    removeSuffix(
                            semanticName,
                            suffix
                    ),
                    ConstraintType.PATTERN,
                    IntentType.INVALID
            );
        }


        return new AgateTestCaseIntent(
                testcaseName,
                null,
                ConstraintType.UNKNOWN,
                IntentType.OTHER
        );
    }




    public ConstraintType resolveConstraint(
            String property) {

        if (property == null) {

            return ConstraintType.NONE;
        }


        return switch (property) {

            case "required" ->
                    ConstraintType.REQUIRED;

            case "minimum" ->
                    ConstraintType.MINIMUM;

            case "maximum" ->
                    ConstraintType.MAXIMUM;

            case "minLength" ->
                    ConstraintType.MIN_LENGTH;

            case "maxLength" ->
                    ConstraintType.MAX_LENGTH;

            case "minItems" ->
                    ConstraintType.MIN_ITEMS;

            case "maxItems" ->
                    ConstraintType.MAX_ITEMS;

            case "enum" ->
                    ConstraintType.ENUM;

            case "pattern" ->
                    ConstraintType.PATTERN;

            case "type" ->
                    ConstraintType.TYPE;

            case "format" ->
                    ConstraintType.FORMAT;

            default ->
                    ConstraintType.UNKNOWN;
        };
    }




    private String removeTechnicalPrefix(
            String name) {

        return name.replaceFirst(
                "^tc\\d+_",
                ""
        );
    }




    private String removeSuffix(
            String value,
            String suffix) {

        return value.substring(
                0,
                value.length()
                        - suffix.length()
        );
    }




    private String normalize(
            String value) {

        String result =
                value
                        .trim()
                        .toLowerCase()
                        .replace(
                                '.',
                                '_'
                        )
                        .replace(
                                '-',
                                '_'
                        );


        while (result.contains(
                "__"
        )) {

            result =
                    result.replace(
                            "__",
                            "_"
                    );
        }


        return result;
    }
}