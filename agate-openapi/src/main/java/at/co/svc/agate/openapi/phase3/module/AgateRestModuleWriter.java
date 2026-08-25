package at.co.svc.agate.openapi.phase3.module;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase3.template.AgateTemplateNameBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class AgateRestModuleWriter {


    private static final String METADATA_FILE =
            "metdata.json";


    private static final String REQUEST_FILE =
            "request.json";


    private static final String EMPTY_REQUEST =
            "{"
                    + System.lineSeparator()
                    + "}"
                    + System.lineSeparator();


    private final AgateRestMetadataGenerator metadataGenerator =
            new AgateRestMetadataGenerator();


    private final AgateRestRequestGenerator requestGenerator =
            new AgateRestRequestGenerator();


    private final AgateTemplateNameBuilder nameBuilder =
            new AgateTemplateNameBuilder();




    public GenerationResult write(
            AgateOperationModel operation,
            Path restModulesDirectory)
            throws IOException {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (restModulesDirectory == null) {

            throw new IllegalArgumentException(
                    "REST modules directory must not be null"
            );
        }


        /*
         * =====================================================
         * MODULE NAME
         * =====================================================
         *
         * DELETE /pet/{petId}
         *
         * ->
         *
         * delete_pet_petId
         */

        String moduleName =
                nameBuilder.buildBaseName(
                        operation.getMethod(),
                        operation.getPath()
                );


        Path moduleDirectory =
                restModulesDirectory.resolve(
                        moduleName
                );


        Files.createDirectories(
                moduleDirectory
        );


        /*
         * =====================================================
         * metdata.json
         * =====================================================
         */

        Path metadataFile =
                moduleDirectory.resolve(
                        METADATA_FILE
                );


        String metadata =
                metadataGenerator.generate(
                        operation
                );


        Files.writeString(
                metadataFile,
                metadata,
                StandardCharsets.UTF_8
        );


        /*
         * =====================================================
         * request.json
         * =====================================================
         *
         * request.json ALWAYS exists.
         *
         * POST/PUT/PATCH with body:
         *
         * {
         *   "name": "{B[name]}"
         * }
         *
         * GET/DELETE without body:
         *
         * {
         * }
         */

        Path requestFile =
                moduleDirectory.resolve(
                        REQUEST_FILE
                );


        String request =
                requestGenerator.generate(
                        operation
                );


        if (request == null ||
                request.isBlank()) {

            request =
                    EMPTY_REQUEST;
        }


        Files.writeString(
                requestFile,
                request,
                StandardCharsets.UTF_8
        );


        /*
         * =====================================================
         * RESULT
         * =====================================================
         */

        return new GenerationResult(
                moduleName,
                moduleDirectory,
                metadataFile,
                requestFile
        );
    }




    public static class GenerationResult {


        private final String moduleName;

        private final Path moduleDirectory;

        private final Path metadataFile;

        private final Path requestFile;




        public GenerationResult(
                String moduleName,
                Path moduleDirectory,
                Path metadataFile,
                Path requestFile) {

            this.moduleName =
                    moduleName;


            this.moduleDirectory =
                    moduleDirectory;


            this.metadataFile =
                    metadataFile;


            this.requestFile =
                    requestFile;
        }




        public String getModuleName() {

            return moduleName;
        }




        public Path getModuleDirectory() {

            return moduleDirectory;
        }




        public Path getMetadataFile() {

            return metadataFile;
        }




        public Path getRequestFile() {

            return requestFile;
        }
    }
}