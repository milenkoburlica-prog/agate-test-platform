package at.co.svc.agate.openapi.impact.analysis.model;

import java.nio.file.Path;


public class AgateTestCaseImpact {


    private String operationIdentity;

    private String changeLocation;

    private String changeProperty;


    private AgateArtifactType artifactType;

    private Path artifact;


    private String artifactLocation;

    private String testcaseName;


    private String currentValue;

    private String expectedValue;


    private AgateRecommendedAction action;

    private String reason;




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public String getChangeLocation() {

        return changeLocation;
    }


    public void setChangeLocation(
            String changeLocation) {

        this.changeLocation =
                changeLocation;
    }




    public String getChangeProperty() {

        return changeProperty;
    }


    public void setChangeProperty(
            String changeProperty) {

        this.changeProperty =
                changeProperty;
    }




    public AgateArtifactType getArtifactType() {

        return artifactType;
    }


    public void setArtifactType(
            AgateArtifactType artifactType) {

        this.artifactType =
                artifactType;
    }




    public Path getArtifact() {

        return artifact;
    }


    public void setArtifact(
            Path artifact) {

        this.artifact =
                artifact;
    }




    public String getArtifactLocation() {

        return artifactLocation;
    }


    public void setArtifactLocation(
            String artifactLocation) {

        this.artifactLocation =
                artifactLocation;
    }




    public String getTestcaseName() {

        return testcaseName;
    }


    public void setTestcaseName(
            String testcaseName) {

        this.testcaseName =
                testcaseName;
    }




    public String getCurrentValue() {

        return currentValue;
    }


    public void setCurrentValue(
            String currentValue) {

        this.currentValue =
                currentValue;
    }




    public String getExpectedValue() {

        return expectedValue;
    }


    public void setExpectedValue(
            String expectedValue) {

        this.expectedValue =
                expectedValue;
    }




    public AgateRecommendedAction getAction() {

        return action;
    }


    public void setAction(
            AgateRecommendedAction action) {

        this.action =
                action;
    }




    public String getReason() {

        return reason;
    }


    public void setReason(
            String reason) {

        this.reason =
                reason;
    }
}