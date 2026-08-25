package at.co.svc.agate.openapi.impact.analysis.validation;

import at.co.svc.agate.openapi.model.AgateSchema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class AgateContractValueValidator {


    public ValidationResult validate(
            AgateSchema schema,
            String rawValue,
            boolean required) {

        if (isNull(
                rawValue
        )) {

            if (required) {

                return ValidationResult.invalid(
                        "Required value is missing"
                );
            }


            return ValidationResult.valid();
        }


        if (schema == null) {

            return ValidationResult.valid();
        }


        String type =
                schema.getType();


        if ("string".equals(type)) {

            return validateString(
                    schema,
                    rawValue
            );
        }


        if ("integer".equals(type) ||
                "number".equals(type)) {

            return validateNumber(
                    schema,
                    rawValue
            );
        }


        if ("array".equals(type)) {

            return validateArray(
                    schema,
                    rawValue
            );
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateString(
            AgateSchema schema,
            String value) {

        if (schema.getMinLength() != null &&
                value.length()
                        < schema.getMinLength()) {

            return ValidationResult.invalid(
                    "Length "
                            + value.length()
                            + " is below minLength "
                            + schema.getMinLength()
            );
        }


        if (schema.getMaxLength() != null &&
                value.length()
                        > schema.getMaxLength()) {

            return ValidationResult.invalid(
                    "Length "
                            + value.length()
                            + " is above maxLength "
                            + schema.getMaxLength()
            );
        }


        if (schema.getEnumValues() != null &&
                !schema.getEnumValues().isEmpty()) {

            boolean found =
                    schema
                            .getEnumValues()
                            .stream()
                            .map(
                                    String::valueOf
                            )
                            .anyMatch(
                                    value::equals
                            );


            if (!found) {

                return ValidationResult.invalid(
                        "Value is not part of new enum "
                                + schema.getEnumValues()
                );
            }
        }


        if (schema.getPattern() != null) {

            try {

                if (!value.matches(
                        schema.getPattern()
                )) {

                    return ValidationResult.invalid(
                            "Value does not match pattern "
                                    + schema.getPattern()
                    );
                }

            } catch (Exception ignored) {
            }
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateNumber(
            AgateSchema schema,
            String rawValue) {

        BigDecimal value;


        try {

            value =
                    new BigDecimal(
                            rawValue
                    );

        } catch (Exception e) {

            return ValidationResult.invalid(
                    "Value is not numeric"
            );
        }


        if (schema.getMinimum() != null) {

            BigDecimal minimum =
                    new BigDecimal(
                            schema
                                    .getMinimum()
                                    .toString()
                    );


            if (value.compareTo(
                    minimum
            ) < 0) {

                return ValidationResult.invalid(
                        "Value is below minimum "
                                + minimum
                );
            }
        }


        if (schema.getMaximum() != null) {

            BigDecimal maximum =
                    new BigDecimal(
                            schema
                                    .getMaximum()
                                    .toString()
                    );


            if (value.compareTo(
                    maximum
            ) > 0) {

                return ValidationResult.invalid(
                        "Value is above maximum "
                                + maximum
                );
            }
        }


        return ValidationResult.valid();
    }




    private ValidationResult validateArray(
            AgateSchema schema,
            String rawValue) {

        List<String> values =
                parseSimpleArray(
                        rawValue
                );


        if (schema.getMinItems() != null &&
                values.size()
                        < schema.getMinItems()) {

            return ValidationResult.invalid(
                    "Array size "
                            + values.size()
                            + " is below minItems "
                            + schema.getMinItems()
            );
        }


        if (schema.getMaxItems() != null &&
                values.size()
                        > schema.getMaxItems()) {

            return ValidationResult.invalid(
                    "Array size "
                            + values.size()
                            + " is above maxItems "
                            + schema.getMaxItems()
            );
        }


        return ValidationResult.valid();
    }




    private List<String> parseSimpleArray(
            String raw) {

        List<String> result =
                new ArrayList<>();


        if (raw == null) {

            return result;
        }


        String value =
                raw.trim();


        if (value.startsWith("[") &&
                value.endsWith("]")) {

            value =
                    value.substring(
                            1,
                            value.length() - 1
                    );
        }


        if (value.isBlank()) {

            return result;
        }


        String[] parts =
                value.split(
                        ","
                );


        for (String part :
                parts) {

            result.add(
                    part.trim()
            );
        }


        return result;
    }




    private boolean isNull(
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