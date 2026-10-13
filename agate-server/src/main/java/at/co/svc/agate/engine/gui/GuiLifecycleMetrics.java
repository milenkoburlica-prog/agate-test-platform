package at.co.svc.agate.engine.gui;

import java.util.concurrent.TimeUnit;

/** Per-suite GUI timing counters. No global/shared state between executions. */
final class GuiLifecycleMetrics {
    private final long startedNanos = System.nanoTime();
    private final boolean verbose;

    GuiLifecycleMetrics() { this(Boolean.getBoolean("agate.gui.lifecycleLog")); }
    GuiLifecycleMetrics(boolean verbose) { this.verbose = verbose; }
    boolean verbose() { return verbose; }
    private long driverStartupMs;
    private long browserStartupMs;
    private long contextStartupMs;
    private long contextCleanupMs;
    private long browserCleanupMs;
    private long driverCleanupMs;
    private int browsers;
    private int contexts;
    private int closeFailures;
    private int forcedDriverStops;
    private int forcedChildStops;
    private int childProcessLeaks;

    static long elapsed(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    synchronized void add(String phase, long elapsedMs) {
        switch (phase) {
            case "PLAYWRIGHT_CREATE" -> driverStartupMs += elapsedMs;
            case "BROWSER_LAUNCH" -> { browserStartupMs += elapsedMs; browsers++; }
            case "CONTEXT_CREATE" -> { contextStartupMs += elapsedMs; contexts++; }
            case "CONTEXT_CLOSE" -> contextCleanupMs += elapsedMs;
            case "BROWSER_CLOSE" -> browserCleanupMs += elapsedMs;
            case "PLAYWRIGHT_CLOSE" -> driverCleanupMs += elapsedMs;
            default -> { }
        }
    }
    synchronized void failure() { closeFailures++; }
    synchronized void forcedDriverStop() { forcedDriverStops++; }
    synchronized void forcedChildStops(int count) { forcedChildStops += count; }
    synchronized void childProcessLeaks(int count) { childProcessLeaks += count; }
    synchronized int failures() { return closeFailures; }

    synchronized void printSummary(String suiteId, boolean reuse) {
        long suiteWallMs = elapsed(startedNanos);
        long startupMs = driverStartupMs + browserStartupMs + contextStartupMs;
        long cleanupMs = contextCleanupMs + browserCleanupMs + driverCleanupMs;
        System.out.println("[GUI] SUMMARY browserLaunches=" + browsers
                + " contexts=" + contexts + " cleanupFailures=" + closeFailures
                + " forcedDriverStops=" + forcedDriverStops);
        if (!verbose) return;
        System.out.println("[GUI metrics] SUITE id=" + suiteId + " reuse=" + reuse
                + " wallMs=" + suiteWallMs + " browserLaunches=" + browsers
                + " contexts=" + contexts);
        System.out.println("[GUI metrics] STARTUP playwrightMs=" + driverStartupMs
                + " browserMs=" + browserStartupMs + " contextsMs=" + contextStartupMs
                + " totalMs=" + startupMs);
        System.out.println("[GUI metrics] CLEANUP contextsMs=" + contextCleanupMs
                + " browserMs=" + browserCleanupMs + " playwrightMs=" + driverCleanupMs
                + " totalMs=" + cleanupMs);
        System.out.println("[GUI metrics] OTHER_WALL_APPROX_MS="
                + Math.max(0, suiteWallMs - startupMs - cleanupMs)
                + " (includes GUI steps, report/logging and non-GUI operations)");
        System.out.println("[GUI metrics] STATUS closeFailures=" + closeFailures
                + " driverForceStops=" + forcedDriverStops
                + " childForceStops=" + forcedChildStops
                + " childProcessLeaks=" + childProcessLeaks);
    }
}
