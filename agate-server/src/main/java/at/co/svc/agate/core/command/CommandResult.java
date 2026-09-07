package at.co.svc.agate.core.command;

import java.io.Serializable;

public class CommandResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int exitCode;
    private final String output;
    private final boolean timedOut;
    private final long durationMs;

    public CommandResult(int exitCode, String output, boolean timedOut, long durationMs) {
        this.exitCode = exitCode;
        this.output = output;
        this.timedOut = timedOut;
        this.durationMs = durationMs;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public long getDurationMs() {
        return durationMs;
    }
}