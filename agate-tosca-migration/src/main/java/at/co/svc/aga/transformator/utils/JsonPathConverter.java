package at.co.svc.aga.transformator.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPathConverter {

    public static void main(String[] args) {

        String[] toscaPaths = {
                "RootObject.logs_structured.item#1.level",
                "RootObject.logs_structured.item#1.mdc.event.code",
                "RootObject.logs_structured.item#1.mdc.log.type",
                "RootObject.logs_structured.item#1.mdc.event.message"
        };

        MigrationLog.debugSection("JSONPath Converter Test");

        for (String tosca : toscaPaths) {

            String agate =
                    convertToscaToAgate(tosca);

            MigrationLog.debug(
                    "Tosca: "
                            + tosca
                            + " -> Agate: "
                            + agate
            );
        }
    }

    public static String convertToscaToAgate(String toscaPath) {

        if (toscaPath == null || toscaPath.isEmpty()) {
            return "$";
        }

        // 1. Replace RootObject with $
        String path =
                toscaPath.replace(
                        "RootObject",
                        "$"
                );

        // 2. Replace .item#X with [X-1]
        Pattern itemPattern =
                Pattern.compile("\\.item#(\\d+)");

        Matcher itemMatcher =
                itemPattern.matcher(path);

        StringBuilder sb =
                new StringBuilder();

        while (itemMatcher.find()) {

            int toscaIndex =
                    Integer.parseInt(
                            itemMatcher.group(1)
                    );

            int jsonIndex =
                    toscaIndex - 1;

            itemMatcher.appendReplacement(
                    sb,
                    "[" + jsonIndex + "]"
            );
        }

        itemMatcher.appendTail(sb);

        path = sb.toString();

        /*
         * Handle special keys containing dots:
         *
         * .mdc.event.code
         *
         * becomes:
         *
         * .mdc['event.code']
         */
        if (path.contains(".mdc.")) {

            int mdcIndex =
                    path.indexOf(".mdc.");

            String prefix =
                    path.substring(
                            0,
                            mdcIndex + 4
                    );

            String suffix =
                    path.substring(
                            mdcIndex + 5
                    );

            if (suffix.contains(".")) {

                path =
                        prefix
                                + "['"
                                + suffix
                                + "']";
            }
        }

        return path;
    }
}