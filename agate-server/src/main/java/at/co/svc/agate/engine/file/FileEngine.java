package at.co.svc.agate.engine.file;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;

public class FileEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.FILE;
    }

    @Override
    protected void doExecute(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(
                    logger,
                    step);
        }

        String op =
                step.getOp() != null
                        ? step.getOp().toUpperCase()
                        : "EXEC";

        switch (op) {

        case "EXEC":
            handleExecution(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        case "BUFFER":
            handleBuffer(
                    step,
                    context,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        case "ASSERT":
            handleAssertion(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        default:
            throw AgateStepException.builder("Unsupported FILE operation")
                    .actual(op)
                    .hint("Supported operations: EXEC, BUFFER, ASSERT")
                    .build();
        }
    }

    private void handleExecution(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        String action =
                requireAction(step);

        Map<String, Object> variables =
                createVariables(
                        tc,
                        context);

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        switch (action) {

        case "READ":
            executeRead(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "WRITE":
            executeWrite(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "APPEND":
            executeAppend(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "COPY":
            executeCopy(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "MOVE":
            executeMove(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "DELETE":
            executeDelete(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        case "EXISTS":
            executeExists(
                    tc,
                    step,
                    context,
                    resolver,
                    variables,
                    yamlFile,
                    stepIndex);
            break;

        default:
            throw AgateStepException.builder("Unsupported FILE EXEC action")
                    .actual(action)
                    .hint("Supported actions: READ, WRITE, APPEND, COPY, MOVE, DELETE, EXISTS")
                    .build();
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private void executeRead(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path path =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getPath(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "path",
                                step));

        if (!Files.exists(path)) {
            throw AgateStepException.builder("File does not exist")
                    .path(path)
                    .hint("Check the file path or verify that a previous step created the file.")
                    .build();
        }

        Charset charset =
                resolveCharset(
                        step.getEncoding());

        String content =
                Files.readString(
                        path,
                        charset);

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    content);
        }
    }

    private void executeWrite(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path path =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getPath(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "path",
                                step));

        String text =
                resolve(
                        tc,
                        resolver,
                        step.getText(),
                        variables,
                        yamlFile,
                        stepIndex,
                        "text",
                        step);

        Charset charset =
                resolveCharset(
                        step.getEncoding());

        boolean overwrite =
                step.getOverwrite() == null
                        || step.getOverwrite();

        Path parent =
                path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        if (Files.exists(path)
                && !overwrite) {

            throw new RuntimeException(
                    "Target file already exists: "
                            + path);
        }

        Files.writeString(
                path,
                text != null ? text : "",
                charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        storeSuccessResponse(
                step,
                context);
    }

    private void executeAppend(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path path =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getPath(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "path",
                                step));

        String text =
                resolve(
                        tc,
                        resolver,
                        step.getText(),
                        variables,
                        yamlFile,
                        stepIndex,
                        "text",
                        step);

        if (Boolean.TRUE.equals(
                step.getNewline())) {

            text =
                    (text != null ? text : "")
                            + System.lineSeparator();
        }

        Charset charset =
                resolveCharset(
                        step.getEncoding());

        Path parent =
                path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                path,
                text != null ? text : "",
                charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

        storeSuccessResponse(
                step,
                context);
    }

    private void executeCopy(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path source =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getSource(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "source",
                                step));

        Path target =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getTarget(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "target",
                                step));

        if (!Files.exists(source)) {
            throw AgateStepException.builder("Source file does not exist")
                    .path(source)
                    .hint("Check the source path or verify that a previous step created the file.")
                    .build();
        }

        Path parent =
                target.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        CopyOption[] options =
                Boolean.TRUE.equals(
                        step.getOverwrite())
                        ? new CopyOption[] {
                                StandardCopyOption.REPLACE_EXISTING
                        }
                        : new CopyOption[0];

        Files.copy(
                source,
                target,
                options);

        storeSuccessResponse(
                step,
                context);
    }

    private void executeMove(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path source =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getSource(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "source",
                                step));

        Path target =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getTarget(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "target",
                                step));

        if (!Files.exists(source)) {
            throw AgateStepException.builder("Source file does not exist")
                    .path(source)
                    .hint("Check the source path or verify that a previous step created the file.")
                    .build();
        }

        Path parent =
                target.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        CopyOption[] options =
                Boolean.TRUE.equals(
                        step.getOverwrite())
                        ? new CopyOption[] {
                                StandardCopyOption.REPLACE_EXISTING
                        }
                        : new CopyOption[0];

        Files.move(
                source,
                target,
                options);

        storeSuccessResponse(
                step,
                context);
    }

    private void executeDelete(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) throws Exception {

        Path path =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getPath(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "path",
                                step));

        boolean missingOk =
                Boolean.TRUE.equals(
                        step.getMissingOk());

        if (missingOk) {
            Files.deleteIfExists(path);
        } else {
            if (!Files.exists(path)) {
                throw AgateStepException.builder("File does not exist")
                        .path(path)
                        .hint("Set missingOk: true if a missing file should be ignored.")
                        .build();
            }
            Files.delete(path);
        }

        storeSuccessResponse(
                step,
                context);
    }

    private void executeExists(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex) {

        Path path =
                resolvePath(
                        resolve(
                                tc,
                                resolver,
                                step.getPath(),
                                variables,
                                yamlFile,
                                stepIndex,
                                "path",
                                step));

        boolean exists =
                Files.exists(path);

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    exists);
        }
    }

    private void handleBuffer(
            TestStep step,
            ExecutionContext context,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        Object raw =
                context.getBuffer(
                        step.getResponse());

        if (raw == null) {
            throw new RuntimeException(
                    "No FILE response found for key: "
                            + step.getResponse());
        }

        String text =
                raw.toString();

        String action =
                step.getAction() != null
                        ? step.getAction().toUpperCase()
                        : "TEXT";

        String value =
                step.getValue();

        if ((value == null
                || value.isBlank())
                && ("LINE".equals(action)
                        || "LAST_LINE".equals(action))) {

            value = "0";
        }

        String result;

        switch (action) {

        case "TEXT":
            result = text.trim();
            break;

        case "FILTER":
            result =
                    filterLines(
                            text,
                            value != null
                                    ? value
                                    : "");
            break;

        case "LINE":
            result =
                    getLine(
                            text,
                            Integer.parseInt(value));
            break;

        case "LAST_LINE":
            result =
                    getLastLine(
                            text,
                            Integer.parseInt(value));
            break;

        case "COUNT":
            result =
                    String.valueOf(
                            countOccurrences(
                                    text,
                                    value != null
                                            ? value
                                            : ""));
            break;

        default:
            throw new RuntimeException(
                    "Unsupported FILE BUFFER action: "
                            + action);
        }

        if (step.getName() == null
                || step.getName().isBlank()) {

            throw new RuntimeException(
                    "FILE BUFFER requires 'name'.");
        }

        context.storeBuffer(
                step.getName(),
                result);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS (Buffered)",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private void handleAssertion(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String action =
                requireAction(step);

        if ("EXISTS".equals(action)
                || "NOT_EXISTS".equals(action)) {

            YamlPlaceholderResolver resolver =
                    new YamlPlaceholderResolver();

            Map<String, Object> variables =
                    createVariables(
                            tc,
                            context);

            String resolvedPath =
                    resolve(
                            tc,
                            resolver,
                            step.getPath(),
                            variables,
                            yamlFile,
                            stepIndex,
                            "path",
                            step);

            Path path =
                    resolvePath(
                            resolvedPath);

            boolean exists =
                    Files.exists(path);

            if ("EXISTS".equals(action)
                    && !exists) {

                throw AgateStepException.builder("FILE EXISTS assertion failed")
                        .expected("File exists")
                        .actual("File not found")
                        .path(path)
                        .build();
            }

            if ("NOT_EXISTS".equals(action)
                    && exists) {

                throw AgateStepException.builder("FILE NOT_EXISTS assertion failed")
                        .expected("File does not exist")
                        .actual("File exists")
                        .path(path)
                        .build();
            }

        } else {

            Object raw =
                    context.getBuffer(
                            step.getResponse());

            if (raw == null) {
                throw new RuntimeException(
                        "No FILE response found for key: "
                                + step.getResponse());
            }

            String text =
                    raw.toString();

            String value =
                    step.getValue() != null
                            ? step.getValue()
                            : "";

            String expected =
                    step.getExpected() != null
                            ? step.getExpected()
                            : "0";

            switch (action) {

            case "CONTAINS":
                if (!text.contains(value)) {
                    throw AgateStepException.builder("FILE CONTAINS assertion failed")
                            .expected(value)
                            .actual(abbreviate(text, 300))
                            .build();
                }
                break;

            case "NOT_CONTAINS":
                if (text.contains(value)) {
                    throw AgateStepException.builder("FILE NOT_CONTAINS assertion failed")
                            .expected("Text must not contain: " + value)
                            .actual(abbreviate(text, 300))
                            .build();
                }
                break;

            case "EQUALS":
                if (!text.trim()
                        .equals(value.trim())) {

                    throw AgateStepException.builder("FILE EQUALS assertion failed")
                            .expected(value)
                            .actual(abbreviate(text.trim(), 300))
                            .build();
                }
                break;

            case "NOT_EQUALS":
                if (text.trim()
                        .equals(value.trim())) {

                    throw AgateStepException.builder("FILE NOT_EQUALS assertion failed")
                            .expected("Value different from: " + value)
                            .actual(abbreviate(text.trim(), 300))
                            .build();
                }
                break;

            case "COUNT":
                int count =
                        countOccurrences(
                                text,
                                value);

                if (count
                        != Integer.parseInt(
                                expected)) {

                    throw AgateStepException.builder("FILE COUNT assertion failed")
                            .expected(expected)
                            .actual(count)
                            .build();
                }
                break;

            default:
                throw AgateStepException.builder("Unsupported FILE ASSERT action")
                        .actual(action)
                        .hint("Supported actions: EXISTS, NOT_EXISTS, CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS, COUNT")
                        .build();
            }
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private Map<String, Object> createVariables(
            TestCase tc,
            ExecutionContext context) {

        Map<String, Object> variables =
                new HashMap<>();

        if (tc.getVariables() != null) {
            variables.putAll(
                    tc.getVariables());
        }

        if (context != null
                && context.getVars() != null) {

            variables.putAll(
                    context.getVars());
        }

        if (context != null) {
            variables.putAll(
                    context.getBufferMap());
        }

        return variables;
    }

    private String resolve(
            TestCase tc,
            YamlPlaceholderResolver resolver,
            String value,
            Map<String, Object> variables,
            String yamlFile,
            int stepIndex,
            String field,
            TestStep step) {

        if (value == null) {
            return null;
        }

        String resolved =
                resolver.resolve(
                        tc,
                        value,
                        variables,
                        yamlFile,
                        stepIndex,
                        field);

        if (step.getParameters() != null) {

            resolved =
                    resolver.resolve(
                            tc,
                            resolved,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            field,
                            step);
        }

        return resolved;
    }

    private Path resolvePath(
            String value) {

        if (value == null
                || value.isBlank()) {

            throw AgateStepException.builder("Required property is missing")
                    .field("path")
                    .hint("Provide a path for this FILE operation.")
                    .build();
        }

        Path path =
                Path.of(value);

        if (!path.isAbsolute()) {
            path =
                    Path.of(
                            System.getProperty(
                                    "user.dir"))
                            .resolve(path);
        }

        return path.normalize();
    }

    private Charset resolveCharset(
            String encoding) {

        if (encoding == null
                || encoding.isBlank()) {

            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(
                    encoding);
        } catch (Exception e) {

            throw new RuntimeException(
                    "Unsupported encoding: "
                            + encoding);
        }
    }

    private String requireAction(
            TestStep step) {

        if (step.getAction() == null
                || step.getAction().isBlank()) {

            throw AgateStepException.builder("Required property is missing")
                    .field("action")
                    .hint("Add an action appropriate for FILE op: "
                            + (step.getOp() != null ? step.getOp().toUpperCase() : "EXEC")
                            + ".")
                    .build();
        }

        return step.getAction()
                .toUpperCase();
    }

    private void storeSuccessResponse(
            TestStep step,
            ExecutionContext context) {

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    Boolean.TRUE);
        }
    }

    private String filterLines(
            String text,
            String search) {

        StringBuilder result =
                new StringBuilder();

        for (String line :
                text.split("\\R")) {

            if (line.contains(search)) {

                result.append(line)
                        .append(System.lineSeparator());
            }
        }

        return result.toString().trim();
    }

    private String getLine(
            String text,
            int index) {

        String[] lines =
                text.split("\\R");

        if (index < 0
                || index >= lines.length) {

            throw new RuntimeException(
                    "LINE index out of bounds: "
                            + index);
        }

        return lines[index].trim();
    }

    private String getLastLine(
            String text,
            int offset) {

        String[] lines =
                text.split("\\R");

        int index =
                lines.length - 1 - offset;

        if (index < 0
                || index >= lines.length) {

            throw new RuntimeException(
                    "LAST_LINE index out of bounds: "
                            + offset);
        }

        return lines[index].trim();
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        String normalized = value.replace("\r", "\\r").replace("\n", "\\n");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private int countOccurrences(
            String text,
            String search) {

        if (text == null
                || search == null
                || search.isEmpty()) {

            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index =
                text.indexOf(
                        search,
                        index)) != -1) {

            count++;
            index += search.length();
        }

        return count;
    }
}