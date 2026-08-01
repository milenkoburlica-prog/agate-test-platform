package at.co.svc.open.api.spec.model;


import java.util.List;


public class SchemaParameter {


    private String name;

    private String type;

    private boolean required;

    private List<String> enumValues;

    private String pattern;

    private String example;



    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name=name;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type=type;
    }


    public boolean isRequired() {
        return required;
    }


    public void setRequired(boolean required) {
        this.required=required;
    }


    public List<String> getEnumValues() {
        return enumValues;
    }


    public void setEnumValues(List<String> values) {
        this.enumValues=values;
    }


    public String getPattern() {
        return pattern;
    }


    public void setPattern(String pattern) {
        this.pattern=pattern;
    }


    public String getExample() {
        return example;
    }


    public void setExample(String example) {
        this.example=example;
    }

}