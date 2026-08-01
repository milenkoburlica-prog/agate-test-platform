package at.co.svc.open.api.spec.generator;


import java.util.HashSet;
import java.util.Set;

import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.EndpointDescription;



public class EndpointContextBuilder {


    public Set<String> buildAllowedParameterNames(
            EndpointDescription endpoint) {


        Set<String> names =
                new HashSet<>();


        for(ApiParameter p :
                endpoint.getParameters()) {


            if(p != null &&
               p.getName()!=null) {

                names.add(
                        p.getName()
                );
            }
        }


        return names;
    }




    public boolean containsParameter(
            EndpointDescription endpoint,
            String name) {


        return buildAllowedParameterNames(endpoint)
                .contains(name);
    }

}