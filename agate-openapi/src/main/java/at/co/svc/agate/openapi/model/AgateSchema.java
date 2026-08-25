package at.co.svc.agate.openapi.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateSchema {


    private String name;

    private String type;

    private String format;

    private String description;


    /*
     * Reference metadata
     */

    private String ref;

    private String sourceRef;

    private String resolvedRef;

    private String sourceDocument;


    /*
     * Values
     */

    private Object example;

    private Object defaultValue;


    /*
     * Numeric constraints
     */

    private Object minimum;

    private Object maximum;

    private Object multipleOf;

    private Boolean exclusiveMinimum;

    private Boolean exclusiveMaximum;


    /*
     * String constraints
     */

    private Integer minLength;

    private Integer maxLength;

    private String pattern;


    /*
     * Array constraints
     */

    private Integer minItems;

    private Integer maxItems;

    private Boolean uniqueItems;


    /*
     * OpenAPI flags
     */

    private boolean nullable;

    private boolean readOnly;

    private boolean writeOnly;


    /*
     * Enum
     */

    private List<Object> enumValues =
            new ArrayList<>();


    /*
     * Object schema
     */

    private List<String> required =
            new ArrayList<>();

    private Map<String, AgateSchema> properties =
            new LinkedHashMap<>();

    private AgateSchema additionalProperties;


    /*
     * Array items
     */

    private AgateSchema items;


    /*
     * Composition
     */

    private List<AgateSchema> allOf =
            new ArrayList<>();

    private List<AgateSchema> oneOf =
            new ArrayList<>();

    private List<AgateSchema> anyOf =
            new ArrayList<>();




    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name = name;
    }




    public String getType() {

        return type;
    }


    public void setType(
            String type) {

        this.type = type;
    }




    public String getFormat() {

        return format;
    }


    public void setFormat(
            String format) {

        this.format = format;
    }




    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description) {

        this.description = description;
    }




    public String getRef() {

        return ref;
    }


    public void setRef(
            String ref) {

        this.ref = ref;
    }




    public String getSourceRef() {

        return sourceRef;
    }


    public void setSourceRef(
            String sourceRef) {

        this.sourceRef = sourceRef;
    }




    public String getResolvedRef() {

        return resolvedRef;
    }


    public void setResolvedRef(
            String resolvedRef) {

        this.resolvedRef = resolvedRef;
    }




    public String getSourceDocument() {

        return sourceDocument;
    }


    public void setSourceDocument(
            String sourceDocument) {

        this.sourceDocument = sourceDocument;
    }




    public Object getExample() {

        return example;
    }


    public void setExample(
            Object example) {

        this.example = example;
    }




    public Object getDefaultValue() {

        return defaultValue;
    }


    public void setDefaultValue(
            Object defaultValue) {

        this.defaultValue = defaultValue;
    }




    public Object getMinimum() {

        return minimum;
    }


    public void setMinimum(
            Object minimum) {

        this.minimum = minimum;
    }




    public Object getMaximum() {

        return maximum;
    }


    public void setMaximum(
            Object maximum) {

        this.maximum = maximum;
    }




    public Object getMultipleOf() {

        return multipleOf;
    }


    public void setMultipleOf(
            Object multipleOf) {

        this.multipleOf = multipleOf;
    }




    public Boolean getExclusiveMinimum() {

        return exclusiveMinimum;
    }


    public void setExclusiveMinimum(
            Boolean exclusiveMinimum) {

        this.exclusiveMinimum =
                exclusiveMinimum;
    }




    public Boolean getExclusiveMaximum() {

        return exclusiveMaximum;
    }


    public void setExclusiveMaximum(
            Boolean exclusiveMaximum) {

        this.exclusiveMaximum =
                exclusiveMaximum;
    }




    public Integer getMinLength() {

        return minLength;
    }


    public void setMinLength(
            Integer minLength) {

        this.minLength = minLength;
    }




    public Integer getMaxLength() {

        return maxLength;
    }


    public void setMaxLength(
            Integer maxLength) {

        this.maxLength = maxLength;
    }




    public String getPattern() {

        return pattern;
    }


    public void setPattern(
            String pattern) {

        this.pattern = pattern;
    }




    public Integer getMinItems() {

        return minItems;
    }


    public void setMinItems(
            Integer minItems) {

        this.minItems = minItems;
    }




    public Integer getMaxItems() {

        return maxItems;
    }


    public void setMaxItems(
            Integer maxItems) {

        this.maxItems = maxItems;
    }




    public Boolean getUniqueItems() {

        return uniqueItems;
    }


    public void setUniqueItems(
            Boolean uniqueItems) {

        this.uniqueItems = uniqueItems;
    }




    public boolean isNullable() {

        return nullable;
    }


    public Boolean getNullable() {

        return nullable;
    }


    public void setNullable(
            boolean nullable) {

        this.nullable = nullable;
    }


    public void setNullable(
            Boolean nullable) {

        this.nullable =
                Boolean.TRUE.equals(
                        nullable
                );
    }




    public boolean isReadOnly() {

        return readOnly;
    }


    public Boolean getReadOnly() {

        return readOnly;
    }


    public void setReadOnly(
            boolean readOnly) {

        this.readOnly = readOnly;
    }


    public void setReadOnly(
            Boolean readOnly) {

        this.readOnly =
                Boolean.TRUE.equals(
                        readOnly
                );
    }




    public boolean isWriteOnly() {

        return writeOnly;
    }


    public Boolean getWriteOnly() {

        return writeOnly;
    }


    public void setWriteOnly(
            boolean writeOnly) {

        this.writeOnly = writeOnly;
    }


    public void setWriteOnly(
            Boolean writeOnly) {

        this.writeOnly =
                Boolean.TRUE.equals(
                        writeOnly
                );
    }




    public List<Object> getEnumValues() {

        return enumValues;
    }


    public void setEnumValues(
            List<?> enumValues) {

        this.enumValues =
                new ArrayList<>();


        if (enumValues != null) {

            this.enumValues.addAll(
                    enumValues
            );
        }
    }




    public List<String> getRequired() {

        return required;
    }


    public void setRequired(
            List<String> required) {

        this.required =
                required != null
                        ? new ArrayList<>(
                                required
                        )
                        : new ArrayList<>();
    }




    public Map<String, AgateSchema> getProperties() {

        return properties;
    }


    public void setProperties(
            Map<String, AgateSchema> properties) {

        this.properties =
                properties != null
                        ? new LinkedHashMap<>(
                                properties
                        )
                        : new LinkedHashMap<>();
    }




    public void addProperty(
            String name,
            AgateSchema schema) {

        if (name == null) {

            return;
        }


        properties.put(
                name,
                schema
        );
    }




    public AgateSchema getAdditionalProperties() {

        return additionalProperties;
    }


    public void setAdditionalProperties(
            AgateSchema additionalProperties) {

        this.additionalProperties =
                additionalProperties;
    }




    public AgateSchema getItems() {

        return items;
    }


    public void setItems(
            AgateSchema items) {

        this.items = items;
    }




    public List<AgateSchema> getAllOf() {

        return allOf;
    }


    public void setAllOf(
            List<AgateSchema> allOf) {

        this.allOf =
                allOf != null
                        ? new ArrayList<>(
                                allOf
                        )
                        : new ArrayList<>();
    }




    public List<AgateSchema> getOneOf() {

        return oneOf;
    }


    public void setOneOf(
            List<AgateSchema> oneOf) {

        this.oneOf =
                oneOf != null
                        ? new ArrayList<>(
                                oneOf
                        )
                        : new ArrayList<>();
    }




    public List<AgateSchema> getAnyOf() {

        return anyOf;
    }


    public void setAnyOf(
            List<AgateSchema> anyOf) {

        this.anyOf =
                anyOf != null
                        ? new ArrayList<>(
                                anyOf
                        )
                        : new ArrayList<>();
    }
}