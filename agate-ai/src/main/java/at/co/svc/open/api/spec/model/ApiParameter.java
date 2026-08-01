package at.co.svc.open.api.spec.model;

import java.util.ArrayList;
import java.util.List;

public class ApiParameter {

    private String name;
    private ParameterLocation location;
    private boolean required;
    private String type;
    private String format;
    private String pattern;
    private String example;
    private String defaultValue;
    private String description;
    private List<String> enumValues = new ArrayList<>();

    public ApiParameter() {
    }

    public ApiParameter(String name, ParameterLocation location) {

        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterLocation getLocation() {
        return location;
    }

    public void setLocation(ParameterLocation location) {
        this.location = location;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    @Override
    public String toString() {

        return "ApiParameter{" + "name='" + name + '\'' + ", location=" + location + ", required=" + required
                + ", type='" + type + '\'' + '}';
    }
}