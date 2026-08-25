package at.co.svc.agate.openapi.impact.analysis.model;

import java.nio.file.Path;
import java.util.Objects;


public class AgateImpactKey {


    private final String operationIdentity;

    private final AgateArtifactType artifactType;

    private final Path artifact;

    private final String artifactLocation;

    private final String testcaseName;

    private final AgateRecommendedAction action;




    public AgateImpactKey(
            String operationIdentity,
            AgateArtifactType artifactType,
            Path artifact,
            String artifactLocation,
            String testcaseName,
            AgateRecommendedAction action) {

        this.operationIdentity =
                operationIdentity;


        this.artifactType =
                artifactType;


        this.artifact =
                artifact;


        this.artifactLocation =
                artifactLocation;


        this.testcaseName =
                testcaseName;


        this.action =
                action;
    }




    @Override
    public boolean equals(
            Object object) {

        if (this == object) {

            return true;
        }


        if (!(object instanceof AgateImpactKey other)) {

            return false;
        }


        return Objects.equals(
                operationIdentity,
                other.operationIdentity
        )
                &&
                artifactType
                        == other.artifactType
                &&
                Objects.equals(
                        artifact,
                        other.artifact
                )
                &&
                Objects.equals(
                        artifactLocation,
                        other.artifactLocation
                )
                &&
                Objects.equals(
                        testcaseName,
                        other.testcaseName
                )
                &&
                action
                        == other.action;
    }




    @Override
    public int hashCode() {

        return Objects.hash(
                operationIdentity,
                artifactType,
                artifact,
                artifactLocation,
                testcaseName,
                action
        );
    }
}