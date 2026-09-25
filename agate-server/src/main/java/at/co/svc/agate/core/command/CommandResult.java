package at.co.svc.agate.core.command;

import java.io.Serializable;

public class CommandResult
        implements Serializable {

    private static final long serialVersionUID =
            1L;


    private final int exitCode;

    private final String output;

    private final boolean timedOut;

    private final long durationMs;


    public CommandResult(
            int exitCode,
            String output,
            boolean timedOut,
            long durationMs) {

        this.exitCode =
                exitCode;

        this.output =
                output;

        this.timedOut =
                timedOut;

        this.durationMs =
                durationMs;
    }


    /**
     * Returns the real process exit code.
     *
     * If AGATE terminated the process because of a timeout,
     * the value is -1.
     */
    public int getExitCode() {

        return exitCode;
    }


    /**
     * Returns the captured stdout/stderr output.
     */
    public String getOutput() {

        return output;
    }


    /**
     * True when AGATE terminated the process because
     * it exceeded the configured timeout.
     */
    public boolean isTimedOut() {

        return timedOut;
    }


    /**
     * Total command execution duration in milliseconds.
     */
    public long getDurationMs() {

        return durationMs;
    }
}