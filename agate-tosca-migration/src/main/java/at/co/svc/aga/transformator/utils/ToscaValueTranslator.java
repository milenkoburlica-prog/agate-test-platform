package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.config.ToscaTranslationConfig;
import at.co.svc.aga.transformator.config.ToscaTranslationConfigLoader;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ToscaValueTranslator {

    private static final Pattern B_PATTERN =
            Pattern.compile(
                    "\\{B\\[([^\\]]+)\\]\\}"
            );

    private static final Pattern S_PATTERN =
            Pattern.compile(
                    "\\{S\\[([^\\]]+)\\]\\}"
            );

    private static final Pattern PL_PATTERN =
            Pattern.compile(
                    "\\{PL\\[([^\\]]+)\\]\\}"
            );


    private static final ToscaTranslationConfig CONFIG =
            ToscaTranslationConfigLoader.load();


    private ToscaValueTranslator() {
    }


    /**
     * Translates Tosca-specific values and placeholders
     * into AGATE-compatible placeholders.
     */
    public static String translateToscaValues(
            String input) {

        if (input == null
                || input.isEmpty()) {

            return input;
        }


        String result =
                translateLiteralMappings(
                        input
                );


        result =
                translateBtoE(
                        result
                );


        result =
                translateStoE(
                        result
                );


        result =
                translatePLtoR(
                        result
                );


        /*
         * Preserve the existing YAML/backslash normalization.
         */
        result =
                result.replace(
                        "\\\\\\",
                        "\\\\"
                );


        return result;
    }


    /**
     * Customer-specific literal replacements.
     *
     * Example:
     *
     * %logcheckcloud%
     *
     * ->
     *
     * {E[env.openshift_logcheck_dir]}
     */
    private static String translateLiteralMappings(
            String input) {

        String result =
                input;


        for (Map.Entry<String, String> entry :
                CONFIG
                        .getLiteralMappings()
                        .entrySet()) {

            String source =
                    entry.getKey();

            String target =
                    entry.getValue();


            if (source == null
                    || source.isEmpty()
                    || target == null) {

                continue;
            }


            result =
                    result.replace(
                            source,
                            target
                    );
        }


        return result;
    }


    /**
     * Customer-defined Tosca buffer mapping.
     *
     * Example:
     *
     * {B[G_EC_OpenShift_Namespace]}
     *
     * ->
     *
     * {E[env.openShift.namespace]}
     *
     * Unknown buffers remain unchanged.
     */
    private static String translateBtoE(
            String input) {

        Matcher matcher =
                B_PATTERN.matcher(
                        input
                );


        StringBuilder result =
                new StringBuilder();


        while (matcher.find()) {

            String oldKey =
                    matcher.group(1);


            String mappedKey =
                    CONFIG
                            .getBufferMappings()
                            .get(oldKey);
            

            
            if (mappedKey != null
                    && !mappedKey.isBlank()) {

                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(
                                "{E["
                                        + mappedKey
                                        + "]}"
                        )
                );

            } else {

                /*
                 * Not customer-mapped.
                 *
                 * This may be a normal runtime BUFFER and must therefore
                 * remain unchanged.
                 */
                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(
                                matcher.group(0)
                        )
                );
            }
        }


        matcher.appendTail(
                result
        );


        return result.toString();
    }


    /**
     * Tosca setting mapping.
     *
     * Example:
     *
     * {S[SVC.Projekt root path]}
     *
     * ->
     *
     * {E[env.SVC_Projekt_root_path]}
     */
    private static String translateStoE(
            String input) {

        Matcher matcher =
                S_PATTERN.matcher(
                        input
                );


        StringBuilder result =
                new StringBuilder();


        while (matcher.find()) {

            String oldKey =
                    matcher.group(1);


            String mappedKey =
                    CONFIG
                            .getSettingMappings()
                            .get(oldKey);


            if (mappedKey != null
                    && !mappedKey.isBlank()) {

                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(
                                "{E["
                                        + mappedKey
                                        + "]}"
                        )
                );

            } else {

                matcher.appendReplacement(
                        result,
                        Matcher.quoteReplacement(
                                matcher.group(0)
                        )
                );
            }
        }


        matcher.appendTail(
                result
        );


        return result.toString();
    }


    /**
     * Tosca reusable parameter:
     *
     * {PL[value]}
     *
     * ->
     *
     * {R[value]}
     *
     * This transformation is generic and therefore does not
     * belong to the customer configuration.
     */
    private static String translatePLtoR(
            String input) {

        Matcher matcher =
                PL_PATTERN.matcher(
                        input
                );


        StringBuilder result =
                new StringBuilder();


        while (matcher.find()) {

            String paramName =
                    matcher.group(1);


            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(
                            "{R["
                                    + paramName
                                    + "]}"
                    )
            );
        }


        matcher.appendTail(
                result
        );


        return result.toString();
    }
}