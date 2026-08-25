package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeDetector;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import at.co.svc.agate.openapi.impact.analysis.AgateImpactAnalyzer;
import at.co.svc.agate.openapi.impact.analysis.model.AgateImpactReport;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class AgateImpactAnalyzerTest {


    @Test
    void shouldReportAffectedAgateArtifacts()
            throws Exception {

        AgateOpenApiModel oldModel =
                parse(
                        "change/test-change-v1.yaml"
                );


        AgateOpenApiModel newModel =
                parse(
                        "change/test-change-v2b.yaml"
                );


        AgateOpenApiChangeSet changeSet =
                new AgateOpenApiChangeDetector()
                        .detect(
                                oldModel,
                                newModel
                        );


        /*
         * Impact Analyzer now works application-oriented.
         *
         * It resolves CSV/YAML/REST module files itself
         * for every affected API operation.
         */

        Path appDirectory =
                Path.of(
                        "data/demo"
                );


        AgateImpactReport report =
                new AgateImpactAnalyzer()
                        .analyze(
                                changeSet,
                                newModel,
                                appDirectory
                        );


        assertTrue(
                report
                        .getImpacts()
                        .stream()
                        .anyMatch(impact ->
                                contains(
                                        impact.getChangeLocation(),
                                        "username"
                                )
                                        ||
                                        contains(
                                                impact.getArtifactLocation(),
                                                "username"
                                        )
                        )
        );


        assertTrue(
                report
                        .getImpacts()
                        .stream()
                        .anyMatch(impact ->
                                contains(
                                        impact.getChangeLocation(),
                                        "tenantId"
                                )
                                        ||
                                        contains(
                                                impact.getArtifactLocation(),
                                                "tenantId"
                                        )
                        )
        );


        assertTrue(
                report.getOpenImpactCount() > 0
        );
    }




    private boolean contains(
            String value,
            String expected) {

        return value != null
                &&
                value.contains(
                        expected
                );
    }




    private AgateOpenApiModel parse(
            String resource)
            throws Exception {

        var url =
                Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(
                                resource
                        );


        if (url == null) {

            throw new IllegalStateException(
                    "Resource not found: "
                            + resource
            );
        }


        return new AgateOpenApiParser()
                .parse(
                        Path.of(
                                url.toURI()
                        )
                                .toString()
                );
    }
}