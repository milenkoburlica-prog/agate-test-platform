package at.co.svc.open.api.spec.generator;

import java.util.List;

import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.ParameterLocation;

public class MetadataGenerator {

    public String generate(EndpointDescription endpoint, List<ApiParameter> parameters) {

        StringBuilder url = new StringBuilder();

        url.append("{{endpoint}}").append(endpoint.getPath());

        boolean firstQuery = true;

        for (ApiParameter p : parameters) {

            if (p.getLocation() == ParameterLocation.QUERY) {

                if (firstQuery) {

                    url.append("?");

                    firstQuery = false;

                } else {

                    url.append("&");

                }

                url.append(p.getName()).append("={B[").append(p.getName()).append("]}");
            }
        }

        return """
                {
                  "url" : "%s",
                  "method" : "%s",
                  "headers" : {
                       "Content-Type" : "application/json;charset=UTF-8",
                       "accept" : "application/json"
                  }
                }
                """.formatted(url.toString(), endpoint.getMethod().toUpperCase());
    }

}