package at.co.svc.agate.openapi.phase2.model;

import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;

public class AgateGeneratedTestCase {

    private String id;

    private String name;

    private AgateTestCaseType type;

    private AgateExpectedOutcome expectedOutcome;

    private String reason;

    private String expectedStatusCode;

    private AgateRequestValues requestValues;


    public String getId() {
        return id;
    }

    public void setId(
            String id) {

        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }


    public AgateTestCaseType getType() {
        return type;
    }

    public void setType(
            AgateTestCaseType type) {

        this.type = type;
    }


    public AgateExpectedOutcome getExpectedOutcome() {
        return expectedOutcome;
    }

    public void setExpectedOutcome(
            AgateExpectedOutcome expectedOutcome) {

        this.expectedOutcome =
                expectedOutcome;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason) {

        this.reason = reason;
    }


    public String getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(
            String expectedStatusCode) {

        this.expectedStatusCode =
                expectedStatusCode;
    }


    public AgateRequestValues getRequestValues() {
        return requestValues;
    }

    public void setRequestValues(
            AgateRequestValues requestValues) {

        this.requestValues =
                requestValues;
    }
}