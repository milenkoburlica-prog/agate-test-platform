package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateContent;
import at.co.svc.agate.openapi.model.AgateRequestBody;
import at.co.svc.agate.openapi.resolver.RequestBodyResolver;
import at.co.svc.agate.openapi.resolver.ResolvedRequestBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestBodyExtractor {

    private final SchemaExtractor schemaExtractor =
            new SchemaExtractor();

    private final RequestBodyResolver requestBodyResolver =
            new RequestBodyResolver();


    public AgateRequestBody extract(
            OpenAPI openApi,
            String sourceDocument,
            RequestBody requestBody) {

        if (requestBody == null) {
            return null;
        }

        ResolvedRequestBody resolution =
                requestBodyResolver.resolve(
                        openApi,
                        sourceDocument,
                        requestBody
                );

        if (resolution == null ||
                resolution.getRequestBody() == null) {

            return null;
        }

        RequestBody resolvedRequestBody =
                resolution.getRequestBody();

        AgateRequestBody result =
                new AgateRequestBody();

        result.setRequired(
                Boolean.TRUE.equals(
                        resolvedRequestBody.getRequired()
                )
        );

        result.setDescription(
                resolvedRequestBody.getDescription()
        );

        result.setSourceRef(
                resolution.getSourceRef()
        );

        Content content =
                resolvedRequestBody.getContent();

        if (content != null) {

            content.forEach(
                    (mediaType, value) ->
                            addContent(
                                    result,
                                    resolution.getOpenApi(),
                                    resolution.getSourceDocument(),
                                    mediaType,
                                    value
                            )
            );
        }

        return result;
    }


    private void addContent(
            AgateRequestBody target,
            OpenAPI openApi,
            String sourceDocument,
            String mediaTypeName,
            MediaType mediaType) {

        if (mediaType == null) {
            return;
        }

        AgateContent content =
                new AgateContent();

        content.setMediaType(
                mediaTypeName
        );

        content.setExample(
                mediaType.getExample()
        );

        content.setExamples(
                extractExamples(
                        mediaType
                )
        );

        content.setSchema(
                schemaExtractor.extract(
                        openApi,
                        sourceDocument,
                        mediaType.getSchema()
                )
        );

        target.addContent(
                content
        );
    }


    private Map<String, Object> extractExamples(
            MediaType mediaType) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (mediaType.getExamples() == null) {
            return result;
        }

        mediaType
                .getExamples()
                .forEach(
                        (name, example) ->
                                result.put(
                                        name,
                                        extractExampleValue(
                                                example
                                        )
                                )
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
}