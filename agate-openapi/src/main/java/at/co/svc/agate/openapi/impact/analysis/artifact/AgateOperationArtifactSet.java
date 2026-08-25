package at.co.svc.agate.openapi.impact.analysis.artifact;

import java.nio.file.Path;


public class AgateOperationArtifactSet {


    private final String operationIdentity;

    private final String baseName;


    private final Path csvFile;

    private final Path yamlFile;

    private final Path restModuleDirectory;

    private final Path requestJsonFile;

    private final Path metadataFile;




    public AgateOperationArtifactSet(
            String operationIdentity,
            String baseName,
            Path csvFile,
            Path yamlFile,
            Path restModuleDirectory,
            Path requestJsonFile,
            Path metadataFile) {

        this.operationIdentity =
                operationIdentity;


        this.baseName =
                baseName;


        this.csvFile =
                csvFile;


        this.yamlFile =
                yamlFile;


        this.restModuleDirectory =
                restModuleDirectory;


        this.requestJsonFile =
                requestJsonFile;


        this.metadataFile =
                metadataFile;
    }




    public String getOperationIdentity() {

        return operationIdentity;
    }




    public String getBaseName() {

        return baseName;
    }




    public Path getCsvFile() {

        return csvFile;
    }




    public Path getYamlFile() {

        return yamlFile;
    }




    public Path getRestModuleDirectory() {

        return restModuleDirectory;
    }




    public Path getRequestJsonFile() {

        return requestJsonFile;
    }




    public Path getMetadataFile() {

        return metadataFile;
    }
}