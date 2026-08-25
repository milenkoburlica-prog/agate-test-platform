package at.co.svc.agate.openapi.impact.analysis.contract;

import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import java.util.List;


public class AgateContractSchemaResolver {


    public ResolvedContractField resolve(
            AgateOperationModel operation,
            String location) {

        if (operation == null ||
                location == null) {

            return null;
        }


        if (location.startsWith(
                "request.body."
        )) {

            return resolveBodyField(
                    operation,
                    location.substring(
                            "request.body.".length()
                    )
            );
        }


        if (location.startsWith(
                "request.path."
        )) {

            return resolveParameter(
                    operation,
                    "path",
                    lastSegment(
                            location
                    )
            );
        }


        if (location.startsWith(
                "request.query."
        )) {

            return resolveParameter(
                    operation,
                    "query",
                    lastSegment(
                            location
                    )
            );
        }


        if (location.startsWith(
                "request.header."
        )) {

            return resolveParameter(
                    operation,
                    "header",
                    lastSegment(
                            location
                    )
            );
        }


        if (location.startsWith(
                "request.cookie."
        )) {

            return resolveParameter(
                    operation,
                    "cookie",
                    lastSegment(
                            location
                    )
            );
        }


        return null;
    }




    private ResolvedContractField resolveBodyField(
            AgateOperationModel operation,
            String fieldPath) {

        AgateSchema schema =
                selectBodySchema(
                        operation
                );


        if (schema == null) {

            return null;
        }


        String[] parts =
                fieldPath.split(
                        "\\."
                );


        AgateSchema current =
                schema;


        boolean required =
                false;


        for (String part :
                parts) {

            if ("array".equals(
                    current.getType()
            )) {

                current =
                        current.getItems();


                if (current == null) {

                    return null;
                }
            }


            if (current.getProperties() == null) {

                return null;
            }


            required =
                    current.getRequired() != null
                            &&
                            current
                                    .getRequired()
                                    .contains(
                                            part
                                    );


            current =
                    current
                            .getProperties()
                            .get(
                                    part
                            );


            if (current == null) {

                return null;
            }
        }


        return new ResolvedContractField(
                fieldPath,
                current,
                required
        );
    }




    private ResolvedContractField resolveParameter(
            AgateOperationModel operation,
            String location,
            String name) {

        AgateRequestModel request =
                operation.getRequest();


        if (request == null) {

            return null;
        }


        List<AgateRequestParameterModel> parameters =
                switch (
                        location
                ) {

                    case "path" ->
                            request.getPathParameters();

                    case "query" ->
                            request.getQueryParameters();

                    case "header" ->
                            request.getHeaderParameters();

                    case "cookie" ->
                            request.getCookieParameters();

                    default ->
                            null;
                };


        if (parameters == null) {

            return null;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            if (name.equals(
                    parameter.getName()
            )) {

                return new ResolvedContractField(
                        name,
                        parameter.getSchema(),
                        parameter.isRequired()
                );
            }
        }


        return null;
    }




    private AgateSchema selectBodySchema(
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


        return content.getSchema();
    }




    private String lastSegment(
            String value) {

        int index =
                value.lastIndexOf(
                        '.'
                );


        return index >= 0
                ? value.substring(
                        index + 1
                )
                : value;
    }




    public static class ResolvedContractField {


        private final String path;

        private final AgateSchema schema;

        private final boolean required;




        public ResolvedContractField(
                String path,
                AgateSchema schema,
                boolean required) {

            this.path =
                    path;


            this.schema =
                    schema;


            this.required =
                    required;
        }




        public String getPath() {

            return path;
        }




        public AgateSchema getSchema() {

            return schema;
        }




        public boolean isRequired() {

            return required;
        }
    }
}