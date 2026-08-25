package at.co.svc.agate.openapi.impact.analysis.contract;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateNewContractIndex {


    private final Map<String, AgateOperationModel> operations =
            new LinkedHashMap<>();




    public AgateNewContractIndex(
            AgateOpenApiModel model) {

        if (model == null ||
                model.getEndpoints() == null) {

            return;
        }


        AgateOperationModelBuilder builder =
                new AgateOperationModelBuilder();


        for (AgateEndpoint endpoint :
                model.getEndpoints()) {

            AgateOperationModel operation =
                    builder.build(
                            endpoint
                    );


            operations.put(
                    operation.getIdentity(),
                    operation
            );
        }
    }




    public AgateOperationModel getOperation(
            String identity) {

        return operations.get(
                identity
        );
    }




    public boolean containsOperation(
            String identity) {

        return operations.containsKey(
                identity
        );
    }




    public List<AgateOperationModel> getOperations() {

        return new ArrayList<>(
                operations.values()
        );
    }
}