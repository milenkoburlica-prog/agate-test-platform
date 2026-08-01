package at.co.svc.open.api.spec.generator;


import java.util.List;

import at.co.svc.open.api.spec.model.GeneratedTestCase;



public class AgateYamlGenerator {


    public String generate(
            String moduleName,
            String baseUrl,
            List<GeneratedTestCase> tests) {


        StringBuilder yaml =
                new StringBuilder();


        yaml.append("testCases:\n");



        for (GeneratedTestCase tc : tests) {



            yaml.append("""
                    
  - id: %s
    description: "%s"
    stage: "*"
    priority: HIGH

    variables:
      apiEndpoint: "%s"

    steps:
      - type: REST
        op: EXEC
        command: rest.%s
        endpoint: "{B[apiEndpoint]}"
"""
            .formatted(
                    tc.getName(),
                    tc.getDescription(),
                    baseUrl,
                    moduleName
            ));



            appendParameters(
                    yaml,
                    tc
            );



            yaml.append("""
        response: response_1
""");



            if (tc.getResponseValidations() != null
                    && !tc.getResponseValidations().isEmpty()) {



                for (var field :
                        tc.getResponseValidations()) {


                    yaml.append("""
                    
      - type: REST
        op: ASSERT
        response: response_1
        source: BODY
        action: EXISTS
        path: %s
"""
                    .formatted(
                            field.getPath()
                    ));

                }


            }
            else {


                yaml.append("""
                    
      - type: REST
        op: ASSERT
        response: response_1
        source: STATUS
        action: EQUALS
        expected: %s
"""
                .formatted(
                        extractStatus(
                                tc.getExpectedResult()
                        )
                ));

            }


        }


        return yaml.toString();

    }







    private void appendParameters(
            StringBuilder yaml,
            GeneratedTestCase tc) {


        if (tc.getTestData() == null
                || tc.getTestData().isBlank()) {

            return;
        }


        yaml.append("""
            parameters:
    """);



        for (String p :
                tc.getTestData().split("&")) {


            String[] pair =
                    p.split("=", 2);


            if (pair.length == 2) {


                yaml.append("""
              %s: %s
    """
                .formatted(
                        pair[0],
                        pair[1]
                ));

            }

        }

    }






    private String extractStatus(
            String expected) {


        if (expected == null) {

            return "200";
        }


        return expected
                .replace("HTTP", "")
                .trim();

    }

}