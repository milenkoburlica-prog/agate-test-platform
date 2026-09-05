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
import at.co.svc.agate.core.reference.UnorderedRule;

/**
 * Factory for parsing YAML map data into TestStep objects.
 * Supports Legacy (Action/Value), REST (Url/Body/Headers), OC (Pod/From/To)
 * and reference-response assertions.
 */
public class StepParserFactory {

    @SuppressWarnings("unchecked")
    public static TestStep parseStep(
            TestCase tc,
            Map<String, Object> stepMap,
            String yamlPath,
            int index,
            YamlPlaceholderResolver resolver) {

        TestStep step = new TestStep();

        // 1. Core Metadata
        String rawType = asString(stepMap.get("type"));
        if (rawType == null) {
            throw new RuntimeException("Step at index " + index + " is missing 'type'");
        }

        step.setType(StepType.valueOf(rawType.toUpperCase()));
        step.setId(asString(stepMap.get("id")));
        step.setOp(asString(stepMap.get("op")));
        step.setName(asString(stepMap.get("name")));
        step.setCondition(asString(stepMap.get("condition")));

        // CMD / General
        step.setCommand(asString(stepMap.get("command")));
        step.setResponse(asString(stepMap.get("response")));

        // ASSERT / BUFFER
        step.setExpected(asString(stepMap.get("expected")));
        step.setValue(asString(stepMap.get("value")));
        step.setAction(asString(stepMap.get("action")));
        step.setSource(asString(stepMap.get("source")));
        step.setPath(asString(stepMap.get("path")));
        step.setSelector(asString(stepMap.get("selector")));

        // JSON ASSERT
        step.setFile(asString(stepMap.get("file")));

        // SQL ASSERT
        step.setRow(asString(stepMap.get("row")));
        step.setColumn(asString(stepMap.get("column")));

        // REST / SOAP semantic fields
        step.setUrl(asString(stepMap.get("url")));
        step.setBody(asString(stepMap.get("body")));
        step.setEndpoint(asString(stepMap.get("endpoint")));

        Object headersObj = stepMap.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headers = new java.util.LinkedHashMap<>();
            ((Map<?, ?>) headersObj).forEach((key, value) -> {
                if (key != null && value != null) {
                    headers.put(key.toString(), value.toString());
                }
            });
            step.setHeaders(headers);
        }

        // Assertion & Buffer specific fields
        step.setField(asString(stepMap.get("field")));
        step.setAssertType(asString(stepMap.get("assertType")));

        // OC / File Transfer fields
        step.setPod(asString(stepMap.get("pod")));
        step.setFrom(asString(stepMap.get("from")));
        step.setTo(asString(stepMap.get("to")));

        // 6. Constraints (SQL/XML/General)
        Object constraintsObj = stepMap.get("constraints");
        if (constraintsObj instanceof List) {
            List<Constraint> constraintsList = new ArrayList<>();

            for (Object item : (List<?>) constraintsObj) {
                if (!(item instanceof Map)) {
                    continue;
                }

                Map<String, Object> cMap = (Map<String, Object>) item;
                Constraint c = new Constraint();
                c.setColumn(asString(cMap.get("column")));
                c.setPath(asString(cMap.get("path")));
                c.setAction(asString(cMap.get("action")));
                c.setExpected(asString(cMap.get("expected")));
                constraintsList.add(c);
            }

            step.setConstraints(constraintsList);
        }

        // 7. Automated Downloads Extraction Configuration
        Object downloadObj = stepMap.get("download");
        if (downloadObj instanceof List) {
            List<DownloadConfig> downloadList = new ArrayList<>();

            for (Object item : (List<?>) downloadObj) {
                if (!(item instanceof Map)) {
                    continue;
                }

                Map<String, Object> dlMap = (Map<String, Object>) item;
                DownloadConfig config = new DownloadConfig();

                String method = dlMap.get("method") != null
                        ? dlMap.get("method").toString()
                        : "INLINE";

                config.setMethod(method);
                config.setPath(asString(dlMap.get("path")));
                config.setTargetPath(asString(dlMap.get("targetPath")));

                downloadList.add(config);
            }

            step.setDownload(downloadList);
        }

        // 8. Automated Uploads Configuration
        Object uploadObj = stepMap.get("upload");
        if (uploadObj instanceof List) {
            List<UploadConfig> uploadList = new ArrayList<>();

            for (Object item : (List<?>) uploadObj) {
                if (!(item instanceof Map)) {
                    continue;
                }

                Map<String, Object> ulMap = (Map<String, Object>) item;
                UploadConfig config = new UploadConfig();

                String method = ulMap.get("method") != null
                        ? ulMap.get("method").toString()
                        : "INLINE";

                config.setMethod(method);
                config.setPath(asString(ulMap.get("path")));
                config.setSourceFile(asString(ulMap.get("sourceFile")));

                uploadList.add(config);
            }

            step.setUpload(uploadList);
        }

        // 9. Automated PDF Configuration & Assertions
        if (step.getType() == StepType.PDF) {
            step.setTargetPDF(asString(stepMap.get("targetPDF")));
            step.setPdfPassword(asString(stepMap.get("pdfPassword")));

            Object assertionsObj = stepMap.get("assertions");
            if (assertionsObj instanceof List) {
                List<PdfAssertion> pdfAssertionList = new ArrayList<>();

                for (Object item : (List<?>) assertionsObj) {
                    if (!(item instanceof Map)) {
                        continue;
                    }

                    Map<String, Object> aMap = (Map<String, Object>) item;
                    PdfAssertion assertionConfig = new PdfAssertion();

                    assertionConfig.setValue(asString(aMap.get("value")));
                    assertionConfig.setAction(
                            aMap.get("action") != null
                                    ? aMap.get("action").toString()
                                    : "EXIST");

                    if (aMap.get("expected") != null) {
                        assertionConfig.setExpected(
                                Integer.parseInt(aMap.get("expected").toString()));
                    } else {
                        assertionConfig.setExpected(1);
                    }

                    pdfAssertionList.add(assertionConfig);
                }

                step.setPdfAssertions(pdfAssertionList);
            }
        }

        // 10. Reference-response assertion: ignored fields
        Object ignoreObj = stepMap.get("ignore");
        if (ignoreObj instanceof List) {
            List<String> ignorePaths = new ArrayList<>();

            for (Object item : (List<?>) ignoreObj) {
                if (item != null && !item.toString().isBlank()) {
                    ignorePaths.add(item.toString());
                }
            }

            step.setIgnore(ignorePaths);
        }

        // 11. Reference-response assertion: unordered lists
        Object unorderedObj = stepMap.get("unordered");
        if (unorderedObj instanceof List) {
            List<UnorderedRule> unorderedRules = new ArrayList<>();

            for (Object item : (List<?>) unorderedObj) {
                if (item instanceof String) {
                    String path = item.toString();
                    if (!path.isBlank()) {
                        unorderedRules.add(new UnorderedRule(path));
                    }
                    continue;
                }

                if (!(item instanceof Map)) {
                    continue;
                }

                Map<String, Object> ruleMap = (Map<String, Object>) item;
                String path = asString(ruleMap.get("path"));
                String matchBy = asString(ruleMap.get("matchBy"));

                if (path != null && !path.isBlank()) {
                    unorderedRules.add(new UnorderedRule(path, matchBy));
                }
            }

            step.setUnordered(unorderedRules);
        }

        return step;
    }

    private static String asString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
