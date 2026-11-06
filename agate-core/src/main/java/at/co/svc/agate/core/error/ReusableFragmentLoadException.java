package at.co.svc.agate.core.error;

/**
 * Signals that a reusable AGATE YAML fragment could not be loaded.
 *
 * The reusable loader is responsible for printing the detailed,
 * tester-facing error message including fragment file, test case
 * and parent step.
 *
 * This exception is used to prevent the main YAML loader from
 * interpreting the same error again as an error in the parent
 * test-suite YAML file.
 */
public class ReusableFragmentLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReusableFragmentLoadException(String message) {
        super(message);
    }

    public ReusableFragmentLoadException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}