package at.co.svc.open.api.spec.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class GeneratedTestCase {


    private List<ApiParameter> parameters;


    private String name;


    private String description;


    private String endpoint;


    private String method;


    private String category;


    private String expectedResult;


    private String testData;


    /*
     * OpenAPI generated REST command.
     *
     * Example:
     * get_clv_svsc_v1_connections__clientIp__ginos
     */
    private String command;



    private List<ResponseField> responseValidations =
            new ArrayList<>();




    @SuppressWarnings("deprecation")
    @JsonSetter("testData")
    public void setTestData(Object value) {


        if(value == null) {

            this.testData = null;

            return;

        }



        if(value instanceof String) {

            this.testData = (String)value;

            return;

        }



        if(value instanceof JsonNode node) {


            StringBuilder result =
                    new StringBuilder();



            node.fields()
                .forEachRemaining(entry -> {


                    if(result.length() > 0) {

                        result.append("&");

                    }


                    result.append(entry.getKey())
                          .append("=")
                          .append(entry.getValue().asText());


                });



            this.testData =
                    result.toString();

        }

    }




    public void addResponseValidation(
            ResponseField validation) {


        responseValidations.add(
                validation
        );

    }

}