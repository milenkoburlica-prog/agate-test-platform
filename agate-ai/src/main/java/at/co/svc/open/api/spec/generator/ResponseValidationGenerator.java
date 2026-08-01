package at.co.svc.open.api.spec.generator;


import java.util.List;

import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;
import at.co.svc.open.api.spec.model.ResponseField;



public class ResponseValidationGenerator {


    public GeneratedTestCase generate(
            EndpointDescription endpoint) {


        List<ResponseField> fields =
                endpoint.getValidations();


        if(fields == null || fields.isEmpty()) {

            return null;
        }



        GeneratedTestCase tc =
                new GeneratedTestCase();



        tc.setName(
                "Verify_Response_Fields_"
                + endpoint.getOperationId()
        );


        tc.setDescription(
                "Verify response contains documented response fields"
        );


        tc.setEndpoint(
                endpoint.getPath()
        );


        tc.setMethod(
                endpoint.getMethod()
        );


        tc.setCategory(
                "response-validation"
        );


        tc.setExpectedResult(
                "HTTP 200"
        );


        tc.setParameters(
                endpoint.getParameters()
        );


        for(ResponseField field : fields) {

            tc.addResponseValidation(field);

        }


        return tc;

    }

}