package at.co.svc.agate.core.interfaces;

/**
 * Functional Interface for logging.
 */
@FunctionalInterface
public interface TestLogger {
    // The single abstract method (SAM)
    void log(String message);

    // Default methods don't break the functional interface contract
    default void info(String message) {
        log(message);
    }

    default void error(String message) {
        log("ERROR: " + message);
    }
    
    default void warn(String message) {
        log(message);
    }

}