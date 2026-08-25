package at.co.svc.agate.openapi.phase3.module;

import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AgateRestRequestGenerator {


    private static final String INDENT =
            "  ";




    public String generate(
            AgateOperationModel operation) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        AgateSchema schema =
                selectRequestSchema(
                        operation
                );


        if (schema == null) {

            return null;
        }


        StringBuilder json =
                new StringBuilder();


        appendSchemaValue(
                json,
                schema,
                "",
                0
        );


        json.append(
                System.lineSeparator()
        );


        return json.toString();
    }




    private AgateSchema selectRequestSchema(
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




    private void appendSchemaValue(
            StringBuilder target,
            AgateSchema schema,
            String bufferPath,
            int level) {

        if (schema == null) {

            appendQuotedBuffer(
                    target,
                    bufferPath
            );

            return;
        }


        String type =
                schema.getType();


        if ("object".equals(type)) {

            appendObject(
                    target,
                    schema,
                    bufferPath,
                    level
            );

            return;
        }


        if ("array".equals(type)) {

            appendArray(
                    target,
                    schema,
                    bufferPath,
                    level
            );

            return;
        }


        appendScalar(
                target,
                schema,
                bufferPath
        );
    }




    private void appendObject(
            StringBuilder target,
            AgateSchema schema,
            String bufferPath,
            int level) {

        target.append("{");


        List<Map.Entry<String, AgateSchema>> properties =
                writableProperties(
                        schema
                );


        if (properties.isEmpty()) {

            target.append("}");

            return;
        }


        target.append(
                System.lineSeparator()
        );


        for (int i = 0;
             i < properties.size();
             i++) {

            Map.Entry<String, AgateSchema> entry =
                    properties.get(i);


            String propertyName =
                    entry.getKey();


            AgateSchema propertySchema =
                    entry.getValue();


            String propertyPath =
                    bufferPath == null ||
                            bufferPath.isBlank()
                            ? propertyName
                            : bufferPath
                            + "."
                            + propertyName;


            appendIndent(
                    target,
                    level + 1
            );


            target.append("\"")
                    .append(
                            escapeJson(
                                    propertyName
                            )
                    )
                    .append("\": ");


            appendSchemaValue(
                    target,
                    propertySchema,
                    propertyPath,
                    level + 1
            );


            if (i <
                    properties.size() - 1) {

                target.append(",");
            }


            target.append(
                    System.lineSeparator()
            );
        }


        appendIndent(
                target,
                level
        );


        target.append("}");
    }




    private void appendArray(
            StringBuilder target,
            AgateSchema schema,
            String bufferPath,
            int level) {

        AgateSchema itemSchema =
                schema.getItems();


        /*
         * Primitive arrays are already represented in
         * the AGATE buffer as a complete JSON value.
         *
         * Example:
         *
         * photoUrls=[AAAA]
         *
         * request template:
         *
         * "photoUrls": {B[photoUrls]}
         */

        if (itemSchema == null ||
                !"object".equals(
                        itemSchema.getType()
                )) {

            appendRawBuffer(
                    target,
                    bufferPath
            );

            return;
        }


        /*
         * Array<Object>
         */

        target.append("[")
                .append(
                        System.lineSeparator()
                );


        appendIndent(
                target,
                level + 1
        );


        appendObject(
                target,
                itemSchema,
                bufferPath,
                level + 1
        );


        target.append(
                System.lineSeparator()
        );


        appendIndent(
                target,
                level
        );


        target.append("]");
    }




    private void appendScalar(
            StringBuilder target,
            AgateSchema schema,
            String bufferPath) {

        String type =
                schema.getType();


        /*
         * Numeric and boolean placeholders are inserted
         * without quotes so JSON type information is kept.
         */

        if ("integer".equals(type) ||
                "number".equals(type) ||
                "boolean".equals(type)) {

            appendRawBuffer(
                    target,
                    bufferPath
            );

            return;
        }


        /*
         * Strings and unknown scalar types are quoted.
         */

        appendQuotedBuffer(
                target,
                bufferPath
        );
    }




    private void appendQuotedBuffer(
            StringBuilder target,
            String bufferPath) {

        target.append("\"{B[")
                .append(
                        bufferPath
                )
                .append("]}\"");
    }




    private void appendRawBuffer(
            StringBuilder target,
            String bufferPath) {

        target.append("{B[")
                .append(
                        bufferPath
                )
                .append("]}");
    }




    private List<Map.Entry<String, AgateSchema>>
    writableProperties(
            AgateSchema schema) {

        List<Map.Entry<String, AgateSchema>> result =
                new ArrayList<>();


        if (schema.getProperties() == null) {

            return result;
        }


        for (Map.Entry<String, AgateSchema> entry :
                schema
                        .getProperties()
                        .entrySet()) {

            AgateSchema property =
                    entry.getValue();


            if (property == null) {

                continue;
            }


            if (property.isReadOnly()) {

                continue;
            }


            result.add(
                    entry
            );
        }


        return result;
    }




    private void appendIndent(
            StringBuilder target,
            int level) {

        target.append(
                INDENT.repeat(
                        Math.max(
                                level,
                                0
                        )
                )
        );
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