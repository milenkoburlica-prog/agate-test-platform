package at.co.svc.agate.core.debug;

public class DebugQuitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DebugQuitException() {
        super("Debug execution stopped by user.");
    }
}
