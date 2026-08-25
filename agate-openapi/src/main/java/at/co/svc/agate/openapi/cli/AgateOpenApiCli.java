package at.co.svc.agate.openapi.cli;

import at.co.svc.agate.openapi.model.AgateContent;
import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.model.AgateParameter;
import at.co.svc.agate.openapi.model.AgateResponse;
import at.co.svc.agate.openapi.model.AgateSchema;
import at.co.svc.agate.openapi.model.AgateValidationResult;
import at.co.svc.agate.openapi.output.AgateModelJsonWriter;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;
import at.co.svc.agate.openapi.validator.AgateOpenApiValidator;

import java.util.List;
import java.util.Map;

public class AgateOpenApiCli {

    public static void main(String[] args) {

        boolean jsonMode =
                args.length == 2
                        && "--json".equals(args[0]);

        if (args.length != 1 &&
                !jsonMode) {

            System.err.println(
                    "Usage: agate-openapi [--json] <openapi-source>"
            );

            System.exit(1);
        }

        String source =
                jsonMode
                        ? args[1]
                        : args[0];

        try {

            AgateOpenApiParser parser =
                    new AgateOpenApiParser();

            AgateOpenApiModel model =
                    parser.parse(
                            source
                    );

            if (jsonMode) {

                AgateModelJsonWriter writer =
                        new AgateModelJsonWriter();

                System.out.println(
                        writer.write(
                                model
                        )
                );

                return;
            }

            print(
                    model
            );

            AgateOpenApiValidator validator =
                    new AgateOpenApiValidator();

            AgateValidationResult validation =
                    validator.validate(
                            model
                    );

            printValidation(
                    validation
            );

        } catch (Exception e) {

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.exit(2);
        }
    }


    private static void print(
            AgateOpenApiModel model) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "AGATE OPENAPI"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Source  : "
                        + model.getSource()
        );

        System.out.println(
                "Title   : "
                        + model.getTitle()
        );

        System.out.println(
                "Version : "
                        + model.getVersion()
        );

        if (!model.getServers().isEmpty()) {

            System.out.println(
                    "Servers:"
            );

            for (String server :
                    model.getServers()) {

                System.out.println(
                        "  "
                                + server
                );
            }
        }

        if (!model.getTags().isEmpty()) {

            System.out.println(
                    "Tags:"
            );

            for (String tag :
                    model.getTags()) {

                System.out.println(
                        "  "
                                + tag
                );
            }
        }

        System.out.println(
                "Endpoints: "
                        + model.getEndpoints().size()
        );

        System.out.println();

        for (AgateEndpoint endpoint :
                model.getEndpoints()) {

            printEndpoint(
                    endpoint
            );
        }
    }


    private static void printEndpoint(
            AgateEndpoint endpoint) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                endpoint.getMethod()
                        + " "
                        + endpoint.getPath()
        );

        System.out.println(
                "identity: "
                        + endpoint.getIdentity()
        );

        System.out.println(
                "operationId: "
                        + endpoint.getOperationId()
        );

        if (endpoint.getSummary() != null) {

            System.out.println(
                    "summary: "
                            + endpoint.getSummary()
            );
        }

        if (endpoint.getDescription() != null) {

            System.out.println(
                    "description: "
                            + endpoint.getDescription()
            );
        }

        if (endpoint.isDeprecated()) {

            System.out.println(
                    "deprecated: true"
            );
        }

        if (!endpoint.getTags().isEmpty()) {

            System.out.println(
                    "tags: "
                            + endpoint.getTags()
            );
        }

        if (!endpoint.getSecurity().isEmpty()) {

            System.out.println(
                    "security:"
            );

            for (Map<String, List<String>> requirement :
                    endpoint.getSecurity()) {

                System.out.println(
                        "  "
                                + requirement
                );
            }
        }

        if (!endpoint.getParameters().isEmpty()) {

            System.out.println(
                    "parameters:"
            );

            for (AgateParameter parameter :
                    endpoint.getParameters()) {

                printParameter(
                        parameter
                );
            }
        }

        if (endpoint.getRequestBody() != null) {

            System.out.println(
                    "requestBody:"
            );

            System.out.println(
                    "  required="
                            + endpoint
                                    .getRequestBody()
                                    .isRequired()
            );

            if (endpoint
                    .getRequestBody()
                    .getDescription() != null) {

                System.out.println(
                        "  description="
                                + endpoint
                                        .getRequestBody()
                                        .getDescription()
                );
            }

            if (endpoint
                    .getRequestBody()
                    .getSourceRef() != null) {

                System.out.println(
                        "  ref="
                                + endpoint
                                        .getRequestBody()
                                        .getSourceRef()
                );
            }

            for (AgateContent content :
                    endpoint
                            .getRequestBody()
                            .getContents()) {

                printContent(
                        content,
                        "  "
                );
            }
        }

        if (!endpoint.getResponses().isEmpty()) {

            System.out.println(
                    "responses:"
            );

            for (AgateResponse response :
                    endpoint.getResponses()) {

                System.out.println(
                        "  "
                                + response.getStatusCode()
                                + " "
                                + response.getDescription()
                );

                if (response.getSourceRef() != null) {

                    System.out.println(
                            "    ref="
                                    + response.getSourceRef()
                    );
                }

                for (AgateContent content :
                        response.getContents()) {

                    printContent(
                            content,
                            "    "
                    );
                }
            }
        }

        System.out.println();
    }


    private static void printParameter(
            AgateParameter parameter) {

        System.out.println(
                "  "
                        + parameter.getLocation()
                        + " "
                        + parameter.getName()
                        + " required="
                        + parameter.isRequired()
        );

        if (parameter.getDescription() != null) {

            System.out.println(
                    "    description="
                            + parameter.getDescription()
            );
        }

        if (parameter.getSourceRef() != null) {

            System.out.println(
                    "    ref="
                            + parameter.getSourceRef()
            );
        }

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

        printSchema(
                parameter.getSchema(),
                "    "
        );
    }


    private static void printContent(
            AgateContent content,
            String indent) {

        System.out.println(
                indent
                        + content.getMediaType()
        );

        if (content.getExample() != null) {

            System.out.println(
                    indent
                            + "  example="
                            + content.getExample()
            );
        }

        if (!content.getExamples().isEmpty()) {

            System.out.println(
                    indent
                            + "  examples="
                            + content.getExamples()
            );
        }

        printSchema(
                content.getSchema(),
                indent + "  "
        );
    }


    private static void printSchema(
            AgateSchema schema,
            String indent) {

        if (schema == null) {
            return;
        }

        System.out.println(
                indent
                        + schemaHeader(
                                schema
                        )
        );

        printConstraints(
                schema,
                indent
        );

        if (schema.getDescription() != null) {

            System.out.println(
                    indent
                            + "description="
                            + schema.getDescription()
            );
        }

        if (schema.getDefaultValue() != null) {

            System.out.println(
                    indent
                            + "default="
                            + schema.getDefaultValue()
            );
        }

        if (schema.getExample() != null) {

            System.out.println(
                    indent
                            + "example="
                            + schema.getExample()
            );
        }

        if (!schema.getRequired().isEmpty()) {

            System.out.println(
                    indent
                            + "required="
                            + schema.getRequired()
            );
        }

        if (!schema.getEnumValues().isEmpty()) {

            System.out.println(
                    indent
                            + "enum="
                            + schema.getEnumValues()
            );
        }

        if (schema.getItems() != null) {

            System.out.println(
                    indent
                            + "items:"
            );

            printSchema(
                    schema.getItems(),
                    indent + "  "
            );
        }

        for (Map.Entry<String, AgateSchema> entry :
                schema
                        .getProperties()
                        .entrySet()) {

            System.out.println(
                    indent
                            + entry.getKey()
                            + ":"
            );

            printSchema(
                    entry.getValue(),
                    indent + "  "
            );
        }

        if (schema.getAdditionalProperties() != null) {

            System.out.println(
                    indent
                            + "additionalProperties:"
            );

            printSchema(
                    schema.getAdditionalProperties(),
                    indent + "  "
            );
        }

        printSchemaList(
                "allOf",
                schema.getAllOf(),
                indent
        );

        printSchemaList(
                "oneOf",
                schema.getOneOf(),
                indent
        );

        printSchemaList(
                "anyOf",
                schema.getAnyOf(),
                indent
        );
    }


    private static String schemaHeader(
            AgateSchema schema) {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "type="
        );

        result.append(
                schema.getType()
        );

        if (schema.getFormat() != null) {

            result.append(
                    " format="
            );

            result.append(
                    schema.getFormat()
            );
        }

        if (schema.getSourceRef() != null) {

            result.append(
                    " ref="
            );

            result.append(
                    schema.getSourceRef()
            );
        }

        if (schema.getResolvedRef() != null &&
                !schema
                        .getResolvedRef()
                        .equals(
                                schema.getSourceRef()
                        )) {

            result.append(
                    " resolvedRef="
            );

            result.append(
                    schema.getResolvedRef()
            );
        }

        if (schema.getSourceDocument() != null) {

            result.append(
                    " source="
            );

            result.append(
                    schema.getSourceDocument()
            );
        }

        if (schema.isNullable()) {

            result.append(
                    " nullable=true"
            );
        }

        if (schema.isReadOnly()) {

            result.append(
                    " readOnly=true"
            );
        }

        if (schema.isWriteOnly()) {

            result.append(
                    " writeOnly=true"
            );
        }

        return result.toString();
    }


    private static void printConstraints(
            AgateSchema schema,
            String indent) {

        if (schema.getMinLength() != null) {

            System.out.println(
                    indent
                            + "minLength="
                            + schema.getMinLength()
            );
        }

        if (schema.getMaxLength() != null) {

            System.out.println(
                    indent
                            + "maxLength="
                            + schema.getMaxLength()
            );
        }

        if (schema.getPattern() != null) {

            System.out.println(
                    indent
                            + "pattern="
                            + schema.getPattern()
            );
        }

        if (schema.getMinimum() != null) {

            System.out.println(
                    indent
                            + "minimum="
                            + schema.getMinimum()
            );
        }

        if (schema.getMaximum() != null) {

            System.out.println(
                    indent
                            + "maximum="
                            + schema.getMaximum()
            );
        }

        if (schema.getMinItems() != null) {

            System.out.println(
                    indent
                            + "minItems="
                            + schema.getMinItems()
            );
        }

        if (schema.getMaxItems() != null) {

            System.out.println(
                    indent
                            + "maxItems="
                            + schema.getMaxItems()
            );
        }

        if (schema.getUniqueItems() != null) {

            System.out.println(
                    indent
                            + "uniqueItems="
                            + schema.getUniqueItems()
            );
        }
    }


    private static void printSchemaList(
            String name,
            List<AgateSchema> schemas,
            String indent) {

        if (schemas == null ||
                schemas.isEmpty()) {

            return;
        }

        System.out.println(
                indent
                        + name
                        + ":"
        );

        for (int i = 0;
             i < schemas.size();
             i++) {

            System.out.println(
                    indent
                            + "  ["
                            + i
                            + "]"
            );

            printSchema(
                    schemas.get(i),
                    indent + "    "
            );
        }
    }


    private static void printValidation(
            AgateValidationResult result) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "VALIDATION"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                result.isValid()
                        ? "VALID"
                        : "INVALID"
        );

        System.out.println(
                "endpoints="
                        + result.getEndpointCount()
        );

        System.out.println(
                "parameters="
                        + result.getParameterCount()
        );

        System.out.println(
                "requestBodies="
                        + result.getRequestBodyCount()
        );

        System.out.println(
                "responses="
                        + result.getResponseCount()
        );

        System.out.println(
                "unresolvedRefs="
                        + result.getUnresolvedRefCount()
        );

        System.out.println(
                "unsupported="
                        + result.getUnsupportedCount()
        );

        System.out.println(
                "warnings="
                        + result.getWarningCount()
        );

        if (!result.getErrors().isEmpty()) {

            System.out.println(
                    "errors:"
            );

            for (String error :
                    result.getErrors()) {

                System.out.println(
                        "  "
                                + error
                );
            }
        }

        if (!result.getUnsupported().isEmpty()) {

            System.out.println(
                    "unsupported:"
            );

            for (String unsupported :
                    result.getUnsupported()) {

                System.out.println(
                        "  "
                                + unsupported
                );
            }
        }

        if (!result.getWarnings().isEmpty()) {

            System.out.println(
                    "warnings:"
            );

            for (String warning :
                    result.getWarnings()) {

                System.out.println(
                        "  "
                                + warning
                );
            }
        }
    }
}