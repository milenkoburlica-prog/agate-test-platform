package at.co.svc.aga.transformator.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Tosca DATE/DATETIME format tokens to Java DateTimeFormatter syntax
 * used by AGATE.
 *
 * Example:
 *
 *   {DATE[][][yyyy-MM-ddTHH:mm:ss]}
 *
 * becomes:
 *
 *   {DATE[][][yyyy-MM-dd'T'HH:mm:ss]}
 *
 * Important:
 * This class changes only the FORMAT part of DATE/DATETIME placeholders.
 * Base date and offset are preserved unchanged.
 */
public final class ToscaDateFormatTranslator {

    private static final Pattern DATE_PATTERN =
            Pattern.compile(
                    "\\{(DATE|DATETIME)(\\[[^]]*])?(\\[[^]]*])?\\[([^]]*)]}"
            );

    private ToscaDateFormatTranslator() {
    }

    public static String translate(
            String input) {

        if (input == null
                || input.isBlank()) {

            return input;
        }

        Matcher matcher =
                DATE_PATTERN.matcher(
                        input
                );

        StringBuffer result =
                new StringBuffer();

        while (matcher.find()) {

            String type =
                    matcher.group(1);

            String baseDate =
                    matcher.group(2) != null
                            ? matcher.group(2)
                            : "[]";

            String offset =
                    matcher.group(3) != null
                            ? matcher.group(3)
                            : "[]";

            String format =
                    matcher.group(4);

            String javaFormat =
                    toJavaDateTimeFormat(
                            format
                    );

            String replacement =
                    "{"
                            + type
                            + baseDate
                            + offset
                            + "["
                            + javaFormat
                            + "]}";

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(
                            replacement
                    )
            );
        }

        matcher.appendTail(
                result
        );

        return result.toString();
    }


    static String toJavaDateTimeFormat(
            String format) {

        if (format == null
                || format.isBlank()) {

            return format;
        }

        String javaFormat =
                format;

        /*
         * Tosca fractional-second token:
         *
         *   f / ff / fff ...
         *
         * Java DateTimeFormatter:
         *
         *   S / SS / SSS ...
         */
        javaFormat =
                javaFormat.replace(
                        "f",
                        "S"
                );

        /*
         * Tosca commonly uses an unquoted literal T:
         *
         *   yyyy-MM-ddTHH:mm:ss
         *
         * Java DateTimeFormatter interprets unquoted T as a pattern letter
         * and throws IllegalArgumentException: Unknown pattern letter: T.
         *
         * Quote only standalone, currently unquoted T characters.
         */
        javaFormat =
                quoteLiteralT(
                        javaFormat
                );

        return javaFormat;
    }


    private static String quoteLiteralT(
            String format) {

        StringBuilder result =
                new StringBuilder();

        boolean insideQuote =
                false;

        for (int i = 0;
             i < format.length();
             i++) {

            char c =
                    format.charAt(i);

            if (c == '\'') {

                /*
                 * Java DateTimeFormatter escapes a literal single quote
                 * inside a quoted section as two single quotes.
                 */
                if (i + 1 < format.length()
                        && format.charAt(i + 1) == '\'') {

                    result.append("''");
                    i++;
                    continue;
                }

                insideQuote =
                        !insideQuote;

                result.append(c);
                continue;
            }

            if (c == 'T'
                    && !insideQuote) {

                result.append("'T'");

            } else {

                result.append(c);
            }
        }

        return result.toString();
    }
}
