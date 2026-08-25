package at.co.svc.agate.openapi.change.detection;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AgateOpenApiChangeDetector {


    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();




    public AgateOpenApiChangeSet detect(
            AgateOpenApiModel oldModel,
            AgateOpenApiModel newModel) {

        if (oldModel == null) {

            throw new IllegalArgumentException(
                    "Old OpenAPI model must not be null"
            );
        }


        if (newModel == null) {

            throw new IllegalArgumentException(
                    "New OpenAPI model must not be null"
            );
        }


        Map<String, AgateOperationModel> oldOperations =
                buildOperations(
                        oldModel
                );


        Map<String, AgateOperationModel> newOperations =
                buildOperations(
                        newModel
                );


        AgateOpenApiChangeSet result =
                new AgateOpenApiChangeSet();


        /*
         * Removed operations.
         */

        for (Map.Entry<String, AgateOperationModel> entry :
                oldOperations.entrySet()) {

            if (!newOperations.containsKey(
                    entry.getKey()
            )) {

                result.addChange(
                        change(
                                entry.getKey(),
                                "operation",
                                null,
                                AgateChangeType.REMOVED,
                                AgateChangeSeverity.BREAKING,
                                entry.getKey(),
                                null,
                                "API operation was removed"
                        )
                );
            }
        }


        /*
         * Added operations.
         */

        for (Map.Entry<String, AgateOperationModel> entry :
                newOperations.entrySet()) {

            if (!oldOperations.containsKey(
                    entry.getKey()
            )) {

                result.addChange(
                        change(
                                entry.getKey(),
                                "operation",
                                null,
                                AgateChangeType.ADDED,
                                AgateChangeSeverity.INFO,
                                null,
                                entry.getKey(),
                                "New API operation was added"
                        )
                );
            }
        }


        /*
         * Existing operations.
         */

        for (String identity :
                oldOperations.keySet()) {

            if (!newOperations.containsKey(
                    identity
            )) {

                continue;
            }


            compareOperation(
                    oldOperations.get(
                            identity
                    ),
                    newOperations.get(
                            identity
                    ),
                    result
            );
        }


        return result;
    }




    private Map<String, AgateOperationModel> buildOperations(
            AgateOpenApiModel model) {

        Map<String, AgateOperationModel> result =
                new LinkedHashMap<>();


        if (model.getEndpoints() == null) {

            return result;
        }


        for (AgateEndpoint endpoint :
                model.getEndpoints()) {

            AgateOperationModel operation =
                    operationBuilder.build(
                            endpoint
                    );


            result.put(
                    operation.getIdentity(),
                    operation
            );
        }


        return result;
    }




    private void compareOperation(
            AgateOperationModel oldOperation,
            AgateOperationModel newOperation,
            AgateOpenApiChangeSet result) {

        compareParameters(
                oldOperation,
                newOperation,
                result
        );


        compareRequestBody(
                oldOperation,
                newOperation,
                result
        );
    }




    private void compareParameters(
            AgateOperationModel oldOperation,
            AgateOperationModel newOperation,
            AgateOpenApiChangeSet result) {

        Map<String, ParameterContract> oldParameters =
                parameterContracts(
                        oldOperation.getRequest()
                );


        Map<String, ParameterContract> newParameters =
                parameterContracts(
                        newOperation.getRequest()
                );


        for (String path :
                oldParameters.keySet()) {

            if (!newParameters.containsKey(
                    path
            )) {

                result.addChange(
                        change(
                                oldOperation.getIdentity(),
                                path,
                                null,
                                AgateChangeType.REMOVED,
                                AgateChangeSeverity.BREAKING,
                                oldParameters.get(path),
                                null,
                                "Request parameter was removed"
                        )
                );

                continue;
            }


            compareParameter(
                    oldOperation.getIdentity(),
                    path,
                    oldParameters.get(path),
                    newParameters.get(path),
                    result
            );
        }


        for (String path :
                newParameters.keySet()) {

            if (oldParameters.containsKey(
                    path
            )) {

                continue;
            }


            ParameterContract value =
                    newParameters.get(
                            path
                    );


            result.addChange(
                    change(
                            newOperation.getIdentity(),
                            path,
                            null,
                            AgateChangeType.ADDED,
                            value.required
                                    ? AgateChangeSeverity.BREAKING
                                    : AgateChangeSeverity.REVIEW,
                            null,
                            value,
                            value.required
                                    ? "New required request parameter"
                                    : "New optional request parameter"
                    )
            );
        }
    }




    private void compareParameter(
            String operationIdentity,
            String location,
            ParameterContract oldValue,
            ParameterContract newValue,
            AgateOpenApiChangeSet result) {

        compareAttribute(
                operationIdentity,
                location,
                "required",
                oldValue.required,
                newValue.required,
                newValue.required
                        ? AgateChangeSeverity.BREAKING
                        : AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "type",
                oldValue.type,
                newValue.type,
                AgateChangeSeverity.BREAKING,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "format",
                oldValue.format,
                newValue.format,
                AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "minLength",
                oldValue.minLength,
                newValue.minLength,
                minimumConstraintSeverity(
                        oldValue.minLength,
                        newValue.minLength
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "maxLength",
                oldValue.maxLength,
                newValue.maxLength,
                maximumConstraintSeverity(
                        oldValue.maxLength,
                        newValue.maxLength
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "minimum",
                oldValue.minimum,
                newValue.minimum,
                minimumConstraintSeverity(
                        oldValue.minimum,
                        newValue.minimum
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "maximum",
                oldValue.maximum,
                newValue.maximum,
                maximumConstraintSeverity(
                        oldValue.maximum,
                        newValue.maximum
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "pattern",
                oldValue.pattern,
                newValue.pattern,
                AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "enum",
                oldValue.enumValues,
                newValue.enumValues,
                enumSeverity(
                        oldValue.enumValues,
                        newValue.enumValues
                ),
                result
        );
    }




    private void compareRequestBody(
            AgateOperationModel oldOperation,
            AgateOperationModel newOperation,
            AgateOpenApiChangeSet result) {

        AgateSchema oldSchema =
                requestBodySchema(
                        oldOperation.getRequest()
                );


        AgateSchema newSchema =
                requestBodySchema(
                        newOperation.getRequest()
                );


        if (oldSchema == null &&
                newSchema == null) {

            return;
        }


        if (oldSchema == null) {

            result.addChange(
                    change(
                            oldOperation.getIdentity(),
                            "request.body",
                            null,
                            AgateChangeType.ADDED,
                            AgateChangeSeverity.REVIEW,
                            null,
                            "body",
                            "Request body was added"
                    )
            );

            return;
        }


        if (newSchema == null) {

            result.addChange(
                    change(
                            oldOperation.getIdentity(),
                            "request.body",
                            null,
                            AgateChangeType.REMOVED,
                            AgateChangeSeverity.BREAKING,
                            "body",
                            null,
                            "Request body was removed"
                    )
            );

            return;
        }


        Map<String, FieldContract> oldFields =
                new LinkedHashMap<>();


        Map<String, FieldContract> newFields =
                new LinkedHashMap<>();


        collectFields(
                oldSchema,
                "",
                false,
                oldFields
        );


        collectFields(
                newSchema,
                "",
                false,
                newFields
        );


        compareBodyFields(
                oldOperation.getIdentity(),
                oldFields,
                newFields,
                result
        );
    }




    private void compareBodyFields(
            String operationIdentity,
            Map<String, FieldContract> oldFields,
            Map<String, FieldContract> newFields,
            AgateOpenApiChangeSet result) {

        for (String path :
                oldFields.keySet()) {

            if (!newFields.containsKey(
                    path
            )) {

                result.addChange(
                        change(
                                operationIdentity,
                                "request.body." + path,
                                null,
                                AgateChangeType.REMOVED,
                                AgateChangeSeverity.BREAKING,
                                oldFields.get(path),
                                null,
                                "Request body property was removed"
                        )
                );

                continue;
            }


            compareField(
                    operationIdentity,
                    path,
                    oldFields.get(path),
                    newFields.get(path),
                    result
            );
        }


        for (String path :
                newFields.keySet()) {

            if (oldFields.containsKey(
                    path
            )) {

                continue;
            }


            FieldContract value =
                    newFields.get(
                            path
                    );


            result.addChange(
                    change(
                            operationIdentity,
                            "request.body." + path,
                            null,
                            AgateChangeType.ADDED,
                            value.required
                                    ? AgateChangeSeverity.BREAKING
                                    : AgateChangeSeverity.REVIEW,
                            null,
                            value,
                            value.required
                                    ? "New required request body property"
                                    : "New optional request body property"
                    )
            );
        }
    }




    private void compareField(
            String operationIdentity,
            String fieldPath,
            FieldContract oldValue,
            FieldContract newValue,
            AgateOpenApiChangeSet result) {

        String location =
                "request.body."
                        + fieldPath;


        compareAttribute(
                operationIdentity,
                location,
                "required",
                oldValue.required,
                newValue.required,
                newValue.required
                        ? AgateChangeSeverity.BREAKING
                        : AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "type",
                oldValue.type,
                newValue.type,
                AgateChangeSeverity.BREAKING,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "format",
                oldValue.format,
                newValue.format,
                AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "minLength",
                oldValue.minLength,
                newValue.minLength,
                minimumConstraintSeverity(
                        oldValue.minLength,
                        newValue.minLength
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "maxLength",
                oldValue.maxLength,
                newValue.maxLength,
                maximumConstraintSeverity(
                        oldValue.maxLength,
                        newValue.maxLength
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "minimum",
                oldValue.minimum,
                newValue.minimum,
                minimumConstraintSeverity(
                        oldValue.minimum,
                        newValue.minimum
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "maximum",
                oldValue.maximum,
                newValue.maximum,
                maximumConstraintSeverity(
                        oldValue.maximum,
                        newValue.maximum
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "minItems",
                oldValue.minItems,
                newValue.minItems,
                minimumConstraintSeverity(
                        oldValue.minItems,
                        newValue.minItems
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "maxItems",
                oldValue.maxItems,
                newValue.maxItems,
                maximumConstraintSeverity(
                        oldValue.maxItems,
                        newValue.maxItems
                ),
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "pattern",
                oldValue.pattern,
                newValue.pattern,
                AgateChangeSeverity.REVIEW,
                result
        );


        compareAttribute(
                operationIdentity,
                location,
                "enum",
                oldValue.enumValues,
                newValue.enumValues,
                enumSeverity(
                        oldValue.enumValues,
                        newValue.enumValues
                ),
                result
        );
    }




    private AgateChangeSeverity minimumConstraintSeverity(
            Object oldValue,
            Object newValue) {

        if (Objects.equals(
                oldValue,
                newValue
        )) {

            return AgateChangeSeverity.REVIEW;
        }


        if (oldValue == null &&
                newValue != null) {

            return AgateChangeSeverity.BREAKING;
        }


        if (oldValue != null &&
                newValue == null) {

            return AgateChangeSeverity.REVIEW;
        }


        Integer comparison =
                compareNumbers(
                        oldValue,
                        newValue
                );


        if (comparison == null) {

            return AgateChangeSeverity.REVIEW;
        }


        return comparison < 0
                ? AgateChangeSeverity.BREAKING
                : AgateChangeSeverity.REVIEW;
    }




    private AgateChangeSeverity maximumConstraintSeverity(
            Object oldValue,
            Object newValue) {

        if (Objects.equals(
                oldValue,
                newValue
        )) {

            return AgateChangeSeverity.REVIEW;
        }


        if (oldValue == null &&
                newValue != null) {

            return AgateChangeSeverity.BREAKING;
        }


        if (oldValue != null &&
                newValue == null) {

            return AgateChangeSeverity.REVIEW;
        }


        Integer comparison =
                compareNumbers(
                        oldValue,
                        newValue
                );


        if (comparison == null) {

            return AgateChangeSeverity.REVIEW;
        }


        return comparison > 0
                ? AgateChangeSeverity.BREAKING
                : AgateChangeSeverity.REVIEW;
    }




    private Integer compareNumbers(
            Object oldValue,
            Object newValue) {

        BigDecimal oldNumber =
                toBigDecimal(
                        oldValue
                );


        BigDecimal newNumber =
                toBigDecimal(
                        newValue
                );


        if (oldNumber == null ||
                newNumber == null) {

            return null;
        }


        return oldNumber.compareTo(
                newNumber
        );
    }




    private BigDecimal toBigDecimal(
            Object value) {

        if (value == null) {

            return null;
        }


        if (value instanceof BigDecimal decimal) {

            return decimal;
        }


        if (value instanceof Number number) {

            return new BigDecimal(
                    number.toString()
            );
        }


        try {

            return new BigDecimal(
                    value.toString()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }




    private AgateChangeSeverity enumSeverity(
            List<Object> oldValues,
            List<Object> newValues) {

        List<Object> oldSafe =
                oldValues != null
                        ? oldValues
                        : List.of();


        List<Object> newSafe =
                newValues != null
                        ? newValues
                        : List.of();


        if (oldSafe.equals(
                newSafe
        )) {

            return AgateChangeSeverity.REVIEW;
        }


        if (oldSafe.isEmpty() &&
                !newSafe.isEmpty()) {

            return AgateChangeSeverity.BREAKING;
        }


        if (!oldSafe.isEmpty() &&
                newSafe.isEmpty()) {

            return AgateChangeSeverity.REVIEW;
        }


        return newSafe.containsAll(
                oldSafe
        )
                ? AgateChangeSeverity.REVIEW
                : AgateChangeSeverity.BREAKING;
    }




    private void compareAttribute(
            String operationIdentity,
            String location,
            String property,
            Object oldValue,
            Object newValue,
            AgateChangeSeverity severity,
            AgateOpenApiChangeSet result) {

        if (Objects.equals(
                oldValue,
                newValue
        )) {

            return;
        }


        result.addChange(
                change(
                        operationIdentity,
                        location,
                        property,
                        AgateChangeType.MODIFIED,
                        severity,
                        oldValue,
                        newValue,
                        property + " changed"
                )
        );
    }




    private Map<String, ParameterContract> parameterContracts(
            AgateRequestModel request) {

        Map<String, ParameterContract> result =
                new LinkedHashMap<>();


        if (request == null) {

            return result;
        }


        addParameters(
                result,
                "request.path",
                request.getPathParameters()
        );


        addParameters(
                result,
                "request.query",
                request.getQueryParameters()
        );


        addParameters(
                result,
                "request.header",
                request.getHeaderParameters()
        );


        addParameters(
                result,
                "request.cookie",
                request.getCookieParameters()
        );


        return result;
    }




    private void addParameters(
            Map<String, ParameterContract> target,
            String location,
            List<AgateRequestParameterModel> parameters) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            target.put(
                    location
                            + "."
                            + parameter.getName(),
                    new ParameterContract(
                            parameter
                    )
            );
        }
    }




    private AgateSchema requestBodySchema(
            AgateRequestModel request) {

        if (request == null) {

            return null;
        }


        AgateRequestBodyModel body =
                request.getBody();


        if (body == null ||
                body.getContents() == null ||
                body.getContents().isEmpty()) {

            return null;
        }


        AgateRequestContentModel content =
                body
                        .getContents()
                        .stream()
                        .filter(value ->
                                "application/json".equals(
                                        value.getMediaType()
                                )
                        )
                        .findFirst()
                        .orElse(
                                body.getContents()
                                        .get(0)
                        );


        return content.getSchema();
    }




    private void collectFields(
            AgateSchema schema,
            String path,
            boolean required,
            Map<String, FieldContract> result) {

        if (schema == null) {

            return;
        }


        if (!path.isBlank()) {

            result.put(
                    path,
                    new FieldContract(
                            schema,
                            required
                    )
            );
        }


        if ("object".equals(
                schema.getType()
        )) {

            if (schema.getProperties() == null) {

                return;
            }


            for (Map.Entry<String, AgateSchema> entry :
                    schema.getProperties()
                            .entrySet()) {

                String childPath =
                        path.isBlank()
                                ? entry.getKey()
                                : path
                                + "."
                                + entry.getKey();


                boolean childRequired =
                        schema.getRequired() != null
                                &&
                                schema
                                        .getRequired()
                                        .contains(
                                                entry.getKey()
                                        );


                collectFields(
                        entry.getValue(),
                        childPath,
                        childRequired,
                        result
                );
            }


            return;
        }


        if ("array".equals(
                schema.getType()
        ) &&
                schema.getItems() != null &&
                "object".equals(
                        schema.getItems()
                                .getType()
                )) {

            collectFields(
                    schema.getItems(),
                    path,
                    required,
                    result
            );
        }
    }




    private AgateApiChange change(
            String operationIdentity,
            String location,
            String property,
            AgateChangeType type,
            AgateChangeSeverity severity,
            Object oldValue,
            Object newValue,
            String description) {

        AgateApiChange result =
                new AgateApiChange();


        result.setOperationIdentity(
                operationIdentity
        );


        result.setLocation(
                location
        );


        result.setProperty(
                property
        );


        result.setChangeType(
                type
        );


        result.setSeverity(
                severity
        );


        result.setOldValue(
                oldValue
        );


        result.setNewValue(
                newValue
        );


        result.setDescription(
                description
        );


        return result;
    }




    private static class ParameterContract {


        private final boolean required;

        private final String type;

        private final String format;

        private final Integer minLength;

        private final Integer maxLength;

        private final Object minimum;

        private final Object maximum;

        private final String pattern;

        private final List<Object> enumValues;




        private ParameterContract(
                AgateRequestParameterModel parameter) {

            this.required =
                    parameter.isRequired();


            AgateSchema schema =
                    parameter.getSchema();


            this.type =
                    schema != null
                            ? schema.getType()
                            : null;


            this.format =
                    schema != null
                            ? schema.getFormat()
                            : null;


            this.minLength =
                    schema != null
                            ? schema.getMinLength()
                            : null;


            this.maxLength =
                    schema != null
                            ? schema.getMaxLength()
                            : null;


            this.minimum =
                    schema != null
                            ? schema.getMinimum()
                            : null;


            this.maximum =
                    schema != null
                            ? schema.getMaximum()
                            : null;


            this.pattern =
                    schema != null
                            ? schema.getPattern()
                            : null;


            this.enumValues =
                    schema != null
                            && schema.getEnumValues() != null
                            ? new ArrayList<>(
                                    schema.getEnumValues()
                            )
                            : new ArrayList<>();
        }




        @Override
        public String toString() {

            return "ParameterContract{"
                    + "required="
                    + required
                    + ", type='"
                    + type
                    + '\''
                    + ", format='"
                    + format
                    + '\''
                    + '}';
        }
    }




    private static class FieldContract {


        private final boolean required;


        private final String type;

        private final String format;


        private final Integer minLength;

        private final Integer maxLength;


        private final Object minimum;

        private final Object maximum;


        private final Integer minItems;

        private final Integer maxItems;


        private final String pattern;


        private final List<Object> enumValues;




        private FieldContract(
                AgateSchema schema,
                boolean required) {

            this.required =
                    required;


            this.type =
                    schema.getType();


            this.format =
                    schema.getFormat();


            this.minLength =
                    schema.getMinLength();


            this.maxLength =
                    schema.getMaxLength();


            this.minimum =
                    schema.getMinimum();


            this.maximum =
                    schema.getMaximum();


            this.minItems =
                    schema.getMinItems();


            this.maxItems =
                    schema.getMaxItems();


            this.pattern =
                    schema.getPattern();


            this.enumValues =
                    schema.getEnumValues() != null
                            ? new ArrayList<>(
                                    schema.getEnumValues()
                            )
                            : new ArrayList<>();
        }




        @Override
        public String toString() {

            return "FieldContract{"
                    + "required="
                    + required
                    + ", type='"
                    + type
                    + '\''
                    + ", format='"
                    + format
                    + '\''
                    + '}';
        }
    }
}