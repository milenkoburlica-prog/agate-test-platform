package at.co.svc.open.api.spec.generator;


import java.util.ArrayList;
import java.util.List;

import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.EndpointDescription;



public class NegativeTestDataGenerator {



    public List<String> generate(
            EndpointDescription endpoint) {


        List<String> result =
                new ArrayList<>();


        if (endpoint.getParameters() == null) {

            return result;
        }



        for (ApiParameter p :
                endpoint.getParameters()) {


            if (p.getName() == null) {
                continue;
            }



            /*
             * Missing mandatory parameter
             */
            if (p.isRequired()) {


                result.add(
                        removeParameter(
                                endpoint,
                                p
                        )
                );
            }




            /*
             * Invalid enum value
             */
            if (p.getEnumValues() != null
                    &&
                !p.getEnumValues().isEmpty()) {


                result.add(
                        replaceValue(
                                p,
                                "INVALID"
                        )
                );
            }





            /*
             * Pattern violation
             */
            if (p.getPattern() != null) {


                result.add(
                        replaceValue(
                                p,
                                "###INVALID###"
                        )
                );
            }





            /*
             * Wrong datatype
             */
            if ("integer".equals(p.getType())) {


                result.add(
                        replaceValue(
                                p,
                                "ABC"
                        )
                );
            }
        }


        return result;
    }






    private String removeParameter(
            EndpointDescription endpoint,
            ApiParameter remove) {


        StringBuilder sb =
                new StringBuilder();



        for (ApiParameter p :
                endpoint.getParameters()) {


            if (p.getName() != null
                    &&
                p.getName().equals(remove.getName())) {


                continue;
            }



            sb.append(
                    p.getName()
            )
            .append("=")
            .append(
                    validValue(p)
            )
            .append("&");
        }



        return sb.toString()
                .replaceAll("&$", "");
    }







    private String replaceValue(
            ApiParameter parameter,
            String value) {


        return parameter.getName()
                +
                "="
                +
                value;
    }







    private String validValue(
            ApiParameter p) {


        if (p.getExample() != null) {

            return p.getExample();
        }



        if (p.getEnumValues() != null
                &&
            !p.getEnumValues().isEmpty()) {


            return p.getEnumValues()
                    .get(0);
        }



        if ("integer".equals(p.getType())) {

            return "1";
        }



        return "TEST";
    }

}