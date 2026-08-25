package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateContent;
import at.co.svc.agate.openapi.model.AgateResponse;
import at.co.svc.agate.openapi.resolver.ResolvedResponse;
import at.co.svc.agate.openapi.resolver.ResponseResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResponseExtractor {

    private final SchemaExtractor schemaExtractor =
            new SchemaExtractor();

    private final ResponseResolver responseResolver =
            new ResponseResolver();


    public List<AgateResponse> extract(
            OpenAPI openApi,
            String sourceDocument,
            ApiResponses responses) {

        List<AgateResponse> result =
                new ArrayList<>();

        if (responses == null) {
            return result;
        }

        responses.forEach(
                (statusCode, response) ->
                        addResponse(
                                result,
                                openApi,
                                sourceDocument,
                                statusCode,
                                response
                        )
        );

        return result;
    }


    private void addResponse(
            List<AgateResponse> target,
            OpenAPI openApi,
            String sourceDocument,
            String statusCode,
            ApiResponse response) {

        if (response == null) {
            return;
        }

        ResolvedResponse resolution =
                responseResolver.resolve(
                        openApi,
                        sourceDocument,
                        response
                );

        if (resolution == null ||
                resolution.getResponse() == null) {

            return;
        }

        ApiResponse resolvedResponse =
                resolution.getResponse();

        AgateResponse result =
                new AgateResponse();

        result.setStatusCode(
                statusCode
        );

        result.setDescription(
                resolvedResponse.getDescription()
        );

        result.setSourceRef(
                resolution.getSourceRef()
        );

        Content content =
                resolvedResponse.getContent();

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

        target.add(
                result
        );
    }


    private void addContent(
            AgateResponse target,
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