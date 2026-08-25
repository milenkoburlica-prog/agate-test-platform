package at.co.svc.agate.openapi.impact.analysis;

import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import java.util.ArrayList;
import java.util.List;


public class AgateImpactReport {


    private AgateOpenApiChangeSet changeSet;


    private final List<AgateArtifactImpact> impacts =
            new ArrayList<>();




    public AgateOpenApiChangeSet getChangeSet() {

        return changeSet;
    }


    public void setChangeSet(
            AgateOpenApiChangeSet changeSet) {

        this.changeSet =
                changeSet;
    }




    public void addImpact(
            AgateArtifactImpact impact) {

        if (impact == null) {

            return;
        }


        impacts.add(
                impact
        );
    }




    public List<AgateArtifactImpact> getImpacts() {

        return new ArrayList<>(
                impacts
        );
    }




    public int size() {

        return impacts.size();
    }
}