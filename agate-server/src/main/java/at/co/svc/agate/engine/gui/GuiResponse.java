package at.co.svc.agate.engine.gui;

import java.io.Serializable;

public class GuiResponse implements Serializable {
    private final boolean success;
    private final String value;
    private final String errorMessage;

    public GuiResponse(boolean success, String value, String errorMessage) {
        this.success = success;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public static GuiResponse success(String value) {
        return new GuiResponse(true, value, null);
    }

    public static GuiResponse failure(String errorMessage) {
        return new GuiResponse(false, null, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getValue() { return value; }
    public String getErrorMessage() { return errorMessage; }
}