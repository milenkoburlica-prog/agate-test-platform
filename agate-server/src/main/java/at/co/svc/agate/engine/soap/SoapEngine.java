package at.co.svc.agate.engine.soap;

import java.io.ByteArrayInputStream;

import java.io.StringReader;

import java.io.StringWriter;

import java.net.URI;

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.SecureRandom;

import java.time.Duration;

import java.time.Instant;

import java.util.Base64;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import javax.net.ssl.TrustManager;

import javax.net.ssl.X509TrustManager;

import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;

import javax.xml.transform.Source;

import javax.xml.transform.Transformer;

import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.stream.StreamSource;

import javax.xml.xpath.XPath;

import javax.xml.xpath.XPathConstants;

import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import at.co.svc.agate.core.dsl.model.Constraint;

import at.co.svc.agate.core.dsl.model.DownloadConfig;

import at.co.svc.agate.core.dsl.model.StepType;

import at.co.svc.agate.core.dsl.model.TestCase;

import at.co.svc.agate.core.dsl.model.TestStep;

import at.co.svc.agate.core.dsl.model.UploadConfig;

import at.co.svc.agate.core.dsl.register.PrintDslStepContext;

import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;

import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

import at.co.svc.agate.core.dsl.utils.ConsoleColors;

import at.co.svc.agate.core.reference.ComparisonDifference;

import at.co.svc.agate.core.reference.ReferenceAssertionResult;

import at.co.svc.agate.core.reference.ReferenceCompareConfig;

import at.co.svc.agate.core.reference.ReferenceFileStore;

import at.co.svc.agate.core.reference.ReferencePathResolver;

import at.co.svc.agate.core.reference.ReferenceResponseService;

import at.co.svc.agate.core.reference.ResponseFormat;

import at.co.svc.agate.core.reference.xml.XmlResponseComparator;

import at.co.svc.agate.core.engine.AbstractStepEngine;

import at.co.svc.agate.core.interfaces.TestLogger;

import at.co.svc.agate.engine.rest.RestResponse;

public class SoapEngine extends AbstractStepEngine {

    private static final HttpClient CLIENT = createUnsafeClient();

    private static final ReferenceResponseService REFERENCE_RESPONSE_SERVICE =

            new ReferenceResponseService(

                    new ReferencePathResolver(),

                    new ReferenceFileStore(),

                    new XmlResponseComparator());

    @Override

    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,

            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        boolean isVerboseLocal = true;

        if (step.getParameters() != null && step.getParameters().containsKey("verbose")) {

            isVerboseLocal = Boolean.parseBoolean(step.getParameters().get("verbose").toString());

        }

        if (isVerbose && isVerboseLocal) {

            PrintDslStepContext.logDslStepContext(logger, step);

        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";

        if (isAssertionStep(step)) {

            handleAssertion(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);

        } else if (isBufferStep(step)) {

            handleBuffer(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);

        } else {

            handleHttpCall(tc, step, context, yamlFile, stepIndex, printExecution, logger, op, isVerbose);

        }

    }

    private void handleHttpCall(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,

            Boolean printExecution, TestLogger logger, String op, boolean isVerbose) throws Exception {

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        // 1. Attempt to extract base URL/Action prior to resolution for basic logging

        String rawUrl = (step.getUrl() != null && !step.getUrl().isEmpty()) ? step.getUrl() : step.getAction();

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {

            String logUrl = (rawUrl != null) ? rawUrl : (step.getCommand() != null ? step.getCommand() : "UNKNOWN");

            logger.info(String.format("    %s>>> CALL%s    : [POST] %s (Initializing...)", ConsoleColors.GREEN,

                    ConsoleColors.RESET, logUrl));

        }

        if (rawUrl == null) {

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {

                logger.info(String.format("    %s>>> RESULT%s  : %sFAILURE (URL or Action missing)%s",

                        ConsoleColors.GREEN, ConsoleColors.RESET, ConsoleColors.RED, ConsoleColors.RESET));

            }

            throw new RuntimeException("URL or Action not found for SOAP step (Check command mapping / metadata.json)");

        }

        try {

            // 2. Dynamic resolution of endpoint and URL

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

            // 3. Loading and resolution of XML Body

            String rawBody = (step.getBody() != null) ? step.getBody()

                    : (step.getValue() != null ? step.getValue() : "");

            String xmlBody = resolver.resolve(tc, rawBody, tc.getVariables(), yamlFile, stepIndex, "body");

            String preprocessedXml = xmlBody.replace("{EMPTY}", "");

            String cleanedXmlBody = preprocessedXml;

            cleanedXmlBody = cleanedXmlBody.replaceAll("(?m)^\\s*$\\r?\\n", "");

            cleanedXmlBody = cleanedXmlBody.trim();

            // 4. Filtering {NULL} and complex null nodes

            try {

                Map<String, Object> stepMapForNullFilter = new HashMap<>();

                if (step.getParameters() != null) {

                    stepMapForNullFilter.put("parameters", step.getParameters());

                }

                cleanedXmlBody = removeNullNodesFromXml(cleanedXmlBody, stepMapForNullFilter);

                cleanedXmlBody = removeComplexNullNodesFromXml(cleanedXmlBody, stepMapForNullFilter);

            } catch (Exception e) {

                logger.info("    " + ConsoleColors.YELLOW + ">>> [Warning] Null node filtering failed: " + e.getMessage() + ConsoleColors.RESET);

            }

            // Inject WS-Security header

            if (step.getAuth() != null && "WSS_USERNAME_TOKEN_DIGEST".equalsIgnoreCase(step.getAuth().getType())) {

                String username = step.getAuth().getUsername();

                String password = step.getAuth().getPassword();

                String wssHeader = generateWssHeader(username, password);

                if (cleanedXmlBody.contains("<soap:Header>")) {

                    cleanedXmlBody = cleanedXmlBody.replaceFirst("<soap:Header>.*?</soap:Header>", wssHeader);

                } else {

                    int envelopeEndIndex = cleanedXmlBody.indexOf(">");

                    if (envelopeEndIndex != -1) {

                        cleanedXmlBody = cleanedXmlBody.substring(0, envelopeEndIndex + 1) + "\n" + wssHeader

                                + cleanedXmlBody.substring(envelopeEndIndex + 1);

                    }

                }

            }

            // 5. Upload / MTOM preparation

            String finalHttpContentType = "text/xml;charset=UTF-8";

            byte[] finalHttpBodyBytes = cleanedXmlBody.getBytes(StandardCharsets.UTF_8);

            if (step.getUpload() != null && !step.getUpload().isEmpty()) {

                String boundary = "uuid:" + java.util.UUID.randomUUID().toString();

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

                java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

                javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();

                factory.setNamespaceAware(true);

                org.w3c.dom.Document xmlDoc = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(cleanedXmlBody.getBytes(StandardCharsets.UTF_8)));

                javax.xml.xpath.XPath xPath = javax.xml.xpath.XPathFactory.newInstance().newXPath();

                boolean hasMtom = false;

                java.util.List<byte[]> filesBytesList = new java.util.ArrayList<>();

                java.util.List<String> cidsList = new java.util.ArrayList<>();

                for (UploadConfig config : step.getUpload()) {

                    String uploadMethod = config.getMethod() != null ? config.getMethod().toUpperCase() : "INLINE";

                    String xpathUpload = config.getPath();

                    String resolvedDotNotation = resolver.resolve(tc, config.getSourceFile(), tc.getVariables(), yamlFile, stepIndex, "sourceFile");

                    java.nio.file.Path fileToUpload = resolveDotNotationPath(yamlFile, resolvedDotNotation);

                    if (!java.nio.file.Files.exists(fileToUpload)) {

                        throw new java.io.FileNotFoundException("Upload Error: Source file not found at calculated path: " + fileToUpload.toAbsolutePath());

                    }

                    byte[] fileBytes = java.nio.file.Files.readAllBytes(fileToUpload);

                    org.w3c.dom.Node targetNode = (org.w3c.dom.Node) xPath.evaluate(xpathUpload, xmlDoc, javax.xml.xpath.XPathConstants.NODE);

                    if (targetNode == null) {

                        throw new Exception("Upload Error: XPath '" + xpathUpload + "' did not match any node in request XML.");

                    }

                    if ("INLINE".equals(uploadMethod)) {

                        String base64File = java.util.Base64.getEncoder().encodeToString(fileBytes);

                        targetNode.setTextContent(base64File);

                    } else if ("MTOM".equals(uploadMethod)) {

                        hasMtom = true;

                        String cid = "attachment-" + java.util.UUID.randomUUID().toString() + "@soap.abs.client.chipkarte.at";

                        targetNode.setTextContent(""); 

                        org.w3c.dom.Element xopInclude = xmlDoc.createElementNS("http://www.w3.org/2004/08/xop/include", "xop:Include");

                        xopInclude.setAttribute("href", "cid:" + cid);

                        targetNode.appendChild(xopInclude);

                        filesBytesList.add(fileBytes);

                        cidsList.add(cid);

                    }

                }

                javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();

                transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

                java.io.StringWriter stringWriter = new java.io.StringWriter();

                transformer.transform(new javax.xml.transform.dom.DOMSource(xmlDoc), new javax.xml.transform.stream.StreamResult(stringWriter));

                cleanedXmlBody = stringWriter.toString();

                if (hasMtom) {

                    writer.print("--" + boundary + "\r\n");

                    writer.print("Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\"\r\n");

                    writer.print("Content-Transfer-Encoding: binary\r\n");

                    writer.print("Content-ID: <root.message@cxf.apache.org>\r\n\r\n");

                    writer.flush();

                    baos.write(cleanedXmlBody.getBytes(StandardCharsets.UTF_8));

                    baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

                    for (int i = 0; i < filesBytesList.size(); i++) {

                        writer.print("--" + boundary + "\r\n");

                        writer.print("Content-Type: application/zip\r\n");

                        writer.print("Content-Transfer-Encoding: binary\r\n");

                        writer.print("Content-ID: <" + cidsList.get(i) + ">\r\n\r\n");

                        writer.flush();

                        baos.write(filesBytesList.get(i));

                        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

                    }

                    writer.print("--" + boundary + "--\r\n");

                    writer.flush();

                    finalHttpBodyBytes = baos.toByteArray();

                    finalHttpContentType = "multipart/related; type=\"application/xop+xml\"; boundary=\"" + boundary + "\"; start=\"<root.message@cxf.apache.org>\"; start-info=\"text/xml\"";

                } else {

                    finalHttpBodyBytes = cleanedXmlBody.getBytes(StandardCharsets.UTF_8);

                }

            }

            // 6. HTTP Request Construction

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()

                    .uri(URI.create(finalUrl.trim()))

                    .timeout(Duration.ofSeconds(15))

                    .header("Content-Type", finalHttpContentType)

                    .POST(HttpRequest.BodyPublishers.ofByteArray(finalHttpBodyBytes));

            if (step.getHeaders() != null) {

                step.getHeaders().forEach(requestBuilder::header);

            }

            HttpRequest request = requestBuilder.build();

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {

                printSoapRequest(request.method(), finalUrl, request.headers().map(), cleanedXmlBody, logger);

            }

            HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            // Decode GZIP response

            String finalBody;

            byte[] rawBytes = response.body();

            String encoding = response.headers().firstValue("Content-Encoding").orElse("").toLowerCase();

            if (rawBytes != null && rawBytes.length > 0) {

                if (encoding.contains("gzip")) {

                    try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {

                        finalBody = new String(gis.readAllBytes(), StandardCharsets.UTF_8);

                    }

                } else {

                    finalBody = new String(rawBytes, StandardCharsets.UTF_8);

                }

            } else {

                finalBody = "";

            }

            String cleanedBodyForContext = finalBody;

            if (finalBody != null && finalBody.contains("<soap:Envelope")) {

                try {

                    int xmlStart = finalBody.indexOf("<soap:Envelope");

                    int xmlEnd = finalBody.indexOf("</soap:Envelope>") + "</soap:Envelope>".length();

                    if (xmlStart != -1 && xmlEnd != -1) {

                        cleanedBodyForContext = finalBody.substring(xmlStart, xmlEnd);

                    }

                } catch (Exception ignored) {

                }

            }

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {

                printSoapResponse(response.statusCode(), response.headers().map(), cleanedBodyForContext, logger);

            }

            if (response.statusCode() == 200 && step.getDownload() != null && !step.getDownload().isEmpty()

                    && cleanedBodyForContext.contains("<soap:Envelope")) {

                try {

                    javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();

                    factory.setNamespaceAware(true);

                    javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();

                    org.w3c.dom.Document xmlDoc = builder.parse(new java.io.ByteArrayInputStream(

                            cleanedBodyForContext.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

                    this.processDownloads(xmlDoc, step.getDownload(), rawBytes, tc, context, yamlFile, stepIndex);

                    javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();

                    javax.xml.transform.Transformer transformer = tf.newTransformer();

                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

                    java.io.StringWriter writer = new java.io.StringWriter();

                    transformer.transform(new javax.xml.transform.dom.DOMSource(xmlDoc), new javax.xml.transform.stream.StreamResult(writer));

                    cleanedBodyForContext = writer.toString();

                } catch (Exception e) {

                    logger.info("    " + ConsoleColors.RED + ">>> DOWNLOAD ERROR" + ConsoleColors.RESET + " : " + e.getMessage());

                    throw new RuntimeException("Failed to process inline downloads: " + e.getMessage(), e);

                }

            }

            RestResponse soapRes = new RestResponse(response.statusCode(), cleanedBodyForContext, null, "POST", finalUrl);

            if (!response.headers().map().isEmpty()) {

                response.headers().map().forEach((k, v) -> soapRes.getHeadersMap().put(k, String.join(", ", v)));

            }

            if (step.getResponse() != null) {

                context.storeBuffer(step.getResponse(), soapRes);

            }

        } catch (Exception e) {

            logger.info(String.format("    %s>>> RESULT%s  : %sFAILURE (%s)%s", ConsoleColors.GREEN,

                    ConsoleColors.RESET, ConsoleColors.RED, e.getMessage(), ConsoleColors.RESET));

            throw new RuntimeException("SOAP Call Failed: " + e.getMessage(), e);

        }

    }

    public static void printSoapRequest(String method, String url, Map<String, List<String>> headers, String xmlBody,

            TestLogger logger) {

        logger.info(String.format("    %s>>> CALL%s    : %s%s%s %s", ConsoleColors.GREEN, ConsoleColors.RESET,

                ConsoleColors.CYAN, method, ConsoleColors.RESET, url));

        if (headers != null && !headers.isEmpty()) {

            headers.forEach((headerName, headerValues) -> {

                String values = String.join(", ", headerValues);

                logger.info(String.format("    %s>>> HEADER%s  : %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET,

                        headerName, values));

            });

        }

        if (xmlBody == null || xmlBody.trim().isEmpty()) {

            logger.info(String.format("    %s>>> BODY%s    : (empty)", ConsoleColors.GREEN, ConsoleColors.RESET));

            return;

        }

        String cleanedXml = xmlBody.trim();

        String[] lines = cleanedXml.split("\\R");

        if (lines.length > 0) {

            logger.info(String.format("    %s>>> BODY%s    : %s", ConsoleColors.GREEN, ConsoleColors.RESET, lines[0]));

            for (int i = 1; i < lines.length; i++) {

                logger.info(

                        String.format("    %s>>> BODY%s    : %s", ConsoleColors.GREEN, ConsoleColors.RESET, lines[i]));

            }

        }

    }

    public static void printSoapResponse(int statusCode, Map<String, List<String>> headers, String body,

            TestLogger logger) {

        String color = statusCode >= 400 ? ConsoleColors.RED : ConsoleColors.GREEN;

        logger.info(String.format("    %s<<< HTTP%s    : %s%d%s", color, ConsoleColors.RESET, color, statusCode,

                ConsoleColors.RESET));

        if (headers != null && !headers.isEmpty()) {

            headers.forEach((headerName, headerValues) -> {

                String values = String.join(", ", headerValues);

                logger.info(

                        String.format("    %s<<< HEADER%s : %s: %s", color, ConsoleColors.RESET, headerName, values));

            });

        }

        if (body == null || body.trim().isEmpty()) {

            return;

        }

        String formattedXml;

        try {

            Source xmlInput = new StreamSource(new StringReader(body.trim()));

            StringWriter stringWriter = new StringWriter();

            StreamResult xmlOutput = new StreamResult(stringWriter);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            try {

                transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);

            } catch (Exception ignored) {

            }

            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");

            transformer.transform(xmlInput, xmlOutput);

            formattedXml = xmlOutput.getWriter().toString();

        } catch (Exception e) {

            formattedXml = body.trim().replace("><", ">\n<");

        }

        String[] lines = formattedXml.split("\\R");

        for (String line : lines) {

            if (!line.trim().isEmpty()) {

                logger.info(String.format("    %s<<< BODY%s   : %s", color, ConsoleColors.RESET, line));

            }

        }

    }

    // =========================================================

    // ASSERT

    // =========================================================

    private void handleAssertion(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex, Boolean printExecution,

            TestLogger logger, boolean isVerbose) throws Exception {

        RestResponse res = (RestResponse) context.getBuffer(step.getResponse());

        String source = step.getSource();

        String action = step.getAction();

        String path = step.getPath();

        Object expected = step.getExpected();

        if (res == null) {

            throwMissingResponseDiagnostic(
                    step,
                    logger,
                    printExecution,
                    isVerbose,
                    "ASSERT");

        }

        if (action == null || action.isBlank()) {

            logSoapConfigurationError(
                    logger,
                    printExecution,
                    isVerbose,
                    "SOAP ASSERT cannot be executed.",
                    "The required field 'action' is missing.",
                    """
                    Define an assertion action, for example:

                      action: EQUALS

                    or:

                      action: MATCH_REFERENCE
                    """,
                    step);

            throw new RuntimeException(
                    "SOAP ASSERT cannot be executed because 'action' is missing.");

        }

        // =========================================================

        // REFERENCE RESPONSE ASSERTION

        // =========================================================

        // MATCH_REFERENCE compares the complete SOAP BODY semantically against

        // a permanent XML reference file. It is intentionally handled before

        // the legacy XPath assertion switch because it does not use 'path' or

        // 'expected'.

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

        if (expected != null) {

            YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

            String resolvedStr = resolver.resolve(

                    tc,

                    expected.toString(),

                    tc.getVariables(),

                    yamlFile,

                    stepIndex,

                    "expected");

            expected = resolvedStr;

        }

        boolean passed;

        String actual;

        if (source == null) {

            throw new RuntimeException("source not found: " + step.getSource());

        }

        switch (source.toUpperCase()) {

        case "STATUS":

            int status = res.getStatusCode();

            int exp = Integer.parseInt(expected.toString());

            passed = compareNumbers(status, exp, action);

            actual = String.valueOf(status);

            break;

        case "BODY":

            actual = evaluateXPath(res.getBody(), path);

            if ("EXISTS".equalsIgnoreCase(action)) {

                passed = (actual != null && !actual.trim().isEmpty());

                actual = passed ? "FOUND" : "NOT_FOUND";

            } else {

                passed = compareStrings(actual, expected, action);

            }

            break;

        case "HEADERS":

            Map<String, String> headers = res.getHeadersMap();

            String finalPath = path;

            String actualKey = headers.keySet().stream()

                    .filter(k -> k.equalsIgnoreCase(finalPath))

                    .findFirst()

                    .orElse(null);

            actual = (actualKey != null) ? headers.get(actualKey) : null;

            if ("IS_HEADER_PRESENT".equalsIgnoreCase(action)) {

                boolean headerExists = (actualKey != null);

                boolean expectedPresence = false;

                if (expected != null) {

                    expectedPresence = Boolean.parseBoolean(expected.toString());

                }

                passed = (headerExists == expectedPresence);

                actual = String.valueOf(headerExists);

            } else {

                throw new RuntimeException("Unsupported HEADERS action: " + action);

            }

            break;

        default:

            throw new RuntimeException("Unsupported source: " + source);

        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {

            String expectedStr = (expected != null) ? expected.toString() : "N/A";

            logAssertionResult(

                    logger,

                    "SOAP",

                    passed,

                    action,

                    expectedStr,

                    actual,

                    passed ? null : res.getBody());

        }

        if (!passed) {

            throw new RuntimeException(

                    String.format(

                            "Assertion %s failed! Expected: %s, Actual: %s",

                            action,

                            expected,

                            actual));

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

            throw new RuntimeException(

                    "MATCH_REFERENCE currently supports source BODY only. Actual source: "

                            + source);

        }

        if (step.getId() == null || step.getId().isBlank()) {

            throw new RuntimeException(

                    "MATCH_REFERENCE requires a step id. "

                            + "Define 'id' in YAML or ensure the loader creates a fallback id.");

        }

        ReferenceCompareConfig config = new ReferenceCompareConfig()

                .addIgnore(step.getIgnore())

                .addUnordered(step.getUnordered());

        ReferenceAssertionResult result = REFERENCE_RESPONSE_SERVICE.assertResponse(

                ResponseFormat.XML,

                tc,

                step.getId(),

                yamlFile,

                res.getBody(),

                config);

        if (result.isCreated()) {

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {

                logger.info(String.format(

                        "    %s>>> SOAP ASSERT REFERENCE CREATED%s : %s",

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

                        "    %s>>> SOAP ASSERT SUCCESS%s: MATCH_REFERENCE | Reference: %s",

                        ConsoleColors.GREEN,

                        ConsoleColors.RESET,

                        result.getReferenceFile().toAbsolutePath()));

            }

            return;

        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {

            logReferenceDifferences(logger, result);

        }

        throw new RuntimeException(

                "SOAP MATCH_REFERENCE failed with "

                        + result.getComparisonResult().getDifferenceCount()

                        + " difference(s). Reference: "

                        + result.getReferenceFile().toAbsolutePath());

    }

    private void logReferenceDifferences(

            TestLogger logger,

            ReferenceAssertionResult result) {

        logger.info(String.format(

                "    %s>>> SOAP ASSERT MATCH_REFERENCE: FAILED%s",

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
                    "    %s>>> SOAP %s ERROR%s",
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
            logger.info("          This SOAP step expects a response produced by");
            logger.info("          a previously executed SOAP EXEC step, but AGATE");
            logger.info("          cannot find that response in the current execution context.");

            logger.info("");
            logger.info(String.format(
                    "        %sPossible causes:%s",
                    ConsoleColors.YELLOW,
                    ConsoleColors.RESET));

            logger.info("          - the corresponding SOAP EXEC step was not executed");
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
                logger.info("          Make sure a SOAP EXEC step is executed before this step:");
                logger.info("");
                logger.info("            - type: SOAP");
                logger.info("              op: EXEC");
                logger.info("              ...");
                logger.info("              response: \"" + responseName + "\"");
            } else {
                logger.info("          Define 'response:' in this step and use the same name");
                logger.info("          in the corresponding SOAP EXEC step.");
            }

            logCurrentSoapStep(logger, step);
        }

        throw new RuntimeException(
                "SOAP " + operation
                        + " cannot find response '"
                        + displayResponseName
                        + "'. See diagnostic information above.");
    }


    private void logSoapConfigurationError(
            TestLogger logger,
            Boolean printExecution,
            boolean isVerbose,
            String title,
            String explanation,
            String howToFix,
            TestStep step) {

        if (!Boolean.TRUE.equals(printExecution) || !isVerbose) {
            return;
        }

        logger.info("");
        logger.info(String.format(
                "    %s>>> SOAP CONFIGURATION ERROR%s",
                ConsoleColors.RED,
                ConsoleColors.RESET));

        logger.info("        " + title);

        if (explanation != null && !explanation.isBlank()) {
            logger.info("");
            logger.info("        What happened:");
            logger.info("          " + explanation);
        }

        if (howToFix != null && !howToFix.isBlank()) {
            logger.info("");
            logger.info(String.format(
                    "        %sHow to fix:%s",
                    ConsoleColors.YELLOW,
                    ConsoleColors.RESET));

            for (String line : howToFix.strip().split("\\R")) {
                logger.info("          " + line);
            }
        }

        logCurrentSoapStep(logger, step);
    }


    private void logCurrentSoapStep(
            TestLogger logger,
            TestStep step) {

        logger.info("");
        logger.info(String.format(
                "        %sCurrent step:%s",
                ConsoleColors.YELLOW,
                ConsoleColors.RESET));

        logger.info(
                "          type     : "
                        + valueOrMissing(
                                step.getType() != null
                                        ? step.getType().toString()
                                        : null));

        logger.info(
                "          op       : "
                        + valueOrMissing(step.getOp()));

        logger.info(
                "          id       : "
                        + valueOrMissing(step.getId()));

        logger.info(
                "          source   : "
                        + valueOrMissing(step.getSource()));

        logger.info(
                "          action   : "
                        + valueOrMissing(step.getAction()));

        logger.info(
                "          response : "
                        + valueOrMissing(step.getResponse()));

        if (step.getPath() != null && !step.getPath().isBlank()) {
            logger.info(
                    "          path     : "
                            + step.getPath());
        }

        if (step.getExpected() != null) {
            logger.info(
                    "          expected : "
                            + step.getExpected());
        }

        logger.info("");
    }


    private String valueOrMissing(String value) {
        return value == null || value.isBlank()
                ? "<not defined>"
                : value;
    }


    private void logAssertionResult(TestLogger logger, String type, boolean passed, String action, String expected,

            String actual, String body) {

        String color = passed ? ConsoleColors.GREEN : ConsoleColors.RED;

        String status = passed ? "SUCCESS" : "FAILED";

        logger.info(String.format("    %s>>> %s ASSERT %s: %s | Expected: [%s], Actual: [%s]%s", color,

                type.toUpperCase(), status, action, expected, actual, ConsoleColors.RESET));

        if (!passed && body != null && !body.trim().isEmpty()) {

            logger.info(String.format("    %s<<< SOAP ERROR BODY:%s", ConsoleColors.RED, ConsoleColors.RESET));

            for (String line : body.split("\\R")) {

                if (!line.trim().isEmpty()) {

                    logger.info(String.format("    %s<<< %s%s", ConsoleColors.RED, ConsoleColors.RESET, line));

                }

            }

        }

    }

    private void handleBuffer(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,

            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        RestResponse res = (RestResponse) context.getBuffer(step.getResponse());

        String source = step.getSource();

        String path = step.getPath();

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        String pathResolved = resolver.resolve(tc, path, tc.getVariables(), yamlFile, stepIndex, "body");

        pathResolved = resolver.resolve(tc, pathResolved, tc.getVariables(), yamlFile, stepIndex, "body", step);

        path = pathResolved;

        String value;

        if (res == null) {

            throwMissingResponseDiagnostic(
                    step,
                    logger,
                    printExecution,
                    isVerbose,
                    "BUFFER");

        }

        switch (source.toUpperCase()) {

        case "BODY":

            value = evaluateFilteredXPath(res.getBody(), path, step.getConstraints());

            break;

        case "STATUS":

            value = String.valueOf(res.getStatusCode());

            break;

        case "HEADERS":

            String finalBufferPath = path;

            String matchedKey = res.getHeadersMap().keySet().stream().filter(k -> k.equalsIgnoreCase(finalBufferPath))

                    .findFirst().orElse(null);

            value = (matchedKey != null) ? res.getHeadersMap().get(matchedKey) : null;

            if (value == null) {

                throw new RuntimeException("Header not found: " + path);

            }

            break;

        default:

            throw new RuntimeException("Unsupported source: " + source);

        }

        tc.addVariable(step.getName(), value);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {

            String displayValue = (value.length() > 900) ? value.substring(0, 900) + "..." : value;

            logger.info(String.format("    %s>>> BUFFER      :%s Value [%s] stored in variable [%s]",

                    ConsoleColors.GREEN, ConsoleColors.RESET, displayValue, step.getName()));

        }

    }

    private String evaluateFilteredXPath(String xml, String targetPath, List<Constraint> constraints) throws Exception {

        if (constraints == null || constraints.isEmpty()) {

            return evaluateXPath(xml, targetPath);

        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);

        Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml.trim())));

        XPath xPath = XPathFactory.newInstance().newXPath();

        // 1. Dynamically locate the list of nodes

        // Search for the penultimate path segment (e.g., 'quittung') from targetPath

        String[] parts = targetPath.split("/");

        String listNodeName = parts[parts.length - 2]; // e.g., "*[local-name()='quittung']"

        // 2. Find all such nodes within the document

        org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) xPath.evaluate("//" + listNodeName, doc,

                XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {

            org.w3c.dom.Node currentNode = nodes.item(i);

            boolean allMatch = true;

            for (Constraint c : constraints) {

                // 3. Dynamically extract the relative path for the constraint

                // Take the path portion coming after our listNodeName

                String relConstraintPath = extractRelativeXPath(c.getPath(), listNodeName);

                // Evaluate relatively against the current 'currentNode'

                String actual = (String) xPath.evaluate(relConstraintPath, currentNode, XPathConstants.STRING);

                if (!c.getExpected().toString().replace("'", "").equals(actual)) {

                    allMatch = false;

                    break;

                }

            }

            if (allMatch) {

                // 4. Retrieve the requested value relatively from the current node

                String relTargetPath = extractRelativeXPath(targetPath, listNodeName);

                return (String) xPath.evaluate(relTargetPath, currentNode, XPathConstants.STRING);

            }

        }

        return "NULL";

    }

    private String extractRelativeXPath(String fullPath, String listNode) {

        // Take everything after the listNode part and prepend "./" for a relative XPath

        return "./" + fullPath.substring(fullPath.indexOf(listNode) + listNode.length() + 1);

    }

    private String evaluateXPath(String xml, String query) throws Exception {

        if (xml == null || xml.trim().isEmpty())

            return "NULL";

        String cleanedXml = xml.trim();

        int firstTagIndex = cleanedXml.indexOf("<");

        if (firstTagIndex > 0)

            cleanedXml = cleanedXml.substring(firstTagIndex);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(cleanedXml)));

        XPath xPath = XPathFactory.newInstance().newXPath();

        String result = (String) xPath.evaluate(query, doc, XPathConstants.STRING);

        return (result == null || result.isEmpty()) ? "NULL" : result.trim();

    }

    private boolean compareStrings(String actual, Object expected, String action) {

        String exp = expected != null ? expected.toString() : null;

        if ("{NULL}".equals(exp)) {

            exp = "NULL"; // Convert {NULL} to "NULL" to match what XPath returns

        }

        switch (action.toUpperCase()) {

        case "EQUALS":

            return actual.equals(exp);

        case "NOT_EQUALS":

            return !actual.equals(exp);

        case "CONTAINS":

            return actual.contains(exp);

        case "NOT_CONTAINS":

            return !actual.contains(exp);

        case "EMPTY":

            return actual == null || actual.isEmpty();

        case "NOT_EMPTY":

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

    private String removeNullNodesFromXml(String xmlString, Map<String, Object> stepMap) {

        if (xmlString == null || xmlString.isEmpty()) {

            return xmlString;

        }

        try {

            // 1. Escape '&' before parsing

            String safeXml = xmlString.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");

            // 2. Initialize DOM parser

            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);

            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();

            java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(

                    safeXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            org.w3c.dom.Document doc = builder.parse(input);

            // 3. FIRST PASS: Check whether any element with text "{NULL}" exists

            org.w3c.dom.NodeList allElements = doc.getElementsByTagName("*");

            boolean hasAnyInlineNullText = false;

            for (int i = 0; i < allElements.getLength(); i++) {

                org.w3c.dom.Element element = (org.w3c.dom.Element) allElements.item(i);

                String nodeText = element.getTextContent();

                if (nodeText != null && nodeText.equals("{NULL}")) {

                    hasAnyInlineNullText = true;

                    break;

                }

            }

            // Extract parameters from stepMap set to {NULL}

            java.util.Set<String> tagsToKill = new java.util.HashSet<>();

            if (stepMap != null && stepMap.get("parameters") instanceof Map) {

                Map<String, Object> params = (Map<String, Object>) stepMap.get("parameters");

                for (Map.Entry<String, Object> entry : params.entrySet()) {

                    if (entry.getValue() != null && "{NULL}".equals(entry.getValue().toString())) {

                        tagsToKill.add(entry.getKey());

                    }

                }

            }

            // 4. SECOND PASS: Select nodes for removal based on condition

            java.util.List<org.w3c.dom.Element> nodesToRemove = new java.util.ArrayList<>();

            for (int i = 0; i < allElements.getLength(); i++) {

                org.w3c.dom.Element element = (org.w3c.dom.Element) allElements.item(i);

                String nodeName = element.getNodeName();

                String nodeText = element.getTextContent();

                if (nodeText != null && nodeText.equals("{NULL}")) {

                    nodesToRemove.add(element);

                    continue;

                }

                if (!hasAnyInlineNullText) {

                    boolean shouldKillByTagName = tagsToKill.stream().anyMatch(tag -> 

                        nodeName.equals(tag) || nodeName.endsWith(":" + tag)

                    );

                    if (shouldKillByTagName) {

                        nodesToRemove.add(element);

                    }

                }

            }

            // 5. Delete selected nodes

            for (org.w3c.dom.Element element : nodesToRemove) {

                org.w3c.dom.Node parent = element.getParentNode();

                if (parent != null) {

                    parent.removeChild(element);

                }

            }

            // 6. Transform back to string

            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();

            javax.xml.transform.Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");

            java.io.StringWriter writer = new java.io.StringWriter();

            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),

                    new javax.xml.transform.stream.StreamResult(writer));

            String result = writer.toString();

            // 7. FINAL CLEANUP: Blank lines only

            result = result.replaceAll("(?m)^[ \t]*\r?\n", "");

            return result.trim();

        } catch (Exception e) {

            System.err.println("      # [Warning] Failed to process XML: " + e.getMessage());

            return xmlString;

        }

    }

    public static String removeComplexNullNodesFromXml(String xml, Map<String, Object> stepMap) throws Exception {

        if (xml == null || xml.isEmpty() || stepMap == null) return xml;

        @SuppressWarnings("unchecked")

        Map<String, Object> parameters = (Map<String, Object>) stepMap.get("parameters");

        if (parameters == null || parameters.isEmpty()) {

            return xml;

        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        boolean docModified = false;

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {

            String key = entry.getKey();

            String value = String.valueOf(entry.getValue()).trim();

            // Rule 1: If {SET} -> DO NOTHING

            if ("{SET}".equalsIgnoreCase(value)) {

                continue;

            }

            // Rule 2: If {NULL} -> delete node regardless of complexity

            if ("{NULL}".equalsIgnoreCase(value)) {

                NodeList nodes = doc.getElementsByTagNameNS("*", key);

                if (nodes.getLength() == 0) {

                    nodes = doc.getElementsByTagName(key);

                }

                while (nodes.getLength() > 0) {

                    Node nodeToRemove = nodes.item(0);

                    Node parent = nodeToRemove.getParentNode();

                    if (parent != null) {

                        parent.removeChild(nodeToRemove);

                        docModified = true;

                    } else {

                        break;

                    }

                }

            }

        }

        if (docModified) {

            TransformerFactory tf = TransformerFactory.newInstance();

            Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();

            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.toString().replaceAll("(?m)^\\s*$\\r?\\n", "").trim();

        }

        return xml;

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

        return stepType == StepType.SOAP;

    }

    private String generateWssHeader(String username, String password) throws Exception {

        // 1. Generate random Nonce

        byte[] nonceBytes = new byte[16];

        new java.security.SecureRandom().nextBytes(nonceBytes);

        String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);

        // 2. Create timestamp

        String created = Instant.now().toString();

        // 3. Calculate Password Digest: SHA1(Nonce + Created + Password)

        byte[] createdBytes = created.getBytes(StandardCharsets.UTF_8);

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        baos.write(nonceBytes);

        baos.write(createdBytes);

        baos.write(passwordBytes);

        byte[] concatenated = baos.toByteArray();

        MessageDigest md = MessageDigest.getInstance("SHA-1");

        byte[] digestBytes = md.digest(concatenated);

        String passwordDigest = Base64.getEncoder().encodeToString(digestBytes);

        // 4. Build complete XML <soap:Header> block

        return "<soap:Header>\n" + "  <wsse:Security soap:mustUnderstand=\"1\" "

                + "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" "

                + "xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"

                + "    <wsse:UsernameToken wsu:Id=\"UsernameToken-" + java.util.UUID.randomUUID().toString() + "\">\n"

                + "      <wsse:Username>" + username + "</wsse:Username>\n"

                + "      <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">"

                + passwordDigest + "</wsse:Password>\n"

                + "      <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">"

                + nonceBase64 + "</wsse:Nonce>\n" + "      <wsu:Created>" + created + "</wsu:Created>\n"

                + "    </wsse:UsernameToken>\n" + "  </wsse:Security>\n" + "</soap:Header>";

    }

    public void processDownloads(org.w3c.dom.Document xmlDoc, List<DownloadConfig> downloadConfigs,

            byte[] rawHttpResponseBytes, TestCase tc, ExecutionContext context, String yamlFile, int stepIndex)

            throws Exception {

        if (downloadConfigs == null || downloadConfigs.isEmpty()) {

            return;

        }

        javax.xml.xpath.XPath xPath = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        for (DownloadConfig config : downloadConfigs) {

            String method = config.getMethod() != null ? config.getMethod().toUpperCase() : "INLINE";

            String xpathExpression = config.getPath();

            if (xpathExpression == null || xpathExpression.trim().isEmpty()) {

                if ("INLINE".equals(method)) {

                    xpathExpression = "//*[not(* or text()='') and string-length(text()) > 50]"; 

                } else if ("MTOM".equals(method) || "ATTACHMENT".equals(method)) {

                    xpathExpression = "//*[local-name()='Include']";

                }

            }

            String resolvedTargetPath = resolver.resolve(tc, config.getTargetPath(), tc.getVariables(), yamlFile,

                    stepIndex, "targetPath");

            java.nio.file.Path outputPath = java.nio.file.Paths.get(resolvedTargetPath);

            if ("INLINE".equals(method)) {

                org.w3c.dom.Node targetNode = (org.w3c.dom.Node) xPath.evaluate(xpathExpression, xmlDoc,

                        javax.xml.xpath.XPathConstants.NODE);

                if (targetNode != null) {

                    String base64Data = targetNode.getTextContent();

                    if (base64Data != null && !base64Data.trim().isEmpty() && !"NULL".equals(base64Data)) {

                        byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data.trim());

                        java.nio.file.Files.createDirectories(outputPath.getParent());

                        java.nio.file.Files.write(outputPath, fileBytes);

                        String fileReference = "file:" + resolvedTargetPath;

                        targetNode.setTextContent(fileReference);

                    }

                } else {

                    throw new Exception(

                            "Download Error: XPath '" + xpathExpression + "' did not match any node in response XML.");

                }

            } else if ("MTOM".equals(method) || "ATTACHMENT".equals(method)) {

                org.w3c.dom.Node targetNode = (org.w3c.dom.Node) xPath.evaluate(xpathExpression, xmlDoc,

                        javax.xml.xpath.XPathConstants.NODE);

                if (targetNode == null) {

                    throw new Exception("Download Error (MTOM): XPath '" + xpathExpression + "' did not match any node.");

                }

                String cid = null;

                if (targetNode.hasChildNodes()) {

                    org.w3c.dom.NodeList children = targetNode.getChildNodes();

                    for (int i = 0; i < children.getLength(); i++) {

                        org.w3c.dom.Node child = children.item(i);

                        if (child.getNodeName().contains("Include")) {

                            org.w3c.dom.NamedNodeMap attrs = child.getAttributes();

                            if (attrs != null && attrs.getNamedItem("href") != null) {

                                cid = attrs.getNamedItem("href").getNodeValue();

                                if (cid.startsWith("cid:")) {

                                    cid = cid.substring(4);

                                }

                                break;

                            }

                        }

                    }

                }

                if (cid == null && targetNode.getNodeName().contains("Include")) {

                    org.w3c.dom.NamedNodeMap attrs = targetNode.getAttributes();

                    if (attrs != null && attrs.getNamedItem("href") != null) {

                        cid = attrs.getNamedItem("href").getNodeValue();

                        if (cid.startsWith("cid:")) {

                            cid = cid.substring(4);

                        }

                    }

                }

                if (cid == null || cid.isEmpty()) {

                    throw new Exception("Download Error (MTOM): Could not extract Content-ID (cid) from XML node.");

                }

                byte[] attachmentBytes = extractMtomAttachment(rawHttpResponseBytes, cid);

                if (attachmentBytes == null || attachmentBytes.length == 0) {

                    throw new Exception("Download Error (MTOM): Attachment with Content-ID <" + cid + "> not found in HTTP multipart payload.");

                }

                java.nio.file.Files.createDirectories(outputPath.getParent());

                java.nio.file.Files.write(outputPath, attachmentBytes);

                while (targetNode.hasChildNodes()) {

                    targetNode.removeChild(targetNode.getFirstChild());

                }

                String fileReference = "file:" + resolvedTargetPath;

                targetNode.setTextContent(fileReference);

            }

        }

    }

    private byte[] extractMtomAttachment(byte[] rawBytes, String cid) throws Exception {

        String httpPayload = new String(rawBytes, java.nio.charset.StandardCharsets.ISO_8859_1);

        String lookForCid = "<" + cid + ">";

        int cidIndex = httpPayload.indexOf(lookForCid);

        if (cidIndex == -1) {

            return null;

        }

        int headerEndIndex = httpPayload.indexOf("\r\n\r\n", cidIndex);

        if (headerEndIndex == -1) {

            headerEndIndex = httpPayload.indexOf("\n\n", cidIndex);

            if (headerEndIndex != -1) {

                headerEndIndex += 2;

            }

        } else {

            headerEndIndex += 4;

        }

        if (headerEndIndex == -1) {

            return null;

        }

        int nextBoundaryIndex = httpPayload.indexOf("\r\n--", headerEndIndex);

        if (nextBoundaryIndex == -1) {

            nextBoundaryIndex = httpPayload.indexOf("\n--", headerEndIndex);

        }

        if (nextBoundaryIndex == -1) {

            nextBoundaryIndex = rawBytes.length;

        }

        int length = nextBoundaryIndex - headerEndIndex;

        byte[] fileBytes = new byte[length];

        System.arraycopy(rawBytes, headerEndIndex, fileBytes, 0, length);

        return fileBytes;

    }

    private java.nio.file.Path resolveDotNotationPath(String yamlFilePath, String dotNotation) {

        java.nio.file.Path yamlDir = java.nio.file.Paths.get(yamlFilePath).getParent();

        int lastDot = dotNotation.lastIndexOf('.');

        if (lastDot == -1) {

            return yamlDir.resolve(dotNotation);

        }

        String pathWithoutExt = dotNotation.substring(0, lastDot);

        String extension = dotNotation.substring(lastDot);

        String relativeFolderStructure = pathWithoutExt.replace(".", java.io.File.separator);

        String finalRelativePath = relativeFolderStructure + extension;

        return yamlDir.resolve(finalRelativePath).normalize();

    }

}
