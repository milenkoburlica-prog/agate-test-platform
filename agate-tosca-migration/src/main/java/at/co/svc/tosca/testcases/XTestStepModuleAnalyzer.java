package at.co.svc.tosca.testcases;

import java.util.Map;

import at.co.svc.tosca.tsu.dto.ToscaNode;

public class XTestStepModuleAnalyzer {

    private final Map<String, ToscaNode> moduleIndex;

    public XTestStepModuleAnalyzer(Map<String, ToscaNode> moduleIndex) {
        this.moduleIndex = moduleIndex;
    }

    public ModuleAnalysis analyze(String moduleId) {

        ToscaNode module = moduleIndex.get(moduleId);

        if (module == null) {
            return ModuleAnalysis.unknown();
        }

        String objectClass = module.objectClass;
        String name = module.getName();

        // -----------------------------
        // XMODULE
        // -----------------------------
        if ("XModule".equals(objectClass)) {

            String businessType = safe(get(module, "BusinessType"));

            return new ModuleAnalysis(
                    "XMODULE",
                    name,
                    businessType,
                    null
            );
        }

        // -----------------------------
        // APIMODULE
        // -----------------------------
        if ("ApiModule".equals(objectClass)) {

            String method = safe(getTcProp(module, "Method"));
            String resource = safe(getTcProp(module, "Resource"));

            return new ModuleAnalysis(
                    "APIMODULE",
                    name,
                    method,
                    resource
            );
        }

        return ModuleAnalysis.unknown();
    }

    private String get(ToscaNode node, String key) {
        if (node.attributes == null) return "";
        Object v = node.attributes.get(key);
        return v == null ? "" : v.toString();
    }

    private String getTcProp(ToscaNode node, String key) {
        if (node.attributes == null) return "";
        Object tc = node.attributes.get("TCProperties");
        if (!(tc instanceof Map)) return "";
        Object v = ((Map<?, ?>) tc).get(key);
        return v == null ? "" : v.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // -------------------------
    // RESULT MODEL
    // -------------------------
    public static class ModuleAnalysis {

        public String type;
        public String name;
        public String subtype;
        public String resource;

        public ModuleAnalysis(String type, String name, String subtype, String resource) {
            this.type = type;
            this.name = name;
            this.subtype = subtype;
            this.resource = resource;
        }

        public static ModuleAnalysis unknown() {
            return new ModuleAnalysis("UNKNOWN", "", "", "");
        }
    }
}