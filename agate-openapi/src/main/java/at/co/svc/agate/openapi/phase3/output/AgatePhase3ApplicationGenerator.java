package at.co.svc.agate.openapi.phase3.output;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;

import at.co.svc.agate.openapi.phase3.AgatePhase3Compiler;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class AgatePhase3ApplicationGenerator {


    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();


    private final AgatePhase2TestGenerator phase2Generator =
            new AgatePhase2TestGenerator();


    private final AgatePhase3Compiler phase3Compiler =
            new AgatePhase3Compiler();


    private final AgatePhase3ArtifactGenerator artifactGenerator =
            new AgatePhase3ArtifactGenerator();




    public GenerationResult generate(
            String appId,
            AgateOpenApiModel openApiModel)
            throws IOException {

        validate(
                appId,
                openApiModel
        );


        /*
         * Current working directory is the project directory.
         *
         * Example:
         *
         * C:\work\projects\agate-studio-new\agate-openapi
         */

        Path projectDirectory =
                Path
                        .of("")
                        .toAbsolutePath()
                        .normalize();


        Path dataDirectory =
                projectDirectory.resolve(
                        "data"
                );


        Path appDirectory =
                dataDirectory.resolve(
                        appId
                );


        Files.createDirectories(
                appDirectory
        );


        List<AgatePhase3ArtifactGenerator.GenerationResult>
                operationResults =
                new ArrayList<>();


        /*
         * =====================================================
         * PROCESS COMPLETE OPENAPI
         * =====================================================
         */

        for (AgateEndpoint endpoint :
                openApiModel.getEndpoints()) {

            AgateOperationModel operation =
                    operationBuilder.build(
                            endpoint
                    );


            AgateGeneratedTestPlan generatedPlan =
                    phase2Generator.generate(
                            operation
                    );


            AgateExecutableTestPlan executablePlan =
                    phase3Compiler.compile(
                            operation,
                            generatedPlan
                    );


            AgatePhase3ArtifactGenerator.GenerationResult
                    operationResult =
                    artifactGenerator.generate(
                            operation,
                            executablePlan,
                            appDirectory
                    );


            operationResults.add(
                    operationResult
            );
        }


        return new GenerationResult(
                appId,
                projectDirectory,
                dataDirectory,
                appDirectory,
                operationResults
        );
    }




    private void validate(
            String appId,
            AgateOpenApiModel model) {

        if (appId == null ||
                appId.isBlank()) {

            throw new IllegalArgumentException(
                    "appId must not be blank"
            );
        }


        /*
         * appId is also used as directory name.
         *
         * Do not allow path traversal or directory separators.
         */

        if (!appId.matches(
                "[a-zA-Z0-9._-]+"
        )) {

            throw new IllegalArgumentException(
                    "Invalid appId: "
                            + appId
            );
        }


        if (".".equals(appId) ||
                "..".equals(appId)) {

            throw new IllegalArgumentException(
                    "Invalid appId: "
                            + appId
            );
        }


        if (model == null) {

            throw new IllegalArgumentException(
                    "OpenAPI model must not be null"
            );
        }


        if (model.getEndpoints() == null) {

            throw new IllegalArgumentException(
                    "OpenAPI endpoints must not be null"
            );
        }
    }




    public static class GenerationResult {


        private final String appId;


        private final Path projectDirectory;

        private final Path dataDirectory;

        private final Path appDirectory;


        private final List<
                AgatePhase3ArtifactGenerator.GenerationResult
                > operations;




        public GenerationResult(
                String appId,
                Path projectDirectory,
                Path dataDirectory,
                Path appDirectory,
                List<
                        AgatePhase3ArtifactGenerator.GenerationResult
                        > operations) {

            this.appId =
                    appId;


            this.projectDirectory =
                    projectDirectory;


            this.dataDirectory =
                    dataDirectory;


            this.appDirectory =
                    appDirectory;


            this.operations =
                    operations != null
                            ? new ArrayList<>(
                                    operations
                            )
                            : new ArrayList<>();
        }




        public String getAppId() {

            return appId;
        }




        public Path getProjectDirectory() {

            return projectDirectory;
        }




        public Path getDataDirectory() {

            return dataDirectory;
        }




        public Path getAppDirectory() {

            return appDirectory;
        }




        public List<
                AgatePhase3ArtifactGenerator.GenerationResult
                > getOperations() {

            return new ArrayList<>(
                    operations
            );
        }




        public int getOperationCount() {

            return operations.size();
        }
    }
}