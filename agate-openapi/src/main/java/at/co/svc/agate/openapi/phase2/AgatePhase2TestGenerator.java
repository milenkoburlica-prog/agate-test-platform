package at.co.svc.agate.openapi.phase2;

import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;

import at.co.svc.agate.openapi.phase2.model.AgateExpectedOutcome;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;
import at.co.svc.agate.openapi.phase2.model.AgatePhase2Options;
import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AgatePhase2TestGenerator {


    private final AgateDeterministicValueFactory valueFactory =
            new AgateDeterministicValueFactory();


    private final AgateResponseStatusSelector statusSelector =
            new AgateResponseStatusSelector();




    public AgateGeneratedTestPlan generate(
            AgateOperationModel operation) {

        return generate(
                operation,
                new AgatePhase2Options()
        );
    }




    public AgateGeneratedTestPlan generate(
            AgateOperationModel operation,
            AgatePhase2Options options) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (operation.getRequest() == null) {

            throw new IllegalArgumentException(
                    "Operation request must not be null"
            );
        }


        AgatePhase2Options effectiveOptions =
                options != null
                        ? options
                        : new AgatePhase2Options();


        AgateGeneratedTestPlan plan =
                new AgateGeneratedTestPlan();


        plan.setOperationIdentity(
                operation.getIdentity()
        );


        String successStatus =
                statusSelector.selectSuccessStatus(
                        operation
                );


        /*
         * -------------------------------------------------
         * MINIMAL BASELINE
         * -------------------------------------------------
         */

        AgateRequestValues baseline =
                createBaselineValues(
                        operation.getRequest()
                );


        plan.addTestCase(
                createCase(
                        "Baseline valid request",
                        AgateTestCaseType.BASELINE_VALID,
                        AgateExpectedOutcome.ACCEPT,
                        "All required request data contains deterministic valid values",
                        successStatus,
                        baseline
                )
        );


        /*
         * -------------------------------------------------
         * FULL REQUEST
         * -------------------------------------------------
         *
         * Include every available parameter and every
         * request-body property.
         *
         * Add it only when it differs from the minimal
         * baseline.
         */

        AgateRequestValues fullRequest =
                createFullRequestValues(
                        operation.getRequest()
                );


        if (!sameRequest(
                baseline,
                fullRequest
        )) {

            plan.addTestCase(
                    createCase(
                            "Full request",
                            AgateTestCaseType.FULL_REQUEST,
                            AgateExpectedOutcome.ACCEPT,
                            "Send full request with all available fields",
                            successStatus,
                            fullRequest
                    )
            );
        }


        generateParameterCases(
                operation,
                plan,
                baseline,
                successStatus,
                effectiveOptions
        );


        generateBodyCases(
                operation,
                plan,
                baseline,
                successStatus,
                effectiveOptions
        );


        renumber(
                operation,
                plan
        );


        return plan;
    }




    private AgateRequestValues createBaselineValues(
            AgateRequestModel request) {

        AgateRequestValues result =
                new AgateRequestValues();


        addRequiredParameters(
                request.getPathParameters(),
                result.getPath()
        );


        addRequiredParameters(
                request.getQueryParameters(),
                result.getQuery()
        );


        addRequiredParameters(
                request.getHeaderParameters(),
                result.getHeaders()
        );


        addRequiredParameters(
                request.getCookieParameters(),
                result.getCookies()
        );


        AgateRequestBodyModel body =
                request.getBody();


        if (body != null &&
                body.isRequired()) {

            AgateRequestContentModel content =
                    selectRequestContent(
                            body
                    );


            if (content != null) {

                result.setBodyMediaType(
                        content.getMediaType()
                );


                result.setBody(
                        valueFactory.createValidValue(
                                content.getSchema()
                        )
                );
            }
        }


        return result;
    }




    private AgateRequestValues createFullRequestValues(
            AgateRequestModel request) {

        AgateRequestValues result =
                new AgateRequestValues();


        addAllParameters(
                request.getPathParameters(),
                result.getPath()
        );


        addAllParameters(
                request.getQueryParameters(),
                result.getQuery()
        );


        addAllParameters(
                request.getHeaderParameters(),
                result.getHeaders()
        );


        addAllParameters(
                request.getCookieParameters(),
                result.getCookies()
        );


        AgateRequestBodyModel body =
                request.getBody();


        if (body != null) {

            AgateRequestContentModel content =
                    selectRequestContent(
                            body
                    );


            if (content != null) {

                result.setBodyMediaType(
                        content.getMediaType()
                );


                result.setBody(
                        valueFactory.createFullValue(
                                content.getSchema()
                        )
                );
            }
        }


        return result;
    }




    private void addRequiredParameters(
            List<AgateRequestParameterModel> parameters,
            Map<String, Object> target) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            if (!parameter.isRequired()) {

                continue;
            }


            target.put(
                    parameter.getName(),
                    valueFactory.createValidValue(
                            parameter.getSchema()
                    )
            );
        }
    }




    private void addAllParameters(
            List<AgateRequestParameterModel> parameters,
            Map<String, Object> target) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            target.put(
                    parameter.getName(),
                    valueFactory.createFullValue(
                            parameter.getSchema()
                    )
            );
        }
    }




    private boolean sameRequest(
            AgateRequestValues first,
            AgateRequestValues second) {

        return Objects.equals(
                first.getPath(),
                second.getPath()
        )
                &&
                Objects.equals(
                        first.getQuery(),
                        second.getQuery()
                )
                &&
                Objects.equals(
                        first.getHeaders(),
                        second.getHeaders()
                )
                &&
                Objects.equals(
                        first.getCookies(),
                        second.getCookies()
                )
                &&
                Objects.equals(
                        first.getBodyMediaType(),
                        second.getBodyMediaType()
                )
                &&
                Objects.equals(
                        first.getBody(),
                        second.getBody()
                );
    }




    private void generateParameterCases(
            AgateOperationModel operation,
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options) {

        AgateRequestModel request =
                operation.getRequest();


        generateParameterGroup(
                plan,
                baseline,
                successStatus,
                options,
                request.getPathParameters()
        );


        generateParameterGroup(
                plan,
                baseline,
                successStatus,
                options,
                request.getQueryParameters()
        );


        generateParameterGroup(
                plan,
                baseline,
                successStatus,
                options,
                request.getHeaderParameters()
        );


        generateParameterGroup(
                plan,
                baseline,
                successStatus,
                options,
                request.getCookieParameters()
        );
    }




    private void generateParameterGroup(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            List<AgateRequestParameterModel> parameters) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {


            if (!parameter.isRequired() &&
                    options.isGenerateOptionalParameterTests()) {

                AgateRequestValues values =
                        copy(
                                baseline
                        );


                putParameterValue(
                        values,
                        parameter,
                        valueFactory.createValidValue(
                                parameter.getSchema()
                        )
                );


                plan.addTestCase(
                        createCase(
                                "Optional parameter "
                                        + parameter.getName(),
                                AgateTestCaseType.OPTIONAL_PARAMETER,
                                AgateExpectedOutcome.ACCEPT,
                                "Optional parameter is present",
                                successStatus,
                                values
                        )
                );
            }


            if (parameter.isRequired() &&
                    options.isGenerateNegativeTests()) {

                AgateRequestValues values =
                        copy(
                                baseline
                        );


                removeParameter(
                        values,
                        parameter
                );


                plan.addTestCase(
                        createCase(
                                "Missing required parameter "
                                        + parameter.getName(),
                                AgateTestCaseType.MISSING_REQUIRED_PARAMETER,
                                AgateExpectedOutcome.REJECT,
                                "Required "
                                        + parameter.getLocation()
                                        + " parameter is omitted",
                                null,
                                values
                        )
                );
            }


            generateSchemaCasesForParameter(
                    plan,
                    baseline,
                    successStatus,
                    options,
                    parameter
            );
        }
    }




    private void generateSchemaCasesForParameter(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter) {

        AgateSchema schema =
                parameter.getSchema();


        if (schema == null) {

            return;
        }


        if (options.isGenerateEnumTests()) {

            generateEnumCases(
                    plan,
                    baseline,
                    successStatus,
                    options,
                    parameter,
                    schema
            );
        }


        if (!options.isGenerateBoundaryTests()) {

            return;
        }


        generateNumericBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateExclusiveNumericCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateMultipleOfCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateStringBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generatePatternCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateFormatCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateArrayBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateUniqueItemsCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateArrayItemBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateArrayItemPatternCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );


        generateArrayItemFormatCases(
                plan,
                baseline,
                successStatus,
                options,
                parameter,
                schema
        );
    }




    private void generateEnumCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (schema.getEnumValues() == null ||
                schema.getEnumValues().isEmpty()) {

            return;
        }


        int count =
                0;


        for (Object value :
                schema.getEnumValues()) {

            if (count >=
                    options.getMaximumEnumCases()) {

                break;
            }


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    value,
                    AgateTestCaseType.ENUM_VALUE,
                    AgateExpectedOutcome.ACCEPT,
                    "valid enum value"
            );


            count++;
        }


        if (options.isGenerateNegativeTests()) {

            addParameterCase(
                    plan,
                    baseline,
                    null,
                    parameter,
                    valueFactory.createInvalidEnumValue(
                            schema
                    ),
                    AgateTestCaseType.INVALID_ENUM,
                    AgateExpectedOutcome.REJECT,
                    "invalid enum value"
            );
        }
    }




    private void generateNumericBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (schema.getMinimum() != null &&
                !Boolean.TRUE.equals(
                        schema.getExclusiveMinimum()
                )) {

            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createMinimumValue(
                            schema
                    ),
                    AgateTestCaseType.MINIMUM,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createBelowMinimumValue(
                                schema
                        ),
                        AgateTestCaseType.BELOW_MINIMUM,
                        AgateExpectedOutcome.REJECT,
                        "below minimum"
                );
            }
        }


        if (schema.getMaximum() != null &&
                !Boolean.TRUE.equals(
                        schema.getExclusiveMaximum()
                )) {

            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createMaximumValue(
                            schema
                    ),
                    AgateTestCaseType.MAXIMUM,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createAboveMaximumValue(
                                schema
                        ),
                        AgateTestCaseType.ABOVE_MAXIMUM,
                        AgateExpectedOutcome.REJECT,
                        "above maximum"
                );
            }
        }
    }




    private void generateExclusiveNumericCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (Boolean.TRUE.equals(
                schema.getExclusiveMinimum()
        )) {

            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createExclusiveMinimumValidValue(
                            schema
                    ),
                    AgateTestCaseType.EXCLUSIVE_MINIMUM_VALID,
                    AgateExpectedOutcome.ACCEPT,
                    "value above exclusive minimum"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createExclusiveMinimumInvalidValue(
                                schema
                        ),
                        AgateTestCaseType.EXCLUSIVE_MINIMUM_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "value equals exclusive minimum"
                );
            }
        }


        if (Boolean.TRUE.equals(
                schema.getExclusiveMaximum()
        )) {

            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createExclusiveMaximumValidValue(
                            schema
                    ),
                    AgateTestCaseType.EXCLUSIVE_MAXIMUM_VALID,
                    AgateExpectedOutcome.ACCEPT,
                    "value below exclusive maximum"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createExclusiveMaximumInvalidValue(
                                schema
                        ),
                        AgateTestCaseType.EXCLUSIVE_MAXIMUM_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "value equals exclusive maximum"
                );
            }
        }
    }




    private void generateMultipleOfCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (schema.getMultipleOf() == null) {

            return;
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                valueFactory.createMultipleOfValidValue(
                        schema
                ),
                AgateTestCaseType.MULTIPLE_OF_VALID,
                AgateExpectedOutcome.ACCEPT,
                "value satisfies multipleOf"
        );


        if (options.isGenerateNegativeTests()) {

            addParameterCase(
                    plan,
                    baseline,
                    null,
                    parameter,
                    valueFactory.createMultipleOfInvalidValue(
                            schema
                    ),
                    AgateTestCaseType.MULTIPLE_OF_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "value violates multipleOf"
            );
        }
    }




    private void generateStringBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"string".equals(
                schema.getType()
        )) {

            return;
        }


        if (schema.getMinLength() != null) {

            int min =
                    schema.getMinLength();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createValidStringWithLength(
                            schema,
                            min
                    ),
                    AgateTestCaseType.MIN_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum string length"
            );


            if (options.isGenerateNegativeTests() &&
                    min > 0) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createValidStringWithLength(
                                schema,
                                min - 1
                        ),
                        AgateTestCaseType.BELOW_MIN_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "below minimum string length"
                );
            }
        }


        if (schema.getMaxLength() != null) {

            int max =
                    schema.getMaxLength();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createValidStringWithLength(
                            schema,
                            max
                    ),
                    AgateTestCaseType.MAX_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum string length"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createValidStringWithLength(
                                schema,
                                max + 1
                        ),
                        AgateTestCaseType.ABOVE_MAX_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "above maximum string length"
                );
            }
        }
    }




    private void generatePatternCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        String valid =
                valueFactory.createPatternValidValue(
                        schema
                );


        if (valid == null) {

            return;
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                valid,
                AgateTestCaseType.PATTERN_VALID,
                AgateExpectedOutcome.ACCEPT,
                "value matches pattern"
        );


        if (options.isGenerateNegativeTests()) {

            addParameterCase(
                    plan,
                    baseline,
                    null,
                    parameter,
                    valueFactory.createPatternInvalidValue(
                            schema
                    ),
                    AgateTestCaseType.PATTERN_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "value violates pattern"
            );
        }
    }




    private void generateFormatCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        String valid =
                valueFactory.createFormatValidValue(
                        schema
                );


        if (valid == null) {

            return;
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                valid,
                AgateTestCaseType.FORMAT_VALID,
                AgateExpectedOutcome.ACCEPT,
                "valid "
                        + schema.getFormat()
                        + " format"
        );


        if (options.isGenerateNegativeTests()) {

            addParameterCase(
                    plan,
                    baseline,
                    null,
                    parameter,
                    valueFactory.createFormatInvalidValue(
                            schema
                    ),
                    AgateTestCaseType.FORMAT_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "invalid "
                            + schema.getFormat()
                            + " format"
            );
        }
    }




    private void generateArrayBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        )) {

            return;
        }


        if (schema.getMinItems() != null) {

            int min =
                    schema.getMinItems();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createArrayWithSize(
                            schema,
                            min
                    ),
                    AgateTestCaseType.MIN_ITEMS,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum array size"
            );


            if (options.isGenerateNegativeTests() &&
                    min > 0) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createArrayWithSize(
                                schema,
                                min - 1
                        ),
                        AgateTestCaseType.BELOW_MIN_ITEMS,
                        AgateExpectedOutcome.REJECT,
                        "below minimum array size"
                );
            }
        }


        if (schema.getMaxItems() != null) {

            int max =
                    schema.getMaxItems();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    valueFactory.createArrayWithSize(
                            schema,
                            max
                    ),
                    AgateTestCaseType.MAX_ITEMS,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum array size"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        valueFactory.createArrayWithSize(
                                schema,
                                max + 1
                        ),
                        AgateTestCaseType.ABOVE_MAX_ITEMS,
                        AgateExpectedOutcome.REJECT,
                        "above maximum array size"
                );
            }
        }
    }




    private void generateUniqueItemsCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        ) ||
                !Boolean.TRUE.equals(
                        schema.getUniqueItems()
                )) {

            return;
        }


        int size =
                2;


        if (schema.getMinItems() != null &&
                schema.getMinItems() > size) {

            size =
                    schema.getMinItems();
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                valueFactory.createUniqueArray(
                        schema,
                        size
                ),
                AgateTestCaseType.UNIQUE_ITEMS_VALID,
                AgateExpectedOutcome.ACCEPT,
                "array contains unique items"
        );


        if (options.isGenerateNegativeTests()) {

            addParameterCase(
                    plan,
                    baseline,
                    null,
                    parameter,
                    valueFactory.createNonUniqueArray(
                            schema
                    ),
                    AgateTestCaseType.UNIQUE_ITEMS_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "array contains duplicate items"
            );
        }
    }




    private void generateArrayItemBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        ) ||
                schema.getItems() == null) {

            return;
        }


        AgateSchema itemSchema =
                schema.getItems();


        if (itemSchema.getMinLength() != null) {

            int min =
                    itemSchema.getMinLength();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    List.of(
                            valueFactory.createValidStringWithLength(
                                    itemSchema,
                                    min
                            )
                    ),
                    AgateTestCaseType.ARRAY_ITEM_MIN_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "array item minimum string length"
            );


            if (options.isGenerateNegativeTests() &&
                    min > 0) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        List.of(
                                valueFactory.createValidStringWithLength(
                                        itemSchema,
                                        min - 1
                                )
                        ),
                        AgateTestCaseType.ARRAY_ITEM_BELOW_MIN_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "array item below minimum string length"
                );
            }
        }


        if (itemSchema.getMaxLength() != null) {

            int max =
                    itemSchema.getMaxLength();


            addParameterCase(
                    plan,
                    baseline,
                    successStatus,
                    parameter,
                    List.of(
                            valueFactory.createValidStringWithLength(
                                    itemSchema,
                                    max
                            )
                    ),
                    AgateTestCaseType.ARRAY_ITEM_MAX_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "array item maximum string length"
            );


            if (options.isGenerateNegativeTests()) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        List.of(
                                valueFactory.createValidStringWithLength(
                                        itemSchema,
                                        max + 1
                                )
                        ),
                        AgateTestCaseType.ARRAY_ITEM_ABOVE_MAX_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "array item above maximum string length"
                );
            }
        }
    }




    private void generateArrayItemPatternCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        ) ||
                schema.getItems() == null) {

            return;
        }


        AgateSchema itemSchema =
                schema.getItems();


        String valid =
                valueFactory.createPatternValidValue(
                        itemSchema
                );


        if (valid == null) {

            return;
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                List.of(
                        valid
                ),
                AgateTestCaseType.ARRAY_ITEM_PATTERN_VALID,
                AgateExpectedOutcome.ACCEPT,
                "array item matches pattern"
        );


        if (options.isGenerateNegativeTests()) {

            String invalid =
                    valueFactory.createPatternInvalidValue(
                            itemSchema
                    );


            if (invalid != null) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        List.of(
                                invalid
                        ),
                        AgateTestCaseType.ARRAY_ITEM_PATTERN_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "array item violates pattern"
                );
            }
        }
    }




    private void generateArrayItemFormatCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateRequestParameterModel parameter,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        ) ||
                schema.getItems() == null) {

            return;
        }


        AgateSchema itemSchema =
                schema.getItems();


        String valid =
                valueFactory.createFormatValidValue(
                        itemSchema
                );


        if (valid == null) {

            return;
        }


        addParameterCase(
                plan,
                baseline,
                successStatus,
                parameter,
                List.of(
                        valid
                ),
                AgateTestCaseType.ARRAY_ITEM_FORMAT_VALID,
                AgateExpectedOutcome.ACCEPT,
                "array item has valid format"
        );


        if (options.isGenerateNegativeTests()) {

            String invalid =
                    valueFactory.createFormatInvalidValue(
                            itemSchema
                    );


            if (invalid != null) {

                addParameterCase(
                        plan,
                        baseline,
                        null,
                        parameter,
                        List.of(
                                invalid
                        ),
                        AgateTestCaseType.ARRAY_ITEM_FORMAT_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "array item has invalid format"
                );
            }
        }
    }




    private void addParameterCase(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String statusCode,
            AgateRequestParameterModel parameter,
            Object value,
            AgateTestCaseType type,
            AgateExpectedOutcome outcome,
            String reason) {

        if (value == null) {

            return;
        }


        AgateRequestValues values =
                copy(
                        baseline
                );


        putParameterValue(
                values,
                parameter,
                value
        );


        plan.addTestCase(
                createCase(
                        parameter.getName()
                                + " "
                                + reason,
                        type,
                        outcome,
                        reason,
                        statusCode,
                        values
                )
        );
    }




    private void generateBodyCases(
            AgateOperationModel operation,
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options) {

        AgateRequestBodyModel body =
                operation
                        .getRequest()
                        .getBody();


        if (body == null) {

            return;
        }


        AgateRequestContentModel content =
                selectRequestContent(
                        body
                );


        if (content == null ||
                content.getSchema() == null) {

            return;
        }


        AgateSchema rootSchema =
                content.getSchema();


        if (options.isGenerateNegativeTests() &&
                "object".equals(
                        rootSchema.getType()
                )) {

            generateMissingRequiredBodyProperties(
                    plan,
                    baseline,
                    rootSchema
            );
        }


        generateBodySchemaCases(
                plan,
                baseline,
                successStatus,
                options,
                rootSchema,
                ""
        );
    }




    private void generateMissingRequiredBodyProperties(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            AgateSchema schema) {

        if (!(baseline.getBody()
                instanceof Map<?, ?>)) {

            return;
        }


        for (String requiredProperty :
                schema.getRequired()) {

            AgateRequestValues values =
                    copy(
                            baseline
                    );


            removeBodyValue(
                    values,
                    requiredProperty
            );


            plan.addTestCase(
                    createCase(
                            "Missing required body property "
                                    + requiredProperty,
                            AgateTestCaseType.MISSING_REQUIRED_BODY_PROPERTY,
                            AgateExpectedOutcome.REJECT,
                            "Required request body property is omitted",
                            null,
                            values
                    )
            );
        }
    }




    private void generateBodySchemaCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            AgateSchema schema,
            String path) {

        if (schema == null) {

            return;
        }


        if ("object".equals(
                schema.getType()
        )) {

            for (Map.Entry<String, AgateSchema> entry :
                    schema
                            .getProperties()
                            .entrySet()) {

                String propertyPath =
                        path.isEmpty()
                                ? entry.getKey()
                                : path
                                + "."
                                + entry.getKey();


                generateBodyPropertyCases(
                        plan,
                        baseline,
                        successStatus,
                        options,
                        propertyPath,
                        entry.getValue()
                );


                generateBodySchemaCases(
                        plan,
                        baseline,
                        successStatus,
                        options,
                        entry.getValue(),
                        propertyPath
                );
            }
        }
    }




    private void generateBodyPropertyCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (schema == null) {

            return;
        }


        if (options.isGenerateEnumTests()) {

            generateBodyEnumCases(
                    plan,
                    baseline,
                    successStatus,
                    options,
                    propertyPath,
                    schema
            );
        }


        if (!options.isGenerateBoundaryTests()) {

            return;
        }


        generateBodyNumericBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyExclusiveNumericCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyMultipleOfCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyStringBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyPatternCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyFormatCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyArrayBoundaryCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );


        generateBodyUniqueItemsCases(
                plan,
                baseline,
                successStatus,
                options,
                propertyPath,
                schema
        );
    }




    private void generateBodyEnumCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (schema.getEnumValues() == null ||
                schema.getEnumValues().isEmpty()) {

            return;
        }


        int count =
                0;


        for (Object value :
                schema.getEnumValues()) {

            if (count >=
                    options.getMaximumEnumCases()) {

                break;
            }


            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    value,
                    AgateTestCaseType.BODY_ENUM_VALUE,
                    AgateExpectedOutcome.ACCEPT,
                    "valid enum value"
            );


            count++;
        }


        if (options.isGenerateNegativeTests()) {

            addBodyCase(
                    plan,
                    baseline,
                    null,
                    propertyPath,
                    valueFactory.createInvalidEnumValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_INVALID_ENUM,
                    AgateExpectedOutcome.REJECT,
                    "invalid enum value"
            );
        }
    }




    private void generateBodyNumericBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (schema.getMinimum() != null &&
                !Boolean.TRUE.equals(
                        schema.getExclusiveMinimum()
                )) {

            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createMinimumValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_MINIMUM,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createBelowMinimumValue(
                                schema
                        ),
                        AgateTestCaseType.BODY_BELOW_MINIMUM,
                        AgateExpectedOutcome.REJECT,
                        "below minimum"
                );
            }
        }


        if (schema.getMaximum() != null &&
                !Boolean.TRUE.equals(
                        schema.getExclusiveMaximum()
                )) {

            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createMaximumValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_MAXIMUM,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createAboveMaximumValue(
                                schema
                        ),
                        AgateTestCaseType.BODY_ABOVE_MAXIMUM,
                        AgateExpectedOutcome.REJECT,
                        "above maximum"
                );
            }
        }
    }




    private void generateBodyExclusiveNumericCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (Boolean.TRUE.equals(
                schema.getExclusiveMinimum()
        )) {

            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createExclusiveMinimumValidValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_EXCLUSIVE_MINIMUM_VALID,
                    AgateExpectedOutcome.ACCEPT,
                    "value above exclusive minimum"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createExclusiveMinimumInvalidValue(
                                schema
                        ),
                        AgateTestCaseType.BODY_EXCLUSIVE_MINIMUM_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "value equals exclusive minimum"
                );
            }
        }


        if (Boolean.TRUE.equals(
                schema.getExclusiveMaximum()
        )) {

            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createExclusiveMaximumValidValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_EXCLUSIVE_MAXIMUM_VALID,
                    AgateExpectedOutcome.ACCEPT,
                    "value below exclusive maximum"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createExclusiveMaximumInvalidValue(
                                schema
                        ),
                        AgateTestCaseType.BODY_EXCLUSIVE_MAXIMUM_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "value equals exclusive maximum"
                );
            }
        }
    }




    private void generateBodyMultipleOfCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (schema.getMultipleOf() == null) {

            return;
        }


        addBodyCase(
                plan,
                baseline,
                successStatus,
                propertyPath,
                valueFactory.createMultipleOfValidValue(
                        schema
                ),
                AgateTestCaseType.BODY_MULTIPLE_OF_VALID,
                AgateExpectedOutcome.ACCEPT,
                "value satisfies multipleOf"
        );


        if (options.isGenerateNegativeTests()) {

            addBodyCase(
                    plan,
                    baseline,
                    null,
                    propertyPath,
                    valueFactory.createMultipleOfInvalidValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_MULTIPLE_OF_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "value violates multipleOf"
            );
        }
    }




    private void generateBodyStringBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (!"string".equals(
                schema.getType()
        )) {

            return;
        }


        if (schema.getMinLength() != null) {

            int min =
                    schema.getMinLength();


            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createValidStringWithLength(
                            schema,
                            min
                    ),
                    AgateTestCaseType.BODY_MIN_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum string length"
            );


            if (options.isGenerateNegativeTests() &&
                    min > 0) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createValidStringWithLength(
                                schema,
                                min - 1
                        ),
                        AgateTestCaseType.BODY_BELOW_MIN_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "below minimum string length"
                );
            }
        }


        if (schema.getMaxLength() != null) {

            int max =
                    schema.getMaxLength();


            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createValidStringWithLength(
                            schema,
                            max
                    ),
                    AgateTestCaseType.BODY_MAX_LENGTH,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum string length"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createValidStringWithLength(
                                schema,
                                max + 1
                        ),
                        AgateTestCaseType.BODY_ABOVE_MAX_LENGTH,
                        AgateExpectedOutcome.REJECT,
                        "above maximum string length"
                );
            }
        }
    }




    private void generateBodyPatternCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        String valid =
                valueFactory.createPatternValidValue(
                        schema
                );


        if (valid == null) {

            return;
        }


        addBodyCase(
                plan,
                baseline,
                successStatus,
                propertyPath,
                valid,
                AgateTestCaseType.BODY_PATTERN_VALID,
                AgateExpectedOutcome.ACCEPT,
                "value matches pattern"
        );


        if (options.isGenerateNegativeTests()) {

            addBodyCase(
                    plan,
                    baseline,
                    null,
                    propertyPath,
                    valueFactory.createPatternInvalidValue(
                            schema
                    ),
                    AgateTestCaseType.BODY_PATTERN_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "value violates pattern"
            );
        }
    }




    private void generateBodyFormatCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        String valid =
                valueFactory.createFormatValidValue(
                        schema
                );


        if (valid == null) {

            return;
        }


        addBodyCase(
                plan,
                baseline,
                successStatus,
                propertyPath,
                valid,
                AgateTestCaseType.BODY_FORMAT_VALID,
                AgateExpectedOutcome.ACCEPT,
                "valid "
                        + schema.getFormat()
                        + " format"
        );


        if (options.isGenerateNegativeTests()) {

            String invalid =
                    valueFactory.createFormatInvalidValue(
                            schema
                    );


            if (invalid != null) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        invalid,
                        AgateTestCaseType.BODY_FORMAT_INVALID,
                        AgateExpectedOutcome.REJECT,
                        "invalid "
                                + schema.getFormat()
                                + " format"
                );
            }
        }
    }




    private void generateBodyArrayBoundaryCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        )) {

            return;
        }


        if (schema.getMinItems() != null) {

            int min =
                    schema.getMinItems();


            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createArrayWithSize(
                            schema,
                            min
                    ),
                    AgateTestCaseType.BODY_MIN_ITEMS,
                    AgateExpectedOutcome.ACCEPT,
                    "minimum array size"
            );


            if (options.isGenerateNegativeTests() &&
                    min > 0) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createArrayWithSize(
                                schema,
                                min - 1
                        ),
                        AgateTestCaseType.BODY_BELOW_MIN_ITEMS,
                        AgateExpectedOutcome.REJECT,
                        "below minimum array size"
                );
            }
        }


        if (schema.getMaxItems() != null) {

            int max =
                    schema.getMaxItems();


            addBodyCase(
                    plan,
                    baseline,
                    successStatus,
                    propertyPath,
                    valueFactory.createArrayWithSize(
                            schema,
                            max
                    ),
                    AgateTestCaseType.BODY_MAX_ITEMS,
                    AgateExpectedOutcome.ACCEPT,
                    "maximum array size"
            );


            if (options.isGenerateNegativeTests()) {

                addBodyCase(
                        plan,
                        baseline,
                        null,
                        propertyPath,
                        valueFactory.createArrayWithSize(
                                schema,
                                max + 1
                        ),
                        AgateTestCaseType.BODY_ABOVE_MAX_ITEMS,
                        AgateExpectedOutcome.REJECT,
                        "above maximum array size"
                );
            }
        }
    }




    private void generateBodyUniqueItemsCases(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String successStatus,
            AgatePhase2Options options,
            String propertyPath,
            AgateSchema schema) {

        if (!"array".equals(
                schema.getType()
        ) ||
                !Boolean.TRUE.equals(
                        schema.getUniqueItems()
                )) {

            return;
        }


        int size =
                2;


        if (schema.getMinItems() != null &&
                schema.getMinItems() > size) {

            size =
                    schema.getMinItems();
        }


        addBodyCase(
                plan,
                baseline,
                successStatus,
                propertyPath,
                valueFactory.createUniqueArray(
                        schema,
                        size
                ),
                AgateTestCaseType.BODY_UNIQUE_ITEMS_VALID,
                AgateExpectedOutcome.ACCEPT,
                "array contains unique items"
        );


        if (options.isGenerateNegativeTests()) {

            addBodyCase(
                    plan,
                    baseline,
                    null,
                    propertyPath,
                    valueFactory.createNonUniqueArray(
                            schema
                    ),
                    AgateTestCaseType.BODY_UNIQUE_ITEMS_INVALID,
                    AgateExpectedOutcome.REJECT,
                    "array contains duplicate items"
            );
        }
    }




    private void addBodyCase(
            AgateGeneratedTestPlan plan,
            AgateRequestValues baseline,
            String statusCode,
            String propertyPath,
            Object value,
            AgateTestCaseType type,
            AgateExpectedOutcome outcome,
            String reason) {

        if (value == null) {

            return;
        }


        AgateRequestValues values =
                copy(
                        baseline
                );


        setBodyValue(
                values,
                propertyPath,
                value
        );


        plan.addTestCase(
                createCase(
                        propertyPath
                                + " "
                                + reason,
                        type,
                        outcome,
                        reason,
                        statusCode,
                        values
                )
        );
    }




    private AgateRequestContentModel selectRequestContent(
            AgateRequestBodyModel body) {

        if (body == null ||
                body.getContents() == null ||
                body.getContents().isEmpty()) {

            return null;
        }


        return body
                .getContents()
                .stream()
                .filter(content ->
                        "application/json".equals(
                                content.getMediaType()
                        )
                )
                .findFirst()
                .orElse(
                        body
                                .getContents()
                                .get(0)
                );
    }




    private void putParameterValue(
            AgateRequestValues values,
            AgateRequestParameterModel parameter,
            Object value) {

        switch (
                parameter.getLocation()
        ) {

            case "path" ->
                    values.putPath(
                            parameter.getName(),
                            value
                    );

            case "query" ->
                    values.putQuery(
                            parameter.getName(),
                            value
                    );

            case "header" ->
                    values.putHeader(
                            parameter.getName(),
                            value
                    );

            case "cookie" ->
                    values.putCookie(
                            parameter.getName(),
                            value
                    );

            default -> {
            }
        }
    }




    private void removeParameter(
            AgateRequestValues values,
            AgateRequestParameterModel parameter) {

        switch (
                parameter.getLocation()
        ) {

            case "path" ->
                    values
                            .getPath()
                            .remove(
                                    parameter.getName()
                            );

            case "query" ->
                    values
                            .getQuery()
                            .remove(
                                    parameter.getName()
                            );

            case "header" ->
                    values
                            .getHeaders()
                            .remove(
                                    parameter.getName()
                            );

            case "cookie" ->
                    values
                            .getCookies()
                            .remove(
                                    parameter.getName()
                            );

            default -> {
            }
        }
    }




    private void setBodyValue(
            AgateRequestValues values,
            String path,
            Object value) {

        if (!(values.getBody()
                instanceof Map<?, ?> rawMap)) {

            return;
        }


        @SuppressWarnings("unchecked")
        Map<String, Object> body =
                (Map<String, Object>) rawMap;


        String[] parts =
                path.split(
                        "\\."
                );


        Map<String, Object> current =
                body;


        for (int i = 0;
             i < parts.length - 1;
             i++) {

            String part =
                    parts[i];


            Object existing =
                    current.get(
                            part
                    );


            if (existing instanceof Map<?, ?> rawNested) {

                @SuppressWarnings("unchecked")
                Map<String, Object> nested =
                        (Map<String, Object>) rawNested;


                current =
                        nested;

            } else {

                Map<String, Object> nested =
                        new LinkedHashMap<>();


                current.put(
                        part,
                        nested
                );


                current =
                        nested;
            }
        }


        current.put(
                parts[
                        parts.length - 1
                        ],
                value
        );
    }




    private void removeBodyValue(
            AgateRequestValues values,
            String property) {

        if (!(values.getBody()
                instanceof Map<?, ?> rawMap)) {

            return;
        }


        @SuppressWarnings("unchecked")
        Map<String, Object> body =
                (Map<String, Object>) rawMap;


        body.remove(
                property
        );
    }




    private AgateGeneratedTestCase createCase(
            String name,
            AgateTestCaseType type,
            AgateExpectedOutcome outcome,
            String reason,
            String statusCode,
            AgateRequestValues values) {

        AgateGeneratedTestCase result =
                new AgateGeneratedTestCase();


        result.setName(
                name
        );


        result.setType(
                type
        );


        result.setExpectedOutcome(
                outcome
        );


        result.setReason(
                reason
        );


        result.setExpectedStatusCode(
                statusCode
        );


        result.setRequestValues(
                values
        );


        return result;
    }




    private void renumber(
            AgateOperationModel operation,
            AgateGeneratedTestPlan plan) {

        int sequence =
                1;


        for (AgateGeneratedTestCase testCase :
                plan.getTestCases()) {

            testCase.setId(
                    operation.getIdentity()
                            + "#TC"
                            + String.format(
                                    "%03d",
                                    sequence
                            )
            );


            sequence++;
        }
    }




    private AgateRequestValues copy(
            AgateRequestValues source) {

        AgateRequestValues result =
                new AgateRequestValues();


        result.setPath(
                deepCopyMap(
                        source.getPath()
                )
        );


        result.setQuery(
                deepCopyMap(
                        source.getQuery()
                )
        );


        result.setHeaders(
                deepCopyMap(
                        source.getHeaders()
                )
        );


        result.setCookies(
                deepCopyMap(
                        source.getCookies()
                )
        );


        result.setBodyMediaType(
                source.getBodyMediaType()
        );


        result.setBody(
                deepCopyValue(
                        source.getBody()
                )
        );


        return result;
    }




    private Map<String, Object> deepCopyMap(
            Map<String, Object> source) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (source == null) {

            return result;
        }


        source.forEach(
                (key, value) ->
                        result.put(
                                key,
                                deepCopyValue(
                                        value
                                )
                        )
        );


        return result;
    }




    private Object deepCopyValue(
            Object value) {

        if (value instanceof Map<?, ?> map) {

            Map<String, Object> result =
                    new LinkedHashMap<>();


            map.forEach(
                    (key, nestedValue) ->
                            result.put(
                                    String.valueOf(
                                            key
                                    ),
                                    deepCopyValue(
                                            nestedValue
                                    )
                            )
            );


            return result;
        }


        if (value instanceof List<?> list) {

            List<Object> result =
                    new ArrayList<>();


            for (Object item :
                    list) {

                result.add(
                        deepCopyValue(
                                item
                        )
                );
            }


            return result;
        }


        return value;
    }
}