package at.co.svc.agate.openapi.impact.analysis.coverage;


public class AgateCoverageRequirement {


    private String operationIdentity;

    private String field;

    private AgateCoverageRequirementType type;

    private Object expectedValue;

    private String expectedTestNameFragment;

    private String description;




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public String getField() {

        return field;
    }


    public void setField(
            String field) {

        this.field =
                field;
    }




    public AgateCoverageRequirementType getType() {

        return type;
    }


    public void setType(
            AgateCoverageRequirementType type) {

        this.type =
                type;
    }




    public Object getExpectedValue() {

        return expectedValue;
    }


    public void setExpectedValue(
            Object expectedValue) {

        this.expectedValue =
                expectedValue;
    }




    public String getExpectedTestNameFragment() {

        return expectedTestNameFragment;
    }


    public void setExpectedTestNameFragment(
            String expectedTestNameFragment) {

        this.expectedTestNameFragment =
                expectedTestNameFragment;
    }




    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description) {

        this.description =
                description;
    }
}