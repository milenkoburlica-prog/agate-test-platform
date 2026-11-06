package at.co.svc.agate.engine.rest;

import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Utility for pretty-printing REST requests and responses to the console.
 */
public class RestPrinter {    
    /**
     * Prints the original DSL YAML step text that triggered the execution.
     */
    public static void printDslStep(String textYaml, TestLogger logger) {
        if (textYaml == null || textYaml.isEmpty()) {
            return;
        }

        logger.info("..."); // Prazan red pre bloka radi preglednosti
        
        // Razbijamo originalni tekst na linije kako bismo svaku lepo formatirali
        String[] lines = textYaml.split("\\r?\\n");
        for (String line : lines) {
            // Koristimo PURPLE za DSL oznaku kako bi se razlikovala od same HTTP eksekucije
            logger.info(String.format("%s>>> DSL%s     : %s", 
                    ConsoleColors.BLUE, ConsoleColors.RESET, line));
        }
    }

    public static void printRequest(String method, String url, Map<String, String> headers, String body,
            TestLogger logger) {
        logger.info(String.format("    %s>>> CALL%s    : %s%s%s %s", ConsoleColors.GREEN, ConsoleColors.RESET,
                ConsoleColors.CYAN, method, ConsoleColors.RESET, url));

        // --- Added: Header Printing ---
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((k, v) -> logger.info(
                    String.format("    %s>>> HEADER%s  : %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET, k, v)));
        }

        if (body != null && !body.isEmpty()) {
            // Clean up body for single-line display if needed, or keep as is
            String cleanBody = body.replace("\n", "").replace("\r", "").replaceAll("\\s+", " ");
            logger.info(
                    String.format("    %s>>> BODY%s    : %s", ConsoleColors.GREEN, ConsoleColors.RESET, cleanBody));
        }
    }

    public static void printResponse(int statusCode, String body, TestLogger logger) {
        String color = (statusCode >= 200 && statusCode < 300) ? ConsoleColors.GREEN : ConsoleColors.RED;
        logger.info("");
        logger.info(String.format("    %s<<< HTTP%s    : %s%d%s", ConsoleColors.GREEN, ConsoleColors.RESET, color,
                statusCode, ConsoleColors.RESET));

        if (body != null && !body.isEmpty()) {
            logger.info(String.format("    %s<<< BODY%s    : %s", ConsoleColors.GREEN, ConsoleColors.RESET, body));
        }
    }

    public static void printResponse(int statusCode, Map<String, List<String>> headers, String body, TestLogger logger) {

        // Determine color based on HTTP status code
        String color = (statusCode >= 200 && statusCode < 300) ? ConsoleColors.GREEN : ConsoleColors.RED;

        // Add spacing before response block for readability
        logger.info("");

        // Print HTTP status line
        logger.info(String.format("    %s<<< HTTP%s    : %s%d%s", ConsoleColors.GREEN, ConsoleColors.RESET, color,
                statusCode, ConsoleColors.RESET));

        // Print response headers (if available)
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, values) -> {

                // Join multiple header values into a single string
                String joinedValues = String.join(",", values);

                logger.info(String.format("    %s<<< HEADER%s : %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET, key,
                        joinedValues));
            });
        }

        // Print response body (if present)
        if (body != null && !body.isEmpty()) {
            logger.info(String.format("    %s<<< BODY%s    : %s", ConsoleColors.GREEN, ConsoleColors.RESET, body));
        }

        // Add spacing after response block for readability
    }

}