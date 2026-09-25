package at.co.svc.agate.engine.rest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.interfaces.TestStepEngine;
import at.co.svc.agate.core.reference.ComparisonDifference;
import at.co.svc.agate.core.reference.ReferenceAssertionResult;
import at.co.svc.agate.core.reference.ReferenceCompareConfig;
import at.co.svc.agate.core.reference.ReferenceFileStore;
import at.co.svc.agate.core.reference.ReferencePathResolver;
import at.co.svc.agate.core.reference.ReferenceResponseService;
import at.co.svc.agate.core.reference.ResponseFormat;
import at.co.svc.agate.core.reference.json.JsonResponseComparator;

public class RestEngine implements TestStepEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = createUnsafeClient();

    private static final ReferenceResponseService REFERENCE_RESPONSE_SERVICE =
            new ReferenceResponseService(
                    new ReferencePathResolver(),
                    new ReferenceFileStore(),
                    new JsonResponseComparator());

    @Override
    public void execute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";

        if (!"EXEC".equals(op) && !"ASSERT".equals(op) && !"BUFFER".equals(op)) {
            throw AgateStepException.builder("Unsupported REST operation")
                    .actual(op)
                    .hint("Supported operations: EXEC, ASSERT, BUFFER").build();
        }

        if (isAssertionStep(step)) {
            handleAssertion(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
        } else if (isBufferStep(step)) {
            handleBuffer(tc, step, context, stepIndex, printExecution, logger, isVerbose);
        } else {
            handleHttpCall(tc, step, context, yamlFile, stepIndex, printExecution, logger, op, isVerbose);
        }
    }

    // =========================================================
    // HTTP CALL
    // =========================================================
    private void handleHttpCall(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, String op, boolean isVerbose) throws Exception {

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        String method = step.getMethod();

        String rawUrl = step.getUrl();
        if (rawUrl == null || rawUrl.isBlank()) {
            throw AgateStepException.builder("REST URL is missing")
                    .field("url")
                    .hint("Check the REST module metadata or define a URL for this REST EXEC step.").build();
        }

        String providedEndpoint = step.getEndpoint();
        String urlWithEndpoint = rawUrl;

        if (providedEndpoint != null && !providedEndpoint.isEmpty()) {
            String resolvedEndpoint = resolver.resolve(tc, providedEndpoint, tc.getVariables(), yamlFile, stepIndex,
                    "endpoint");

            if (resolvedEndpoint.endsWith("/")) {
                resolvedEndpoint = resolvedEndpoint.substring(0, resolvedEndpoint.length() - 1);
            }

            urlWithEndpoint = rawUrl.replace("{{endpoint}}", resolvedEndpoint);
        }
        String finalUrl = resolver.resolve(tc, urlWithEndpoint, tc.getVariables(), yamlFile, stepIndex, "url");

        String rawBody = step.getBody();
        String finalBody = (rawBody != null)
                ? resolver.resolve(tc, rawBody, tc.getVariables(), yamlFile, stepIndex, "body")
                : "";
        finalBody = (finalBody != null)
                ? resolver.resolve(tc, finalBody, step.getParameters(), yamlFile, stepIndex, "body", step)
                : "";

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(finalUrl.trim()))
                    .method(method, finalBody.isEmpty() ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(finalBody));

            Map<String, String> resolvedHeaders = new HashMap<>();

            if (step.getHeaders() != null) {
                step.getHeaders().forEach((k, v) -> {
                    String dynamicValue = v;

                    // ====================================================
                    // 3. LOGIK FOR {NULL} AND {EMPTY}
                    // =========================================================
                    if (dynamicValue != null) {
                        String cleanVal = dynamicValue.replace("\"", "").trim();

                        if ("{NULL}".equalsIgnoreCase(cleanVal)) {
                            return; // Ekvivalentno sa 'continue' unutar forEach petlje
                        }

                        if ("{EMPTY}".equalsIgnoreCase(cleanVal)) {
                            dynamicValue = "";
                        }
                    }

                    // =========================================================
                    // 1. LOGIKA ZA B BUFFER {B[...]} ili B[...]
                    // =========================================================
                    if (dynamicValue != null && dynamicValue.contains("B[")) {
                        try {
                            int start = dynamicValue.indexOf("B[") + 2;
                            int end = dynamicValue.indexOf("]", start);
                            if (start > 1 && end > start) {
                                String bufferKey = dynamicValue.substring(start, end);
                                String realValue = null;

                                Object bufferVal = context.getBuffer(bufferKey);
                                if (bufferVal != null) {
                                    if (bufferVal instanceof RestResponse) {
                                        realValue = ((RestResponse) bufferVal).getBody();
                                    } else {
                                        realValue = bufferVal.toString();
                                    }
                                }

                                // FALLBACK: Ako nema u bufferu, proveravamo statičke varijable
                                if (realValue == null && step.getParameters() != null) {
                                    Object varVal = step.getParameters().get(bufferKey);
                                    if (varVal != null) {
                                        realValue = varVal.toString();
                                    }
                                }
                                // FALLBACK: Ako nema u bufferu, proveravamo statičke varijable
                                if (realValue == null && tc.getVariables() != null) {
                                    Object varVal = tc.getVariables().get(bufferKey);
                                    if (varVal != null) {
                                        realValue = varVal.toString();
                                    }
                                }

                                if (realValue != null) {
                                    realValue = realValue.trim().replace("\"", "");
                                    String placeholder = dynamicValue.contains("{B[" + bufferKey + "]}")
                                            ? "{B[" + bufferKey + "]}"
                                            : "B[" + bufferKey + "]";
                                    dynamicValue = dynamicValue.replace(placeholder, realValue);
                                }
                            }
                        } catch (Exception ex) {
                            // U slučaju greške, pusti dalje
                        }
                    }

                    // =========================================================
                    // 2. LOGIKA ZA E BUFFER {E[...]} ili E[...] (NOVO)
                    // =========================================================
                    if (dynamicValue != null && dynamicValue.contains("E[")) {
                        try {
                            int start = dynamicValue.indexOf("E[") + 2;
                            int end = dynamicValue.indexOf("]", start);
                            if (start > 1 && end > start) {
                                String envKey = dynamicValue.substring(start, end);
                                String realValue = null;

                                // Uzimamo vrednost iz varijabli test case-a (gde se obično nalaze env/globalne
                                // varijable)
                                if (tc.getVariables() != null) {
                                    Object varVal = tc.getVariables().get(envKey);
                                    if (varVal != null) {
                                        realValue = varVal.toString();
                                    }
                                }

                                if (realValue != null) {
                                    realValue = realValue.trim().replace("\"", "");
                                    String placeholder = dynamicValue.contains("{E[" + envKey + "]}")
                                            ? "{E[" + envKey + "]}"
                                            : "E[" + envKey + "]";
                                    dynamicValue = dynamicValue.replace(placeholder, realValue);
                                }
                            }
                        } catch (Exception ex) {
                            // U slučaju greške, pusti dalje
                        }
                    }

                    String val = resolver.resolve(tc, dynamicValue, tc.getVariables(), yamlFile, stepIndex, "header");

                    requestBuilder.header(k, val);
                    resolvedHeaders.put(k, val);
                });
            } else {
                requestBuilder.header("Content-Type", "application/json");
                resolvedHeaders.put("Content-Type", "application/json");
            }

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                RestPrinter.printRequest(method, finalUrl, resolvedHeaders, finalBody, logger);
            }

            // =================================================================
            // NOVI DODATAK: IZVRŠAVANJE SA RETRY MEHANIZMOM (Maksimalno 3 pokušaja)
            // =================================================================
            HttpResponse<String> response = null;
            int maxAttempts = 3;
            int attempt = 0;

            while (attempt < maxAttempts) {
                try {
                    attempt++;
                    response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    break;
                } catch (IOException e) {
                    boolean isHeaderError = e.getMessage() != null
                            && e.getMessage().contains("header parser received no bytes");
                    boolean isResetByPeer = e.getMessage() != null && (e.getMessage().contains("softwaregesteuert")
                            || e.getMessage().contains("Connection reset"));

                    if ((isHeaderError || isResetByPeer) && attempt < maxAttempts) {
                        if (isVerbose) {
                        logger.warn(String.format(
                                "    [RETRY] Attempt %d/%d failed (Connection closed by server). Retrying in 10ms...",
                                attempt, maxAttempts));
                        }
                        Thread.sleep(10); // Pauza pre otvaranja novog soketa
                        continue;
                    }
                    throw e;
                }
            }

            RestResponse restRes = new RestResponse(response.statusCode(), response.body(), response.headers().map(),
                    method, finalUrl);

            if (step.getResponse() != null) {
                context.storeBuffer(step.getResponse(), restRes);
            }

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                RestPrinter.printResponse(response.statusCode(), response.headers().map(), response.body(), logger);
            }

        } catch (AgateStepException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof HttpConnectTimeoutException) {
                throw AgateStepException.builder("REST connection timed out")
                        .detail("URL", finalUrl)
                        .hint("Check endpoint availability, network connectivity, proxy/firewall settings, or increase the HTTP timeout if appropriate.")
                        .cause(e).build();
            }

            if (e instanceof java.net.ConnectException) {
                throw AgateStepException.builder("REST connection failed")
                        .detail("URL", finalUrl)
                        .hint("Check whether the endpoint is reachable and whether host/port are correct.")
                        .cause(e).build();
            }

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw AgateStepException.builder("REST execution was interrupted")
                        .detail("URL", finalUrl)
                        .cause(e).build();
            }

            throw AgateStepException.builder("REST call failed")
                    .detail("URL", finalUrl)
                    .detail("Technical", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                    .cause(e).build();
        }
    }

    // =========================================================
    // ASSERT
    // =========================================================
    private void handleAssertion(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        String responseKey = step.getResponse();
        if (responseKey == null || responseKey.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("response")
                    .hint("Reference the response created by a previous REST EXEC step.").build();
        }

        RestResponse res =
                (RestResponse) context.getBuffer(responseKey);

        String source = step.getSource();
        String action = step.getAction();
        String path = step.getPath();

        // =========================================================
        // BASIC VALIDATION
        // =========================================================

        if (res == null) {
            throw AgateStepException.builder("REST response was not found")
                    .detail("Response", responseKey)
                    .hint("Make sure a REST EXEC step runs before this step and uses the same response name.").build();
        }

        if (action == null || action.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("action")
                    .hint("Define an assertion action, for example: EQUALS, CONTAINS, EXISTS or MATCH_REFERENCE.").build();
        }

        // =========================================================
        // MATCH_REFERENCE
        //
        // IMPORTANT:
        // Must be handled BEFORE expected/path/value resolution.
        // MATCH_REFERENCE does not require:
        //   - path
        //   - expected
        //   - value
        // =========================================================

        if ("MATCH_REFERENCE".equalsIgnoreCase(action)) {

            handleReferenceAssertion(
                    tc,
                    step,
                    res,
                    yamlFile,
                    logger,
                    printExecution,
                    isVerbose);

            return;
        }

        // =========================================================
        // NORMAL ASSERTIONS
        // =========================================================

        if (source == null || source.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("source")
                    .hint("Supported REST ASSERT sources: STATUS, BODY, HEADERS").build();
        }

        Object rawExpected = step.getExpected();

        String expected =
                rawExpected != null
                        ? rawExpected.toString()
                        : "";

        String valueField = step.getValue();

        boolean passed = false;
        String actual = "";

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        if (!expected.isEmpty()) {

            expected =
                    resolver.resolve(
                            tc,
                            expected,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "expected",
                            step
                    );

            expected =
                    resolver.resolve(
                            tc,
                            expected,
                            tc.getVariables(),
                            yamlFile,
                            stepIndex,
                            "expected",
                            step
                    );
        }
        
        if (valueField != null && !valueField.isBlank()) {
            valueField = resolver.resolve(
                    tc,
                    valueField,
                    tc.getVariables(),
                    yamlFile,
                    stepIndex,
                    "value");
        }

        switch (source.toUpperCase()) {

        case "STATUS":

            int status = res.getStatusCode();
            int expStatus;
            try {
                expStatus = Integer.parseInt(expected);
            } catch (NumberFormatException e) {
                throw AgateStepException.builder("Invalid expected HTTP status")
                        .expected("integer status code")
                        .actual(expected)
                        .field("expected")
                        .hint("Use a numeric HTTP status, for example: 200")
                        .cause(e).build();
            }

            passed = compareNumbers(
                    status,
                    expStatus,
                    action);

            actual = String.valueOf(status);
            break;

        case "BODY":

            if ("$.Body".equalsIgnoreCase(path)
                    && "CONTAINS".equalsIgnoreCase(action)) {

                actual = res.getBody();

                passed =
                        actual != null
                                && actual.contains(expected);

                break;
            }

            if (path == null || path.isBlank()) {
                throw AgateStepException.builder("Required property is missing")
                        .field("path")
                        .hint("REST BODY ASSERT requires a JSON path, for example: path: \"$.title\"").build();
            }

            Object jsonPathResult = null;
            boolean pathExists = true;

            try {

                jsonPathResult =
                        com.jayway.jsonpath.JsonPath.read(
                                res.getBody(),
                                path);

            } catch (com.jayway.jsonpath.PathNotFoundException e) {

                pathExists = false;
            }

            if (!pathExists
                    && !"EXISTS".equalsIgnoreCase(action)
                    && !"COUNT".equalsIgnoreCase(action)) {

                throw AgateStepException.builder("JSON path was not found")
                        .path(path)
                        .hint("Check the JSON response structure and the configured path.").build();
            }

            switch (action.toUpperCase()) {

            case "EXISTS":

                if (!pathExists || jsonPathResult == null) {

                    passed = false;
                    actual = "Path not found";

                } else if (jsonPathResult instanceof java.util.List) {

                    java.util.List<?> list =
                            (java.util.List<?>) jsonPathResult;

                    if (expected == null || expected.isEmpty()) {

                        passed = !list.isEmpty();

                        actual =
                                passed
                                        ? "Found elements in path: " + path
                                        : "Path found, but array is empty";

                    } else {

                        for (Object element : list) {

                            if (element != null
                                    && element.toString().equals(expected)) {

                                passed = true;
                                break;
                            }
                        }

                        actual =
                                "Array checked for value '"
                                        + expected
                                        + "'. Found? "
                                        + passed;
                    }

                } else {

                    passed = true;
                    actual = jsonPathResult.toString();
                }

                break;

            case "COUNT":

                if (pathExists
                        && jsonPathResult instanceof java.util.List) {

                    java.util.List<?> list =
                            (java.util.List<?>) jsonPathResult;

                    if (valueField != null
                            && !valueField.isEmpty()) {

                        int count = 0;

                        for (Object element : list) {

                            if (element != null
                                    && element.toString().equals(valueField)) {

                                count++;
                            }
                        }

                        actual = String.valueOf(count);

                    } else {

                        actual = String.valueOf(list.size());
                    }

                    passed =
                            compareNumbers(
                                    Integer.parseInt(actual),
                                    Integer.parseInt(expected),
                                    "EQUALS");

                } else if (pathExists) {

                    actual = "1";

                    passed =
                            compareNumbers(
                                    1,
                                    Integer.parseInt(expected),
                                    "EQUALS");

                } else {

                    actual = "0";

                    passed =
                            compareNumbers(
                                    0,
                                    Integer.parseInt(expected),
                                    "EQUALS");
                }

                break;

            case "CONTAINS":

                if (!pathExists || jsonPathResult == null) {

                    passed = false;
                    actual = "Path not found";

                } else if (jsonPathResult instanceof java.util.List) {

                    java.util.List<?> list =
                            (java.util.List<?>) jsonPathResult;

                    actual = list.toString();

                    for (Object element : list) {

                        if (element != null
                                && element.toString().equals(expected)) {

                            passed = true;
                            break;
                        }
                    }

                } else {

                    actual = jsonPathResult.toString();
                    passed = actual.contains(expected);
                }

                break;

            default:

                if (!pathExists || jsonPathResult == null) {
                    throw AgateStepException.builder("JSON path was not found")
                            .path(path)
                            .hint("Check the JSON response structure and the configured path.").build();
                }

                if (jsonPathResult instanceof java.util.List) {

                    java.util.List<?> list =
                            (java.util.List<?>) jsonPathResult;

                    if (list.size() > 1) {
                        throw AgateStepException.builder("JSON path returned multiple elements")
                                .path(path)
                                .actual(list.size() + " elements")
                                .hint("Specify an array index such as $[0], or use EXISTS/COUNT.").build();
                    }

                    if (list.size() == 1) {

                        actual = list.get(0).toString();

                    } else {

                        actual = "";
                    }

                } else {

                    actual = jsonPathResult.toString();
                }

                passed =
                        compareStrings(
                                actual,
                                expected,
                                action);

                break;
            }

            break;

        case "HEADERS":

            Map<String, String> headers =
                    res.getHeadersMap();

            actual = headers.get(path);

            if ("IS_HEADER_PRESENT".equalsIgnoreCase(action)) {

                passed = headers.containsKey(path);

            } else {

                throw AgateStepException.builder("Unsupported REST HEADERS assertion action")
                        .actual(action)
                        .hint("Supported HEADERS action: IS_HEADER_PRESENT").build();
            }

            break;

        default:

            throw AgateStepException.builder("Unsupported REST assertion source")
                    .actual(source)
                    .hint("Supported REST ASSERT sources: STATUS, BODY, HEADERS").build();
        }

        if (passed && Boolean.TRUE.equals(printExecution) && isVerbose) {
            logAssertionResult(
                    logger,
                    true,
                    action,
                    expected,
                    actual,
                    null);
        }

        if (!passed) {
            AgateStepException.Builder failure = AgateStepException.builder("REST assertion failed")
                    .expected(expected)
                    .actual(actual)
                    .detail("Source", source)
                    .detail("Action", action);

            if (path != null && !path.isBlank()) {
                failure.path(path);
            }

            throw failure.build();
        }
    }
    

    private void handleReferenceAssertion(
            TestCase tc,
            TestStep step,
            RestResponse res,
            String yamlFile,
            TestLogger logger,
            Boolean printExecution,
            boolean isVerbose) throws Exception {

        String source = step.getSource();

        if (source != null && !"BODY".equalsIgnoreCase(source)) {
            throw AgateStepException.builder("Unsupported source for MATCH_REFERENCE")
                    .expected("BODY")
                    .actual(source)
                    .hint("REST MATCH_REFERENCE currently supports source BODY only.").build();
        }

        if (step.getId() == null || step.getId().isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("id")
                    .hint("MATCH_REFERENCE requires a step id so AGATE can resolve the reference file.").build();
        }

        ReferenceCompareConfig config = new ReferenceCompareConfig()
                .addIgnore(step.getIgnore())
                .addUnordered(step.getUnordered());

        ReferenceAssertionResult result = REFERENCE_RESPONSE_SERVICE.assertResponse(
                ResponseFormat.JSON,
                tc,
                step.getId(),
                yamlFile,
                res.getBody(),
                config);

        if (result.isCreated()) {
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format(
                        "    %s>>> REST ASSERT REFERENCE CREATED%s : %s",
                        ConsoleColors.YELLOW,
                        ConsoleColors.RESET,
                        result.getReferenceFile().toAbsolutePath()));

                logger.info(String.format(
                        "    %s>>> REVIEW REQUIRED%s : Check the generated reference response and keep/commit it only if it is correct.",
                        ConsoleColors.YELLOW,
                        ConsoleColors.RESET));
            }
            return;
        }

        if (result.isMatched()) {
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format(
                        "    %s>>> REST ASSERT SUCCESS%s: MATCH_REFERENCE | Reference: %s",
                        ConsoleColors.GREEN,
                        ConsoleColors.RESET,
                        result.getReferenceFile().toAbsolutePath()));
            }
            return;
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logReferenceDifferences(logger, result);
        }

        throw AgateStepException.builder("REST reference comparison failed")
                .actual(result.getComparisonResult().getDifferenceCount() + " difference(s)")
                .detail("Reference", result.getReferenceFile().toAbsolutePath())
                .hint("Review the reported differences. Update the reference only if the new response is correct.").build();
    }

    private void logReferenceDifferences(
            TestLogger logger,
            ReferenceAssertionResult result) {

        logger.info(String.format(
                "    %s>>> REST ASSERT MATCH_REFERENCE: FAILED%s",
                ConsoleColors.RED,
                ConsoleColors.RESET));

        logger.info(String.format(
                "    %s>>> REFERENCE%s: %s",
                ConsoleColors.RED,
                ConsoleColors.RESET,
                result.getReferenceFile().toAbsolutePath()));

        int index = 1;

        for (ComparisonDifference difference
                : result.getComparisonResult().getDifferences()) {

            logger.info(String.format(
                    "    %s>>> DIFFERENCE [%d] %s%s",
                    ConsoleColors.RED,
                    index,
                    difference.type(),
                    ConsoleColors.RESET));

            logger.info("        path     : " + nullSafe(difference.path()));
            logger.info("        expected : " + nullSafe(difference.expected()));
            logger.info("        actual   : " + nullSafe(difference.actual()));

            index++;
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "<null>";
    }

    private void throwMissingResponseDiagnostic(
            TestStep step,
            TestLogger logger,
            Boolean printExecution,
            boolean isVerbose,
            String operation) {

        String responseName = step.getResponse();
        String displayResponseName =
                responseName == null || responseName.isBlank()
                        ? "<not defined>"
                        : responseName;

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info("");
            logger.info(String.format(
                    "    %s>>> REST %s ERROR%s",
                    ConsoleColors.RED,
                    operation,
                    ConsoleColors.RESET));

            logger.info(String.format(
                    "        %sResponse '%s' was not found.%s",
                    ConsoleColors.RED,
                    displayResponseName,
                    ConsoleColors.RESET));

            logger.info("");
            logger.info("        What happened:");
            logger.info("          This REST step expects a response produced by");
            logger.info("          a previously executed REST EXEC step, but AGATE");
            logger.info("          cannot find that response in the current execution context.");

            logger.info("");
            logger.info(String.format(
                    "        %sPossible causes:%s",
                    ConsoleColors.YELLOW,
                    ConsoleColors.RESET));

            logger.info("          - the corresponding REST EXEC step was not executed");
            logger.info("          - the EXEC step uses a different 'response:' name");
            logger.info("          - the EXEC step was skipped because its condition was false");
            logger.info("          - this step is placed before the corresponding EXEC step");

            if (responseName == null || responseName.isBlank()) {
                logger.info("          - this step does not define 'response:' at all");
            }

            logger.info("");
            logger.info(String.format(
                    "        %sHow to fix:%s",
                    ConsoleColors.YELLOW,
                    ConsoleColors.RESET));

            if (responseName != null && !responseName.isBlank()) {
                logger.info("          Make sure a REST EXEC step is executed before this step:");
                logger.info("");
                logger.info("            - type: REST");
                logger.info("              op: EXEC");
                logger.info("              ...");
                logger.info("              response: \"" + responseName + "\"");
            } else {
                logger.info("          Define 'response:' in this step and use the same name");
                logger.info("          in the corresponding REST EXEC step.");
            }

            logger.info("");
            logger.info(String.format(
                    "        %sCurrent step:%s",
                    ConsoleColors.YELLOW,
                    ConsoleColors.RESET));
            logger.info("          type     : " + valueOrMissing(step.getType() != null ? step.getType().toString() : null));
            logger.info("          op       : " + valueOrMissing(step.getOp()));
            logger.info("          id       : " + valueOrMissing(step.getId()));
            logger.info("          source   : " + valueOrMissing(step.getSource()));
            logger.info("          action   : " + valueOrMissing(step.getAction()));
            logger.info("          response : " + valueOrMissing(step.getResponse()));
            logger.info("");
        }

        throw AgateStepException.builder("REST response was not found")
                .detail("Response", displayResponseName)
                .hint("Make sure a REST EXEC step runs before this step and uses the same response name.").build();
    }

    private String valueOrMissing(String value) {
        return value == null || value.isBlank()
                ? "<not defined>"
                : value;
    }

    private void logAssertionResult(TestLogger logger, boolean passed, String action, String expected, String actual, String body) {
        String color = passed ? ConsoleColors.GREEN : ConsoleColors.RED;
        String status = passed ? "SUCCESS" : "FAILED";
        
        // Ispis glavne linije
        logger.info(String.format("    %s>>> %s: %s | Expected: [%s], Actual: [%s]%s", 
                color, status, action, expected, actual, ConsoleColors.RESET));

        // Ispis ERROR BODY-a (samo ako nije prošlo i ako body postoji)
        if (!passed && body != null && !body.trim().isEmpty()) {
            logger.info(String.format("    %s<<< ERROR BODY:%s", ConsoleColors.RED, ConsoleColors.RESET));
            for (String line : body.split("\\R")) {
                if (!line.trim().isEmpty()) {
                    logger.info(String.format("    %s<<< %s%s", ConsoleColors.RED, ConsoleColors.RESET, line));
                }
            }
        }
    }

    // =========================================================
    // BUFFER
    // =========================================================
    private void handleBuffer(TestCase tc, TestStep step, ExecutionContext context, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        String responseKey = step.getResponse();
        if (responseKey == null || responseKey.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("response")
                    .hint("REST BUFFER must reference a response created by a previous REST EXEC step.").build();
        }

        RestResponse res = (RestResponse) context.getBuffer(responseKey);

        String source = step.getSource();
        String path = step.getPath();
        String value;

        if (res == null) {
            throw AgateStepException.builder("REST response was not found")
                    .detail("Response", responseKey)
                    .hint("Make sure a REST EXEC step runs before this BUFFER step and uses the same response name.").build();
        }

        if (source == null || source.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("source")
                    .hint("Supported REST BUFFER sources: BODY, STATUS, HEADERS").build();
        }

        if (step.getName() == null || step.getName().isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("name")
                    .hint("REST BUFFER requires 'name' to store the extracted value.").build();
        }

        switch (source.toUpperCase()) {

        case "BODY":
            if (path == null || path.isBlank()) {
                throw AgateStepException.builder("Required property is missing")
                        .field("path")
                        .hint("REST BUFFER with source BODY requires a JSON path, for example: path: \"$.title\"").build();
            }

            JsonNode root = MAPPER.readTree(res.getBody());
            JsonNode node;

            // ISTA LOGIKA KAO U ASSERT-u: Ako je putanja $.item ili prazna, a koren je niz
            if (path.equals("$.item") || path.equals("$") || path.isEmpty()) {
                node = root;
            } else {
                // Standardno pretvaranje u Jackson pointer za ostale putanje
                String cleanPath = path.replace("$.", "");
                String ptr = "/" + cleanPath.replace(".", "/");
                node = root.at(ptr);
            }

            if (node == null || node.isMissingNode()) {
                throw AgateStepException.builder("JSON path was not found")
                        .path(path)
                        .hint("Check the JSON response structure and the configured path.").build();
            }

            // DEO ZA PODRŠKU "COUNT" AKCIJE
            String action = step.getAction();
            if (action != null && action.toUpperCase().equals("COUNT")) {
                if (node.isArray()) {
                    value = String.valueOf(node.size());
                } else if (node.isObject()) {
                    value = String.valueOf(node.size());
                } else {
                    value = "1";
                }
            } else {
                value = node.asText();
            }
            break;

        case "STATUS":
            value = String.valueOf(res.getStatusCode());
            break;

        case "HEADERS":
            value = res.getHeadersMap().get(path);
            if (value == null) {
                throw AgateStepException.builder("REST header was not found")
                        .path(path)
                        .hint("Check the response headers and the configured header name.").build();
            }
            break;

        default:
            throw AgateStepException.builder("Unsupported REST buffer source")
                    .actual(source)
                    .hint("Supported REST BUFFER sources: BODY, STATUS, HEADERS").build();
        }

        tc.addVariable(step.getName(), value);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            // Skraćivanje vrednosti ako je predugačka (opciono, radi čistoće loga)
            String displayValue = (value.length() > 900) ? value.substring(0, 900) + "..." : value;
            
            // Uniformni ispis
            logger.info(String.format("    %s>>> BUFFER      :%s Value [%s] stored in variable [%s]", 
                    ConsoleColors.GREEN, ConsoleColors.RESET, displayValue, step.getName()));
        }        
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private boolean compareStrings(String actual, Object expected, String action) {
        String exp = expected != null ? expected.toString() : null;

        switch (action.toUpperCase()) {
        case "EQUALS":
            return actual.equals(exp);
        case "NOT_EQUALS":
            return !actual.equals(exp);
        case "CONTAINS":
            return actual.contains(exp);
        case "IS_EMPTY":
            return actual == null || actual.isEmpty();
        case "IS_NOT_EMPTY":
            return actual != null && !actual.isEmpty();
        default:
            throw AgateStepException.builder("Unsupported REST assertion action")
                    .actual(action)
                    .hint("Supported string actions: EQUALS, NOT_EQUALS, CONTAINS, IS_EMPTY, IS_NOT_EMPTY").build();
        }
    }

    private boolean compareNumbers(int actual, int expected, String action) {
        switch (action.toUpperCase()) {
        case "EQUALS":
        case "EXITCODE":
            return actual == expected;
        case "GREATER_THAN":
            return actual > expected;
        case "GREATER_OR_EQUALS":
            return actual >= expected;
        case "LESS_THAN":
            return actual < expected;
        case "LESS_OR_EQUALS":
            return actual <= expected;
        default:
            throw AgateStepException.builder("Unsupported REST numeric assertion action")
                    .actual(action)
                    .hint("Supported numeric actions: EQUALS, GREATER_THAN, GREATER_OR_EQUALS, LESS_THAN, LESS_OR_EQUALS").build();
        }
    }

    private boolean isAssertionStep(TestStep step) {
        return "ASSERT".equalsIgnoreCase(step.getOp());
    }

    private boolean isBufferStep(TestStep step) {
        return "BUFFER".equalsIgnoreCase(step.getOp());
    }
    
    private static HttpClient createUnsafeClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return HttpClient.newBuilder().sslContext(sslContext).connectTimeout(Duration.ofSeconds(10)).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HTTP client", e);
        }
    }

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.REST;
    }

}