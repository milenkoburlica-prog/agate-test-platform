package at.co.svc.aga.transformator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.co.svc.aga.transformator.utils.MigrationLog;

/**
 * Helper class for step value details.
 */
public class StepValueDetails {

    private String value;

    private String actionMode; // Input, Verify, Buffer, etc.

    private String actionProperty;

    /**
     * Original Tosca operator code.
     *
     * Known values:
     * 1 = ==
     * 2 = !=
     *
     * The original Tosca value is preserved here.
     * Translation to the AGATE operator is performed later.
     */
    private String operator;

    private String toscaPath;

    private String xmlPath;

    private String jsonPath;

    @JsonProperty("xCondition")
    private String xCondition;

    private String name;

    private Constraint constrain;

    public StepValueDetails() {
        // Required by Jackson
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getToscaPath() {
        return toscaPath;
    }

    public void setToscaPath(String toscaPath) {
        this.toscaPath = toscaPath;
    }

    public String getActionProperty() {
        return actionProperty;
    }

    public void setActionProperty(String actionProperty) {
        this.actionProperty = actionProperty;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getActionMode() {
        return actionMode;
    }

    public void setActionMode(String actionMode) {
        this.actionMode = actionMode;
    }

    @JsonProperty("xCondition")
    public String getxCondition() {
        return xCondition;
    }

    @JsonProperty("xCondition")
    public void setxCondition(String xCondition) {

        MigrationLog.debug(
                "Jackson xCondition: "
                        + name
                        + " -> "
                        + xCondition
        );

        this.xCondition = xCondition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Constraint getConstrain() {
        return constrain;
    }

    public void setConstrain(Constraint constrain) {
        this.constrain = constrain;
    }

    @Override
    public String toString() {
        return "StepValueDetails{name='"
                + name
                + "', value='"
                + value
                + "', actionMode='"
                + actionMode
                + "', actionProperty='"
                + actionProperty
                + "', operator='"
                + operator
                + "', toscaPath='"
                + toscaPath
                + "', xCondition='"
                + xCondition
                + "'}";
    }
}