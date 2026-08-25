package at.co.svc.agate.openapi.phase1;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import java.util.ArrayList;
import java.util.List;

public class AgatePhase1ModelBuilder {

    private final AgateOperationModelBuilder operationModelBuilder =
            new AgateOperationModelBuilder();


    public List<AgateOperationModel> build(
            AgateOpenApiModel model) {

        List<AgateOperationModel> result =
                new ArrayList<>();

        if (model == null ||
                model.getEndpoints() == null) {

            return result;
        }

        for (AgateEndpoint endpoint :
                model.getEndpoints()) {

            result.add(
                    operationModelBuilder.build(
                            endpoint
                    )
            );
        }

        return result;
    }


    public AgateOperationModel find(
            AgateOpenApiModel model,
            String identity) {

        if (model == null ||
                identity == null) {

            return null;
        }

        return model
                .getEndpoints()
                .stream()
                .filter(endpoint ->
                        identity.equals(
                                endpoint.getIdentity()
                        )
                )
                .findFirst()
                .map(
                        operationModelBuilder::build
                )
                .orElse(null);
    }
}