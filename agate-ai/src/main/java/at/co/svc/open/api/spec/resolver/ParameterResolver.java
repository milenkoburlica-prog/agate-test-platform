package at.co.svc.open.api.spec.resolver;


import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;



public class ParameterResolver {


    public Parameter resolve(
            Parameter parameter,
            OpenAPI api) {


        if (parameter == null) {
            return null;
        }


        /*
         * Already resolved
         */
        if (parameter.getName() != null) {
            return parameter;
        }


        String ref = parameter.get$ref();


        if (ref == null) {
            return parameter;
        }



        /*
         * Example:
         *
         * #/components/parameters/ClientIp
         */
        if (ref.startsWith("#/components/parameters/")) {


            String name =
                    ref.substring(
                        "#/components/parameters/".length());


            Map<String, Parameter> parameters =
                    api.getComponents()
                       .getParameters();


            if (parameters != null) {

                Parameter resolved =
                        parameters.get(name);


                if (resolved != null) {

                    return resolved;

                }
            }
        }


        return parameter;

    }

}