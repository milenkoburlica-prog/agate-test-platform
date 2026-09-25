package at.co.svc.aga.transformator.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.TransformerFactoryConfigurationError;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.Constraint;
import at.co.svc.aga.transformator.dto.Module;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/**
 * Converts Tosca API-related steps into AGATE REST/SOAP DSL and module files.
 */
public class ProcessAPILogic {
    public static void processAPILogic(Map<String, Module> moduleMap, CleanStep step, CleanStep nextStep, String appID) throws Exception {
        
        Module myModule = moduleMap.get(step.getModuleSurrogate());
        if (myModule == null) return;

        String moduleName = step.getModule() != null ? step.getModule().toLowerCase() : "";
        String method = myModule.getAttributes().get("Method"); 

        boolean isApiRequest = (method != null && !method.isEmpty()) || !moduleName.contains("response");

        if (isApiRequest) {
            
            String cleanModuleName = moduleName.replace(" ", "_");
            ToscaToAgaPhase1.lastApiResponseName = cleanModuleName + "_response";

            processApiStep(step, nextStep, moduleMap, "request.json", appID);
        } 
        
        else {
            String rawPayload = myModule.getAttributes().get("Payload");
            String decodedPayload = decodeBase64(rawPayload).trim();
            String protocol = "rest";
            if (decodedPayload.contains("Envelope"))
                protocol = "soap";
            processApiResponseAsserts(step, protocol, decodedPayload);
        }
    }

    private static void processApiStep(CleanStep step, CleanStep nextStep, Map<String, Module> moduleMap, String fileName, String appID) {
        String moduleSurrogate = step.getModuleSurrogate();
        Module myModule = moduleMap.get(moduleSurrogate);

        if (myModule == null) return;

        String rawName = myModule.getAttributes().get("Name");
//        String cleanModuleName = rawName.toLowerCase().trim().replaceAll("\\s+", "_");
        String cleanModuleName =
                normalizeApiModuleName(rawName);

        String rawPayload = myModule.getAttributes().get("Payload");
        String decodedPayload = decodeBase64(rawPayload).trim();

        String rawTCProperties = myModule.getAttributes().get("TCProperties");
        String decodedTCProperties = extractResourceFromTCProperties(rawTCProperties);
        
        String rawExplicitConnection = myModule.getAttributes().get("ExplicitConnection");
        String decodedExplicitConnection = decodeBase64(rawExplicitConnection).trim();

        String rawHeaders = myModule.getAttributes().get("Headers");
        String decodedHeaders = decodeBase64(rawHeaders).trim();

        boolean isSoap = decodedPayload.startsWith("<") && decodedPayload.contains("Envelope");
        String protocol = isSoap ? "soap" : "rest";
        String extension = isSoap ? ".xml" : ".json";

        String dotPrefix = getDotPrefixFormat(step, myModule, cleanModuleName);
        if (dotPrefix != null && dotPrefix.contains("?")) {
            dotPrefix = dotPrefix.substring(0, dotPrefix.indexOf("?"));
        }
        dotPrefix = dotPrefix.replace("__", "_");
        String fullDotAction = protocol + "." + dotPrefix;
        if (!"".equals(cleanModuleName)) {
            fullDotAction = fullDotAction + "." + cleanModuleName;
        }
        fullDotAction = fullDotAction.replace("..", ".");
        fullDotAction = fullDotAction.replace("__", "_");

        String folderStructure = dotPrefix.replace(".", "/");
        String finalFolderPath = "migration/data/" + System.getProperty("APPLICATION") + "/modules/" 

                                 + protocol + "/" + folderStructure + cleanModuleName;
        
        finalFolderPath = finalFolderPath.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("(", "_")
                .replace(")", "_")
                .replace(":", "_")
                .replace("#", "_")
                .replace("=", "_")
                .replace("<", "_")
                .replace(">", "_")

                .replace("__", "_")
                .replace("__", "_")
                .replace("{b", "")
                .replace("{xl", "")
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "");
        
        finalFolderPath = finalFolderPath.replace("//", "/");
        finalFolderPath = finalFolderPath.replace("__", "_");
        String lastPart = Paths.get(finalFolderPath).getFileName().toString();
        ToscaToAgaPhase1.lastApiResponseName = lastPart + "_response";

        step.setAction(fullDotAction);
        
        if (fileName.contains("request") && !decodedPayload.isEmpty()) {
            if ("rest".equals(protocol)) {
                decodedPayload = prepareRestBody(step, decodedPayload);
            }
            if ("soap".equals(protocol)) {
                decodedPayload = prepareSoapBody(step, decodedPayload);
            }
            
        }

        String endpoint = "";
        if (step.getValues() != null && step.getValuesAsMap().containsKey("Endpoint")) {
            endpoint = step.getValuesAsMap().get("Endpoint").getValue();
        }
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = myModule.getAttributes().get("Endpoint");
        }

        if (fileName.contains("request")) {
            if (finalFolderPath == null) {
                Path path = Paths.get("migration", "data", appID, "modules", protocol);
                MigrationLog.debug("API fallback path: " + path.getFileName().toString());

            } else {
                if (finalFolderPath.contains("null")) {
                    
                    finalFolderPath = finalFolderPath.replace("null", appID);
                }
            }
            ToscaToAgaPhase1.currentResourcePath = finalFolderPath;
            
            ToscaToAgaPhase1.writeLine("\n      # " + step.getName());
            ToscaToAgaPhase1.writeLine("      - type: " + protocol.toUpperCase());
            ToscaToAgaPhase1.writeLine("        op: EXEC"); 
            if ((step.getCondition() != null) && (!step.getCondition().equals(""))) {
                String condition = step.getCondition();
                String cleanCondition = ToscaValueTranslator.translateToscaValues(condition);

                String formattedCondition = cleanCondition.replace("\"", "'");
                ToscaToAgaPhase1.writeLine("        condition: \"" + formattedCondition + "\"");
            }

            String newEndpoint = ToscaValueTranslator.translateToscaValues(endpoint);
            if (newEndpoint.startsWith("{E[env.ecard.service]}")) {
                newEndpoint= "https://{E[env.ecard.service]}";
                
            }
            ToscaToAgaPhase1.writeLine("        endpoint: \"" + newEndpoint + "\""); 

            String translatedCommand = ToscaValueTranslator.translateToscaValues(step.getAction());

            if (translatedCommand != null) {
                translatedCommand = translatedCommand.replace("{B", "")
                        .replace("[", "")
                        .replace("}", "")
                        .replace("]", "")
                        .replace("{", "")
                        .replace("-", "")
                        .replace("__","_").toLowerCase();
            }
            ToscaToAgaPhase1.writeLine("        command: " + translatedCommand); 
            ToscaToAgaPhase1.writeLine("        response: " + ToscaToAgaPhase1.lastApiResponseName);

            if (nextStep != null && nextStep.getValues() != null) {
                for (StepValueDetails details : nextStep.getValues()) {
                    
                    if ("File".equalsIgnoreCase(details.getActionMode())) {
                        
                        String sourceFile = details.getValue();
                        sourceFile = ToscaValueTranslator.translateToscaValues(sourceFile);
                        
                        ToscaToAgaPhase1.writeLine("        download:");
                        ToscaToAgaPhase1.writeLine("          - method: MTOM");
                        
                        //sourceFile = sourceFile.replace("{S[SVC.Projekt root path]}", "..").replace("\\", "\\\\");
                        sourceFile = sourceFile
                                .replace(
                                        "{S[SVC.Projekt root path]}",
                                        "{E[env.SVC_Projekt_root_path]}"
                                )
                                .replace("\\", "\\\\");
                        ToscaToAgaPhase1.writeLine("            targetPath: \"" + sourceFile + "\"");
                        
                        break; 
                    }
                }
            }

            if (step.getValues() != null && !step.getValues().isEmpty()) {
                boolean hasParams = false;
             
                Map<String, List<StepValueDetails>> groupedParams = new LinkedHashMap<>();
                for (StepValueDetails details : step.getValues()) {
                    String k = details.getName();
                    
                    if (!k.equalsIgnoreCase("Endpoint") && !k.equalsIgnoreCase("Resource")) {
                        groupedParams.computeIfAbsent(k, key -> new ArrayList<>()).add(details);
                    }
                }

                for (Map.Entry<String, List<StepValueDetails>> entry : groupedParams.entrySet()) {
                    String k = entry.getKey();
                    List<StepValueDetails> detailsList = entry.getValue();

                    if (!hasParams) {
                        ToscaToAgaPhase1.writeLine("        parameters:");
                        hasParams = true;
                    }

                    if (detailsList.size() == 1) {
                        StepValueDetails details = detailsList.get(0);
                        String tValue = details.getValue();
                        tValue = ToscaValueTranslator.translateToscaValues(tValue);

                        ToscaToAgaPhase1.writeLine("          " + k + ": \"" + tValue + "\"");
                    } 
                    
                    else {
                        
                        ToscaToAgaPhase1.writeLine("          " + k + ":");
                        
                        for (StepValueDetails details : detailsList) {
                            String tValue = details.getValue();
                            tValue = ToscaValueTranslator.translateToscaValues(tValue);

                            String condition =
                                    normalizeApiParameterCondition(
                                            details.getxCondition()
                                    );

                            if (condition == null || condition.isBlank()) {
                                condition = "true";
                            }

                            ToscaToAgaPhase1.writeLine(
                                    "            - condition: \""
                                            + escapeYamlDoubleQuoted(condition)
                                            + "\""
                            );

                            ToscaToAgaPhase1.writeLine(
                                    "              value: \""
                                            + escapeYamlDoubleQuoted(tValue)
                                            + "\""
                            );
                        }
                    }
                }

            }

            generateMetadata(step, protocol, myModule, finalFolderPath);
            saveToFile(finalFolderPath, "request" + extension, decodedPayload);
        }
    }

    public static String getDotPrefixFormat(CleanStep step, Module myModule, String cleanModuleName) {
        String resource = "";
        if (step.getValues() != null && step.getValuesAsMap().containsKey("Resource")) {
            resource = step.getValuesAsMap().get("Resource").getValue();
        } else {
            resource = getResourceFromModule(myModule);
        }

        String moduleResources = resource; 
        String dotPrefix = "";
        if (moduleResources != null && !moduleResources.isEmpty()) {
            dotPrefix = moduleResources.replace("/", ".").replace("\\", ".");
            if (dotPrefix.startsWith(".")) dotPrefix = dotPrefix.substring(1);
            if (!dotPrefix.endsWith(".")) dotPrefix += ".";
        }

        return dotPrefix;
    }

private static String prepareSoapBody(CleanStep step, String decodedPayload)
        throws TransformerFactoryConfigurationError {
    try {
        
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        
        java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(decodedPayload.getBytes("UTF-8"));
        org.w3c.dom.Document doc = builder.parse(input);

        if (step.getValues() == null) {
            step.setValues(new java.util.ArrayList<>());
        }

        org.w3c.dom.NodeList allElements = doc.getElementsByTagName("*");
        
        for (int i = 0; i < allElements.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) allElements.item(i);

            if (element.getChildNodes().getLength() == 1 && 
                element.getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                
                String key = element.getLocalName(); 
                String valueStr = element.getTextContent().trim();

                element.setTextContent("{B[" + key + "]}");

                if (!step.getValuesAsMap().containsKey(key)) {
                    StepValueDetails details = new StepValueDetails();
                    details.setValue(valueStr);
                    details.setActionMode("Input");
                    
                    step.getValuesAsMap().put(key, details);
                }
            }
        }

        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();

        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        java.io.StringWriter writer = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(writer));
        
        String rawXml = writer.toString();

        decodedPayload = rawXml.replaceAll("(?m)^\\s*\\r?\\n", "");
    } catch (Exception e) {
        MigrationLog.warn("SOAP/XML parse failed: " + e.getMessage());
    }
    return decodedPayload;
}

    private static String prepareRestBody(CleanStep step, String decodedPayload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            Map<String, Object> jsonMap = mapper.readValue(decodedPayload, Map.class);
            Map<String, Object> parameterizedMap = new LinkedHashMap<>();

            if (step.getValues() == null) {
                step.setValues(new java.util.ArrayList<>());
            }

            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                String key = entry.getKey(); 
                Object rawValue = entry.getValue();
                String valueStr = (rawValue == null) ? "" : String.valueOf(rawValue);

                String bufferKey = key;

                parameterizedMap.put(key, "{B[" + bufferKey + "]}");

                if (!step.getValuesAsMap().containsKey(key)) {
                    StepValueDetails details = new StepValueDetails();
                    details.setValue(valueStr);
                    details.setActionMode("Input");
                    
                    step.getValuesAsMap().put(key, details); 
                }
            }

            decodedPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parameterizedMap);
            
        } catch (Exception e) {
            MigrationLog.warn("JSON parse failed: " + e.getMessage());
        }
        return decodedPayload;
    }

    private static void processApiResponseAsserts(
            CleanStep step,
            String protocol,
            String decodedPayload)
            throws Exception {

        if (step.getValues() == null) {
            return;
        }

        for (StepValueDetails details : step.getValues()) {

            String key = details.getName();

            String value = details.getValue();

            String expectedValue = details.getValue();

            /*
             * Translate Tosca placeholders before generating AGATE ASSERT YAML.
             *
             * Important for reusable parameters, e.g.:
             *
             *   {PL[Response.SVTCode]}
             *
             * must become:
             *
             *   {R[Response.SVTCode]}
             *
             * provided ToscaValueTranslator contains the PL -> R mapping.
             */
            expectedValue =
                    ToscaValueTranslator.translateToscaValues(
                            expectedValue
                    );

            String actionProperty =
                    details.getActionProperty() != null
                            ? details.getActionProperty().trim()
                            : "";

            String mode =
                    translateActionMode(
                            details.getActionMode()
                    );

            String lastPart =
                    key.contains(".")
                            ? key.substring(
                                    key.lastIndexOf(".") + 1
                            )
                            : key;

            if (actionProperty.isEmpty()
                    && ("{NULL}".equals(expectedValue)
                    || lastPart.equalsIgnoreCase(expectedValue))) {

                continue;
            }

            ToscaToAgaPhase1.writeLine(
                    "\n      # "
                            + step.getName()
                            + " - "
                            + key
                            + " ("
                            + mode
                            + ")"
            );

            String typ = "REST";

            if (protocol.equals("soap")) {
                typ = "SOAP";
            }

            List<Constraint> relevantConstraints =
                    new ArrayList<>();

            if ("Verify".equalsIgnoreCase(mode)) {

                ToscaToAgaPhase1.writeLine(
                        "      - type: " + typ
                );

                ToscaToAgaPhase1.writeLine(
                        "        op: ASSERT"
                );

                if (!relevantConstraints.isEmpty()) {

                    ToscaToAgaPhase1.writeLine(
                            "        constraints:"
                    );

                    for (Constraint c : relevantConstraints) {

                        ToscaToAgaPhase1.writeLine(
                                "          - path: \""
                                        + c.getPath()
                                        + "\""
                        );

                        ToscaToAgaPhase1.writeLine(
                                "            action: \""
                                        + c.getAction()
                                        + "\""
                        );

                        ToscaToAgaPhase1.writeLine(
                                "            expected: \""
                                        + c.getExpected()
                                        + "\""
                        );
                    }
                }

                {
                    String condition =
                            step.getCondition();

                    String cleanCondition =
                            condition != null
                                    ? ToscaValueTranslator.translateToscaValues(
                                            condition
                                    )
                                    : "";

                    String formattedCondition =
                            cleanCondition != null
                                    ? cleanCondition.replace(
                                            "\"",
                                            "'"
                                    )
                                    : "";

                    String combined =
                            step.getCombinedCondition(
                                    details.getxCondition(),
                                    formattedCondition
                            );

                    if (!"".equals(combined)) {

                        ToscaToAgaPhase1.writeLine(
                                "        condition: \""
                                        + combined
                                        + "\""
                        );
                    }
                }

                if ("StatusCode".equalsIgnoreCase(key)) {

                    ToscaToAgaPhase1.writeLine(
                            "        source: STATUS"
                    );

                    ToscaToAgaPhase1.writeLine(
                            "        action: EQUALS"
                    );

                    if (expectedValue != null
                            && expectedValue.matches("^\\d{3}.*")) {

                        expectedValue =
                                expectedValue.substring(
                                        0,
                                        3
                                );
                    }

                    if (expectedValue.contains("\"")) {

                        ToscaToAgaPhase1.writeLine(
                                "        expected: |"
                        );

                        expectedValue =
                                expectedValue.replace(
                                        "\"\"\"\"",
                                        "\""
                                );

                        expectedValue =
                                expectedValue.replace(
                                        "\"\"\"",
                                        "\""
                                );

                        ToscaToAgaPhase1.writeLine(
                                "            "
                                        + expectedValue
                        );

                    } else {

                        ToscaToAgaPhase1.writeLine(
                                "        expected: \""
                                        + expectedValue
                                        + "\""
                        );
                    }

                } else {

                    ToscaToAgaPhase1.writeLine(
                            "        source: BODY"
                    );

                    String overridePath =
                            details.getToscaPath();

                    String constraintCond =
                            findConstraintForPath(
                                    step,
                                    details.getToscaPath()
                            );

                    key =
                            getPath(
                                    protocol,
                                    details,
                                    key,
                                    overridePath,
                                    constraintCond
                            );

                    ToscaToAgaPhase1.writeLine(
                            "        path: \""
                                    + key
                                    + "\""
                    );

                    if ("Exists".equalsIgnoreCase(
                            actionProperty
                    )) {

                        ToscaToAgaPhase1.writeLine(
                                "        action: \"EXISTS\""
                        );

                    } else {

                        if (expectedValue.contains("*")) {

                            ToscaToAgaPhase1.writeLine(
                                    "        action: \"CONTAINS\""
                            );

                            expectedValue =
                                    expectedValue.replace(
                                            "*",
                                            ""
                                    );

                        } else {

                            ToscaToAgaPhase1.writeLine(
                                    "        action: \"EQUALS\""
                            );
                        }

                        if (expectedValue.contains("\"")) {

                            ToscaToAgaPhase1.writeLine(
                                    "        expected: |"
                            );

                            String cleaned =
                                    expectedValue
                                            .replaceAll(
                                                    "\\r?\\n",
                                                    " "
                                            )
                                            .trim();

                            cleaned =
                                    cleaned.replaceAll(
                                            "^\"+|\"+$",
                                            ""
                                    );

                            cleaned =
                                    cleaned.replaceAll(
                                            "^'+|'+$",
                                            ""
                                    );

                            expectedValue =
                                    expectedValue.replace(
                                            "\"\"\"\"",
                                            "\""
                                    );

                            expectedValue =
                                    expectedValue.replace(
                                            "\"\"\"",
                                            "\""
                                    );

                            ToscaToAgaPhase1.writeLine(
                                    "            "
                                            + cleaned
                            );

                        } else {

                            ToscaToAgaPhase1.writeLine(
                                    "        expected: \""
                                            + expectedValue
                                            + "\""
                            );
                        }
                    }
                }

                ToscaToAgaPhase1.writeLine(
                        "        response: "
                                + ToscaToAgaPhase1.lastApiResponseName
                );

                if (details.getConstrain() != null) {

                    String cAction =
                            details.getConstrain()
                                    .getAction();

                    String cExpected =
                            details.getConstrain()
                                    .getExpected();

                    String cPath =
                            details.getConstrain()
                                    .getPath();

                    ToscaToAgaPhase1.writeLine(
                            "        constraints: "
                    );

                    ToscaToAgaPhase1.writeLine(
                            "          - path: \""
                                    + cPath
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            "            action: \""
                                    + cAction
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            "            expected: \""
                                    + cExpected
                                    + "\""
                    );
                }

            } else if ("Buffer".equalsIgnoreCase(mode)) {

                ToscaToAgaPhase1.writeLine(
                        "      - type: " + typ
                );

                ToscaToAgaPhase1.writeLine(
                        "        op: BUFFER"
                );

                if ((step.getCondition() != null)
                        && !step.getCondition()
                        .equals("")) {

                    String condition =
                            step.getCondition();

                    String cleanCondition =
                            ToscaValueTranslator.translateToscaValues(
                                    condition
                            );

                    String formattedCondition =
                            cleanCondition.replace(
                                    "\"",
                                    "'"
                            );

                    ToscaToAgaPhase1.writeLine(
                            "        condition: \""
                                    + formattedCondition
                                    + "\""
                    );
                }

                String varName =
                        cleanBufferName(
                                expectedValue
                        );

                ToscaToAgaPhase1.writeLine(
                        "        name: "
                                + varName
                );

                ToscaToAgaPhase1.writeLine(
                        "        source: BODY"
                );

                String overridePath =
                        details.getToscaPath();

                if (protocol.equals("rest")) {

                    ToscaToAgaPhase1.writeLine(
                            "        selector: JSON_PATH"
                    );

                    overridePath =
                            details.getJsonPath();

                } else {

                    ToscaToAgaPhase1.writeLine(
                            "        selector: XML_PATH"
                    );

                    overridePath =
                            details.getXmlPath();
                }

                String constraintCond =
                        findConstraintForPath(
                                step,
                                details.getToscaPath()
                        );

                key =
                        getPath(
                                protocol,
                                details,
                                key,
                                overridePath,
                                constraintCond
                        );

                ToscaToAgaPhase1.writeLine(
                        "        path: \""
                                + key
                                + "\""
                );

                ToscaToAgaPhase1.writeLine(
                        "        response: "
                                + ToscaToAgaPhase1.lastApiResponseName
                );

                if (details.getConstrain() != null) {

                    String cAction =
                            details.getConstrain()
                                    .getAction();

                    String cExpected =
                            details.getConstrain()
                                    .getExpected();

                    String cPath =
                            details.getConstrain()
                                    .getPath();

                    ToscaToAgaPhase1.writeLine(
                            "        constraints: "
                    );

                    ToscaToAgaPhase1.writeLine(
                            "          - path: \""
                                    + cPath
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            "            action: \""
                                    + cAction
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            "            expected: \""
                                    + cExpected
                                    + "\""
                    );
                }

            } else if ("FileUpload".equalsIgnoreCase(mode)) {

                ToscaToAgaPhase1.writeLine(
                        "      # [Warning] Skipped "
                                + key
                                + ". Unsupported ActionMode: "
                                + mode
                );

            } else {

                ToscaToAgaPhase1.writeLine(
                        "      # [Warning] Skipped "
                                + key
                                + ". Unsupported ActionMode: "
                                + mode
                );
            }
        }
    }
    /**
     * Normalizes Tosca API parameter xCondition expressions before they are
     * written to AGATE YAML.
     *
     * Examples:
     *
     *   'CardTokenDMP.TokenWert' != NULL
     *
     * becomes:
     *
     *   {XL[CardTokenDMP.TokenWert]} != NULL
     *
     * and:
     *
     *   'CardTokenDMP.TokenWert' == "{NULL}"
     *
     * becomes:
     *
     *   {XL[CardTokenDMP.TokenWert]} == NULL
     *
     * Important:
     * We intentionally only convert quoted identifiers which contain a dot.
     * This avoids treating normal string literals such as 'ACTIVE' as AGATE
     * variables.
     */
    private static String normalizeApiParameterCondition(
            String condition) {

        if (condition == null) {
            return null;
        }

        String normalized =
                condition.trim();

        if (normalized.isEmpty()) {
            return normalized;
        }

        normalized =
                ToscaValueTranslator.translateToscaValues(
                        normalized
                );

        normalized =
                normalized.replace(
                        "\"{NULL}\"",
                        "NULL"
                );

        normalized =
                normalized.replace(
                        "'{NULL}'",
                        "NULL"
                );

        normalized =
                normalized.replace(
                        "{NULL}",
                        "NULL"
                );

        java.util.regex.Pattern quotedDataReference =
                java.util.regex.Pattern.compile(
                        "'([\\p{L}\\p{N}_-]+(?:\\.[\\p{L}\\p{N}_-]+)+)'"
                );

        java.util.regex.Matcher matcher =
                quotedDataReference.matcher(
                        normalized
                );

        StringBuffer buffer =
                new StringBuffer();

        while (matcher.find()) {

            String reference =
                    matcher.group(1);

            String replacement =
                    "{XL["
                            + reference
                            + "]}";

            matcher.appendReplacement(
                    buffer,
                    java.util.regex.Matcher.quoteReplacement(
                            replacement
                    )
            );
        }

        matcher.appendTail(
                buffer
        );

        String result =
                buffer.toString();

        result =
                result.replaceAll(
                        "\\s*==\\s*NULL",
                        " == NULL"
                );

        result =
                result.replaceAll(
                        "\\s*!=\\s*NULL",
                        " != NULL"
                );

        return result;
    }
    
    
    /**
     * Escapes a scalar that is written inside YAML double quotes.
     */
    private static String escapeYamlDoubleQuoted(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }
    
    private static String buildPathWithConstraints(String key, List<Constraint> constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return key;
        }

        String finalPath = key;
        for (Constraint c : constraints) {
            
            String field = c.getPath().substring(c.getPath().lastIndexOf("local-name()='") + 14).replace("']", "");
            String predicate = "[*[local-name()='" + field + "']='" + c.getExpected() + "']";

            finalPath = finalPath.replaceFirst("(\\*\\[local-name\\(\\)\\='quittung'\\])", "$1" + predicate);
        }
        return finalPath;
    }
    
    private static String findConstraintForPath(CleanStep step, String toscaPath) {

        return "";
    }

    private static String getPath(String protocol, StepValueDetails details, String key, String overridePath, String condition) {
        
        String path = key;
        if (protocol.equals("rest")) {
            String overrideJsonPath = details.getJsonPath();
            path = (overrideJsonPath != null && !overrideJsonPath.isEmpty()) ? overrideJsonPath : 
                   (overridePath != null && !overridePath.isEmpty() && !overridePath.startsWith("/") ? "//" + overridePath.trim() : overridePath);
        } else { 
            String overrideXmlPath = details.getXmlPath();
            path = (overrideXmlPath != null && !overrideXmlPath.isEmpty()) ? overrideXmlPath : 
                   (overridePath != null && !overridePath.isEmpty() && !overridePath.startsWith("/") ? "//" + overridePath.trim() : overridePath);
        }

        if (protocol.equals("soap") && condition != null && !condition.isEmpty() && condition.contains("=")) {
            String[] parts = condition.split("=");
            String field = parts[0]; 
            String value = parts[1];

            String predicate = "[*[local-name()='" + field + "']='" + value + "']";

            if (path.contains("quittung")) {
                path = path.replaceFirst("(\\*\\[local-name\\(\\)\\='quittung'\\])", "$1" + predicate);
            }
        }
        return path;
    }
    
    private static String convertToSoapPredicate(String field, String value) {
        String[] nodes = field.split("\\.");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < nodes.length; i++) {
            sb.append("*[local-name()='").append(nodes[i]).append("']");
            if (i < nodes.length - 1) sb.append("/");
        }
        sb.append("='").append(value).append("']");
        return sb.toString();
    }
    
    private static String cleanBufferName(String value) {
        if (value == null || value.isEmpty()) return "unknown_var";

        if (value.startsWith("{B[") && value.endsWith("]}")) {
            return value.substring(3, value.length() - 2).trim();
        }

        return value.trim();
    }
    private static void generateMetadata(CleanStep step, String protocol, Module module, String savePath) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        String resource = "";
        if (step.getValues() != null && step.getValuesAsMap().containsKey("Resource")) {
            resource = step.getValuesAsMap().get("Resource").getValue();
        } else {
            resource = getResourceFromModule(module);
        }

        if (!resource.startsWith("/") && !resource.isEmpty()) {
            resource = "/" + resource;
        }

        metadata.put("url", "{{endpoint}}" + resource);

        metadata.put("method", getHttpMethodFromModule(module)); 

        Map<String, String> headers = new LinkedHashMap<>();
        
        if ("soap".equalsIgnoreCase(protocol)) {
            
            headers.put("Content-Type", "text/xml;charset=UTF-8");
            headers.put("SOAPAction", "\"\"");
            headers.put("User-Agent", "Agate-HttpClient (Version 1.0.0)");
            
            metadata.put("headers", headers);
        } else {
            
            String ct = module != null ? module.getAttributes().get("ContentType") : "application/json";
            if (ct == null || ct.isEmpty()) {
                ct = "application/json;charset=UTF-8";
            }
            headers.put("Content-Type", ct);

        Map<String, String> toscaHeaders = extractHeadersFromModule(module, mapper);
        for (String headerKey : toscaHeaders.keySet()) {
            
            if (!headerKey.equals("Content-Type")) {
               headers.put(headerKey, "{B[" + headerKey + "]}");
            }
        }
        metadata.put("headers", headers);
        }

        try {
            saveToFile(savePath, "metadata.json", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata));
        } catch (Exception e) { 
            MigrationLog.error("Metadata generation failed: " + e.getMessage()); 
        }
    }

    private static String decodeBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) return "";
        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64String);
            return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            
            return base64String;
        }
    }

    private static void saveToFile(String folderPath, String fileName, String content) {
        try {
            
            String agateDataRoot = ""; 
            
            File dir = new File(agateDataRoot + folderPath);
            if (!dir.exists()) {
                dir.mkdirs(); 
            }

            if (folderPath.contains("migration/data/MUHXI/modules/soap/muhi/1/absolutesbeschaeftigungsverboteinmelden")) {
                if (fileName.equalsIgnoreCase("request.xml")) {
                    MigrationLog.debug("Special SOAP request detected for MUHXI");
                    MigrationLog.debug("Request content: " + content);
                }
            }
            File file = new File(dir, fileName);
            java.nio.file.Files.write(file.toPath(), content.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            MigrationLog.debug("File created: " + folderPath + "/" + fileName);
            
        } catch (Exception e) {
            MigrationLog.error("Failed to save file: " + e.getMessage());
        }
    }
    private static String getHttpMethodFromModule(Module module) {
        if (module == null) return "GET";

        String tcPropsBase64 = module.getAttributes().get("TCProperties");
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "GET";

        try {
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))) {
                
                StringBuilder xmlContent = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    xmlContent.append(line);
                }

                String content = xmlContent.toString();

                if (content.contains("Name=\"Method\"")) {
                    
                    int methodIndex = content.indexOf("Name=\"Method\"");

                    int valueAttrStart = content.indexOf("Value=\"", methodIndex) + 7;
                    int valueAttrEnd = content.indexOf("\"", valueAttrStart);
                    
                    String method = content.substring(valueAttrStart, valueAttrEnd).toUpperCase();

                    return method.trim();
                }
            }
        } catch (Exception e) {
            MigrationLog.warn("Failed to read TCProperties: " + e.getMessage());
        }

        return "GET"; 
    }

    private static String translateActionMode(String code) {
        if (code == null) return "";
        
        if (code.equalsIgnoreCase("Input")) return "Input";
        if (code.equalsIgnoreCase("Verify")) return "Verify";
        if (code.equalsIgnoreCase("Buffer")) return "Buffer";

        switch (code) {
            
            case "1": return "Select";
            case "37": 
            case "515": return "Input";

            case "38": 
            case "517": 
            case "69": return "Verify";

            case "39": 
            case "514": 
            case "165": return "Buffer"; 

            case "File": return "FileUpload";
            default: 
                return "Unknown_" + code;
        }
    }

    private static String determineActionPath(CleanStep step, Module myModule) {
        String resourceValue = "";

        if (step.getValues() != null && step.getValuesAsMap().containsKey("Resource")) {
            resourceValue = step.getValuesAsMap().get("Resource").getValue();
        }

        if (resourceValue == null || resourceValue.isEmpty()) {
            resourceValue = getResourceFromModule(myModule);
            
        }

        if (resourceValue == null || resourceValue.isEmpty()) {
            resourceValue = ToscaToAgaPhase1.currentFolderPath;
            
        }

        String cleanPath = resourceValue.startsWith("/") ? resourceValue.substring(1) : resourceValue;

        cleanPath = cleanPath.replace("/", ".").replace("-", "_").toLowerCase();

        if (cleanPath.startsWith("rest.")) {
            return cleanPath;
        } else {
            return "rest." + cleanPath;
        }
    }
    
    private static String getResourceFromModule(Module module) {

        String content = getDecompressedTCProperties(module); 
        if (content.contains("Name=\"Resource\"")) {
            int index = content.indexOf("Name=\"Resource\"");
            int start = content.indexOf("Value=\"", index) + 7;
            int end = content.indexOf("\"", start);
            return content.substring(start, end);
        }
        return "";
    }    

    private static Map<String, String> extractHeadersFromModule(Module module, ObjectMapper mapper) {
        Map<String, String> extractedHeaders = new LinkedHashMap<>();
        if (module == null || module.getAttributes() == null) return extractedHeaders;

        String rawHeadersBase64 = module.getAttributes().get("Headers");
        if (rawHeadersBase64 == null || rawHeadersBase64.trim().isEmpty()) return extractedHeaders;

        try {
            
            byte[] decodedBytes = Base64.getDecoder().decode(rawHeadersBase64.trim());
            String jsonString = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8).trim();

            List<List<Map<String, Object>>> rootList = mapper.readValue(jsonString, List.class);

            for (List<Map<String, Object>> innerList : rootList) {
                for (Map<String, Object> entry : innerList) {
                    
                    if ("Key".equals(entry.get("Key"))) {
                        Object headerNameObj = entry.get("Value");
                        if (headerNameObj != null) {
                            String headerName = String.valueOf(headerNameObj).trim();
                            if (!headerName.isEmpty()) {
                                extractedHeaders.put(headerName, ""); 
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MigrationLog.warn("Failed to read or parse Headers field: " + e.getMessage());
        }

        return extractedHeaders;
    }

    private static String getDecompressedTCProperties(Module module) {
        if (module == null) return "";

        String tcPropsBase64 = module.getAttributes().get("TCProperties");
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";

        try {
            
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);

            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))) {
                
                StringBuilder xmlContent = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    xmlContent.append(line);
                }
                return xmlContent.toString();
            }
        } catch (Exception e) {
            MigrationLog.warn("Failed to decompress TCProperties: " + e.getMessage());
            return "";
        }
    }

    private static String extractResourceFromTCProperties(String tcPropsBase64) {
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";
        try {
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 Scanner s = new Scanner(gis, "UTF-8").useDelimiter("\\A")) {
                
                String xml = s.hasNext() ? s.next() : "";

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Name=\"Resource\"\\s+Value=\"([^\"]+)\"");
                java.util.regex.Matcher matcher = pattern.matcher(xml);

                if (matcher.find()) {
                    return matcher.group(1); 
                }
            }
        } catch (Exception e) {
            
        }
        return "";
    }    
    private static String normalizeApiModuleName(String name) {

        if (name == null || name.isBlank()) {
            return "";
        }

        return name
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
