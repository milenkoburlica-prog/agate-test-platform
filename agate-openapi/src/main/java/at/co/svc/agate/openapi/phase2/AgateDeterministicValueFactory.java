package at.co.svc.agate.openapi.phase2;

import at.co.svc.agate.openapi.model.AgateSchema;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class AgateDeterministicValueFactory {


    public Object createValidValue(
            AgateSchema schema) {

        if (schema == null) {

            return "test";
        }


        if (schema.getExample() != null &&
                isValidAgainstKnownConstraints(
                        schema,
                        schema.getExample()
                )) {

            return schema.getExample();
        }


        if (schema.getDefaultValue() != null &&
                isValidAgainstKnownConstraints(
                        schema,
                        schema.getDefaultValue()
                )) {

            return schema.getDefaultValue();
        }


        if (schema.getEnumValues() != null &&
                !schema.getEnumValues().isEmpty()) {

            for (Object value :
                    schema.getEnumValues()) {

                if (isValidAgainstKnownConstraints(
                        schema,
                        value
                )) {

                    return value;
                }
            }
        }


        String type =
                schema.getType();


        if ("object".equals(type)) {

            return createMinimalObject(
                    schema
            );
        }


        if ("array".equals(type)) {

            return createValidArray(
                    schema
            );
        }


        if ("integer".equals(type)) {

            return createValidInteger(
                    schema
            );
        }


        if ("number".equals(type)) {

            return createValidNumber(
                    schema
            );
        }


        if ("boolean".equals(type)) {

            return Boolean.TRUE;
        }


        if ("string".equals(type) ||
                type == null) {

            String formatValue =
                    createFormatValidValue(
                            schema
                    );


            if (formatValue != null) {

                return formatValue;
            }


            String patternValue =
                    createPatternValidValue(
                            schema
                    );


            if (patternValue != null) {

                return patternValue;
            }


            return createValidString(
                    schema
            );
        }


        return "test";
    }




    /*
     * =====================================================
     * FULL VALUE
     * =====================================================
     */


    public Object createFullValue(
            AgateSchema schema) {

        if (schema == null) {

            return "test";
        }


        String type =
                schema.getType();


        if ("object".equals(type)) {

            return createFullObject(
                    schema
            );
        }


        if ("array".equals(type)) {

            return createFullArray(
                    schema
            );
        }


        return createValidValue(
                schema
        );
    }




    private Map<String, Object> createMinimalObject(
            AgateSchema schema) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (schema.getProperties() == null ||
                schema.getProperties().isEmpty()) {

            return result;
        }


        if (schema.getRequired() == null ||
                schema.getRequired().isEmpty()) {

            return result;
        }


        for (String requiredProperty :
                schema.getRequired()) {

            AgateSchema propertySchema =
                    schema
                            .getProperties()
                            .get(
                                    requiredProperty
                            );


            if (propertySchema == null) {

                continue;
            }


            result.put(
                    requiredProperty,
                    createValidValue(
                            propertySchema
                    )
            );
        }


        return result;
    }




    private Map<String, Object> createFullObject(
            AgateSchema schema) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (schema.getProperties() == null ||
                schema.getProperties().isEmpty()) {

            return result;
        }


        for (Map.Entry<String, AgateSchema> entry :
                schema.getProperties().entrySet()) {

            AgateSchema propertySchema =
                    entry.getValue();


            if (propertySchema == null) {

                continue;
            }


            /*
             * readOnly fields normally describe response data.
             * Do not automatically send them in a request.
             */
            if (propertySchema.isReadOnly()) {

                continue;
            }


            result.put(
                    entry.getKey(),
                    createFullValue(
                            propertySchema
                    )
            );
        }


        return result;
    }




    private List<Object> createFullArray(
            AgateSchema schema) {

        int size =
                1;


        if (schema.getMinItems() != null &&
                schema.getMinItems() > size) {

            size =
                    schema.getMinItems();
        }


        if (schema.getMaxItems() != null &&
                size > schema.getMaxItems()) {

            size =
                    schema.getMaxItems();
        }


        List<Object> result =
                new ArrayList<>();


        for (int i = 0;
             i < size;
             i++) {

            result.add(
                    createFullValue(
                            schema.getItems()
                    )
            );
        }


        return result;
    }




    /*
     * =====================================================
     * FORMAT
     * =====================================================
     */


    public String createFormatValidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getFormat() == null) {

            return null;
        }


        String value;


        switch (schema.getFormat()) {

            case "email" ->
                    value =
                            "agate@example.com";

            case "uuid" ->
                    value =
                            UUID
                                    .nameUUIDFromBytes(
                                            "AGATE".getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                                    )
                                    .toString();

            case "date" ->
                    value =
                            LocalDate
                                    .of(
                                            2026,
                                            1,
                                            15
                                    )
                                    .toString();

            case "date-time" ->
                    value =
                            OffsetDateTime
                                    .of(
                                            2026,
                                            1,
                                            15,
                                            12,
                                            0,
                                            0,
                                            0,
                                            ZoneOffset.UTC
                                    )
                                    .toString();

            case "uri" ->
                    value =
                            "https://example.com/agate";

            default -> {

                return null;
            }
        }


        if (!fitsLength(
                schema,
                value
        )) {

            return null;
        }


        return value;
    }




    public String createFormatInvalidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getFormat() == null) {

            return null;
        }


        String value;


        switch (schema.getFormat()) {

            case "email" ->
                    value =
                            "invalid-email";

            case "uuid" ->
                    value =
                            "not-a-uuid";

            case "date" ->
                    value =
                            "2026-99-99";

            case "date-time" ->
                    value =
                            "not-a-date-time";

            case "uri" ->
                    value =
                            "not a uri";

            default -> {

                return null;
            }
        }


        if (schema.getMinLength() != null &&
                value.length() <
                        schema.getMinLength()) {

            StringBuilder result =
                    new StringBuilder(
                            value
                    );


            while (result.length() <
                    schema.getMinLength()) {

                result.append(
                        "!"
                );
            }


            value =
                    result.toString();
        }


        if (schema.getMaxLength() != null &&
                value.length() >
                        schema.getMaxLength()) {

            return null;
        }


        return value;
    }




    /*
     * =====================================================
     * STRING / PATTERN
     * =====================================================
     */


    public String createValidString(
            AgateSchema schema) {

        int length =
                4;


        if (schema != null &&
                schema.getMinLength() != null &&
                length <
                        schema.getMinLength()) {

            length =
                    schema.getMinLength();
        }


        if (schema != null &&
                schema.getMaxLength() != null &&
                length >
                        schema.getMaxLength()) {

            length =
                    schema.getMaxLength();
        }


        return createValidStringWithLength(
                schema,
                length
        );
    }




    public String createValidStringWithLength(
            AgateSchema schema,
            int length) {

        if (length < 0) {

            return null;
        }


        if (schema == null ||
                schema.getPattern() == null) {

            return createStringWithLength(
                    length
            );
        }


        String pattern =
                schema.getPattern();


        if (isAlphaNumericPattern(
                pattern
        )) {

            return repeatPattern(
                    "A1",
                    length
            );
        }


        if (isNumericPattern(
                pattern
        )) {

            return repeatPattern(
                    "1",
                    length
            );
        }


        if (isAlphaPattern(
                pattern
        )) {

            return repeatPattern(
                    "A",
                    length
            );
        }


        if (isUppercasePattern(
                pattern
        )) {

            return repeatPattern(
                    "A",
                    length
            );
        }


        if (isLowercasePattern(
                pattern
        )) {

            return repeatPattern(
                    "a",
                    length
            );
        }


        return null;
    }




    public String createStringWithLength(
            int length) {

        if (length <= 0) {

            return "";
        }


        return "A".repeat(
                length
        );
    }




    public String createPatternValidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getPattern() == null) {

            return null;
        }


        return createValidStringWithLength(
                schema,
                determineNormalValidStringLength(
                        schema
                )
        );
    }




    public String createPatternInvalidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getPattern() == null) {

            return null;
        }


        int length =
                determineNormalValidStringLength(
                        schema
                );


        String pattern =
                schema.getPattern();


        if (isAlphaNumericPattern(
                pattern
        )) {

            return repeatPattern(
                    "!",
                    length
            );
        }


        if (isNumericPattern(
                pattern
        )) {

            return repeatPattern(
                    "A",
                    length
            );
        }


        if (isAlphaPattern(
                pattern
        )) {

            return repeatPattern(
                    "1",
                    length
            );
        }


        if (isUppercasePattern(
                pattern
        )) {

            return repeatPattern(
                    "a",
                    length
            );
        }


        if (isLowercasePattern(
                pattern
        )) {

            return repeatPattern(
                    "A",
                    length
            );
        }


        return null;
    }




    private int determineNormalValidStringLength(
            AgateSchema schema) {

        int length =
                4;


        if (schema.getMinLength() != null &&
                length <
                        schema.getMinLength()) {

            length =
                    schema.getMinLength();
        }


        if (schema.getMaxLength() != null &&
                length >
                        schema.getMaxLength()) {

            length =
                    schema.getMaxLength();
        }


        return Math.max(
                length,
                0
        );
    }




    /*
     * =====================================================
     * NUMERIC
     * =====================================================
     */


    public Object createMinimumValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMinimum() == null) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMinimum()
                )
        );
    }




    public Object createMaximumValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMaximum() == null) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMaximum()
                )
        );
    }




    public Object createBelowMinimumValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMinimum() == null) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMinimum()
                )
                        .subtract(
                                numericStep(
                                        schema
                                )
                        )
        );
    }




    public Object createAboveMaximumValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMaximum() == null) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMaximum()
                )
                        .add(
                                numericStep(
                                        schema
                                )
                        )
        );
    }




    public Object createExclusiveMinimumValidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMinimum() == null ||
                !Boolean.TRUE.equals(
                        schema.getExclusiveMinimum()
                )) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMinimum()
                )
                        .add(
                                numericStep(
                                        schema
                                )
                        )
        );
    }




    public Object createExclusiveMinimumInvalidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMinimum() == null ||
                !Boolean.TRUE.equals(
                        schema.getExclusiveMinimum()
                )) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMinimum()
                )
        );
    }




    public Object createExclusiveMaximumValidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMaximum() == null ||
                !Boolean.TRUE.equals(
                        schema.getExclusiveMaximum()
                )) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMaximum()
                )
                        .subtract(
                                numericStep(
                                        schema
                                )
                        )
        );
    }




    public Object createExclusiveMaximumInvalidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMaximum() == null ||
                !Boolean.TRUE.equals(
                        schema.getExclusiveMaximum()
                )) {

            return null;
        }


        return convertNumber(
                schema,
                decimalValue(
                        schema.getMaximum()
                )
        );
    }




    public Object createMultipleOfValidValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getMultipleOf() == null) {

            return null;
        }


        BigDecimal factor =
                decimalValue(
                        schema.getMultipleOf()
                );


        if (factor.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return null;
        }


        BigDecimal value =
                factor;


        if (schema.getMinimum() != null) {

            BigDecimal minimum =
                    decimalValue(
                            schema.getMinimum()
                    );


            while (value.compareTo(minimum) < 0 ||
                    (
                            Boolean.TRUE.equals(
                                    schema.getExclusiveMinimum()
                            )
                                    &&
                                    value.compareTo(minimum) == 0
                    )) {

                value =
                        value.add(
                                factor
                        );
            }
        }


        if (schema.getMaximum() != null) {

            BigDecimal maximum =
                    decimalValue(
                            schema.getMaximum()
                    );


            if (value.compareTo(maximum) > 0 ||
                    (
                            Boolean.TRUE.equals(
                                    schema.getExclusiveMaximum()
                            )
                                    &&
                                    value.compareTo(maximum) == 0
                    )) {

                return null;
            }
        }


        return convertNumber(
                schema,
                value
        );
    }




    public Object createMultipleOfInvalidValue(
            AgateSchema schema) {

        Object validRaw =
                createMultipleOfValidValue(
                        schema
                );


        if (validRaw == null) {

            return null;
        }


        BigDecimal factor =
                decimalValue(
                        schema.getMultipleOf()
                );


        BigDecimal valid =
                decimalValue(
                        validRaw
                );


        BigDecimal delta;


        if ("integer".equals(
                schema.getType()
        )) {

            if (factor.compareTo(
                    BigDecimal.ONE
            ) <= 0) {

                return null;
            }


            delta =
                    BigDecimal.ONE;

        } else {

            delta =
                    factor.divide(
                            BigDecimal.TEN
                    );
        }


        BigDecimal invalid =
                valid.add(
                        delta
                );


        if (schema.getMaximum() != null &&
                invalid.compareTo(
                        decimalValue(
                                schema.getMaximum()
                        )
                ) > 0) {

            invalid =
                    valid.subtract(
                            delta
                    );
        }


        return convertNumber(
                schema,
                invalid
        );
    }




    private Object createValidInteger(
            AgateSchema schema) {

        Object multiple =
                createMultipleOfValidValue(
                        schema
                );


        if (multiple != null) {

            return multiple;
        }


        if (Boolean.TRUE.equals(
                schema.getExclusiveMinimum()
        )) {

            Object value =
                    createExclusiveMinimumValidValue(
                            schema
                    );


            if (value != null) {

                return value;
            }
        }


        if (schema.getMinimum() != null) {

            return createMinimumValue(
                    schema
            );
        }


        if (schema.getExample() != null) {

            return schema.getExample();
        }


        return 1;
    }




    private Object createValidNumber(
            AgateSchema schema) {

        Object multiple =
                createMultipleOfValidValue(
                        schema
                );


        if (multiple != null) {

            return multiple;
        }


        if (Boolean.TRUE.equals(
                schema.getExclusiveMinimum()
        )) {

            Object value =
                    createExclusiveMinimumValidValue(
                            schema
                    );


            if (value != null) {

                return value;
            }
        }


        if (schema.getMinimum() != null) {

            return createMinimumValue(
                    schema
            );
        }


        return BigDecimal.ONE;
    }




    private BigDecimal numericStep(
            AgateSchema schema) {

        if (schema != null &&
                schema.getMultipleOf() != null) {

            return decimalValue(
                    schema.getMultipleOf()
            );
        }


        if (schema != null &&
                "integer".equals(
                        schema.getType()
                )) {

            return BigDecimal.ONE;
        }


        return new BigDecimal(
                "0.01"
        );
    }




    /*
     * =====================================================
     * ENUM
     * =====================================================
     */


    public Object createInvalidEnumValue(
            AgateSchema schema) {

        if (schema == null ||
                schema.getEnumValues() == null ||
                schema.getEnumValues().isEmpty()) {

            return null;
        }


        String candidate =
                "__AGATE_INVALID_ENUM__";


        boolean exists =
                schema
                        .getEnumValues()
                        .stream()
                        .anyMatch(value ->
                                candidate.equals(
                                        String.valueOf(
                                                value
                                        )
                                )
                        );


        if (!exists) {

            return candidate;
        }


        return "__AGATE_INVALID_ENUM_2__";
    }




    /*
     * =====================================================
     * ARRAY
     * =====================================================
     */


    public List<Object> createArrayWithSize(
            AgateSchema schema,
            int size) {

        if (schema != null &&
                Boolean.TRUE.equals(
                        schema.getUniqueItems()
                )) {

            return createUniqueArray(
                    schema,
                    size
            );
        }


        List<Object> result =
                new ArrayList<>();


        if (size <= 0) {

            return result;
        }


        AgateSchema itemSchema =
                schema != null
                        ? schema.getItems()
                        : null;


        for (int i = 0;
             i < size;
             i++) {

            result.add(
                    createValidValue(
                            itemSchema
                    )
            );
        }


        return result;
    }




    public List<Object> createUniqueArray(
            AgateSchema schema,
            int size) {

        List<Object> result =
                new ArrayList<>();


        if (size <= 0) {

            return result;
        }


        AgateSchema itemSchema =
                schema != null
                        ? schema.getItems()
                        : null;


        for (int i = 0;
             i < size;
             i++) {

            result.add(
                    createUniqueValidValue(
                            itemSchema,
                            i
                    )
            );
        }


        return result;
    }




    public List<Object> createNonUniqueArray(
            AgateSchema schema) {

        Object value =
                createValidValue(
                        schema != null
                                ? schema.getItems()
                                : null
                );


        List<Object> result =
                new ArrayList<>();


        result.add(
                value
        );


        result.add(
                value
        );


        return result;
    }




    private List<Object> createValidArray(
            AgateSchema schema) {

        int size =
                1;


        if (schema.getMinItems() != null &&
                schema.getMinItems() > size) {

            size =
                    schema.getMinItems();
        }


        if (schema.getMaxItems() != null &&
                size > schema.getMaxItems()) {

            size =
                    schema.getMaxItems();
        }


        return createArrayWithSize(
                schema,
                size
        );
    }




    private Object createUniqueValidValue(
            AgateSchema schema,
            int index) {

        if (schema == null) {

            return "VALUE"
                    + index;
        }


        if ("string".equals(
                schema.getType()
        ) ||
                schema.getType() == null) {

            String base =
                    createValidString(
                            schema
                    );


            if (base == null ||
                    base.isEmpty()) {

                return base;
            }


            char[] chars =
                    base.toCharArray();


            chars[
                    chars.length - 1
                    ] =
                    uniqueCharacter(
                            schema,
                            index
                    );


            return new String(
                    chars
            );
        }


        if ("integer".equals(
                schema.getType()
        )) {

            int candidate =
                    index + 1;


            if (schema.getMinimum() != null) {

                candidate =
                        decimalValue(
                                schema.getMinimum()
                        )
                                .intValue()
                                + index;
            }


            return candidate;
        }


        return String.valueOf(
                createValidValue(
                        schema
                )
        )
                + "_"
                + index;
    }




    /*
     * =====================================================
     * HELPERS
     * =====================================================
     */


    private boolean fitsLength(
            AgateSchema schema,
            String value) {

        if (schema.getMinLength() != null &&
                value.length() <
                        schema.getMinLength()) {

            return false;
        }


        if (schema.getMaxLength() != null &&
                value.length() >
                        schema.getMaxLength()) {

            return false;
        }


        return true;
    }




    private boolean isValidAgainstKnownConstraints(
            AgateSchema schema,
            Object value) {

        if (schema == null ||
                value == null) {

            return true;
        }


        if (value instanceof String stringValue) {

            if (!fitsLength(
                    schema,
                    stringValue
            )) {

                return false;
            }


            if (schema.getPattern() != null) {

                try {

                    if (!stringValue.matches(
                            schema.getPattern()
                    )) {

                        return false;
                    }

                } catch (Exception ignored) {
                }
            }
        }


        return true;
    }




    private String repeatPattern(
            String token,
            int length) {

        if (length <= 0) {

            return "";
        }


        StringBuilder result =
                new StringBuilder();


        while (result.length() < length) {

            result.append(
                    token
            );
        }


        if (result.length() > length) {

            result.setLength(
                    length
            );
        }


        return result.toString();
    }




    private char uniqueCharacter(
            AgateSchema schema,
            int index) {

        if (schema.getPattern() != null &&
                isNumericPattern(
                        schema.getPattern()
                )) {

            return (char) (
                    '0'
                            + index % 10
            );
        }


        if (schema.getPattern() != null &&
                isLowercasePattern(
                        schema.getPattern()
                )) {

            return (char) (
                    'a'
                            + index % 26
            );
        }


        return (char) (
                'A'
                        + index % 26
        );
    }




    private boolean isAlphaNumericPattern(
            String pattern) {

        return "^[a-zA-Z0-9]+$".equals(pattern)
                ||
                "[a-zA-Z0-9]+".equals(pattern);
    }




    private boolean isNumericPattern(
            String pattern) {

        return "^[0-9]+$".equals(pattern)
                ||
                "[0-9]+".equals(pattern);
    }




    private boolean isAlphaPattern(
            String pattern) {

        return "^[a-zA-Z]+$".equals(pattern)
                ||
                "[a-zA-Z]+".equals(pattern);
    }




    private boolean isUppercasePattern(
            String pattern) {

        return "^[A-Z]+$".equals(pattern)
                ||
                "[A-Z]+".equals(pattern);
    }




    private boolean isLowercasePattern(
            String pattern) {

        return "^[a-z]+$".equals(pattern)
                ||
                "[a-z]+".equals(pattern);
    }




    private Object convertNumber(
            AgateSchema schema,
            BigDecimal value) {

        if ("integer".equals(
                schema.getType()
        )) {

            try {

                return value.intValueExact();

            } catch (ArithmeticException ignored) {

                return value.longValue();
            }
        }


        return value.stripTrailingZeros();
    }




    private BigDecimal decimalValue(
            Object value) {

        return new BigDecimal(
                value.toString()
        );
    }
}