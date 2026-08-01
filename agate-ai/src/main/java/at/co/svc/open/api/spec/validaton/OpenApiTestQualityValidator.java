package at.co.svc.open.api.spec.validaton;


import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class OpenApiTestQualityValidator {


    public List<GeneratedTestCase> validate(
            List<GeneratedTestCase> testCases,
            EndpointDescription endpoint) {


        // Temp DEBUG
//        System.out.println(
//                "QUALITY VALIDATOR INPUT: "
//                + endpoint.getMethod()
//                + " "
//                + endpoint.getPath()
//                + " count="
//                + testCases.size()
//        );
        
        if(isAlive(endpoint)) {

            return validateAlive(
                    testCases,
                    endpoint
            );

        }



        List<GeneratedTestCase> result =
                new ArrayList<>();


        Set<String> unique =
                new HashSet<>();



        for(GeneratedTestCase tc:testCases) {


            normalize(tc);


            fixInvalidStatus(
                    tc,
                    endpoint
            );


            fixBusinessRules(
                    tc,
                    endpoint
            );
            preserveResponseValidation(tc);


            if(!isValidStatus(
                    tc,
                    endpoint)) {

                continue;

            }



            String key =
                    createKey(tc);



            if(unique.add(key)) {

                result.add(tc);

            }


        }

        // Temp DEBUG
        System.out.println(
                "QUALITY VALIDATOR OUTPUT count="
                + result.size()
        );


//        for(GeneratedTestCase tc : result) {
//
//            System.out.println(
//                    "KEEP: "
//                    + tc.getName()
//                    + " validations="
//                    + tc.getResponseValidations()
//            );
//        }

        return result;

    }





    private boolean isAlive(
            EndpointDescription endpoint) {


        return endpoint.getPath()!=null
                &&
               endpoint.getPath()
                .toLowerCase()
                .contains("alive");

    }






    private List<GeneratedTestCase> validateAlive(
            List<GeneratedTestCase> cases,
            EndpointDescription endpoint) {


        List<GeneratedTestCase> result =
                new ArrayList<>();


        Set<String> unique =
                new HashSet<>();


        for(GeneratedTestCase tc:cases) {


            String status =
                    tc.getExpectedResult();



            if(status==null) {

                status="HTTP 200";

            }



            if(!status.equals("HTTP 200")
                    &&
               !status.equals("HTTP 503")) {


                continue;

            }



            tc.setCategory(
                    "health-check"
            );


            tc.setTestData("");



            tc.setDescription(
                    "Verify that service alive endpoint is available"
            );



            String key =
                    endpoint.getMethod()
                    +
                    "|"
                    +
                    status;



            if(unique.add(key)) {

                result.add(tc);

            }

        }



        return result;

    }







    private void fixInvalidStatus(
            GeneratedTestCase tc,
            EndpointDescription endpoint) {


        String text =
                (
                tc.getName()
                +" "
                +tc.getDescription()
                )
                .toLowerCase();



        /*
         * invalid enum / schema errors
         */
        if(text.contains("invalid")
                &&
           text.contains("id")) {


            if(endpoint.getResponses()!=null
                    &&
               endpoint.getResponses()
                    .contains("400")) {


                tc.setExpectedResult(
                        "HTTP 400"
                );

            }

        }

    }







    private void fixBusinessRules(
            GeneratedTestCase tc,
            EndpointDescription endpoint) {


        String text =
                (
                tc.getName()
                +" "
                +tc.getDescription()
                )
                .toLowerCase();



        if(text.contains("deprecated")
                &&
           endpoint.getResponses()!=null
                &&
           endpoint.getResponses()
                .contains("410")) {


            tc.setExpectedResult(
                    "HTTP 410"
            );

        }


    }








    private boolean isValidStatus(
            GeneratedTestCase tc,
            EndpointDescription endpoint) {


        /*
         * Response validation tests do not validate HTTP status.
         * They validate response body fields.
         */
        if(tc.getResponseValidations()!=null
                && !tc.getResponseValidations().isEmpty()) {

            return true;

        }



        if(tc.getExpectedResult()==null) {

            return false;

        }



        if(endpoint.getResponses()==null
                ||
               endpoint.getResponses().isEmpty()) {

            return true;

        }



        String status =
                tc.getExpectedResult()
                  .replace("HTTP ", "");



        return endpoint.getResponses()
                .stream()
                .anyMatch(
                        r -> r.equals(status)
                );

    }
    







    private void normalize(
            GeneratedTestCase tc) {


        if(tc.getTestData()==null) {

            tc.setTestData("");

        }

    }







    private String createKey(
            GeneratedTestCase tc) {


        return tc.getEndpoint()
                +"|"
                +tc.getMethod()
                +"|"
                +tc.getExpectedResult()
                +"|"
                +tc.getDescription()
                +"|"
                +tc.getResponseValidations();


    }

    private void preserveResponseValidation(
            GeneratedTestCase tc) {


        if(tc.getResponseValidations()!=null
                && !tc.getResponseValidations().isEmpty()) {


            tc.setCategory(
                    "response-validation"
            );


            tc.setExpectedResult(
                    "HTTP 200"
            );

        }

    }
    
}