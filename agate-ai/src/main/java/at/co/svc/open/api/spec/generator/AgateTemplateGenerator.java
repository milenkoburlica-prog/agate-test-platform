package at.co.svc.open.api.spec.generator;

import java.util.List;

import at.co.svc.open.api.spec.model.GeneratedTestCase;


public class AgateTemplateGenerator {


    public String generate(
            List<GeneratedTestCase> tests) {


        StringBuilder yaml =
                new StringBuilder();


        yaml.append("testCases:\n");


        if (tests == null || tests.isEmpty()) {

            return yaml.toString();

        }


        GeneratedTestCase templateCase =
                findTemplateCase(tests);



        yaml.append("""
                
  - id: "{XL[testcaseName]}"
    description: "{XL[testcaseDescription]}"
    stage: "*"
    priority: HIGH

    variables:
      apiEndpoint: "{XL[apiEndpoint]}"

    steps:
      - type: REST
        op: EXEC
        command: rest.%s
        endpoint: "{B[apiEndpoint]}"
"""
        .formatted(
                templateCase.getCommand()
        ));



        appendParametersTemplate(
                yaml,
                templateCase
        );



        yaml.append("""
        response: response_1

      - type: REST
        op: ASSERT
        response: response_1
        source: STATUS
        action: EQUALS
        expected: "{B[statusCode]}"
""");



        appendResponseValidations(
                yaml,
                templateCase
        );



        return yaml.toString();

    }





    private GeneratedTestCase findTemplateCase(
            List<GeneratedTestCase> tests) {


        for(GeneratedTestCase tc : tests) {

            if(tc.getResponseValidations() != null
                    && !tc.getResponseValidations().isEmpty()) {

                return tc;

            }

        }


        return tests.get(0);

    }





    private void appendParametersTemplate(
            StringBuilder yaml,
            GeneratedTestCase tc) {


        if(tc.getParameters() == null
                || tc.getParameters().isEmpty()) {

            return;

        }



        yaml.append("""
        parameters:
""");



        tc.getParameters()
                .forEach(parameter -> {


                    yaml.append("""
          %s: "{B[%s]}"
"""
                    .formatted(
                            parameter.getName(),
                            parameter.getName()
                    ));


                });

    }





    private void appendResponseValidations(
            StringBuilder yaml,
            GeneratedTestCase tc) {


        if(tc.getResponseValidations() == null
                || tc.getResponseValidations().isEmpty()) {

            return;

        }



        tc.getResponseValidations()
                .forEach(field -> {


                    String assertName =
                            createAssertName(
                                    field.getPath()
                            );



                    yaml.append("""
        
      - type: REST
        op: ASSERT
        condition: "{B[statusCode]} == '200' AND {XL[%s]} != NULL"
        response: response_1
        source: BODY
        action: EXISTS
        path: "{XL[%s]}"
"""
                    .formatted(
                            assertName,
                            assertName
                    ));


                });

    }





    private String createAssertName(
            String path) {


        return "assert."
                + path
                    .replace("[*].", "")
                    .replace(".", "_");

    }

}