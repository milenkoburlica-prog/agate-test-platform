package at.co.svc.agate.engine.window;
import java.util.List;
/** Immutable result snapshot. It is not a live connection to the desktop. */
public record WindowResult(List<WindowInfo> windows) {
    public WindowResult { windows=List.copyOf(windows); }
    public record WindowInfo(String handle,String caption,long processId) {}
}
