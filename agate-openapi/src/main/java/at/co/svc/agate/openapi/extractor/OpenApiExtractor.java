package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.List;

public class OpenApiExtractor {

    private final EndpointExtractor endpointExtractor =
            new EndpointExtractor();


    public AgateOpenApiModel extract(
            OpenAPI openApi,
            String source) {

        if (openApi == null) {

            throw new IllegalArgumentException(
                    "OpenAPI must not be null"
            );
        }

        AgateOpenApiModel model =
                new AgateOpenApiModel();

        model.setSource(
                source
        );

        Info info =
                openApi.getInfo();

        if (info != null) {

            model.setTitle(
                    info.getTitle()
            );

            model.setVersion(
                    info.getVersion()
            );
        }

        model.setServers(
                extractServers(
                        openApi
                )
        );

        model.setTags(
                extractTags(
                        openApi
                )
        );

        model.setEndpoints(
                endpointExtractor.extract(
                        openApi,
                        source
                )
        );

        return model;
    }


    private List<String> extractServers(
            OpenAPI openApi) {

        List<String> result =
                new ArrayList<>();

        if (openApi.getServers() == null) {
            return result;
        }

        for (Server server :
                openApi.getServers()) {

            if (server == null ||
                    server.getUrl() == null) {

                continue;
            }

            result.add(
                    server.getUrl()
            );
        }

        return result;
    }


    private List<String> extractTags(
            OpenAPI openApi) {

        List<String> result =
                new ArrayList<>();

        if (openApi.getTags() == null) {
            return result;
        }

        for (Tag tag :
                openApi.getTags()) {

            if (tag == null ||
                    tag.getName() == null) {

                continue;
            }

            result.add(
                    tag.getName()
            );
        }

        return result;
    }
}