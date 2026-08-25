package at.co.svc.agate.openapi.phase3.dsl.model;

import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;


public class AgateRestDslTest {


    private String technicalName;

    private String sourceTestId;

    private String operationIdentity;

    private String name;

    private AgateTestCaseType sourceType;


    private AgateRestDslRequest request;

    private AgateRestDslExpectation expectation;




    public String getTechnicalName() {

        return technicalName;
    }


    public void setTechnicalName(
            String technicalName) {

        this.technicalName =
                technicalName;
    }




    public String getSourceTestId() {

        return sourceTestId;
    }


    public void setSourceTestId(
            String sourceTestId) {

        this.sourceTestId =
                sourceTestId;
    }




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name = name;
    }




    public AgateTestCaseType getSourceType() {

        return sourceType;
    }


    public void setSourceType(
            AgateTestCaseType sourceType) {

        this.sourceType =
                sourceType;
    }




    public AgateRestDslRequest getRequest() {

        return request;
    }


    public void setRequest(
            AgateRestDslRequest request) {

        this.request = request;
    }




    public AgateRestDslExpectation getExpectation() {

        return expectation;
    }


    public void setExpectation(
            AgateRestDslExpectation expectation) {

        this.expectation =
                expectation;
    }
}