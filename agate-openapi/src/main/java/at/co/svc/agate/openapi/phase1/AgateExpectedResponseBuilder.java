package at.co.svc.agate.openapi.phase1;

import at.co.svc.agate.openapi.phase1.model.AgateExpectedResponseContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateExpectedResponseModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseModel;

public class AgateExpectedResponseBuilder {


    public AgateExpectedResponseModel build(
            AgateOperationModel operation,
            String statusCode) {

        return build(
                operation,
                statusCode,
                null
        );
    }


    public AgateExpectedResponseModel build(
            AgateOperationModel operation,
            String statusCode,
            String mediaType) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }

        if (statusCode == null ||
                statusCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Status code must not be empty"
            );
        }

        AgateResponseModel response =
                findResponse(
                        operation,
                        statusCode
                );

        boolean defaultResponse =
                false;

        if (response == null) {

            response =
                    findResponse(
                            operation,
                            "default"
                    );

            defaultResponse =
                    response != null;
        }

        if (response == null) {

            throw new IllegalArgumentException(
                    "Response not defined for status code: "
                            + statusCode
            );
        }

        AgateExpectedResponseModel result =
                new AgateExpectedResponseModel();

        result.setRequestedStatusCode(
                statusCode
        );

        result.setResolvedStatusCode(
                response.getStatusCode()
        );

        result.setDefaultResponse(
                defaultResponse
        );

        result.setDescription(
                response.getDescription()
        );

        result.setSourceRef(
                response.getSourceRef()
        );

        result.setContent(
                resolveContent(
                        response,
                        mediaType
                )
        );

        return result;
    }


    private AgateResponseModel findResponse(
            AgateOperationModel operation,
            String statusCode) {

        if (operation.getResponses() == null) {
            return null;
        }

        return operation
                .getResponses()
                .stream()
                .filter(response ->
                        response != null
                )
                .filter(response ->
                        statusCode.equals(
                                response.getStatusCode()
                        )
                )
                .findFirst()
                .orElse(null);
    }


    private AgateExpectedResponseContentModel resolveContent(
            AgateResponseModel response,
            String requestedMediaType) {

        if (response.getContents() == null ||
                response.getContents().isEmpty()) {

            return null;
        }

        AgateResponseContentModel content;

        if (requestedMediaType != null &&
                !requestedMediaType.isBlank()) {

            content =
                    response
                            .getContents()
                            .stream()
                            .filter(value ->
                                    requestedMediaType.equals(
                                            value.getMediaType()
                                    )
                            )
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Media type "
                                                            + requestedMediaType
                                                            + " not defined for response "
                                                            + response.getStatusCode()
                                            )
                            );

        } else {

            content =
                    response
                            .getContents()
                            .get(0);
        }

        AgateExpectedResponseContentModel result =
                new AgateExpectedResponseContentModel();

        result.setMediaType(
                content.getMediaType()
        );

        result.setExample(
                content.getExample()
        );

        result.setExamples(
                content.getExamples()
        );

        result.setSchema(
                content.getSchema()
        );

        return result;
    }
}