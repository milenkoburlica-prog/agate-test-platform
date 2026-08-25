package at.co.svc.agate.openapi.phase1.cli;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateExpectedResponseBuilder;
import at.co.svc.agate.openapi.phase1.AgateHttpRequestBuilder;
import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;

import at.co.svc.agate.openapi.phase1.model.AgateExpectedResponseModel;
import at.co.svc.agate.openapi.phase1.model.AgateHttpRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;
import at.co.svc.agate.openapi.phase1.model.AgateResponseContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgatePhase1Cli {

    public static void main(
            String[] args) {

        if (args.length < 3) {

            printUsage();

            System.exit(1);
        }

        String source =
                args[0];

        String method =
                args[1]
                        .toUpperCase();

        String path =
                args[2];

        String statusCode =
                null;

        String expectedMediaType =
                null;

        AgateRequestValues requestValues =
                new AgateRequestValues();

        try {

            int index =
                    3;

            if (index < args.length &&
                    !args[index].startsWith("--")) {

                statusCode =
                        args[index];

                index++;
            }

            if (index < args.length &&
                    !args[index].startsWith("--")) {

                expectedMediaType =
                        args[index];

                index++;
            }

            while (index < args.length) {

                String option =
                        args[index];

                if ("--path-param".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    addSingleValue(
                            requestValues.getPath(),
                            args[index]
                    );

                } else if ("--query".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    addMultiValue(
                            requestValues.getQuery(),
                            args[index]
                    );

                } else if ("--header".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    addMultiValue(
                            requestValues.getHeaders(),
                            args[index]
                    );

                } else if ("--cookie".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    addMultiValue(
                            requestValues.getCookies(),
                            args[index]
                    );

                } else if ("--body-media-type".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    requestValues.setBodyMediaType(
                            args[index]
                    );

                } else if ("--body".equals(option)) {

                    index =
                            requireValue(
                                    args,
                                    index,
                                    option
                            );

                    requestValues.setBody(
                            args[index]
                    );

                } else {

                    throw new IllegalArgumentException(
                            "Unknown option: "
                                    + option
                    );
                }

                index++;
            }


            AgateOpenApiParser parser =
                    new AgateOpenApiParser();

            AgateOpenApiModel openApiModel =
                    parser.parse(
                            source
                    );

            AgateEndpoint endpoint =
                    findEndpoint(
                            openApiModel,
                            method,
                            path
                    );


            AgateOperationModelBuilder operationBuilder =
                    new AgateOperationModelBuilder();

            AgateOperationModel operation =
                    operationBuilder.build(
                            endpoint
                    );


            printOperation(
                    operation
            );


            boolean hasRequestValues =
                    hasRequestValues(
                            requestValues
                    );

            if (hasRequestValues) {

                AgateHttpRequestBuilder requestBuilder =
                        new AgateHttpRequestBuilder();

                AgateHttpRequestModel request =
                        requestBuilder.build(
                                operation,
                                requestValues
                        );

                printHttpRequest(
                        request
                );
            }


            if (statusCode != null) {

                AgateExpectedResponseBuilder responseBuilder =
                        new AgateExpectedResponseBuilder();

                AgateExpectedResponseModel expectedResponse =
                        responseBuilder.build(
                                operation,
                                statusCode,
                                expectedMediaType
                        );

                printExpectedResponse(
                        expectedResponse
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.exit(2);
        }
    }


    private static int requireValue(
            String[] args,
            int index,
            String option) {

        int valueIndex =
                index + 1;

        if (valueIndex >= args.length) {

            throw new IllegalArgumentException(
                    "Missing value for "
                            + option
            );
        }

        return valueIndex;
    }


    private static void addSingleValue(
            Map<String, Object> target,
            String expression) {

        KeyValue value =
                parseKeyValue(
                        expression
                );

        target.put(
                value.name(),
                value.value()
        );
    }


    private static void addMultiValue(
            Map<String, Object> target,
            String expression) {

        KeyValue value =
                parseKeyValue(
                        expression
                );

        Object existing =
                target.get(
                        value.name()
                );

        if (existing == null) {

            target.put(
                    value.name(),
                    value.value()
            );

            return;
        }

        if (existing instanceof List<?> list) {

            List<Object> result =
                    new ArrayList<>(
                            list
                    );

            result.add(
                    value.value()
            );

            target.put(
                    value.name(),
                    result
            );

            return;
        }

        List<Object> result =
                new ArrayList<>();

        result.add(
                existing
        );

        result.add(
                value.value()
        );

        target.put(
                value.name(),
                result
        );
    }


    private static KeyValue parseKeyValue(
            String expression) {

        int separator =
                expression.indexOf('=');

        if (separator <= 0) {

            throw new IllegalArgumentException(
                    "Expected name=value but got: "
                            + expression
            );
        }

        String name =
                expression.substring(
                        0,
                        separator
                );

        String value =
                expression.substring(
                        separator + 1
                );

        return new KeyValue(
                name,
                value
        );
    }


    private static boolean hasRequestValues(
            AgateRequestValues values) {

        return !values.getPath().isEmpty()
                || !values.getQuery().isEmpty()
                || !values.getHeaders().isEmpty()
                || !values.getCookies().isEmpty()
                || values.getBody() != null;
    }


    private static AgateEndpoint findEndpoint(
            AgateOpenApiModel model,
            String method,
            String path) {

        return model
                .getEndpoints()
                .stream()
                .filter(endpoint ->
                        method.equalsIgnoreCase(
                                endpoint.getMethod()
                        )
                )
                .filter(endpoint ->
                        path.equals(
                                endpoint.getPath()
                        )
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Endpoint not found: "
                                                + method
                                                + " "
                                                + path
                                )
                );
    }


    private static void printOperation(
            AgateOperationModel operation) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "AGATE PHASE 1"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "identity    : "
                        + operation.getIdentity()
        );

        System.out.println(
                "method      : "
                        + operation.getMethod()
        );

        System.out.println(
                "path        : "
                        + operation.getPath()
        );

        System.out.println(
                "operationId : "
                        + operation.getOperationId()
        );

        if (operation.getSummary() != null) {

            System.out.println(
                    "summary     : "
                            + operation.getSummary()
            );
        }

        if (operation.getDescription() != null) {

            System.out.println(
                    "description : "
                            + operation.getDescription()
            );
        }

        if (operation.isDeprecated()) {

            System.out.println(
                    "deprecated  : true"
            );
        }

        if (!operation.getTags().isEmpty()) {

            System.out.println(
                    "tags        : "
                            + operation.getTags()
            );
        }

        System.out.println();

        printRequest(
                operation.getRequest()
        );

        printResponses(
                operation.getResponses()
        );
    }


    private static void printRequest(
            AgateRequestModel request) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "REQUEST MODEL"
        );

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "method = "
                        + request.getMethod()
        );

        System.out.println(
                "path   = "
                        + request.getPath()
        );

        printParameterGroup(
                "pathParameters",
                request.getPathParameters()
        );

        printParameterGroup(
                "queryParameters",
                request.getQueryParameters()
        );

        printParameterGroup(
                "headerParameters",
                request.getHeaderParameters()
        );

        printParameterGroup(
                "cookieParameters",
                request.getCookieParameters()
        );

        printRequestBody(
                request.getBody()
        );

        System.out.println();
    }


    private static void printParameterGroup(
            String name,
            List<AgateRequestParameterModel> parameters) {

        System.out.println(
                name + ":"
        );

        if (parameters == null ||
                parameters.isEmpty()) {

            System.out.println(
                    "  <none>"
            );

            return;
        }

        for (AgateRequestParameterModel parameter :
                parameters) {

            System.out.println(
                    "  "
                            + parameter.getName()
            );

            System.out.println(
                    "    location="
                            + parameter.getLocation()
            );

            System.out.println(
                    "    required="
                            + parameter.isRequired()
            );

            if (parameter.getStyle() != null) {

                System.out.println(
                        "    style="
                                + parameter.getStyle()
                );
            }

            if (parameter.getExplode() != null) {

                System.out.println(
                        "    explode="
                                + parameter.getExplode()
                );
            }

            if (parameter.getAllowEmptyValue() != null) {

                System.out.println(
                        "    allowEmptyValue="
                                + parameter.getAllowEmptyValue()
                );
            }

            if (parameter.getAllowReserved() != null) {

                System.out.println(
                        "    allowReserved="
                                + parameter.getAllowReserved()
                );
            }

            if (parameter.getExample() != null) {

                System.out.println(
                        "    example="
                                + parameter.getExample()
                );
            }

            if (!parameter.getExamples().isEmpty()) {

                System.out.println(
                        "    examples="
                                + parameter.getExamples()
                );
            }

            if (parameter.getSchema() != null) {

                System.out.println(
                        "    type="
                                + parameter
                                        .getSchema()
                                        .getType()
                );

                if (parameter
                        .getSchema()
                        .getFormat() != null) {

                    System.out.println(
                            "    format="
                                    + parameter
                                            .getSchema()
                                            .getFormat()
                    );
                }

                if (parameter
                        .getSchema()
                        .getItems() != null) {

                    System.out.println(
                            "    itemsType="
                                    + parameter
                                            .getSchema()
                                            .getItems()
                                            .getType()
                    );
                }
            }
        }
    }


    private static void printRequestBody(
            AgateRequestBodyModel body) {

        System.out.println(
                "body:"
        );

        if (body == null) {

            System.out.println(
                    "  <none>"
            );

            return;
        }

        System.out.println(
                "  required="
                        + body.isRequired()
        );

        if (body.getDescription() != null) {

            System.out.println(
                    "  description="
                            + body.getDescription()
            );
        }

        for (AgateRequestContentModel content :
                body.getContents()) {

            System.out.println(
                    "  mediaType="
                            + content.getMediaType()
            );

            if (content.getExample() != null) {

                System.out.println(
                        "    example="
                                + content.getExample()
                );
            }

            if (!content.getExamples().isEmpty()) {

                System.out.println(
                        "    examples="
                                + content.getExamples()
                );
            }

            if (content.getSchema() != null) {

                System.out.println(
                        "    schemaType="
                                + content
                                        .getSchema()
                                        .getType()
                );
            }
        }
    }


    private static void printResponses(
            List<AgateResponseModel> responses) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                "RESPONSES"
        );

        System.out.println(
                "----------------------------------------"
        );

        if (responses == null ||
                responses.isEmpty()) {

            System.out.println(
                    "<none>"
            );

            return;
        }

        for (AgateResponseModel response :
                responses) {

            System.out.println(
                    response.getStatusCode()
                            + " "
                            + response.getDescription()
            );

            if (response.getContents() == null ||
                    response.getContents().isEmpty()) {

                System.out.println(
                        "  <no content>"
                );

                continue;
            }

            for (AgateResponseContentModel content :
                    response.getContents()) {

                System.out.println(
                        "  mediaType="
                                + content.getMediaType()
                );

                if (content.getExample() != null) {

                    System.out.println(
                            "    example="
                                    + content.getExample()
                    );
                }

                if (!content.getExamples().isEmpty()) {

                    System.out.println(
                            "    examples="
                                    + content.getExamples()
                    );
                }

                if (content.getSchema() != null) {

                    System.out.println(
                            "    schemaType="
                                    + content
                                            .getSchema()
                                            .getType()
                    );
                }
            }
        }

        System.out.println();
    }


    private static void printHttpRequest(
            AgateHttpRequestModel request) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "CONCRETE HTTP REQUEST"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "method="
                        + request.getMethod()
        );

        System.out.println(
                "path="
                        + request.getPath()
        );

        printValues(
                "query",
                request.getQueryParameters()
        );

        printValues(
                "headers",
                request.getHeaders()
        );

        printValues(
                "cookies",
                request.getCookies()
        );

        if (request.getBody() == null) {

            System.out.println(
                    "body=<none>"
            );

        } else {

            System.out.println(
                    "bodyMediaType="
                            + request
                                    .getBody()
                                    .getMediaType()
            );

            System.out.println(
                    "body="
                            + request
                                    .getBody()
                                    .getValue()
            );
        }

        System.out.println();
    }


    private static void printValues(
            String name,
            Map<String, List<String>> values) {

        System.out.println(
                name + ":"
        );

        if (values == null ||
                values.isEmpty()) {

            System.out.println(
                    "  <none>"
            );

            return;
        }

        values.forEach(
                (key, entries) -> {

                    if (entries == null ||
                            entries.isEmpty()) {

                        System.out.println(
                                "  "
                                        + key
                                        + "=<empty>"
                        );

                        return;
                    }

                    for (String value :
                            entries) {

                        System.out.println(
                                "  "
                                        + key
                                        + "="
                                        + value
                        );
                    }
                }
        );
    }


    private static void printExpectedResponse(
            AgateExpectedResponseModel response) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "EXPECTED RESPONSE"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "requestedStatusCode="
                        + response.getRequestedStatusCode()
        );

        System.out.println(
                "resolvedStatusCode="
                        + response.getResolvedStatusCode()
        );

        System.out.println(
                "defaultResponse="
                        + response.isDefaultResponse()
        );

        System.out.println(
                "description="
                        + response.getDescription()
        );

        if (response.getContent() == null) {

            System.out.println(
                    "content=<none>"
            );

            return;
        }

        System.out.println(
                "mediaType="
                        + response
                                .getContent()
                                .getMediaType()
        );

        if (response
                .getContent()
                .getExample() != null) {

            System.out.println(
                    "example="
                            + response
                                    .getContent()
                                    .getExample()
            );
        }

        if (!response
                .getContent()
                .getExamples()
                .isEmpty()) {

            System.out.println(
                    "examples="
                            + response
                                    .getContent()
                                    .getExamples()
            );
        }

        if (response
                .getContent()
                .getSchema() != null) {

            System.out.println(
                    "schemaType="
                            + response
                                    .getContent()
                                    .getSchema()
                                    .getType()
            );
        }
    }


    private static void printUsage() {

        System.err.println(
                "Usage:"
        );

        System.err.println(
                "  AgatePhase1Cli "
                        + "<openapi-source> "
                        + "<METHOD> "
                        + "<PATH> "
                        + "[STATUS] "
                        + "[MEDIA-TYPE] "
                        + "[options]"
        );

        System.err.println();

        System.err.println(
                "Options:"
        );

        System.err.println(
                "  --path-param name=value"
        );

        System.err.println(
                "  --query name=value"
        );

        System.err.println(
                "  --header name=value"
        );

        System.err.println(
                "  --cookie name=value"
        );

        System.err.println(
                "  --body-media-type application/json"
        );

        System.err.println(
                "  --body value"
        );

        System.err.println();

        System.err.println(
                "Example:"
        );

        System.err.println(
                "  <openapi-source> GET /readersUrl 200 application/json "
                        + "--query groups=Gruppe-1 "
                        + "--query groups=Gruppe-2"
        );
    }


    private record KeyValue(
            String name,
            String value) {
    }
}