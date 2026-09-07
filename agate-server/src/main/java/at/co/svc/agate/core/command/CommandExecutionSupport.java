package at.co.svc.agate.core.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class CommandExecutionSupport {

    private CommandExecutionSupport() {
    }

    public static CommandResult executeWindowsShell(
            String command,
            File workingDirectory,
            int timeoutSeconds) throws Exception {

        long start = System.nanoTime();

        ProcessBuilder processBuilder =
                new ProcessBuilder("cmd.exe", "/c", command);

        processBuilder.redirectErrorStream(true);

        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory);
        }

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         process.getInputStream(),
                                         StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    synchronized (output) {
                        output.append(line).append(System.lineSeparator());
                    }
                }
            } catch (Exception e) {
                synchronized (output) {
                    output.append("OUTPUT_READ_ERROR: ")
                            .append(e.getMessage())
                            .append(System.lineSeparator());
                }
            }
        });

        outputReader.setDaemon(true);
        outputReader.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        boolean timedOut = !finished;
        int exitCode;

        if (timedOut) {
            process.destroyForcibly();
            process.waitFor();
            exitCode = -1;
        } else {
            exitCode = process.exitValue();
        }

        outputReader.join(2000);

        long durationMs =
                (System.nanoTime() - start) / 1_000_000;

        String finalOutput;

        synchronized (output) {
            finalOutput = output.toString();
        }

        return new CommandResult(
                exitCode,
                finalOutput,
                timedOut,
                durationMs);
    }

    public static void writeOutputFile(
            String outputFile,
            String content,
            File workingDirectory) throws Exception {

        if (outputFile == null || outputFile.isBlank()) {
            return;
        }

        Path outputPath = Path.of(outputFile);

        if (!outputPath.isAbsolute()) {
            Path basePath;

            if (workingDirectory != null) {
                basePath = workingDirectory.toPath();
            } else {
                basePath = Path.of(System.getProperty("user.dir"));
            }

            outputPath = basePath.resolve(outputPath);
        }

        outputPath = outputPath.normalize();

        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                outputPath,
                content != null ? content : "",
                StandardCharsets.UTF_8);
    }
}