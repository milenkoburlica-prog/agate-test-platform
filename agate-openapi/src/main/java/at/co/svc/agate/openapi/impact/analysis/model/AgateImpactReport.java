package at.co.svc.agate.openapi.impact.analysis.model;

import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateImpactReport {


    private AgateOpenApiChangeSet changeSet;


    private final Map<
            AgateImpactKey,
            AgateTestCaseImpact
            > impacts =
            new LinkedHashMap<>();




    public AgateOpenApiChangeSet getChangeSet() {

        return changeSet;
    }




    public void setChangeSet(
            AgateOpenApiChangeSet changeSet) {

        this.changeSet =
                changeSet;
    }




    public void addImpact(
            AgateTestCaseImpact impact) {

        if (impact == null) {

            return;
        }


        AgateImpactKey key =
                new AgateImpactKey(
                        impact.getOperationIdentity(),
                        impact.getArtifactType(),
                        impact.getArtifact(),
                        impact.getArtifactLocation(),
                        impact.getTestcaseName(),
                        impact.getAction()
                );


        AgateTestCaseImpact existing =
                impacts.get(
                        key
                );


        if (existing == null) {

            impacts.put(
                    key,
                    impact
            );


            return;
        }


        /*
         * Several API changes can result in the same
         * artifact action.
         *
         * Example:
         *
         * age.minimum changed
         * age.maximum changed
         *
         * but CSV row age is missing.
         *
         * We report ADD_CSV_ROW only once.
         */

        String existingReason =
                existing.getReason();


        String newReason =
                impact.getReason();


        if (newReason != null &&
                !newReason.isBlank() &&
                (
                        existingReason == null
                                ||
                                !existingReason.contains(
                                        newReason
                                )
                )) {

            if (existingReason == null ||
                    existingReason.isBlank()) {

                existing.setReason(
                        newReason
                );

            } else {

                existing.setReason(
                        existingReason
                                + "; "
                                + newReason
                );
            }
        }
    }




    public List<AgateTestCaseImpact> getImpacts() {

        return new ArrayList<>(
                impacts.values()
        );
    }




    public int getOpenImpactCount() {

        return impacts.size();
    }




    public boolean isClean() {

        return impacts.isEmpty();
    }
}