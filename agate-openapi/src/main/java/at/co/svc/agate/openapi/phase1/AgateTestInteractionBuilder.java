package at.co.svc.agate.openapi.phase1;

import at.co.svc.agate.openapi.phase1.model.AgateExpectedResponseModel;
import at.co.svc.agate.openapi.phase1.model.AgateHttpRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;
import at.co.svc.agate.openapi.phase1.model.AgateTestInteractionModel;

public class AgateTestInteractionBuilder {

    private final AgateHttpRequestBuilder requestBuilder =
            new AgateHttpRequestBuilder();

    private final AgateExpectedResponseBuilder responseBuilder =
            new AgateExpectedResponseBuilder();


    public AgateTestInteractionModel build(
            AgateOperationModel operation,
            AgateRequestValues requestValues,
            String expectedStatusCode) {

        return build(
                operation,
                requestValues,
                expectedStatusCode,
                null
        );
    }


    public AgateTestInteractionModel build(
            AgateOperationModel operation,
            AgateRequestValues requestValues,
            String expectedStatusCode,
            String expectedMediaType) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }

        AgateHttpRequestModel request =
                requestBuilder.build(
                        operation,
                        requestValues
                );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        expectedStatusCode,
                        expectedMediaType
                );

        AgateTestInteractionModel result =
                new AgateTestInteractionModel();

        result.setIdentity(
                operation.getIdentity()
        );

        result.setRequest(
                request
        );

        result.setExpectedResponse(
                response
        );

        return result;
    }
}