package at.co.svc.agate.openapi.phase3.output;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase3.csv.AgateCsvGenerator;
import at.co.svc.agate.openapi.phase3.csv.AgateCsvWriter;
import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvTable;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import at.co.svc.agate.openapi.phase3.module.AgateRestModuleWriter;

import at.co.svc.agate.openapi.phase3.template.AgateTemplateNameBuilder;
import at.co.svc.agate.openapi.phase3.template.AgateYamlTemplateWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class AgatePhase3ArtifactGenerator {


    private final AgateCsvGenerator csvGenerator =
            new AgateCsvGenerator();


    private final AgateCsvWriter csvWriter =
            new AgateCsvWriter();


    private final AgateYamlTemplateWriter yamlWriter =
            new AgateYamlTemplateWriter();


    private final AgateRestModuleWriter restModuleWriter =
            new AgateRestModuleWriter();


    private final AgateTemplateNameBuilder nameBuilder =
            new AgateTemplateNameBuilder();




    public GenerationResult generate(
            AgateOperationModel operation,
            AgateExecutableTestPlan plan,
            Path appDirectory)
            throws IOException {

        validate(
                operation,
                plan,
                appDirectory
        );


        /*
         * =====================================================
         * APPLICATION DIRECTORY
         * =====================================================
         *
         * appDirectory is already:
         *
         * <project>/data/<appId>
         */

        Path templateDirectory =
                appDirectory.resolve(
                        "template"
                );


        Path restModulesDirectory =
                appDirectory
                        .resolve(
                                "modules"
                        )
                        .resolve(
                                "rest"
                        );


        Files.createDirectories(
                templateDirectory
        );


        Files.createDirectories(
                restModulesDirectory
        );


        /*
         * =====================================================
         * OPERATION NAME
         * =====================================================
         *
         * POST /pet
         *
         * ->
         *
         * post_pet
         */

        String baseName =
                nameBuilder.buildBaseName(
                        operation.getMethod(),
                        operation.getPath()
                );


        /*
         * =====================================================
         * CSV
         * =====================================================
         */

        Path csvFile =
                templateDirectory.resolve(
                        baseName
                                + ".csv"
                );


        AgateCsvTable csvTable =
                csvGenerator.generate(
                        operation,
                        plan
                );


        csvWriter.write(
                csvTable,
                csvFile
        );


        /*
         * =====================================================
         * YAML
         * =====================================================
         */

        Path yamlFile =
                templateDirectory.resolve(
                        baseName
                                + ".yaml"
                );


        yamlWriter.write(
                operation,
                yamlFile
        );


        /*
         * =====================================================
         * REST MODULE
         * =====================================================
         */

        AgateRestModuleWriter.GenerationResult moduleResult =
                restModuleWriter.write(
                        operation,
                        restModulesDirectory
                );


        return new GenerationResult(
                baseName,
                operation.getIdentity(),
                csvFile,
                yamlFile,
                moduleResult.getModuleName(),
                moduleResult.getModuleDirectory(),
                moduleResult.getMetadataFile(),
                moduleResult.getRequestFile()
        );
    }




    private void validate(
            AgateOperationModel operation,
            AgateExecutableTestPlan plan,
            Path appDirectory) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (plan == null) {

            throw new IllegalArgumentException(
                    "Executable test plan must not be null"
            );
        }


        if (appDirectory == null) {

            throw new IllegalArgumentException(
                    "Application directory must not be null"
            );
        }
    }




    public static class GenerationResult {


        private final String testSuiteName;

        private final String operationIdentity;


        private final Path csvFile;

        private final Path yamlFile;


        private final String moduleName;

        private final Path moduleDirectory;

        private final Path metadataFile;

        private final Path requestFile;




        public GenerationResult(
                String testSuiteName,
                String operationIdentity,
                Path csvFile,
                Path yamlFile,
                String moduleName,
                Path moduleDirectory,
                Path metadataFile,
                Path requestFile) {

            this.testSuiteName =
                    testSuiteName;


            this.operationIdentity =
                    operationIdentity;


            this.csvFile =
                    csvFile;


            this.yamlFile =
                    yamlFile;


            this.moduleName =
                    moduleName;


            this.moduleDirectory =
                    moduleDirectory;


            this.metadataFile =
                    metadataFile;


            this.requestFile =
                    requestFile;
        }




        public String getTestSuiteName() {

            return testSuiteName;
        }




        public String getOperationIdentity() {

            return operationIdentity;
        }




        public Path getCsvFile() {

            return csvFile;
        }




        public Path getYamlFile() {

            return yamlFile;
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