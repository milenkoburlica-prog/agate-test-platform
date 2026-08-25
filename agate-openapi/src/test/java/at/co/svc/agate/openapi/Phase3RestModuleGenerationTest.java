package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase3.module.AgateRestMetadataGenerator;
import at.co.svc.agate.openapi.phase3.module.AgateRestRequestGenerator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Phase3RestModuleGenerationTest {


    @Test
    void shouldGeneratePostPetMetadata()
            throws Exception {

        AgateOperationModel operation =
                operation();


        String metadata =
                new AgateRestMetadataGenerator()
                        .generate(
                                operation
                        );


        assertNotNull(
                metadata
        );


        assertTrue(
                metadata.contains(
                        "\"url\" : \"{{endpoint}}/pet\""
                )
        );


        assertTrue(
                metadata.contains(
                        "\"method\" : \"POST\""
                )
        );


        assertTrue(
                metadata.contains(
                        "\"Content-Type\" : "
                                + "\"application/json;charset=UTF-8\""
                )
        );


        assertTrue(
                metadata.contains(
                        "\"accept\" : \"application/json\""
                )
        );
    }




    @Test
    void shouldGeneratePostPetRequestTemplate()
            throws Exception {

        AgateOperationModel operation =
                operation();


        String request =
                new AgateRestRequestGenerator()
                        .generate(
                                operation
                        );


        assertNotNull(
                request
        );


        /*
         * integer
         */

        assertTrue(
                request.contains(
                        "\"id\": {B[id]}"
                )
        );


        /*
         * string
         */

        assertTrue(
                request.contains(
                        "\"name\": \"{B[name]}\""
                )
        );


        /*
         * nested integer
         */

        assertTrue(
                request.contains(
                        "\"id\": {B[category.id]}"
                )
        );


        /*
         * nested string
         */

        assertTrue(
                request.contains(
                        "\"name\": \"{B[category.name]}\""
                )
        );


        /*
         * primitive array
         */

        assertTrue(
                request.contains(
                        "\"photoUrls\": {B[photoUrls]}"
                )
        );


        /*
         * object array
         */

        assertTrue(
                request.contains(
                        "\"tags\": ["
                )
        );


        assertTrue(
                request.contains(
                        "\"id\": {B[tags.id]}"
                )
        );


        assertTrue(
                request.contains(
                        "\"name\": \"{B[tags.name]}\""
                )
        );


        /*
         * string enum
         */

        assertTrue(
                request.contains(
                        "\"status\": \"{B[status]}\""
                )
        );
    }




    private AgateOperationModel operation()
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v14-pet-module.yaml"
                );


        AgateEndpoint endpoint =
                model
                        .getEndpoints()
                        .stream()
                        .filter(value ->
                                "POST".equals(
                                        value.getMethod()
                                )
                        )
                        .filter(value ->
                                "/pet".equals(
                                        value.getPath()
                                )
                        )
                        .findFirst()
                        .orElseThrow();


        return new AgateOperationModelBuilder()
                .build(
                        endpoint
                );
    }




    private AgateOpenApiModel parse(
            String resourceName)
            throws Exception {

        var resource =
                Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(
                                resourceName
                        );


        if (resource == null) {

            throw new IllegalStateException(
                    "Test resource not found: "
                            + resourceName
            );
        }


        return new AgateOpenApiParser()
                .parse(
                        Path
                                .of(
                                        resource.toURI()
                                )
                                .toString()
                );
    }
}