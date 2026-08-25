package at.co.svc.agate.openapi.phase1.serialization;

public class AgateParameterSerializationException
        extends RuntimeException {


    public AgateParameterSerializationException(
            String message) {

        super(
                message
        );
    }


    public AgateParameterSerializationException(
            String message,
            Throwable cause) {

        super(
                message,
                cause
        );
    }
}