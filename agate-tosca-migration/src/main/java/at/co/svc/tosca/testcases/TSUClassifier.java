package at.co.svc.tosca.testcases;

import at.co.svc.tosca.tsu.dto.ToscaNode;

import java.util.List;
import java.util.Map;

public class TSUClassifier {

    private final Map<String, ToscaNode> moduleIndex;

    public TSUClassifier(Map<String, ToscaNode> moduleIndex) {
        this.moduleIndex = moduleIndex;
    }

    public String classify(ToscaNode node) {

        // =================================================
        // DEFAULT
        // =================================================
        String result = "NOT_CLASSIFIED";

        // =================================================
        // REUSABLE
        // =================================================
        if (isReusable(node)) {
            return "REUSABLE";
        }

        // =================================================
        // ONLY XTestStep CAN BE RESOLVED VIA MODULE
        // =================================================
        if (!"XTestStep".equals(node.objectClass)) {
            return result;
        }

        if (node.extraAssocs == null) {
            return result;
        }

        List<String> modules = node.extraAssocs.get("Module");

        if (modules == null || modules.isEmpty()) {
            return result;
        }

        String moduleId = modules.get(0);

        ToscaNode module = moduleIndex.get(moduleId);

        if (module == null) {
            return result;
        }

        // =================================================
        // ApiModule
        // =================================================
        if ("ApiModule".equals(module.objectClass)) {
            return "ApiModule";
        }

        // =================================================
        // XModule -> BusinessType
        // =================================================
        if ("XModule".equals(module.objectClass)) {

            String businessType = getAttr(module, "BusinessType");

            if (businessType != null && !businessType.isBlank()) {
                return businessType;
            }
        }

        return result;
    }

    // =================================================
    // REUSABLE
    // =================================================
    private boolean isReusable(ToscaNode node) {

        if (!"TestStepFolderReference".equals(node.objectClass)) {
            return false;
        }

        if (node.extraAssocs == null) {
            return false;
        }

        List<String> reused = node.extraAssocs.get("ReusedItem");

        return reused != null && !reused.isEmpty();
    }

    // =================================================
    // HELPERS
    // =================================================
    private String getAttr(ToscaNode node, String key) {

        if (node.attributes == null) {
            return null;
        }

        Object v = node.attributes.get(key);

        return v == null ? null : String.valueOf(v);
    }
}