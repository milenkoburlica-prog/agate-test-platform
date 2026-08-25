package at.co.svc.agate.openapi.phase3.dsl.model;

import at.co.svc.agate.openapi.phase2.model.AgateExpectedOutcome;


public class AgateRestDslExpectation {


    private AgateExpectedOutcome outcome;

    private String statusCode;

    private boolean exactStatusRequired;




    public AgateExpectedOutcome getOutcome() {

        return outcome;
    }


    public void setOutcome(
            AgateExpectedOutcome outcome) {

        this.outcome = outcome;
    }




    public String getStatusCode() {

        return statusCode;
    }


    public void setStatusCode(
            String statusCode) {

        this.statusCode =
                statusCode;
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