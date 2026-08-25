package at.co.svc.agate.openapi.phase1.model;

public class AgateTestInteractionModel {

    private String identity;

    private AgateHttpRequestModel request;

    private AgateExpectedResponseModel expectedResponse;


    public String getIdentity() {
        return identity;
    }

    public void setIdentity(
            String identity) {

        this.identity = identity;
    }


    public AgateHttpRequestModel getRequest() {
        return request;
    }

    public void setRequest(
            AgateHttpRequestModel request) {

        this.request = request;
    }


    public AgateExpectedResponseModel getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(
            AgateExpectedResponseModel expectedResponse) {

        this.expectedResponse = expectedResponse;
    }
}