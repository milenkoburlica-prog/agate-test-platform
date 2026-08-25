package at.co.svc.agate.openapi.model;

import java.util.ArrayList;
import java.util.List;

public class AgateValidationResult {

    private boolean valid;

    private int endpointCount;
    private int parameterCount;
    private int requestBodyCount;
    private int responseCount;
    private int unresolvedRefCount;
    private int unsupportedCount;
    private int warningCount;

    private List<String> errors =
            new ArrayList<>();

    private List<String> warnings =
            new ArrayList<>();

    private List<String> unsupported =
            new ArrayList<>();


    public boolean isValid() {
        return valid;
    }

    public void setValid(
            boolean valid) {

        this.valid = valid;
    }

    public int getEndpointCount() {
        return endpointCount;
    }

    public void setEndpointCount(
            int endpointCount) {

        this.endpointCount = endpointCount;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(
            int parameterCount) {

        this.parameterCount = parameterCount;
    }

    public int getRequestBodyCount() {
        return requestBodyCount;
    }

    public void setRequestBodyCount(
            int requestBodyCount) {

        this.requestBodyCount = requestBodyCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(
            int responseCount) {

        this.responseCount = responseCount;
    }

    public int getUnresolvedRefCount() {
        return unresolvedRefCount;
    }

    public void setUnresolvedRefCount(
            int unresolvedRefCount) {

        this.unresolvedRefCount = unresolvedRefCount;
    }

    public int getUnsupportedCount() {
        return unsupportedCount;
    }

    public void setUnsupportedCount(
            int unsupportedCount) {

        this.unsupportedCount = unsupportedCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(
            int warningCount) {

        this.warningCount = warningCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(
            List<String> errors) {

        this.errors =
                errors != null
                        ? errors
                        : new ArrayList<>();
    }

    public void addError(
            String error) {

        if (error != null) {
            errors.add(error);
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(
            List<String> warnings) {

        this.warnings =
                warnings != null
                        ? warnings
                        : new ArrayList<>();
    }

    public void addWarning(
            String warning) {

        if (warning != null) {
            warnings.add(warning);
        }
    }

    public List<String> getUnsupported() {
        return unsupported;
    }

    public void setUnsupported(
            List<String> unsupported) {

        this.unsupported =
                unsupported != null
                        ? unsupported
                        : new ArrayList<>();
    }

    public void addUnsupported(
            String value) {

        if (value != null) {
            unsupported.add(value);
        }
    }
}