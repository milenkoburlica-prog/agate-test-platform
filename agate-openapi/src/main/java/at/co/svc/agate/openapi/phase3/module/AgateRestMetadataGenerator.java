package at.co.svc.agate.openapi.phase3.module;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;


public class AgateRestMetadataGenerator {


    public String generate(
            AgateOperationModel operation) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        StringBuilder json =
                new StringBuilder();


        json.append("{")
                .append(System.lineSeparator());


        json.append("  \"url\" : \"{{endpoint}}")
                .append(escapeJson(
                        operation.getPath()
                ))
                .append("\",")
                .append(System.lineSeparator());


        json.append("  \"method\" : \"")
                .append(
                        escapeJson(
                                operation.getMethod()
                        )
                )
                .append("\",")
                .append(System.lineSeparator());


        json.append("  \"headers\" : {")
                .append(System.lineSeparator());


        String requestMediaType =
                determineRequestMediaType(
                        operation
                );


        /*
         * For operations with request body we add Content-Type.
         */

        if (requestMediaType != null) {

            json.append(
                            "    \"Content-Type\" : \""
                    )
                    .append(
                            escapeJson(
                                    createContentType(
                                            requestMediaType
                                    )
                            )
                    )
                    .append("\",")
                    .append(
                            System.lineSeparator()
                    );
        }


        /*
         * For Phase 3 we use JSON as deterministic
         * response representation.
         *
         * We can later derive this more precisely from
         * the selected OpenAPI response content.
         */

        json.append(
                        "    \"accept\" : \"application/json\""
                )
                .append(
                        System.lineSeparator()
                );


        json.append("  }")
                .append(System.lineSeparator());


        json.append("}")
                .append(System.lineSeparator());


        return json.toString();
    }




    private String determineRequestMediaType(
            AgateOperationModel operation) {

        if (operation.getRequest() == null) {

            return null;
        }


        AgateRequestBodyModel body =
                operation
                        .getRequest()
                        .getBody();


        if (body == null ||
                body.getContents() == null ||
                body.getContents().isEmpty()) {

            return null;
        }


        AgateRequestContentModel content =
                body
                        .getContents()
                        .stream()
                        .filter(value ->
                                "application/json".equals(
                                        value.getMediaType()
                                )
                        )
                        .findFirst()
                        .orElse(
                                body
                                        .getContents()
                                        .get(0)
                        );


        return content.getMediaType();
    }




    private String createContentType(
            String mediaType) {

        if (mediaType == null) {

            return null;
        }


        if ("application/json".equalsIgnoreCase(
                mediaType
        )) {

            return "application/json;charset=UTF-8";
        }


        return mediaType;
    }




    private String escapeJson(
            String value) {

        if (value == null) {

            return "";
        }


        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }
}