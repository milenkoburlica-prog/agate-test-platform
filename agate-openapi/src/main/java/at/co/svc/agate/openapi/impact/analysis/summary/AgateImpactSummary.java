package at.co.svc.agate.openapi.impact.analysis.summary;

import at.co.svc.agate.openapi.impact.analysis.model.AgateArtifactType;
import at.co.svc.agate.openapi.impact.analysis.model.AgateRecommendedAction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class AgateImpactSummary {


    private int openImpacts;


    private final Map<AgateRecommendedAction, Integer> byAction =
            new LinkedHashMap<>();


    private final Map<String, Integer> byOperation =
            new LinkedHashMap<>();


    private final Map<AgateArtifactType, Integer> byArtifact =
            new LinkedHashMap<>();




    public int getOpenImpacts() {

        return openImpacts;
    }




    public void setOpenImpacts(
            int openImpacts) {

        this.openImpacts =
                openImpacts;
    }




    public Map<AgateRecommendedAction, Integer> getByAction() {

        return Collections.unmodifiableMap(
                byAction
        );
    }




    public void putActionCount(
            AgateRecommendedAction action,
            int count) {

        if (action == null) {

            return;
        }


        byAction.put(
                action,
                count
        );
    }




    public Map<String, Integer> getByOperation() {

        return Collections.unmodifiableMap(
                byOperation
        );
    }




    public void putOperationCount(
            String operationIdentity,
            int count) {

        if (operationIdentity == null ||
                operationIdentity.isBlank()) {

            return;
        }


        byOperation.put(
                operationIdentity,
                count
        );
    }




    public Map<AgateArtifactType, Integer> getByArtifact() {

        return Collections.unmodifiableMap(
                byArtifact
        );
    }




    public void putArtifactCount(
            AgateArtifactType artifactType,
            int count) {

        if (artifactType == null) {

            return;
        }


        byArtifact.put(
                artifactType,
                count
        );
    }
}