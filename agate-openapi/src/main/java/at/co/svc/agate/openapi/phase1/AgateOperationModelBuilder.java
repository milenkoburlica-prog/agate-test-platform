package at.co.svc.agate.openapi.phase1;

import at.co.svc.agate.openapi.model.AgateContent;
import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateParameter;
import at.co.svc.agate.openapi.model.AgateRequestBody;
import at.co.svc.agate.openapi.model.AgateResponse;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseModel;

public class AgateOperationModelBuilder {


    public AgateOperationModel build(
            AgateEndpoint endpoint) {

        if (endpoint == null) {

            throw new IllegalArgumentException(
                    "Endpoint must not be null"
            );
        }

        AgateOperationModel result =
                new AgateOperationModel();

        result.setIdentity(
                endpoint.getIdentity()
        );

        result.setMethod(
                endpoint.getMethod()
        );

        result.setPath(
                endpoint.getPath()
        );

        result.setOperationId(
                endpoint.getOperationId()
        );

        result.setSummary(
                endpoint.getSummary()
        );

        result.setDescription(
                endpoint.getDescription()
        );

        result.setDeprecated(
                endpoint.isDeprecated()
        );

        result.setTags(
                endpoint.getTags()
        );

        result.setRequest(
                buildRequest(
                        endpoint
                )
        );

        for (AgateResponse response :
                endpoint.getResponses()) {

            result.addResponse(
                    buildResponse(
                            response
                    )
            );
        }

        return result;
    }


    private AgateRequestModel buildRequest(
            AgateEndpoint endpoint) {

        AgateRequestModel result =
                new AgateRequestModel();

        result.setMethod(
                endpoint.getMethod()
        );

        result.setPath(
                endpoint.getPath()
        );

        for (AgateParameter parameter :
                endpoint.getParameters()) {

            addParameter(
                    result,
                    buildParameter(
                            parameter
                    )
            );
        }

        result.setBody(
                buildRequestBody(
                        endpoint.getRequestBody()
                )
        );

        return result;
    }


    private AgateRequestParameterModel buildParameter(
            AgateParameter parameter) {

        if (parameter == null) {
            return null;
        }

        AgateRequestParameterModel result =
                new AgateRequestParameterModel();

        result.setName(
                parameter.getName()
        );

        result.setLocation(
                parameter.getLocation()
        );

        result.setRequired(
                parameter.isRequired()
        );

        result.setDescription(
                parameter.getDescription()
        );

        result.setStyle(
                parameter.getStyle()
        );

        result.setExplode(
                parameter.getExplode()
        );

        result.setAllowEmptyValue(
                parameter.getAllowEmptyValue()
        );

        result.setAllowReserved(
                parameter.getAllowReserved()
        );

        result.setExample(
                parameter.getExample()
        );

        result.setExamples(
                parameter.getExamples()
        );

        result.setSchema(
                parameter.getSchema()
        );

        return result;
    }


    private void addParameter(
            AgateRequestModel request,
            AgateRequestParameterModel parameter) {

        if (parameter == null) {
            return;
        }

        String location =
                parameter.getLocation();

        if ("path".equals(location)) {

            request.addPathParameter(
                    parameter
            );

            return;
        }

        if ("query".equals(location)) {

            request.addQueryParameter(
                    parameter
            );

            return;
        }

        if ("header".equals(location)) {

            request.addHeaderParameter(
                    parameter
            );

            return;
        }

        if ("cookie".equals(location)) {

            request.addCookieParameter(
                    parameter
            );
        }
    }


    private AgateRequestBodyModel buildRequestBody(
            AgateRequestBody requestBody) {

        if (requestBody == null) {
            return null;
        }

        AgateRequestBodyModel result =
                new AgateRequestBodyModel();

        result.setRequired(
                requestBody.isRequired()
        );

        result.setDescription(
                requestBody.getDescription()
        );

        result.setSourceRef(
                requestBody.getSourceRef()
        );

        for (AgateContent content :
                requestBody.getContents()) {

            result.addContent(
                    buildRequestContent(
                            content
                    )
            );
        }

        return result;
    }


    private AgateRequestContentModel buildRequestContent(
            AgateContent content) {

        if (content == null) {
            return null;
        }

        AgateRequestContentModel result =
                new AgateRequestContentModel();

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


    private AgateResponseModel buildResponse(
            AgateResponse response) {

        if (response == null) {
            return null;
        }

        AgateResponseModel result =
                new AgateResponseModel();

        result.setStatusCode(
                response.getStatusCode()
        );

        result.setDescription(
                response.getDescription()
        );

        result.setSourceRef(
                response.getSourceRef()
        );

        for (AgateContent content :
                response.getContents()) {

            result.addContent(
                    buildResponseContent(
                            content
                    )
            );
        }

        return result;
    }


    private AgateResponseContentModel buildResponseContent(
            AgateContent content) {

        if (content == null) {
            return null;
        }

        AgateResponseContentModel result =
                new AgateResponseContentModel();

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