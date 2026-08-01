package at.co.svc.open.api.spec.generator;


import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.EndpointDescription;


public class OpenApiTestDataGenerator {


    public String generateValid(
            EndpointDescription endpoint) {


        if (endpoint == null
                || endpoint.getParameters() == null
                || endpoint.getParameters().isEmpty()) {


            if (endpoint != null) {

//                System.out.println(
//                        "WARNING: No parameters found for endpoint: "
//                        + endpoint.getPath()
//                );
            }

            return "";
        }



        StringBuilder result =
                new StringBuilder();



        for (ApiParameter p :
                endpoint.getParameters()) {


            /** only for DEBUGING 
            System.out.println(
                    "Generating test data: "
                    + p.getName()
                    + " | location="
                    + p.getLocation()
                    + " | required="
                    + p.isRequired()
                    + " | example="
                    + p.getExample()
                    + " | pattern="
                    + p.getPattern()
                    + " | enum="
                    + p.getEnumValues()
            );
            **/



            /*
             * Ignore optional query parameters.
             */
            if (!p.isRequired()
                    && p.getLocation() != null
                    && p.getLocation()
                           .name()
                           .equals("QUERY")) {


                continue;
            }



            result.append(
                    p.getName()
            )
            .append("=")
            .append(
                    generateValue(p)
            )
            .append("&");

        }



        return result.toString()
                .replaceAll("&$", "");

    }







    private String generateValue(
            ApiParameter p) {



        /*
         * 1. Explicit example
         */
        if (p.getExample() != null
                && !p.getExample().isBlank()) {


            return p.getExample();

        }



        /*
         * 2. Enum values
         */
        if (p.getEnumValues() != null
                && !p.getEnumValues().isEmpty()) {


            return p.getEnumValues()
                    .get(0);

        }



        String pattern =
                p.getPattern();



        /*
         * 3. Pattern based generation
         */
        if (pattern != null) {


            if (pattern.contains("\\d{10}")
                    || pattern.contains("[0-9]{10}")) {


                return "1234567890";

            }



            if (pattern.contains("25[0-5]")
                    || pattern.contains("\\.")) {


                return "111.111.111.111";

            }



            if (pattern.contains("config")
                    && pattern.contains("boot")) {


                return "config";

            }



            if (pattern.contains(".{6}")) {


                return "123456";

            }

        }



        /*
         * 4. Name based fallback
         */
        String name =
                p.getName()
                 .toLowerCase();



        if (name.contains("ip")) {

            return "111.111.111.111";

        }



        if (name.contains("serial")) {

            return "1234567890";

        }



        if (name.contains("timeout")) {

            return "2000";

        }



        /*
         * 5. Type fallback
         */
        if ("integer".equals(p.getType())) {

            return "1";

        }



        if ("string".equals(p.getType())) {

            return "TEST";

        }



        return "TEST";

    }

}