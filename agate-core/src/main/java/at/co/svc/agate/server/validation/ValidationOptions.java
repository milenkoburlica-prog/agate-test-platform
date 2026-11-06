package at.co.svc.agate.server.validation;

public record ValidationOptions(
        boolean strictUnknownFields,
        boolean warnUnusedVariables
) {

    public static ValidationOptions defaults() {

        return new ValidationOptions(
                true,
                true);
    }
}