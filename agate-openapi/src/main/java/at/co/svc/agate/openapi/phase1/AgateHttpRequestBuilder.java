package at.co.svc.agate.openapi.phase1;

import at.co.svc.agate.openapi.phase1.model.AgateHttpRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateHttpRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;
import at.co.svc.agate.openapi.phase1.serialization.AgateParameterSerializer;
import at.co.svc.agate.openapi.phase1.serialization.AgateSerializedParameter;

import java.util.List;
import java.util.Map;

public class AgateHttpRequestBuilder {

    private final AgateParameterSerializer parameterSerializer =
            new AgateParameterSerializer();


    public AgateHttpRequestModel build(
            AgateOperationModel operation,
            AgateRequestValues values) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }

        if (operation.getRequest() == null) {

            throw new IllegalArgumentException(
                    "Operation request must not be null"
            );
        }

        AgateRequestValues effectiveValues =
                values != null
                        ? values
                        : new AgateRequestValues();

        AgateRequestModel request =
                operation.getRequest();

        AgateHttpRequestModel result =
                new AgateHttpRequestModel();

        result.setMethod(
                request.getMethod()
        );

        result.setPath(
                buildPath(
                        request,
                        effectiveValues
                )
        );

        addQueryParameters(
                result,
                request,
                effectiveValues
        );

        addHeaders(
                result,
                request,
                effectiveValues
        );

        addCookies(
                result,
                request,
                effectiveValues
        );

        result.setBody(
                buildBody(
                        request,
                        effectiveValues
                )
        );

        return result;
    }


    private String buildPath(
            AgateRequestModel request,
            AgateRequestValues values) {

        String result =
                request.getPath();

        for (AgateRequestParameterModel parameter :
                request.getPathParameters()) {

            Object value =
                    values
                            .getPath()
                            .get(
                                    parameter.getName()
                            );

            validateRequired(
                    parameter,
                    value
            );

            if (value == null) {
                continue;
            }

            AgateSerializedParameter serialized =
                    parameterSerializer.serialize(
                            parameter,
                            value
                    );

            if (serialized.getValues().isEmpty()) {
                continue;
            }

            String replacement =
                    serialized
                            .getValues()
                            .get(0);

            String placeholder =
                    "{"
                            + parameter.getName()
                            + "}";

            result =
                    result.replace(
                            placeholder,
                            replacement
                    );
        }

        return result;
    }


    private void addQueryParameters(
            AgateHttpRequestModel target,
            AgateRequestModel request,
            AgateRequestValues values) {

        for (AgateRequestParameterModel parameter :
                request.getQueryParameters()) {

            Object value =
                    values
                            .getQuery()
                            .get(
                                    parameter.getName()
                            );

            validateRequired(
                    parameter,
                    value
            );

            if (value == null) {
                continue;
            }

            AgateSerializedParameter serialized =
                    parameterSerializer.serialize(
                            parameter,
                            value
                    );

            target.addQueryParameter(
                    serialized.getName(),
                    serialized.getValues()
            );
        }
    }


    private void addHeaders(
            AgateHttpRequestModel target,
            AgateRequestModel request,
            AgateRequestValues values) {

        for (AgateRequestParameterModel parameter :
                request.getHeaderParameters()) {

            Object value =
                    values
                            .getHeaders()
                            .get(
                                    parameter.getName()
                            );

            validateRequired(
                    parameter,
                    value
            );

            if (value == null) {
                continue;
            }

            AgateSerializedParameter serialized =
                    parameterSerializer.serialize(
                            parameter,
                            value
                    );

            target.addHeader(
                    serialized.getName(),
                    serialized.getValues()
            );
        }
    }


    private void addCookies(
            AgateHttpRequestModel target,
            AgateRequestModel request,
            AgateRequestValues values) {

        for (AgateRequestParameterModel parameter :
                request.getCookieParameters()) {

            Object value =
                    values
                            .getCookies()
                            .get(
                                    parameter.getName()
                            );

            validateRequired(
                    parameter,
                    value
            );

            if (value == null) {
                continue;
            }

            AgateSerializedParameter serialized =
                    parameterSerializer.serialize(
                            parameter,
                            value
                    );

            target.addCookie(
                    serialized.getName(),
                    serialized.getValues()
            );
        }
    }


    private AgateHttpRequestBodyModel buildBody(
            AgateRequestModel request,
            AgateRequestValues values) {

        AgateRequestBodyModel bodyModel =
                request.getBody();

        if (bodyModel == null) {

            return null;
        }

        Object bodyValue =
                values.getBody();

        if (bodyValue == null) {

            if (bodyModel.isRequired()) {

                throw new IllegalArgumentException(
                        "Request body is required"
                );
            }

            return null;
        }

        String mediaType =
                resolveMediaType(
                        bodyModel,
                        values.getBodyMediaType()
                );

        AgateHttpRequestBodyModel result =
                new AgateHttpRequestBodyModel();

        result.setMediaType(
                mediaType
        );

        result.setValue(
                bodyValue
        );

        return result;
    }


    private String resolveMediaType(
            AgateRequestBodyModel body,
            String requestedMediaType) {

        if (requestedMediaType != null &&
                !requestedMediaType.isBlank()) {

            boolean exists =
                    body
                            .getContents()
                            .stream()
                            .map(
                                    AgateRequestContentModel::getMediaType
                            )
                            .anyMatch(
                                    requestedMediaType::equals
                            );

            if (!exists) {

                throw new IllegalArgumentException(
                        "Unsupported request body media type: "
                                + requestedMediaType
                );
            }

            return requestedMediaType;
        }

        List<AgateRequestContentModel> contents =
                body.getContents();

        if (contents.isEmpty()) {

            return null;
        }

        return contents
                .get(0)
                .getMediaType();
    }


    private void validateRequired(
            AgateRequestParameterModel parameter,
            Object value) {

        if (parameter.isRequired() &&
                value == null) {

            throw new IllegalArgumentException(
                    "Required parameter missing: "
                            + parameter.getLocation()
                            + " "
                            + parameter.getName()
            );
        }
    }
}