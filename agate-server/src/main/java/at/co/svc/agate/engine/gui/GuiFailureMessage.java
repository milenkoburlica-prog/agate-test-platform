package at.co.svc.agate.engine.gui;

import java.util.Objects;

/** Short human-readable failure cause; preserves useful assertion expected/actual text. */
public final class GuiFailureMessage {
    private GuiFailureMessage() {}
    public static String compact(String type, String message, double timeout) {
        if (type.equals("TimeoutError"))
            return "Timeout after " + (long) timeout + " ms: target did not become actionable. Check locator, visibility, enabled state and overlays.";
        String text=Objects.toString(message,"No details supplied");
        int call=text.indexOf("Call log:");if(call>=0)text=text.substring(0,call);
        int stack=text.indexOf("stack='");if(stack>=0)text=text.substring(0,stack);
        return type + ": " + text.strip();
    }
}
