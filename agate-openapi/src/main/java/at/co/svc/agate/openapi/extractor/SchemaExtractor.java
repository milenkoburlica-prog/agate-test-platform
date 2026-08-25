package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateSchema;
import at.co.svc.agate.openapi.resolver.ResolvedSchema;
import at.co.svc.agate.openapi.resolver.SchemaResolver;
import at.co.svc.agate.openapi.resolver.SourceDocumentResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SchemaExtractor {


    private final SchemaResolver schemaResolver =
            new SchemaResolver();


    private final SourceDocumentResolver sourceDocumentResolver =
            new SourceDocumentResolver();




    public AgateSchema extract(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema) {

        return extract(
                openApi,
                sourceDocument,
                schema,
                new HashSet<>()
        );
    }




    private AgateSchema extract(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema,
            Set<String> resolvingRefs) {

        if (schema == null) {

            return null;
        }


        String sourceRef =
                schema.get$ref();


        String cycleKey =
                createCycleKey(
                        sourceDocument,
                        sourceRef
                );


        if (cycleKey != null &&
                resolvingRefs.contains(
                        cycleKey
                )) {

            return createReferenceSchema(
                    sourceDocument,
                    sourceRef
            );
        }


        ResolvedSchema resolution =
                schemaResolver.resolve(
                        openApi,
                        sourceDocument,
                        schema
                );


        if (resolution == null ||
                resolution.getSchema() == null) {

            return null;
        }


        Schema<?> resolvedSchema =
                resolution.getSchema();


        OpenAPI resolvedOpenApi =
                resolution.getOpenApi();


        String resolvedSource =
                resolution.getSourceDocument();


        Set<String> nestedRefs =
                new HashSet<>(
                        resolvingRefs
                );


        String resolvedCycleKey =
                createResolvedCycleKey(
                        resolvedSource,
                        resolution.getResolvedRef()
                );


        if (resolvedCycleKey != null) {

            if (nestedRefs.contains(
                    resolvedCycleKey
            )) {

                return createReferenceSchema(
                        sourceDocument,
                        sourceRef
                );
            }


            nestedRefs.add(
                    resolvedCycleKey
            );
        }


        if (cycleKey != null) {

            nestedRefs.add(
                    cycleKey
            );
        }


        AgateSchema result =
                new AgateSchema();


        result.setName(
                resolvedSchema.getName()
        );


        result.setType(
                resolvedSchema.getType()
        );


        result.setFormat(
                resolvedSchema.getFormat()
        );


        result.setDescription(
                resolvedSchema.getDescription()
        );


        result.setNullable(
                Boolean.TRUE.equals(
                        resolvedSchema.getNullable()
                )
        );


        result.setReadOnly(
                Boolean.TRUE.equals(
                        resolvedSchema.getReadOnly()
                )
        );


        result.setWriteOnly(
                Boolean.TRUE.equals(
                        resolvedSchema.getWriteOnly()
                )
        );


        result.setDefaultValue(
                resolvedSchema.getDefault()
        );


        result.setExample(
                resolvedSchema.getExample()
        );


        result.setSourceRef(
                resolution.getSourceRef()
        );


        result.setResolvedRef(
                resolution.getResolvedRef()
        );


        result.setSourceDocument(
                resolvedSource
        );


        /*
         * String constraints
         */

        result.setMinLength(
                resolvedSchema.getMinLength()
        );


        result.setMaxLength(
                resolvedSchema.getMaxLength()
        );


        result.setPattern(
                resolvedSchema.getPattern()
        );


        /*
         * Numeric constraints
         */

        result.setMinimum(
                toBigDecimal(
                        resolvedSchema.getMinimum()
                )
        );


        result.setMaximum(
                toBigDecimal(
                        resolvedSchema.getMaximum()
                )
        );


        result.setMultipleOf(
                toBigDecimal(
                        resolvedSchema.getMultipleOf()
                )
        );


        result.setExclusiveMinimum(
                resolvedSchema.getExclusiveMinimum()
        );


        result.setExclusiveMaximum(
                resolvedSchema.getExclusiveMaximum()
        );


        /*
         * Array constraints
         */

        result.setMinItems(
                resolvedSchema.getMinItems()
        );


        result.setMaxItems(
                resolvedSchema.getMaxItems()
        );


        result.setUniqueItems(
                resolvedSchema.getUniqueItems()
        );


        /*
         * Object constraints
         */

        result.setRequired(
                resolvedSchema.getRequired()
        );


        /*
         * Enum
         */

        if (resolvedSchema.getEnum() != null) {

            result.setEnumValues(
                    resolvedSchema
                            .getEnum()
                            .stream()
                            .map(
                                    value ->
                                            (Object) value
                            )
                            .toList()
            );
        }


        /*
         * Object properties
         */

        extractProperties(
                resolvedOpenApi,
                resolvedSource,
                resolvedSchema,
                result,
                nestedRefs
        );


        /*
         * Array items
         */

        result.setItems(
                extract(
                        resolvedOpenApi,
                        resolvedSource,
                        resolvedSchema.getItems(),
                        nestedRefs
                )
        );


        /*
         * additionalProperties
         */

        extractAdditionalProperties(
                resolvedOpenApi,
                resolvedSource,
                resolvedSchema,
                result,
                nestedRefs
        );


        /*
         * Composition
         */

        result.setAllOf(
                extractSchemas(
                        resolvedOpenApi,
                        resolvedSource,
                        resolvedSchema.getAllOf(),
                        nestedRefs
                )
        );


        result.setOneOf(
                extractSchemas(
                        resolvedOpenApi,
                        resolvedSource,
                        resolvedSchema.getOneOf(),
                        nestedRefs
                )
        );


        result.setAnyOf(
                extractSchemas(
                        resolvedOpenApi,
                        resolvedSource,
                        resolvedSchema.getAnyOf(),
                        nestedRefs
                )
        );


        return result;
    }




    private void extractProperties(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema,
            AgateSchema target,
            Set<String> resolvingRefs) {

        Map<String, Schema> properties =
                schema.getProperties();


        if (properties == null) {

            return;
        }


        Map<String, AgateSchema> extractedProperties =
                new LinkedHashMap<>();


        properties.forEach(
                (name, propertySchema) -> {

                    AgateSchema extracted =
                            extract(
                                    openApi,
                                    sourceDocument,
                                    propertySchema,
                                    resolvingRefs
                            );


                    if (extracted != null) {

                        extracted.setName(
                                name
                        );
                    }


                    extractedProperties.put(
                            name,
                            extracted
                    );
                }
        );


        target.setProperties(
                extractedProperties
        );
    }




    private void extractAdditionalProperties(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema,
            AgateSchema target,
            Set<String> resolvingRefs) {

        Object additionalProperties =
                schema.getAdditionalProperties();


        if (additionalProperties
                instanceof Schema<?> additionalSchema) {

            target.setAdditionalProperties(
                    extract(
                            openApi,
                            sourceDocument,
                            additionalSchema,
                            resolvingRefs
                    )
            );
        }
    }




    private List<AgateSchema> extractSchemas(
            OpenAPI openApi,
            String sourceDocument,
            List<Schema> schemas,
            Set<String> resolvingRefs) {

        List<AgateSchema> result =
                new ArrayList<>();


        if (schemas == null) {

            return result;
        }


        for (Schema<?> schema :
                schemas) {

            AgateSchema extracted =
                    extract(
                            openApi,
                            sourceDocument,
                            schema,
                            resolvingRefs
                    );


            if (extracted != null) {

                result.add(
                        extracted
                );
            }
        }


        return result;
    }




    private AgateSchema createReferenceSchema(
            String sourceDocument,
            String sourceRef) {

        AgateSchema result =
                new AgateSchema();


        result.setSourceRef(
                sourceRef
        );


        result.setSourceDocument(
                normalizeDocument(
                        sourceDocument
                )
        );


        result.setResolvedRef(
                extractFragment(
                        sourceRef
                )
        );


        return result;
    }




    private String createCycleKey(
            String sourceDocument,
            String sourceRef) {

        if (sourceRef == null ||
                sourceRef.isBlank()) {

            return null;
        }


        if (sourceRef.startsWith("#")) {

            return normalizeDocument(
                    sourceDocument
            )
                    + "|"
                    + sourceRef;
        }


        int separator =
                sourceRef.indexOf('#');


        String documentPart =
                separator >= 0
                        ? sourceRef.substring(
                                0,
                                separator
                        )
                        : sourceRef;


        String fragment =
                separator >= 0
                        ? sourceRef.substring(
                                separator
                        )
                        : "";


        String resolvedDocument =
                resolveReferencedDocument(
                        sourceDocument,
                        documentPart
                );


        return resolvedDocument
                + "|"
                + fragment;
    }




    private String createResolvedCycleKey(
            String sourceDocument,
            String resolvedRef) {

        if (resolvedRef == null ||
                resolvedRef.isBlank()) {

            return null;
        }


        return normalizeDocument(
                sourceDocument
        )
                + "|"
                + resolvedRef;
    }




    private String resolveReferencedDocument(
            String sourceDocument,
            String referencedDocument) {

        return sourceDocumentResolver.resolve(
                sourceDocument,
                referencedDocument
        );
    }




    private String normalizeDocument(
            String sourceDocument) {

        return sourceDocumentResolver.normalize(
                sourceDocument
        );
    }




    private String extractFragment(
            String ref) {

        if (ref == null) {

            return null;
        }


        int separator =
                ref.indexOf('#');


        if (separator < 0) {

            return null;
        }


        return ref.substring(
                separator
        );
    }




    private BigDecimal toBigDecimal(
            Object value) {

        if (value == null) {

            return null;
        }


        if (value instanceof BigDecimal bigDecimal) {

            return bigDecimal;
        }


        return new BigDecimal(
                value.toString()
        );
    }
}