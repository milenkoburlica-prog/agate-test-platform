package at.co.svc.agate.openapi.parser;

import at.co.svc.agate.openapi.extractor.OpenApiExtractor;
import at.co.svc.agate.openapi.inspector.OpenApiSourceInspector;
import at.co.svc.agate.openapi.loader.OpenApiLoader;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import io.swagger.v3.oas.models.OpenAPI;

public class AgateOpenApiParser {

    private final OpenApiLoader loader =
            new OpenApiLoader();

    private final OpenApiExtractor extractor =
            new OpenApiExtractor();

    private final OpenApiSourceInspector sourceInspector =
            new OpenApiSourceInspector();


    public AgateOpenApiModel parse(
            String source) throws Exception {

        OpenAPI openApi =
                loader.load(
                        source
                );

        AgateOpenApiModel model =
                extractor.extract(
                        openApi,
                        source
                );

        sourceInspector.inspect(
                openApi,
                source,
                model
        );

        return model;
    }
}