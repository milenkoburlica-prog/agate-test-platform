package at.co.svc.agate.openapi.phase3.template;

import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import java.util.List;
import java.util.Map;


public class AgateYamlTemplateGenerator {


    private static final String INDENT =
            "  ";


    private final AgateTemplateNameBuilder nameBuilder =
            new AgateTemplateNameBuilder();




    public String generate(
            AgateOperationModel operation) {

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


        StringBuilder yaml =
                new StringBuilder();


        appendLine(
                yaml,
                0,
                "testCases:"
        );


        appendBlank(
                yaml
        );


        appendLine(
                yaml,
                1,
                "- id: \"{XL[testcaseName]}\""
        );


        appendLine(
                yaml,
                2,
                "description: \"{XL[testcaseDescription]}\""
        );


        appendLine(
                yaml,
                2,
                "stage: \"*\""
        );


        appendLine(
                yaml,
                2,
                "priority: HIGH"
        );


        appendBlank(
                yaml
        );


        appendLine(
                yaml,
                2,
                "variables:"
        );


        appendBlank(
                yaml
        );


        appendLine(
                yaml,
                3,
                "apiEndpoint: \"{XL[apiEndpoint]}\""
        );


        appendBlank(
                yaml
        );


        appendLine(
                yaml,
                2,
                "steps:"
        );


        appendBlank(
                yaml
        );


        appendExecStep(
                yaml,
                operation
        );


        appendBlank(
                yaml
        );


        appendAssertStep(
                yaml
        );


        return yaml.toString();
    }




    private void appendExecStep(
            StringBuilder yaml,
            AgateOperationModel operation) {

        appendLine(
                yaml,
                3,
                "- type: REST"
        );


        appendLine(
                yaml,
                4,
                "op: EXEC"
        );


        appendLine(
                yaml,
                4,
                "command: "
                        + nameBuilder.buildRestCommand(
                                operation.getMethod(),
                                operation.getPath()
                        )
        );


        appendLine(
                yaml,
                4,
                "endpoint: \"{B[apiEndpoint]}\""
        );


        /*
         * Request parameters.
         *
         * Path/query/header/cookie values are also available
         * as XL variables in the CSV.
         *
         * We put them directly into REST EXEC, following
         * the same convention as body fields.
         */

        appendParameters(
                yaml,
                operation.getRequest()
        );


        /*
         * Request body.
         */

        appendRequestBody(
                yaml,
                operation.getRequest()
        );


        appendLine(
                yaml,
                4,
                "response: response_1"
        );
    }




    private void appendParameters(
            StringBuilder yaml,
            AgateRequestModel request) {

        appendParameterList(
                yaml,
                request.getPathParameters()
        );


        appendParameterList(
                yaml,
                request.getQueryParameters()
        );


        appendParameterList(
                yaml,
                request.getHeaderParameters()
        );


        appendParameterList(
                yaml,
                request.getCookieParameters()
        );
    }




    private void appendParameterList(
            StringBuilder yaml,
            List<AgateRequestParameterModel> parameters) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            if (parameter == null ||
                    parameter.getName() == null ||
                    parameter.getName().isBlank()) {

                continue;
            }


            appendLine(
                    yaml,
                    4,
                    parameter.getName()
                            + ": \"{XL["
                            + parameter.getName()
                            + "]}\""
            );
        }
    }




    private void appendRequestBody(
            StringBuilder yaml,
            AgateRequestModel request) {

        AgateSchema bodySchema =
                selectBodySchema(
                        request.getBody()
                );


        if (bodySchema == null) {

            return;
        }


        if ("object".equals(
                bodySchema.getType()
        )) {

            appendObjectProperties(
                    yaml,
                    bodySchema,
                    "",
                    4
            );


            return;
        }


        /*
         * Fallback for non-object body.
         */

        appendLine(
                yaml,
                4,
                "body: \"{XL[body]}\""
        );
    }




    private void appendObjectProperties(
            StringBuilder yaml,
            AgateSchema objectSchema,
            String csvPath,
            int yamlLevel) {

        if (objectSchema == null ||
                objectSchema.getProperties() == null) {

            return;
        }


        for (Map.Entry<String, AgateSchema> entry :
                objectSchema
                        .getProperties()
                        .entrySet()) {

            String propertyName =
                    entry.getKey();


            AgateSchema propertySchema =
                    entry.getValue();


            if (propertySchema == null) {

                continue;
            }


            if (propertySchema.isReadOnly()) {

                continue;
            }


            String propertyCsvPath =
                    csvPath == null ||
                            csvPath.isBlank()
                            ? propertyName
                            : csvPath
                            + "."
                            + propertyName;


            appendSchemaProperty(
                    yaml,
                    propertyName,
                    propertyCsvPath,
                    propertySchema,
                    yamlLevel
            );
        }
    }




    private void appendSchemaProperty(
            StringBuilder yaml,
            String propertyName,
            String csvPath,
            AgateSchema schema,
            int yamlLevel) {

        String type =
                schema.getType();


        /*
         * -------------------------------------------------
         * OBJECT
         * -------------------------------------------------
         */

        if ("object".equals(type)) {

            appendLine(
                    yaml,
                    yamlLevel,
                    propertyName
                            + ":"
            );


            appendObjectProperties(
                    yaml,
                    schema,
                    csvPath,
                    yamlLevel + 1
            );


            return;
        }


        /*
         * -------------------------------------------------
         * ARRAY
         * -------------------------------------------------
         */

        if ("array".equals(type)) {

            appendArrayProperty(
                    yaml,
                    propertyName,
                    csvPath,
                    schema,
                    yamlLevel
            );


            return;
        }


        /*
         * -------------------------------------------------
         * SCALAR
         * -------------------------------------------------
         */

        appendLine(
                yaml,
                yamlLevel,
                propertyName
                        + ": \"{XL["
                        + csvPath
                        + "]}\""
        );
    }




    private void appendArrayProperty(
            StringBuilder yaml,
            String propertyName,
            String csvPath,
            AgateSchema arraySchema,
            int yamlLevel) {

        AgateSchema itemSchema =
                arraySchema.getItems();


        /*
         * Array<Object>
         *
         * Example:
         *
         * tags:
         *   - id: "{XL[tags.id]}"
         *     name: "{XL[tags.name]}"
         */

        if (itemSchema != null &&
                "object".equals(
                        itemSchema.getType()
                )) {

            appendLine(
                    yaml,
                    yamlLevel,
                    propertyName
                            + ":"
            );


            appendObjectArrayItem(
                    yaml,
                    itemSchema,
                    csvPath,
                    yamlLevel + 1
            );


            return;
        }


        /*
         * Primitive array:
         *
         * photoUrls: "{XL[photoUrls]}"
         */

        appendLine(
                yaml,
                yamlLevel,
                propertyName
                        + ": \"{XL["
                        + csvPath
                        + "]}\""
        );
    }




    private void appendObjectArrayItem(
            StringBuilder yaml,
            AgateSchema itemSchema,
            String csvPath,
            int yamlLevel) {

        if (itemSchema.getProperties() == null ||
                itemSchema.getProperties().isEmpty()) {

            appendLine(
                    yaml,
                    yamlLevel,
                    "- \"{XL["
                            + csvPath
                            + "]}\""
            );


            return;
        }


        boolean first =
                true;


        for (Map.Entry<String, AgateSchema> entry :
                itemSchema
                        .getProperties()
                        .entrySet()) {

            String childName =
                    entry.getKey();


            AgateSchema childSchema =
                    entry.getValue();


            if (childSchema == null ||
                    childSchema.isReadOnly()) {

                continue;
            }


            String childCsvPath =
                    csvPath
                            + "."
                            + childName;


            if (first) {

                appendLine(
                        yaml,
                        yamlLevel,
                        "- "
                                + childName
                                + ": \"{XL["
                                + childCsvPath
                                + "]}\""
                );


                first =
                        false;

            } else {

                appendLine(
                        yaml,
                        yamlLevel + 1,
                        childName
                                + ": \"{XL["
                                + childCsvPath
                                + "]}\""
                );
            }
        }
    }




    private void appendAssertStep(
            StringBuilder yaml) {

        appendLine(
                yaml,
                3,
                "- type: REST"
        );


        appendLine(
                yaml,
                4,
                "op: ASSERT"
        );


        appendLine(
                yaml,
                4,
                "response: response_1"
        );


        appendLine(
                yaml,
                4,
                "source: STATUS"
        );


        appendLine(
                yaml,
                4,
                "action: EQUALS"
        );


        appendLine(
                yaml,
                4,
                "expected: \"{XL[statusCode]}\""
        );
    }




    private AgateSchema selectBodySchema(
            AgateRequestBodyModel body) {

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




    private void appendLine(
            StringBuilder target,
            int level,
            String value) {

        target.append(
                INDENT.repeat(
                        Math.max(
                                level,
                                0
                        )
                )
        );


        target.append(
                value
        );


        target.append(
                System.lineSeparator()
        );
    }




    private void appendBlank(
            StringBuilder target) {

        target.append(
                System.lineSeparator()
        );
    }
}