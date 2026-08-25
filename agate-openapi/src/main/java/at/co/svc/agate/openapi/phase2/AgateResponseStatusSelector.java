package at.co.svc.agate.openapi.phase2;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseModel;

import java.util.Comparator;

public class AgateResponseStatusSelector {


    public String selectSuccessStatus(
            AgateOperationModel operation) {

        if (operation == null ||
                operation.getResponses() == null) {

            return null;
        }

        return operation
                .getResponses()
                .stream()
                .map(
                        AgateResponseModel::getStatusCode
                )
                .filter(
                        this::isSuccessStatus
                )
                .sorted(
                        Comparator.comparingInt(
                                Integer::parseInt
                        )
                )
                .findFirst()
                .orElse(null);
    }


    private boolean isSuccessStatus(
            String statusCode) {

        if (statusCode == null ||
                statusCode.length() != 3) {

            return false;
        }

        try {

            int code =
                    Integer.parseInt(
                            statusCode
                    );

            return code >= 200
                    && code < 300;

        } catch (NumberFormatException e) {

            return false;
        }
    }
}