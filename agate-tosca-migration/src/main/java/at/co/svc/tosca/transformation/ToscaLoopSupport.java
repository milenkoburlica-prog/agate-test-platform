package at.co.svc.tosca.transformation;

import com.fasterxml.jackson.databind.JsonNode;

public final class ToscaLoopSupport {

    private ToscaLoopSupport() {
    }

    public static boolean isLoopControlFlow(
            JsonNode node) {

        return getLoopMode(node) != null;
    }

    public static LoopMode getLoopMode(
            JsonNode node) {

        if (node == null) {
            return null;
        }

        if (!"TestCaseControlFlowItem".equals(
                node.path("ObjectClass").asText())) {

            return null;
        }

        String maximumRepetitions =
                node.path("Attributes")
                        .path("MaximumRepetitions")
                        .asText("");

        /*
         * Normal IF structures have no MaximumRepetitions.
         *
         * Therefore this is also our protection against accidentally
         * treating an IF as a LOOP.
         */
        if (maximumRepetitions.isBlank()) {
            return null;
        }

        String statementType =
                node.path("Attributes")
                        .path("StatementType")
                        .asText("");

        return switch (statementType) {

            case "2" ->
                    LoopMode.WHILE_DO;

            case "3" ->
                    LoopMode.DO_WHILE;

            default ->
                    null;
        };
    }

    public static int getMaximumRepetitions(
            JsonNode node,
            int defaultValue) {

        if (node == null) {
            return defaultValue;
        }

        String value =
                node.path("Attributes")
                        .path("MaximumRepetitions")
                        .asText("");

        if (value.isBlank()) {
            return defaultValue;
        }

        try {

            int parsed =
                    Integer.parseInt(
                            value.trim()
                    );

            if (parsed <= 0) {
                return defaultValue;
            }

            return parsed;

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }

    public static boolean isConditionFolder(
            JsonNode folder) {

        if (folder == null) {
            return false;
        }

        return "TestCaseControlFlowFolder".equals(
                folder.path("ObjectClass").asText())
                && "0".equals(
                folder.path("Attributes")
                        .path("StatementType")
                        .asText("")
        );
    }

    public static boolean isBodyFolder(
            JsonNode folder) {

        if (folder == null) {
            return false;
        }

        if (!"TestCaseControlFlowFolder".equals(
                folder.path("ObjectClass").asText())) {

            return false;
        }

        String statementType =
                folder.path("Attributes")
                        .path("StatementType")
                        .asText("");

        /*
         * 0 is condition.
         *
         * All other folders inside a recognized LOOP belong to
         * executable loop content.
         */
        return !"0".equals(statementType);
    }
}