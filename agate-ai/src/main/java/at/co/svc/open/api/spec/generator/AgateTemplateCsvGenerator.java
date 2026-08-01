package at.co.svc.open.api.spec.generator;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import at.co.svc.open.api.spec.model.GeneratedTestCase;



public class AgateTemplateCsvGenerator {


    public String generate(String baseUrl,
            List<GeneratedTestCase> tests) {


        if (tests == null || tests.isEmpty()) {

            return "";

        }


        StringBuilder csv =
                new StringBuilder();



        List<String> rows =
                new ArrayList<>();


        rows.add("testcaseName");
        rows.add("testcaseDescription");
        rows.add("apiEndpoint");
        rows.add("statusCode");



        Set<String> parameters =
                new LinkedHashSet<>();


        for (GeneratedTestCase tc : tests) {


            if (tc.getParameters() != null) {


                tc.getParameters()
                        .forEach(p ->
                                parameters.add(
                                        p.getName()
                                )
                        );

            }

        }


        rows.addAll(parameters);




        Set<String> responseFields =
                new LinkedHashSet<>();


        for (GeneratedTestCase tc : tests) {


            if (tc.getResponseValidations() != null) {


                tc.getResponseValidations()
                        .forEach(field ->
                                responseFields.add(
                                        createAssertName(
                                                field.getPath()
                                        )
                                )
                        );

            }

        }


        rows.addAll(responseFields);





        for (String row : rows) {


            csv.append(row);



            for (GeneratedTestCase tc : tests) {


                csv.append(";");


                csv.append(
                        resolveValue(
                                row,
                                tc,
                                baseUrl
                        )
                );


            }


            csv.append("\n");


        }



        return csv.toString();

    }







    private String resolveValue(
            String field,
            GeneratedTestCase tc,
            String baseUrl) {



        switch(field) {


            case "testcaseName":

                return safe(
                        tc.getName()
                );



            case "testcaseDescription":

                return safe(
                        tc.getDescription()
                );



            case "apiEndpoint":

                return baseUrl;



            case "statusCode":

                return extractStatus(
                        tc.getExpectedResult()
                );


            default:

                break;

        }







        if (tc.getParameters() != null) {


            for (var parameter :
                    tc.getParameters()) {



                if(parameter.getName()
                        .equals(field)) {



                    return extractParameterValue(
                            tc,
                            parameter.getName()
                    );

                }

            }

        }








        if(tc.getResponseValidations() != null) {


            for(var response :
                    tc.getResponseValidations()) {



                String assertName =
                        createAssertName(
                                response.getPath()
                        );



                if(assertName.equals(field)) {


                    return response.getPath();


                }


            }

        }



        return "";

    }







    private String extractParameterValue(
            GeneratedTestCase tc,
            String parameterName) {


        String data =
                tc.getTestData();



        if(data == null
                || data.isBlank()) {

            return "";

        }



        for(String entry :
                data.split("&")) {


            String[] pair =
                    entry.split("=", 2);



            if(pair.length == 2
                    && pair[0].equals(parameterName)) {


                return pair[1];

            }

        }



        return "";

    }





    private String createAssertName(
            String path) {


        return "assert."
                + path
                    .replace("[*].", "")
                    .replace(".", "_");

    }







    private String extractStatus(
            String expected) {


        if(expected == null) {

            return "";

        }


        return expected
                .replace(
                        "HTTP",
                        ""
                )
                .trim();

    }







    private String safe(
            String value) {


        return value == null
                ? ""
                : value.replace(
                        ";",
                        ","
                );

    }


}