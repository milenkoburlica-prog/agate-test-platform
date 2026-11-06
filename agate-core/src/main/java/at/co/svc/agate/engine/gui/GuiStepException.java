package at.co.svc.agate.engine.gui;

/** Step context with the original Playwright failure retained as its cause. */
public final class GuiStepException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public GuiStepException(String message, Throwable cause) { super(message, cause); }
}
