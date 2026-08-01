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
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.interfaces.TestStepEngine;

public class RestEngine implements TestStepEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = createUnsafeClient();

    @Override
    public void execute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";

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
        if (rawUrl == null) {
            throw new RuntimeException("URL or Action not found for REST step");
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

        } catch (Exception e) {
            String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();

            if (e instanceof HttpConnectTimeoutException) {
                msg = "Connection timed out (10s).";
            } else if (e instanceof java.net.ConnectException) {
                msg = "Connection refused/failed: " + e.getClass().getSimpleName();
            } else if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                msg = "Thread execution interrupted during retry delay.";
            }

            throw new RuntimeException(msg);
        }
    }

    // =========================================================
    // ASSERT
    // =========================================================
    private void handleAssertion(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        RestResponse res = (RestResponse) context.getBuffer(step.getResponse());

        String source = step.getSource();
        String action = step.getAction();
        String path = step.getPath();

        Object rawExpected = step.getExpected();
        String expected = rawExpected != null ? rawExpected.toString() : "";
        String valueField = step.getValue(); 

        boolean passed = false;
        String actual = "";

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        expected = resolver.resolve(tc, expected, tc.getVariables(), yamlFile, stepIndex, "");
        if (valueField != null) {
            valueField = resolver.resolve(tc, valueField, tc.getVariables(), yamlFile, stepIndex, "");
        }

        if (res == null) {
            throw new RuntimeException("Response not found: " + step.getResponse());
        }

        if (source == null) {
            throw new RuntimeException("source not found!");
        }

        switch (source.toUpperCase()) {

        case "STATUS":
            int status = res.getStatusCode();
            int expStatus = Integer.parseInt(expected);
            passed = compareNumbers(status, expStatus, action);
            actual = String.valueOf(status);
            break;

        case "BODY":
            if ("$.Body".equalsIgnoreCase(path) && "CONTAINS".equalsIgnoreCase(action)) {
                actual = res.getBody();
                passed = actual != null && actual.contains(expected);
                break;
            }

            // --- NOVI JSON PATH ENGINE (Jayway JsonPath) ---
            Object jsonPathResult = null;
            boolean pathExists = true;

            try {
                jsonPathResult = com.jayway.jsonpath.JsonPath.read(res.getBody(), path);
            } catch (com.jayway.jsonpath.PathNotFoundException e) {
                pathExists = false;
            }

            // Standardna provera - ako putanja ne postoji, a akcija nije EXISTS ili COUNT
            if (!pathExists && !"EXISTS".equalsIgnoreCase(action) && !"COUNT".equalsIgnoreCase(action)) {
                throw new RuntimeException("Path not found: " + path);
            }

            switch (action.toUpperCase()) {
            case "EXISTS":
                if (!pathExists || jsonPathResult == null) {
                    passed = false;
                    actual = "Path not found";
                } else if (jsonPathResult instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) jsonPathResult;
                    if (expected == null || expected.isEmpty()) {
                        // SCENARIO 1: Samo provera postojanja elemenata ($[*].something)
                        passed = !list.isEmpty();
                        actual = passed ? "Found elements in path: " + path : "Path found, but array is empty";
                    } else {
                        // SCENARIO 2: Provera da li bar jedan od sakupljenih elemenata ima traženu
                        // vrednost
                        for (Object element : list) {
                            if (element != null && element.toString().equals(expected)) {
                                passed = true;
                                break;
                            }
                        }
                        actual = "Array checked for value '" + expected + "'. Found? " + passed;
                    }
                } else {
                    // Standardni objekat preko eksplicitnog indeksa ($[0].something)
                    passed = true;
                    actual = jsonPathResult.toString();
                }
                break;

            case "COUNT":
                if (pathExists && jsonPathResult instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) jsonPathResult;
                    if (valueField != null && !valueField.isEmpty()) {
                        int count = 0;
                        for (Object element : list) {
                            if (element != null && element.toString().equals(valueField)) {
                                count++;
                            }
                        }
                        actual = String.valueOf(count);
                    } else {
                        actual = String.valueOf(list.size());
                    }
                    passed = compareNumbers(Integer.parseInt(actual), Integer.parseInt(expected), "EQUALS");
                } else if (pathExists) {
                    // Ako je putanja npr $[0] (jedan objekat), a traži se COUNT, tehnički je
                    // rezultat 1
                    actual = "1";
                    passed = compareNumbers(1, Integer.parseInt(expected), "EQUALS");
                } else {
                    actual = "0";
                    passed = compareNumbers(0, Integer.parseInt(expected), "EQUALS");
                }
                break;
            // >>> NOVI CASE KOJI REŠAVA PROBLEM <<<
            case "CONTAINS":
                if (!pathExists || jsonPathResult == null) {
                    passed = false;
                    actual = "Path not found";
                } else if (jsonPathResult instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) jsonPathResult;
                    actual = list.toString(); // Za ispis u logu tipa ["Chanda", "SIMU"...]

                    // Proveravamo da li lista sadrži očekivani string
                    for (Object element : list) {
                        if (element != null && element.toString().equals(expected)) {
                            passed = true;
                            break;
                        }
                    }
                } else {
                    // Ako je putanja ipak vratila jedan objekat (skalar), radi klasičan
                    // String.contains
                    actual = jsonPathResult.toString();
                    passed = actual.contains(expected);
                }
                break;
            default:
                // Ovo pokriva VERIFY, EQUALS, i sve ostale string komparacije
                if (!pathExists || jsonPathResult == null) {
                    throw new RuntimeException("Path not found: " + path);
                }

                if (jsonPathResult instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) jsonPathResult;

                    // Ako je korisnik stavio [*] a koristi Verify akciju koja vraća više elemenata
                    if (list.size() > 1) {
                        throw new RuntimeException("Validation failed: Path '" + path + "' returned multiple elements ("
                                + list.size() + "). You must specify an index like $[0] or use EXISTS/COUNT action!");
                    }

                    if (list.size() == 1) {
                        // Ako wildcard vrati samo jedan element, dozvoljavamo "prečicu" i čitamo njega
                        actual = list.get(0).toString();
                    } else {
                        actual = "";
                    }
                } else {
                    // Standardni objekat/skalar iz $[0].urlWithVersion[0].url
                    actual = jsonPathResult.toString();
                }

                passed = compareStrings(actual, expected, action);
                break;
            }
            break;

        case "HEADERS":
            Map<String, String> headers = res.getHeadersMap();
            actual = headers.get(path);

            if ("IS_HEADER_PRESENT".equalsIgnoreCase(action)) {
                passed = headers.containsKey(path);
            } else {
                throw new RuntimeException("Unsupported HEADERS action: " + action);
            }
            break;

        default:
            throw new RuntimeException("Unsupported source: " + source);
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logAssertionResult(logger, passed, action, expected, actual, passed ? null : null);
        }

        // 2. Ako nije prošlo, baci izuzetak (ovo prekida test)
        if (!passed) {
            throw new RuntimeException(String.format("Assertion %s failed! Expected: %s, Actual: %s", action, expected, actual));
        }
        
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

        RestResponse res = (RestResponse) context.getBuffer(step.getResponse());

        String source = step.getSource();
        String path = step.getPath();
        String value;

        if (res == null) {
            throw new RuntimeException("Response not found: " + step.getResponse());
        }

        switch (source.toUpperCase()) {

        case "BODY":
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
                throw new RuntimeException("Path not found: " + path);
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
                throw new RuntimeException("Header not found: " + path);
            }
            break;

        default:
            throw new RuntimeException("Unsupported source: " + source);
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
            throw new RuntimeException("Invalid action: " + action);
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
            throw new RuntimeException("Invalid numeric action: " + action);
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