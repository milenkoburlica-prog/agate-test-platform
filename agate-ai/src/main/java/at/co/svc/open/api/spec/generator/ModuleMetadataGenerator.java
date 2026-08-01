package at.co.svc.open.api.spec.generator;


import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.ParameterLocation;



public class ModuleMetadataGenerator {


    public String generate(
            EndpointDescription endpoint) {


        StringBuilder url =
                new StringBuilder(
                        "{{endpoint}}"
                );



        String path =
                endpoint.getPath();



        for(ApiParameter p :
                endpoint.getParameters()) {


            if(p.getLocation()
                    == ParameterLocation.PATH) {


                path =
                path.replace(
                        "{"+p.getName()+"}",
                        "{B["+p.getName()+"]}"
                );

            }


            if(p.getLocation()
                    == ParameterLocation.QUERY) {


                if(!path.contains("?")) {

                    path += "?";

                }
                else {

                    path += "&";

                }


                path +=
                        p.getName()
                        +"={B["
                        +p.getName()
                        +"]}";

            }

        }



        url.append(path);



        return """
{
  "url":"%s",
  "method":"%s",
  "headers":{
    "Content-Type":"application/json;charset=UTF-8",
    "accept":"application/json"
  }
}
"""
.formatted(
        url,
        endpoint.getMethod()
                .toUpperCase()
);

    }

}