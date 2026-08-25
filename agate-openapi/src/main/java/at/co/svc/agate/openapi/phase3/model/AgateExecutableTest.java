package at.co.svc.agate.openapi.phase3.model;

import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;


public class AgateExecutableTest {


    private String id;

    private String operationIdentity;

    private String name;

    private String technicalName;

    private String reason;


    private AgateTestCaseType sourceType;


    private AgateExecutableRequest request;

    private AgateExecutableExpectation expectation;




    public String getId() {

        return id;
    }


    public void setId(
            String id) {

        this.id = id;
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




    public String getTechnicalName() {

        return technicalName;
    }


    public void setTechnicalName(
            String technicalName) {

        this.technicalName =
                technicalName;
    }




    public String getReason() {

        return reason;
    }


    public void setReason(
            String reason) {

        this.reason = reason;
    }




    public AgateTestCaseType getSourceType() {

        return sourceType;
    }


    public void setSourceType(
            AgateTestCaseType sourceType) {

        this.sourceType =
                sourceType;
    }




    public AgateExecutableRequest getRequest() {

        return request;
    }


    public void setRequest(
            AgateExecutableRequest request) {

        this.request = request;
    }




    public AgateExecutableExpectation getExpectation() {

        return expectation;
    }


    public void setExpectation(
            AgateExecutableExpectation expectation) {

        this.expectation =
                expectation;
    }
}