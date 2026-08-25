package at.co.svc.agate.openapi.impact.analysis;

import java.nio.file.Path;


public class AgateArtifactImpact {


    private Path artifact;

    private String location;

    private AgateImpactType impactType;

    private String reason;




    public Path getArtifact() {

        return artifact;
    }


    public void setArtifact(
            Path artifact) {

        this.artifact =
                artifact;
    }




    public String getLocation() {

        return location;
    }


    public void setLocation(
            String location) {

        this.location =
                location;
    }




    public AgateImpactType getImpactType() {

        return impactType;
    }


    public void setImpactType(
            AgateImpactType impactType) {

        this.impactType =
                impactType;
    }




    public String getReason() {

        return reason;
    }


    public void setReason(
            String reason) {

        this.reason =
                reason;
    }
}