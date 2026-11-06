package at.co.svc.agate.engine.oc;

import java.io.Serializable;

/**
 * Represents the response returned by executing a local OpenShift (oc) command,
 * including its process exit code and standard output/error stream text.
 */
public class OcCmdResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int exitCode;
    private final String output;

    /**
     * Constructs a new OcCmdResponse with the specified exit code and output.
     *
     * @param exitCode the process exit code (0 typically indicates success)
     * @param output   the captured command output and error stream
     */
    public OcCmdResponse(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    /**
     * Returns the process exit code.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Returns the captured output text.
     *
     * @return the command output
     */
    public String getOutput() {
        return output;
    }
}