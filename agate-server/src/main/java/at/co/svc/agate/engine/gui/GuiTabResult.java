package at.co.svc.agate.engine.gui;
import java.util.List;
/** Snapshot of named tabs opened with OPEN_TAB in one AGATE session. */
public record GuiTabResult(List<TabInfo> tabs) {
 public GuiTabResult { tabs=List.copyOf(tabs); }
 public record TabInfo(String tab,String url,String title) {}
}
