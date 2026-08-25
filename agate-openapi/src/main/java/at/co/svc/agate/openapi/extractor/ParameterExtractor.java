package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateParameter;
import at.co.svc.agate.openapi.resolver.ParameterResolver;
import at.co.svc.agate.openapi.resolver.ResolvedParameter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParameterExtractor {

    private final SchemaExtractor schemaExtractor =
            new SchemaExtractor();

    private final ParameterResolver parameterResolver =
            new ParameterResolver();


    public List<AgateParameter> extract(
            OpenAPI openApi,
            String sourceDocument,
            PathItem pathItem,
            Operation operation) {

        Map<String, AgateParameter> parameters =
                new LinkedHashMap<>();

        if (pathItem != null) {

            addParameters(
                    openApi,
                    sourceDocument,
                    parameters,
                    pathItem.getParameters()
            );
        }

        if (operation != null) {

            addParameters(
                    openApi,
                    sourceDocument,
                    parameters,
                    operation.getParameters()
            );
        }

        return new ArrayList<>(
                parameters.values()
        );
    }


    private void addParameters(
            OpenAPI openApi,
            String sourceDocument,
            Map<String, AgateParameter> target,
            List<Parameter> source) {

        if (source == null) {
            return;
        }

        for (Parameter parameter :
                source) {

            if (parameter == null) {
                continue;
            }

            AgateParameter agateParameter =
                    extractParameter(
                            openApi,
                            sourceDocument,
                            parameter
                    );

            if (agateParameter == null) {
                continue;
            }

            String key =
                    createKey(
                            agateParameter.getName(),
                            agateParameter.getLocation()
                    );

            target.put(
                    key,
                    agateParameter
            );
        }
    }


    private AgateParameter extractParameter(
            OpenAPI openApi,
            String sourceDocument,
            Parameter parameter) {

        ResolvedParameter resolution =
                parameterResolver.resolve(
                        openApi,
                        sourceDocument,
                        parameter
                );

        if (resolution == null ||
                resolution.getParameter() == null) {

            return null;
        }

        Parameter resolvedParameter =
                resolution.getParameter();

        AgateParameter result =
                new AgateParameter();

        result.setName(
                resolvedParameter.getName()
        );

        result.setLocation(
                resolvedParameter.getIn()
        );

        result.setRequired(
                Boolean.TRUE.equals(
                        resolvedParameter.getRequired()
                )
        );

        result.setDescription(
                resolvedParameter.getDescription()
        );

        result.setSourceRef(
                resolution.getSourceRef()
        );

        if (resolvedParameter.getStyle() != null) {

            result.setStyle(
                    resolvedParameter
                            .getStyle()
                            .toString()
            );
        }

        result.setExplode(
                resolvedParameter.getExplode()
        );

        result.setAllowEmptyValue(
                resolvedParameter.getAllowEmptyValue()
        );

        result.setAllowReserved(
                resolvedParameter.getAllowReserved()
        );

        result.setExample(
                resolvedParameter.getExample()
        );

        result.setExamples(
                extractExamples(
                        resolvedParameter
                )
        );

        result.setSchema(
                schemaExtractor.extract(
                        resolution.getOpenApi(),
                        resolution.getSourceDocument(),
                        resolvedParameter.getSchema()
                )
        );

        return result;
    }


    private Map<String, Object> extractExamples(
            Parameter parameter) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (parameter.getExamples() == null) {
            return result;
        }

        parameter
                .getExamples()
                .forEach(
                        (name, example) -> {

                            if (example == null) {

                                result.put(
                                        name,
                                        null
                                );

                                return;
                            }

                            result.put(
                                    name,
                                    extractExampleValue(
                                            example
                                    )
                            );
                        }
                );

        return result;
    }


    private Object extractExampleValue(
            Example example) {

        if (example == null) {
            return null;
        }

        if (example.getValue() != null) {

            return example.getValue();
        }

        if (example.getExternalValue() != null) {

            return example.getExternalValue();
        }

        if (example.get$ref() != null) {

            return example.get$ref();
        }

        return null;
    }


    private String createKey(
            String name,
            String location) {

        return String.valueOf(
                location
        )
                + ":"
                + String.valueOf(
                        name
                );
    }
}