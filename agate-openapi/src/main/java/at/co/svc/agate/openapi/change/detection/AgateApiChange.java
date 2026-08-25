package at.co.svc.agate.openapi.change.detection;


public class AgateApiChange {


    private String operationIdentity;

    private String location;

    private String property;


    private AgateChangeType changeType;

    private AgateChangeSeverity severity;


    private Object oldValue;

    private Object newValue;


    private String description;




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public String getLocation() {

        return location;
    }


    public void setLocation(
            String location) {

        this.location =
                location;
    }




    public String getProperty() {

        return property;
    }


    public void setProperty(
            String property) {

        this.property =
                property;
    }




    public AgateChangeType getChangeType() {

        return changeType;
    }


    public void setChangeType(
            AgateChangeType changeType) {

        this.changeType =
                changeType;
    }




    public AgateChangeSeverity getSeverity() {

        return severity;
    }


    public void setSeverity(
            AgateChangeSeverity severity) {

        this.severity =
                severity;
    }




    public Object getOldValue() {

        return oldValue;
    }


    public void setOldValue(
            Object oldValue) {

        this.oldValue =
                oldValue;
    }




    public Object getNewValue() {

        return newValue;
    }


    public void setNewValue(
            Object newValue) {

        this.newValue =
                newValue;
    }




    public String getDescription() {

        return description;
    }


    public void setDescription(
            String description) {

        this.description =
                description;
    }




    @Override
    public String toString() {

        return operationIdentity
                + " "
                + changeType
                + " "
                + location
                + (
                        property != null
                                ? " [" + property + "]"
                                : ""
                )
                + " "
                + oldValue
                + " -> "
                + newValue;
    }
}