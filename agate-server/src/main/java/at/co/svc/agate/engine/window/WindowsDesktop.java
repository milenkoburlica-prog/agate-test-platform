
package at.co.svc.agate.engine.window;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Native Windows desktop window provider.
 *
 * Enumerates visible top-level desktop windows using Win32 APIs. Does not
 * launch or attach to a browser.
 *
 * Supports: FIND, CLOSE, MAXIMIZE, MINIMIZE, RESTORE.
 */
public class WindowsDesktop {

    private static final long TIMEOUT_SECONDS = 30;
    private static final int MAX_OUTPUT_BYTES = 1_048_576;
    private static final ObjectMapper JSON = new ObjectMapper();

    public boolean exists(String glob) throws Exception {
        return !find(glob).windows().isEmpty();
    }

    public WindowResult find(String glob) throws Exception {
        return new WindowResult(run(glob, "EXISTS"));
    }

    public int action(String glob, String operation) throws Exception {
        return run(glob, operation).size();
    }

    private List<WindowResult.WindowInfo> run(String glob, String operation) throws Exception {

        if (!System.getProperty("os.name", "").startsWith("Windows")) {
            throw new IllegalStateException("WINDOW requires an interactive Windows desktop");
        }

        if (glob == null || glob.isBlank()) {
            throw new IllegalArgumentException("WINDOW caption/glob must not be blank");
        }

        String normalizedOperation = operation.toUpperCase(Locale.ROOT);

        String script = buildScript(glob, normalizedOperation);

        return executeScript(script, glob, normalizedOperation);
    }

    List<WindowResult.WindowInfo> executeScript(String script, String glob, String normalizedOperation) throws Exception {
        if (!System.getProperty("os.name", "").startsWith("Windows")) {
            throw new IllegalStateException("WINDOW requires an interactive Windows desktop");
        }
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));

        ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
                "-EncodedCommand", encoded);

        // Important: JSON must not be mixed with PowerShell errors.
        builder.redirectErrorStream(false);

        Process process = builder.start();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        Thread stdoutReader = readerThread(process.getInputStream(), stdout, "stdout");

        Thread stderrReader = readerThread(process.getErrorStream(), stderr, "stderr");

        stdoutReader.start();
        stderrReader.start();

        try {
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);

                throw new IllegalStateException("WINDOW " + normalizedOperation + " timed out after " + TIMEOUT_SECONDS
                        + " seconds" + " | caption=" + glob);
            }

            stdoutReader.join(5000);
            stderrReader.join(5000);

            if (stdoutReader.isAlive() || stderrReader.isAlive()) {
                throw new IllegalStateException("WINDOW output reader did not finish");
            }

            String out = decode(stdout.toByteArray()).trim();
            String err = decode(stderr.toByteArray()).trim();

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new IllegalStateException("WINDOW " + normalizedOperation + " PowerShell execution failed"
                        + " | exitCode=" + exitCode + " | caption=" + glob + " | STDOUT=" + abbreviate(out)
                        + " | STDERR=" + abbreviate(err));
            }

            if (out.isBlank()) {
                throw new IllegalStateException(
                        "WINDOW " + normalizedOperation + " returned empty STDOUT" + " | STDERR=" + abbreviate(err));
            }

            if (out.startsWith("#< CLIXML")) {
                throw new IllegalStateException("WINDOW received PowerShell CLIXML " + "instead of JSON" + " | STDOUT="
                        + abbreviate(out) + " | STDERR=" + abbreviate(err));
            }

            JsonNode root;

            try {
                root = JSON.readTree(out);
            } catch (Exception e) {
                throw new IllegalStateException("WINDOW returned invalid JSON" + " | operation=" + normalizedOperation
                        + " | caption=" + glob + " | STDOUT=" + abbreviate(out) + " | STDERR=" + abbreviate(err), e);
            }

            if (root == null || !root.isArray()) {
                throw new IllegalStateException("WINDOW expected a JSON array" + " | actual=" + abbreviate(out));
            }

            List<WindowResult.WindowInfo> windows = new ArrayList<>();

            for (JsonNode item : root) {

                if (!item.isObject() || !item.hasNonNull("handle") || !item.hasNonNull("caption")
                        || !item.hasNonNull("processId")) {
                    throw new IllegalStateException("WINDOW received malformed window entry: " + item);
                }

                windows.add(new WindowResult.WindowInfo(item.get("handle").asText(), item.get("caption").asText(),
                        item.get("processId").asLong()));
            }

            return windows;

        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static String buildScript(String glob, String operation) {

        String pattern = Base64.getEncoder().encodeToString(glob.getBytes(StandardCharsets.UTF_8));

        String script = """
                $ErrorActionPreference = 'Stop'
                $ProgressPreference = 'SilentlyContinue'

                Add-Type -TypeDefinition @'
                using System;
                using System.Text;
                using System.Collections.Generic;
                using System.Runtime.InteropServices;

                public static class AgateWindows {

                    public delegate bool Callback(
                        IntPtr handle, IntPtr parameter);

                    [DllImport("user32.dll", SetLastError=true)]
                    static extern bool EnumWindows(
                        Callback callback, IntPtr parameter);

                    [DllImport("user32.dll", CharSet=CharSet.Unicode)]
                    static extern int GetWindowText(
                        IntPtr handle, StringBuilder buffer, int length);

                    [DllImport("user32.dll")]
                    static extern bool IsWindowVisible(IntPtr handle);

                    [DllImport("user32.dll", SetLastError=true)]
                    public static extern bool PostMessage(
                        IntPtr handle, uint message,
                        IntPtr wParam, IntPtr lParam);

                    [DllImport("user32.dll", SetLastError=true)]
                    public static extern bool ShowWindowAsync(
                        IntPtr handle, int command);

                    [DllImport("user32.dll")]
                    public static extern uint GetWindowThreadProcessId(
                        IntPtr handle, out uint processId);

                    public static Dictionary<IntPtr, string> List() {

                        var result = new Dictionary<IntPtr, string>();

                        bool success = EnumWindows(
                            (handle, parameter) => {

                                var buffer = new StringBuilder(4096);

                                GetWindowText(
                                    handle,
                                    buffer,
                                    buffer.Capacity);

                                if (IsWindowVisible(handle)
                                        && buffer.Length > 0) {
                                    result.Add(
                                        handle,
                                        buffer.ToString());
                                }

                                return true;
                            },
                            IntPtr.Zero
                        );

                        if (!success) {
                            throw new System.ComponentModel
                                .Win32Exception(
                                    Marshal.GetLastWin32Error());
                        }

                        return result;
                    }
                }
                '@
                """;

        script += """
                $glob = [Text.Encoding]::UTF8.GetString(
                    [Convert]::FromBase64String('__PATTERN__')
                )

                # Translate glob wildcards into a regular expression.
                # All other characters remain literal.
                $escaped = [Regex]::Escape($glob)
                $escaped = $escaped.Replace('\\*', '.*')
                $escaped = $escaped.Replace('\\?', '.')
                $regex = '\\A' + $escaped + '\\z'

                $items = @()

                foreach ($pair in [AgateWindows]::List().GetEnumerator()) {

                    if ([Regex]::IsMatch(
                        $pair.Value,
                        $regex,
                        [Text.RegularExpressions.RegexOptions]::IgnoreCase)) {
                """.replace("__PATTERN__", pattern);

        script += switch (operation) {

        case "EXISTS" -> "";

        case "CLOSE" -> """
                if (-not [AgateWindows]::PostMessage(
                    $pair.Key,
                    0x10,
                    [IntPtr]::Zero,
                    [IntPtr]::Zero)) {
                    throw 'WM_CLOSE failed'
                }
                """;

        case "MAXIMIZE" -> """
                [void][AgateWindows]::ShowWindowAsync(
                    $pair.Key, 3)
                """;

        case "MINIMIZE" -> """
                [void][AgateWindows]::ShowWindowAsync(
                    $pair.Key, 6)
                """;

        case "RESTORE" -> """
                [void][AgateWindows]::ShowWindowAsync(
                    $pair.Key, 9)
                """;

        default -> throw new IllegalArgumentException("Unsupported WINDOW operation: " + operation);
        };

        script += """
                    [uint32]$windowPid = 0

                    [void][AgateWindows]::GetWindowThreadProcessId(
                        $pair.Key, [ref]$windowPid)

                    $items += @{
                        handle = $pair.Key.ToInt64().ToString()
                        caption = $pair.Value
                        processId = $windowPid
                    }
                }
                }

                # Return a JSON array, including when no windows match.
                $json = ConvertTo-Json -InputObject @($items) -Compress -Depth 5

                [Console]::Out.WriteLine($json)
                """;

        return script;
    }

    private static Thread readerThread(InputStream stream, ByteArrayOutputStream destination, String name) {

        Thread thread = new Thread(() -> {
            try (InputStream input = stream) {
                byte[] buffer = new byte[8192];
                int count;

                while ((count = input.read(buffer)) != -1) {
                    synchronized (destination) {
                        if (destination.size() + count <= MAX_OUTPUT_BYTES) {
                            destination.write(buffer, 0, count);
                        }
                    }
                }
            } catch (IOException ignored) {
                // Process termination can close the stream.
            }
        }, "agate-window-" + name);

        thread.setDaemon(true);

        return thread;
    }

    private static String decode(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    private static String abbreviate(String text) {

        if (text == null) {
            return "";
        }

        int maximum = 2000;

        if (text.length() <= maximum) {
            return text;
        }

        return text.substring(0, maximum) + "... [truncated]";
    }
}
