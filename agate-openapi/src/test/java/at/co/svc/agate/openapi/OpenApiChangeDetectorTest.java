package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.change.detection.AgateApiChange;
import at.co.svc.agate.openapi.change.detection.AgateChangeType;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeDetector;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class OpenApiChangeDetectorTest {


    @Test
    void shouldDetectCompatibleV2aChanges()
            throws Exception {

        AgateOpenApiChangeSet changes =
                detect(
                        "change/test-change-v1.yaml",
                        "change/test-change-v2a.yaml"
                );


        assertTrue(
                contains(
                        changes,
                        "GET:/users/{id}/history",
                        "operation",
                        AgateChangeType.ADDED
                )
        );


        assertTrue(
                contains(
                        changes,
                        "GET:/users/{id}",
                        "request.query.language",
                        AgateChangeType.ADDED
                )
        );


        assertTrue(
                contains(
                        changes,
                        "POST:/users",
                        "request.body.comment",
                        AgateChangeType.ADDED
                )
        );
    }




    @Test
    void shouldDetectBreakingV2bChanges()
            throws Exception {

        AgateOpenApiChangeSet changes =
                detect(
                        "change/test-change-v1.yaml",
                        "change/test-change-v2b.yaml"
                );


        assertTrue(
                containsPropertyChange(
                        changes,
                        "GET:/users/{id}",
                        "request.path.id",
                        "type"
                )
        );


        assertTrue(
                containsPropertyChange(
                        changes,
                        "GET:/users/{id}",
                        "request.query.details",
                        "required"
                )
        );


        assertTrue(
                containsPropertyChange(
                        changes,
                        "POST:/users",
                        "request.body.username",
                        "maxLength"
                )
        );


        assertTrue(
                contains(
                        changes,
                        "POST:/users",
                        "request.body.age",
                        AgateChangeType.REMOVED
                )
        );


        assertTrue(
                contains(
                        changes,
                        "POST:/users",
                        "request.body.tenantId",
                        AgateChangeType.ADDED
                )
        );
    }




    private AgateOpenApiChangeSet detect(
            String oldResource,
            String newResource)
            throws Exception {

        AgateOpenApiModel oldModel =
                parse(
                        oldResource
                );


        AgateOpenApiModel newModel =
                parse(
                        newResource
                );


        return new AgateOpenApiChangeDetector()
                .detect(
                        oldModel,
                        newModel
                );
    }




    private boolean contains(
            AgateOpenApiChangeSet set,
            String operation,
            String location,
            AgateChangeType type) {

        return set
                .getChanges()
                .stream()
                .anyMatch(change ->
                        operation.equals(
                                change.getOperationIdentity()
                        )
                                &&
                                location.equals(
                                        change.getLocation()
                                )
                                &&
                                type
                                        == change.getChangeType()
                );
    }




    private boolean containsPropertyChange(
            AgateOpenApiChangeSet set,
            String operation,
            String location,
            String property) {

        return set
                .getChanges()
                .stream()
                .anyMatch(change ->
                        operation.equals(
                                change.getOperationIdentity()
                        )
                                &&
                                location.equals(
                                        change.getLocation()
                                )
                                &&
                                property.equals(
                                        change.getProperty()
                                )
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