package at.co.svc.agate.openapi.impact.analysis.summary;

import at.co.svc.agate.openapi.impact.analysis.model.AgateArtifactType;
import at.co.svc.agate.openapi.impact.analysis.model.AgateImpactReport;
import at.co.svc.agate.openapi.impact.analysis.model.AgateRecommendedAction;
import at.co.svc.agate.openapi.impact.analysis.model.AgateTestCaseImpact;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class AgateImpactSummaryBuilder {


    public AgateImpactSummary build(
            AgateImpactReport report) {

        if (report == null) {

            throw new IllegalArgumentException(
                    "Impact report must not be null"
            );
        }


        AgateImpactSummary summary =
                new AgateImpactSummary();


        summary.setOpenImpacts(
                report.getOpenImpactCount()
        );


        Map<AgateRecommendedAction, Integer> actionCounts =
                new EnumMap<>(
                        AgateRecommendedAction.class
                );


        Map<String, Integer> operationCounts =
                new LinkedHashMap<>();


        Map<AgateArtifactType, Integer> artifactCounts =
                new EnumMap<>(
                        AgateArtifactType.class
                );


        /*
         * Initialize every enum value with zero.
         *
         * This is intentional so that the CLI can also show:
         *
         * YAML         : 0
         * REQUEST_JSON : 0
         */

        for (AgateRecommendedAction action :
                AgateRecommendedAction.values()) {

            actionCounts.put(
                    action,
                    0
            );
        }


        for (AgateArtifactType artifactType :
                AgateArtifactType.values()) {

            artifactCounts.put(
                    artifactType,
                    0
            );
        }


        for (AgateTestCaseImpact impact :
                report.getImpacts()) {

            if (impact == null) {

                continue;
            }


            AgateRecommendedAction action =
                    impact.getAction();


            if (action != null) {

                actionCounts.compute(
                        action,
                        (key, value) ->
                                value == null
                                        ? 1
                                        : value + 1
                );
            }


            String operation =
                    impact.getOperationIdentity();


            if (operation != null &&
                    !operation.isBlank()) {

                operationCounts.compute(
                        operation,
                        (key, value) ->
                                value == null
                                        ? 1
                                        : value + 1
                );
            }


            AgateArtifactType artifactType =
                    impact.getArtifactType();


            if (artifactType != null) {

                artifactCounts.compute(
                        artifactType,
                        (key, value) ->
                                value == null
                                        ? 1
                                        : value + 1
                );
            }
        }


        for (Map.Entry<
                AgateRecommendedAction,
                Integer
                > entry :
                actionCounts.entrySet()) {

            summary.putActionCount(
                    entry.getKey(),
                    entry.getValue()
            );
        }


        for (Map.Entry<String, Integer> entry :
                operationCounts.entrySet()) {

            summary.putOperationCount(
                    entry.getKey(),
                    entry.getValue()
            );
        }


        for (Map.Entry<
                AgateArtifactType,
                Integer
                > entry :
                artifactCounts.entrySet()) {

            summary.putArtifactCount(
                    entry.getKey(),
                    entry.getValue()
            );
        }


        return summary;
    }
}