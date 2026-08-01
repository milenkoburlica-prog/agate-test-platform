package at.co.svc.open.api.spec.model;


import java.util.ArrayList;
import java.util.List;



public class EndpointDescription {


    private String path;

    private String method;

    private String operationId;

    private String summary;

    private String description;



    private List<ApiParameter> parameters =
            new ArrayList<>();


    private List<String> responses =
            new ArrayList<>();


    private List<ResponseField> validations =
            new ArrayList<>();




    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }



    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }



    public String getOperationId() {
        return operationId;
    }


    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }



    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }



    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }



    public List<ApiParameter> getParameters() {
        return parameters;
    }


    public void setParameters(
            List<ApiParameter> parameters) {

        this.parameters = parameters;
    }



    public List<String> getResponses() {
        return responses;
    }


    public void setResponses(
            List<String> responses) {

        this.responses = responses;
    }



    public List<ResponseField> getValidations() {
        return validations;
    }


    public void setValidations(
            List<ResponseField> validations) {

        this.validations = validations;
    }




    public void addValidation(
            ResponseField field) {

        validations.add(field);

    }



    public void addParameter(
            ApiParameter parameter) {

        parameters.add(parameter);

    }




    @Override
    public String toString() {

        return method 
                + " "
                + path
                + " validations="
                + validations;

    }

}