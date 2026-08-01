package at.co.svc.open.api.spec.loader;


import java.nio.file.Files;
import java.nio.file.Path;

import at.co.svc.open.api.spec.parser.OpenApiParserService;
import io.swagger.v3.oas.models.OpenAPI;



public class OpenApiLoader {


    private final OpenApiParserService parser =
            new OpenApiParserService();




    public OpenAPI load(
            Path file) {


        if(file==null) {
            throw new IllegalArgumentException(
                    "OpenAPI file is null"
            );
        }



        if(!Files.exists(file)) {

            throw new RuntimeException(
                    "OpenAPI file not found: "
                    + file
            );

        }



        return parser.load(file);

    }

}