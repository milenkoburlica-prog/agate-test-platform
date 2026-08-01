package at.co.svc.open.api.spec.generator;

import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HallucinationDetector {
    private final EndpointContextBuilder contextBuilder = new EndpointContextBuilder();

    public void clean(GeneratedTestCase tc, EndpointDescription endpoint) {
        if (tc == null) {
            return;
        }
        cleanTestData(tc, endpoint);
        cleanName(tc, endpoint);
        cleanDescription(tc, endpoint);
    }

    private void cleanTestData(GeneratedTestCase tc, EndpointDescription endpoint) {
        String data = tc.getTestData();
        if (data == null || data.isBlank()) {
            return;
        }
        Set<String> allowed = contextBuilder.buildAllowedParameterNames(endpoint);
        String[] pairs = data.split("&");
        Map<String, String> cleaned = new HashMap<>();
        for (String pair : pairs) {
            if (pair == null || pair.isBlank() || !pair.contains("=")) {
                continue;
            }
            String[] kv = pair.split("=", 2);
            String key = kv[0].trim();
            String value = kv.length > 1 ? kv[1] : "";
            if (allowed.contains(key)) {
                cleaned.put(key, value);
            }
        }
        StringBuilder sb = new StringBuilder();
        cleaned.forEach((k, v) -> {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(k).append("=").append(v);
        });
        tc.setTestData(sb.toString());
    }

    private void cleanName(GeneratedTestCase tc, EndpointDescription endpoint) {
        if (tc.getName() == null) {
            return;
        }
        String operation = endpoint.getOperationId();
        if (operation == null || operation.isBlank()) {
            operation = "operation";
        }
        String name = tc.getName();
        name = name.replace("BPK_mapping", operation);
        name = name.replace("SVNR", "resource");
        name = name.replace("svnrs", "resource");
        tc.setName(name);
    }

    private void cleanDescription(GeneratedTestCase tc, EndpointDescription endpoint) {
        if (tc.getDescription() == null) {
            return;
        }
        String d = tc.getDescription();
        d = d.replace("BPK mapping", "resource");
        d = d.replace("SVNR", "resource");
        d = d.replace("svnrs", "resource");
        tc.setDescription(d);
    }

    public boolean isRelevant(GeneratedTestCase tc, EndpointDescription endpoint) {

        String text = (tc.getName() + " " + tc.getDescription() + " " + tc.getTestData()).toLowerCase();

        if (text.contains("bpk") || text.contains("svnr") || text.contains("sector") || text.contains("id-type")) {

            return false;
        }

        return true;
    }

    public void sanitize(GeneratedTestCase tc, EndpointDescription endpoint) {

        if (tc == null) {
            return;
        }

        if (tc.getName() != null) {

            tc.setName(tc.getName().replace("BPK_mapping", "").replace("SVNR", "").trim());
        }

        if (tc.getDescription() != null) {

            tc.setDescription(tc.getDescription().replace("BPK", "").replace("SVNR", "").replace("sector", "")
                    .replace("id-type", "").trim());
        }

        if (tc.getTestData() != null) {

            String data = tc.getTestData();

            data = data.replace("sector=SV", "");
            data = data.replace("id-type=svnrs", "");
            data = data.replace("id=1000220199", "");

            tc.setTestData(data.trim());
        }
    }
}