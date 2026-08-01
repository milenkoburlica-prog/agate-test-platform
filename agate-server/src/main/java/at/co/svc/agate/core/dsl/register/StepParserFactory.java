package at.co.svc.agate.core.dsl.register;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.dsl.model.Constraint;
import at.co.svc.agate.core.dsl.model.DownloadConfig;
import at.co.svc.agate.core.dsl.model.PdfAssertion;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.model.UploadConfig;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;

/**
 * Factory for parsing YAML map data into TestStep objects.
 * Supports Legacy (Action/Value), REST (Url/Body/Headers), and OC (Pod/From/To).
 */
public class StepParserFactory {

    @SuppressWarnings("unchecked")
    public static TestStep parseStep(TestCase tc, Map<String, Object> stepMap, String yamlPath, int index, YamlPlaceholderResolver resolver) {
        TestStep step = new TestStep();
        
        // 1. Core Metadata
        String rawType = asString(stepMap.get("type"));
        if (rawType == null) {
            throw new RuntimeException("Step at index " + index + " is missing 'type'");
        }
        step.setType(StepType.valueOf(rawType.toUpperCase()));
        
        step.setOp(asString(stepMap.get("op"))); 
        step.setName(asString(stepMap.get("name"))); 
        step.setCondition(asString(stepMap.get("condition"))); 
        
        // CMD
        step.setCommand(asString(stepMap.get("command")));
        step.setResponse(asString(stepMap.get("response")));

        // CMD_ASSERT
        step.setExpected(asString(stepMap.get("expected")));
        step.setValue(asString(stepMap.get("value")));
        step.setAction(asString(stepMap.get("action")));
        step.setSource(asString(stepMap.get("source")));
        step.setPath(asString(stepMap.get("path")));
        step.setSelector(asString(stepMap.get("selector")));

        //JSON ASSERT
        if (stepMap.get("file") != null) {
//             System.out.println("STOP: " + stepMap.get("file"));
        }
        step.setFile(asString(stepMap.get("file")));
        //SQL ASSERT
        step.setRow(asString(stepMap.get("row")));
        step.setColumn(asString(stepMap.get("column")));
       
        // 2. Legacy / General fields (Backward Compatibility)

        // 3. REST Semantic fields (New professional DSL style)
        step.setUrl(asString(stepMap.get("url")));
        step.setBody(asString(stepMap.get("body")));
        step.setEndpoint(asString(stepMap.get("endpoint")));
        
        // Safely handle Headers map
        Object headersObj = stepMap.get("headers");
        if (headersObj instanceof Map) {
            step.setHeaders((Map<String, String>) headersObj);
        }

        
        // 4. Assertion & Buffer specific fields
        step.setField(asString(stepMap.get("field")));
        step.setAssertType(asString(stepMap.get("assertType")));

        // 5. OC / File Transfer fields (RE-ADDED)
        // These are kept for specialized operations or legacy OC logic
        step.setPod(asString(stepMap.get("pod")));
        step.setFrom(asString(stepMap.get("from")));
        step.setTo(asString(stepMap.get("to")));

        // 6. Constraints (SQL/XML/General)
        Object constraintsObj = stepMap.get("constraints");
        if (constraintsObj instanceof List) {
            List<Map<String, Object>> rawConstraints = (List<Map<String, Object>>) constraintsObj;
            List<Constraint> constraintsList = new ArrayList<>();

            for (Map<String, Object> cMap : rawConstraints) {
                Constraint c = new Constraint();
                c.setColumn(asString(cMap.get("column")));
                c.setPath(asString(cMap.get("path")));
                c.setAction(asString(cMap.get("action")));
                c.setExpected(asString(cMap.get("expected")));
                constraintsList.add(c);
            }
            step.setConstraints(constraintsList);
        }
 
        // 7. Automated Downloads Extraction Configuration (NEW)
        Object downloadObj = stepMap.get("download");
        if (downloadObj instanceof List) {
            List<Map<String, Object>> rawDownloads = (List<Map<String, Object>>) downloadObj;
            List<DownloadConfig> downloadList = new ArrayList<>();

            for (Map<String, Object> dlMap : rawDownloads) {
                DownloadConfig config = new DownloadConfig();
                
                String method = dlMap.get("method") != null ? dlMap.get("method").toString() : "INLINE";
                config.setMethod(method);
                config.setPath(asString(dlMap.get("path")));
                config.setTargetPath(asString(dlMap.get("targetPath")));
                
                downloadList.add(config);
            }
            step.setDownload(downloadList);
        }

        // =========================================================================
        // 8. Automated Uploads Configuration (NOVO)
        // =========================================================================
        Object uploadObj = stepMap.get("upload");
        if (uploadObj instanceof List) {
            List<Map<String, Object>> rawUploads = (List<Map<String, Object>>) uploadObj;
            List<UploadConfig> uploadList = new ArrayList<>();

            for (Map<String, Object> ulMap : rawUploads) {
                UploadConfig config = new UploadConfig();
                
                // Ako metoda nije definisana u YAML-u, podrazumeva se "INLINE"
                String method = ulMap.get("method") != null ? ulMap.get("method").toString() : "INLINE";
                config.setMethod(method);
                
                config.setPath(asString(ulMap.get("path")));
                config.setSourceFile(asString(ulMap.get("sourceFile")));
                
                uploadList.add(config);
            }
            step.setUpload(uploadList);
        }
        
     // =========================================================================
        // 9. Automated PDF Configuration & Assertions (KONAČNO REŠENJE)
        // =========================================================================
        if (step.getType() == StepType.PDF) {
            step.setTargetPDF(asString(stepMap.get("targetPDF")));
            step.setPdfPassword(asString(stepMap.get("pdfPassword")));

            Object assertionsObj = stepMap.get("assertions");
            if (assertionsObj instanceof List) {
                List<Map<String, Object>> rawAssertions = (List<Map<String, Object>>) assertionsObj;
                List<PdfAssertion> pdfAssertionList = new ArrayList<>();

                for (Map<String, Object> aMap : rawAssertions) {
                    PdfAssertion assertionConfig = new PdfAssertion();
                    
                    assertionConfig.setValue(asString(aMap.get("value")));
                    assertionConfig.setAction(aMap.get("action") != null ? aMap.get("action").toString() : "EXIST");
                    
                    if (aMap.get("expected") != null) {
                        assertionConfig.setExpected(Integer.parseInt(aMap.get("expected").toString()));
                    } else {
                        assertionConfig.setExpected(1); // Default za COUNT ako se ne unese u YAML-u
                    }
                    
                    pdfAssertionList.add(assertionConfig);
                }
                step.setPdfAssertions(pdfAssertionList);
            }
        }        
        return step;
    }

    private static String asString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}