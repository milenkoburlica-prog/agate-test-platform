package at.co.svc.agate.openapi.loader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class OpenApiLoader {

    public OpenAPI load(String source) {

        ParseOptions options =
                new ParseOptions();

        options.setResolve(false);
        options.setResolveFully(false);
        options.setResolveCombinators(false);

        SwaggerParseResult result =
                new OpenAPIV3Parser()
                        .readLocation(
                                source,
                                null,
                                options
                        );

        if (result == null) {

            throw new IllegalStateException(
                    "OpenAPI parser returned no result: "
                            + source
            );
        }

        if (result.getOpenAPI() == null) {

            String messages =
                    result.getMessages() != null
                            ? String.join(
                                    System.lineSeparator(),
                                    result.getMessages()
                            )
                            : "";

            throw new IllegalStateException(
                    "Unable to load OpenAPI source: "
                            + source
                            + System.lineSeparator()
                            + messages
            );
        }

        return result.getOpenAPI();
    }
}