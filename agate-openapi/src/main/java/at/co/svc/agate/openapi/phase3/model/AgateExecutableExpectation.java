package at.co.svc.agate.openapi.phase3.model;

import at.co.svc.agate.openapi.phase2.model.AgateExpectedOutcome;


public class AgateExecutableExpectation {


    private AgateExpectedOutcome expectedOutcome;

    private String expectedStatusCode;

    private boolean exactStatusRequired;




    public AgateExpectedOutcome getExpectedOutcome() {

        return expectedOutcome;
    }


    public void setExpectedOutcome(
            AgateExpectedOutcome expectedOutcome) {

        this.expectedOutcome =
                expectedOutcome;
    }




    public String getExpectedStatusCode() {

        return expectedStatusCode;
    }


    public void setExpectedStatusCode(
            String expectedStatusCode) {

        this.expectedStatusCode =
                expectedStatusCode;
    }




    public boolean isExactStatusRequired() {

        return exactStatusRequired;
    }


    public void setExactStatusRequired(
            boolean exactStatusRequired) {

        this.exactStatusRequired =
                exactStatusRequired;
    }
}