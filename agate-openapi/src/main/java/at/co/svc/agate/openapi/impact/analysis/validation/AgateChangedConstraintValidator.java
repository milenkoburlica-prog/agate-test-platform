package at.co.svc.agate.openapi.impact.analysis.validation;

import at.co.svc.agate.openapi.change.detection.AgateApiChange;
import at.co.svc.agate.openapi.model.AgateSchema;

import java.math.BigDecimal;
import java.util.List;


public class AgateChangedConstraintValidator {


    public ValidationResult validate(
            AgateApiChange change,
            AgateSchema schema,
            String value) {

        if (change == null ||
                schema == null) {

            return ValidationResult.valid();
        }


        if (isNullValue(value)) {

            return ValidationResult.valid();
        }


        String property =
                change.getProperty();


        if (property == null) {

            return ValidationResult.valid();
        }


        return switch (property) {

            case "minLength" ->
                    validateMinLength(
                            schema,
                            value
                    );

            case "maxLength" ->
                    validateMaxLength(
                            schema,
                            value
                    );

            case "minimum" ->
                    validateMinimum(
                            schema,
                            value
                    );

            case "maximum" ->
                    validateMaximum(
                            schema,
                            value
                    );

            case "minItems" ->
                    validateMinItems(
                            schema,
                            value
                    );

            case "maxItems" ->
                    validateMaxItems(
                            schema,
                            value
                    );

            case "enum" ->
                    validateEnum(
                            schema,
                            value
                    );

            default ->
                    ValidationResult.valid();
        };
    }




    private ValidationResult validateMinLength(
            AgateSchema schema,
            String value) {

        Integer minimum =
                schema.getMinLength();


        if (minimum == null) {

            return ValidationResult.valid();
        }


        if (value.length() < minimum) {

            return ValidationResult.invalid(
                    "Length "
                            + value.length()
                            + " is below minLength "
                            + minimum
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateMaxLength(
            AgateSchema schema,
            String value) {

        Integer maximum =
                schema.getMaxLength();


        if (maximum == null) {

            return ValidationResult.valid();
        }


        if (value.length() > maximum) {

            return ValidationResult.invalid(
                    "Length "
                            + value.length()
                            + " is above maxLength "
                            + maximum
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateMinimum(
            AgateSchema schema,
            String value) {

        Object minimum =
                schema.getMinimum();


        if (minimum == null) {

            return ValidationResult.valid();
        }


        try {

            BigDecimal current =
                    new BigDecimal(
                            value
                    );


            BigDecimal boundary =
                    new BigDecimal(
                            minimum.toString()
                    );


            if (current.compareTo(
                    boundary
            ) < 0) {

                return ValidationResult.invalid(
                        "Value "
                                + value
                                + " is below minimum "
                                + minimum
                );
            }


        } catch (NumberFormatException e) {

            return ValidationResult.invalid(
                    "Value "
                            + value
                            + " is not numeric"
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateMaximum(
            AgateSchema schema,
            String value) {

        Object maximum =
                schema.getMaximum();


        if (maximum == null) {

            return ValidationResult.valid();
        }


        try {

            BigDecimal current =
                    new BigDecimal(
                            value
                    );


            BigDecimal boundary =
                    new BigDecimal(
                            maximum.toString()
                    );


            if (current.compareTo(
                    boundary
            ) > 0) {

                return ValidationResult.invalid(
                        "Value "
                                + value
                                + " is above maximum "
                                + maximum
                );
            }


        } catch (NumberFormatException e) {

            return ValidationResult.invalid(
                    "Value "
                            + value
                            + " is not numeric"
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateMinItems(
            AgateSchema schema,
            String value) {

        Integer minimum =
                schema.getMinItems();


        if (minimum == null) {

            return ValidationResult.valid();
        }


        int size =
                arraySize(
                        value
                );


        if (size < minimum) {

            return ValidationResult.invalid(
                    "Array size "
                            + size
                            + " is below minItems "
                            + minimum
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateMaxItems(
            AgateSchema schema,
            String value) {

        Integer maximum =
                schema.getMaxItems();


        if (maximum == null) {

            return ValidationResult.valid();
        }


        int size =
                arraySize(
                        value
                );


        if (size > maximum) {

            return ValidationResult.invalid(
                    "Array size "
                            + size
                            + " is above maxItems "
                            + maximum
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateEnum(
            AgateSchema schema,
            String value) {

        List<Object> enumValues =
                schema.getEnumValues();


        if (enumValues == null ||
                enumValues.isEmpty()) {

            return ValidationResult.valid();
        }


        for (Object enumValue :
                enumValues) {

            if (enumValue != null &&
                    enumValue
                            .toString()
                            .equals(
                                    value
                            )) {

                return ValidationResult.valid();
            }
        }


        return ValidationResult.invalid(
                "Value "
                        + value
                        + " is not part of new enum "
                        + enumValues
        );
    }




    private int arraySize(
            String value) {

        if (value == null) {

            return 0;
        }


        String trimmed =
                value.trim();


        if ("[]".equals(
                trimmed
        )) {

            return 0;
        }


        if (!trimmed.startsWith("[") ||
                !trimmed.endsWith("]")) {

            return 1;
        }


        String content =
                trimmed.substring(
                        1,
                        trimmed.length() - 1
                ).trim();


        if (content.isEmpty()) {

            return 0;
        }


        return content
                .split(",")
                .length;
    }




    private boolean isNullValue(
            String value) {

        return value == null
                ||
                value.isBlank()
                ||
                "{NULL}".equalsIgnoreCase(
                        value.trim()
                );
    }




    public static class ValidationResult {


        private final boolean valid;

        private final String reason;




        private ValidationResult(
                boolean valid,
                String reason) {

            this.valid =
                    valid;

            this.reason =
                    reason;
        }




        public static ValidationResult valid() {

            return new ValidationResult(
                    true,
                    null
            );
        }




        public static ValidationResult invalid(
                String reason) {

            return new ValidationResult(
                    false,
                    reason
            );
        }




        public boolean isValid() {

            return valid;
        }




        public String getReason() {

            return reason;
        }
    }
}