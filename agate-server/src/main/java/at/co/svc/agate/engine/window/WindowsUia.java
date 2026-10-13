package at.co.svc.agate.engine.window;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/** UIA provider for the Edge restore-session panel, scoped to msedge windows. */
public final class WindowsUia {
    private final WindowsDesktop desktop;
    public WindowsUia(WindowsDesktop desktop) { this.desktop = desktop; }
    public WindowResult execute(String caption, String role, String action) throws Exception {
        if (!Set.of("PANEL", "RESTORE", "CLOSE").contains(role))
            throw new IllegalArgumentException("Unsupported Edge restore role: " + role);
        if (!Set.of("FIND", "CLICK").contains(action) || action.equals("CLICK") && !role.equals("CLOSE"))
            throw new IllegalArgumentException("Edge restore supports FIND and CLICK CLOSE only");
        return new WindowResult(desktop.executeScript(buildScript(caption, role, action), caption, action));
    }
    static String buildScript(String caption, String role, String action) {
        String encoded = Base64.getEncoder().encodeToString(caption.getBytes(StandardCharsets.UTF_8));
        return SCRIPT.replace("__CAPTION__", encoded).replace("__ROLE__", role).replace("__ACTION__", action);
    }
    private static final String SCRIPT = """
            $ErrorActionPreference = 'Stop'
            $ProgressPreference = 'SilentlyContinue'
            [Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
            Add-Type -AssemblyName UIAutomationClient
            Add-Type -AssemblyName UIAutomationTypes
            function Clean([string]$s) { return $s.Replace([string][char]0x200B,'').Replace([string][char]0xFEFF,'') }
            function Desc($element) {
                return $element.FindAll([System.Windows.Automation.TreeScope]::Descendants,[System.Windows.Automation.Condition]::TrueCondition)
            }
            function Named($element, $names) { return $names -contains (Clean $element.Current.Name) }
            $glob = Clean ([Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('__CAPTION__')))
            $escaped = [Regex]::Escape($glob)
            $escaped = $escaped.Replace('\\*','.*').Replace('\\?','.')
            $regex = '\\A' + $escaped + '\\z'
            $role = '__ROLE__'
            $action = '__ACTION__'
            $headings = @('Seiten wiederherstellen','Restore pages')
            $restores = @('Wiederherstellen','Restore')
            $closes = @('Schließen','Close')
            $items = @()
            $matches = @()
            $windows = [System.Windows.Automation.AutomationElement]::RootElement.FindAll([System.Windows.Automation.TreeScope]::Children,[System.Windows.Automation.Condition]::TrueCondition)
            foreach($window in $windows) {
                $state = $window.Current
                if($state.IsOffscreen -or -not [Regex]::IsMatch((Clean $state.Name),$regex,[Text.RegularExpressions.RegexOptions]::IgnoreCase)) { continue }
                $proc = Get-Process -Id $state.ProcessId -ErrorAction Stop
                if($proc.ProcessName -ne 'msedge') { continue }
                $nodes = @(Desc $window)
                $panels = @()
                foreach($node in $nodes) {
                    if($node.Current.IsOffscreen -or -not (Named $node $headings)) { continue }
                    $panel = $node
                    for($depth=0; $depth -lt 8; $depth++) {
                        if($null -eq $panel -or $panel.Equals($window)) { break }
                        if($panel.Current.ControlType -eq [System.Windows.Automation.ControlType]::Document) { break }
                        $children = @(Desc $panel)
                        $buttons = @($children | Where-Object { -not $_.Current.IsOffscreen -and $_.Current.ControlType -eq [System.Windows.Automation.ControlType]::Button })
                        $restore = @($buttons | Where-Object { Named $_ $restores })
                        $close = @($buttons | Where-Object { Named $_ $closes })
                        if($restore.Count -eq 1 -and $close.Count -eq 1) {
                            if(-not @($panels | Where-Object { $_.Equals($panel) }).Count) { $panels += $panel }
                            break
                        }
                        $panel = [System.Windows.Automation.TreeWalker]::ControlViewWalker.GetParent($panel)
                    }
                }
                foreach($panel in $panels) {
                    $element = $panel
                    if($role -ne 'PANEL') {
                        $names = if($role -eq 'CLOSE') { $closes } else { $restores }
                        $elements = @((Desc $panel) | Where-Object { -not $_.Current.IsOffscreen -and $_.Current.ControlType -eq [System.Windows.Automation.ControlType]::Button -and (Named $_ $names) })
                        if($elements.Count -ne 1) { throw 'Edge restore control became missing or ambiguous' }
                        $element = $elements[0]
                    }
                    $matches += $element
                    $items += @{ handle=($element.GetRuntimeId() -join '.'); caption=$element.Current.Name; processId=$element.Current.ProcessId }
                }
            }
            if($action -eq 'CLICK') {
                if($matches.Count -ne 1) { throw "Expected exactly one Edge restore close button; matches=$($matches.Count)" }
                if(-not $matches[0].Current.IsEnabled) { throw 'Edge restore close button is disabled' }
                $pattern = $matches[0].GetCurrentPattern([System.Windows.Automation.InvokePattern]::Pattern)
                ([System.Windows.Automation.InvokePattern]$pattern).Invoke()
            }
            [Console]::Out.WriteLine((ConvertTo-Json -InputObject @($items) -Compress -Depth 5))
            """;
}
