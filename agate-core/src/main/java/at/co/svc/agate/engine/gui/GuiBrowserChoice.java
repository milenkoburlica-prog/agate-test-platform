package at.co.svc.agate.engine.gui;
import com.microsoft.playwright.BrowserType;
import java.nio.file.Path;
import java.util.Locale;
final class GuiBrowserChoice {
    private GuiBrowserChoice() {}
    static String configure(BrowserType.LaunchOptions launch, String requested, String executable) {
        if (executable != null && !executable.isBlank()) {
            launch.setExecutablePath(Path.of(executable));
            return "Custom browser (" + executable + ")";
        }
        String choice = requested == null || requested.isBlank() ? "chromium" : requested.trim().toLowerCase(Locale.ROOT);
        return switch(choice) {
            case "chromium", "internal" -> "Playwright Chromium";
            case "edge", "msedge" -> { launch.setChannel("msedge"); yield "Installed Microsoft Edge"; }
            case "chrome" -> { launch.setChannel("chrome"); yield "Installed Google Chrome"; }
            default -> throw new IllegalArgumentException("Unsupported agate.gui.browser: " + choice
                    + ". Supported values: chromium, edge, chrome");
        };
    }
}
