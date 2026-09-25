package at.co.svc.agate.core.runner;

import java.util.Locale;

public final class AgateDescribe {

    private AgateDescribe() {
    }

    public static void describe(String type) {

        if (type == null || type.isBlank()) {
            printOverview();
            return;
        }

        switch (type.trim().toUpperCase(Locale.ROOT)) {

            case "REST" ->
                    describeRest();

            case "SOAP" ->
                    describeSoap();

            case "CALL" ->
                    describeCall();

            case "FILE" ->
                    describeFile();

            case "CMD" ->
                    describeCmd();

            case "SQL" ->
                    describeSql();

            case "BUFFER" ->
                    describeBuffer();

            case "WAIT" ->
                    describeWait();

            case "OC",
                 "OPENSHIFT" ->
                    describeOpenShift();

            default ->
                    printUnknown(type);
        }
    }

    private static void printOverview() {

        System.out.println();
        System.out.println("AGATE DSL DESCRIBE");
        System.out.println("------------------------------------------------------------");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  startTests.bat describe <TYPE>");
        System.out.println();
        System.out.println("Available types:");
        System.out.println();
        System.out.println("  REST");
        System.out.println("  SOAP");
        System.out.println("  CALL");
        System.out.println("  FILE");
        System.out.println("  CMD");
        System.out.println("  SQL");
        System.out.println("  BUFFER");
        System.out.println("  WAIT");
        System.out.println("  OC");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  startTests.bat describe SOAP");
        System.out.println();
    }

    private static void describeRest() {

        header(
                "REST",
                "Execute HTTP requests and validate responses."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: REST");
        System.out.println("    op: EXEC");
        System.out.println("    command: rest.example.get");
        System.out.println("    endpoint: \"{E[env.example.url]}\"");
        System.out.println("    response: response1");
        System.out.println();
        System.out.println("Important fields:");
        field("op", "Operation, e.g. EXEC or ASSERT");
        field("command", "REST resource / request definition");
        field("endpoint", "Target endpoint");
        field("response", "Name under which the response is stored");
        field("source", "ASSERT source, e.g. STATUS, BODY, HEADERS");
        field("action", "Assertion action, e.g. EQUALS");
        field("expected", "Expected value");
    }

    private static void describeSoap() {

        header(
                "SOAP",
                "Execute SOAP requests and validate SOAP responses."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: SOAP");
        System.out.println("    op: EXEC");
        System.out.println("    endpoint: \"https://{E[env.ecard.service]}\"");
        System.out.println("    command: soap.dmp.11.getdmps_request_v11");
        System.out.println("    response: soap_response");
        System.out.println("    parameters:");
        System.out.println("      dialogId: \"{B[dialogId]}\"");
        System.out.println();
        System.out.println("ASSERT example:");
        System.out.println();
        System.out.println("  - type: SOAP");
        System.out.println("    op: ASSERT");
        System.out.println("    source: BODY");
        System.out.println("    path: \"/*[local-name()='Envelope']/*[local-name()='Body']\"");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: \"value\"");
        System.out.println("    response: soap_response");
        System.out.println();
        System.out.println("Important fields:");
        field("op", "EXEC or ASSERT");
        field("endpoint", "SOAP endpoint");
        field("command", "SOAP request definition");
        field("parameters", "Parameters passed into request template");
        field("response", "Stored SOAP response");
        field("source", "STATUS, BODY or HEADERS");
        field("path", "XPath for BODY assertions");
        field("action", "Assertion operation");
        field("expected", "Expected value");
    }

    private static void describeCall() {

        header(
                "CALL",
                "Call a reusable AGATE test fragment."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: CALL");
        System.out.println("    command: 'reusable.execute_psql_script'");
        System.out.println("    parameters:");
        System.out.println("      verbose: 'true'");
        System.out.println("      psqlFilename: 'delete.psql'");
        System.out.println();

        System.out.println("Important fields:");
        field("command", "Reusable name: reusable.<name>");
        field("parameters", "Values passed to the reusable block (Optional)");
        field("condition", "Execution condition (Optional)");
        System.out.println();

        System.out.println("Special parameter:");
        field("verbose", "Controls logging inside the reusable module");
        System.out.println("               false = internal reusable steps are hidden (default)");
        System.out.println("               true  = internal reusable steps are shown in the DSL log");
        System.out.println();

        System.out.println("Parameter access inside the reusable:");
        field("{R[name]}", "Reads a value passed through CALL parameters");
        field("{B[name]}", "Reads a global/runtime buffer from the test context");
        System.out.println();

        System.out.println("Example:");
        System.out.println();
        System.out.println("  parameters:");
        System.out.println("    verbose: 'true'");
        System.out.println("    command: 'mvn --version'");
        System.out.println();
        System.out.println("  Inside reusable:");
        System.out.println("    {R[command]} -> mvn --version");
    }
    
    private static void describeFile() {

        header(
                "FILE",
                "Read, write, copy, move, delete and validate files on the local AGATE filesystem."
        );

        System.out.println("Operations:");
        System.out.println();
        System.out.println("  EXEC    Execute a file operation");
        System.out.println("  BUFFER  Extract data from a previously read file");
        System.out.println("  ASSERT  Validate file existence or file content");
        System.out.println();

        System.out.println("EXEC actions:");
        System.out.println();
        System.out.println("  READ");
        System.out.println("  WRITE");
        System.out.println("  APPEND");
        System.out.println("  COPY");
        System.out.println("  MOVE");
        System.out.println("  DELETE");
        System.out.println("  EXISTS");
        System.out.println();

        System.out.println("Example - READ:");
        System.out.println();
        System.out.println("  - type: FILE");
        System.out.println("    op: EXEC");
        System.out.println("    action: READ");
        System.out.println("    path: \"output/report.txt\"");
        System.out.println("    response: report_raw");
        System.out.println();

        System.out.println("Example - COPY:");
        System.out.println();
        System.out.println("  - type: FILE");
        System.out.println("    op: EXEC");
        System.out.println("    action: COPY");
        System.out.println("    source: \"output/report.txt\"");
        System.out.println("    target: \"backup/report.txt\"");
        System.out.println("    overwrite: true");
        System.out.println();

        System.out.println("BUFFER actions:");
        System.out.println();
        System.out.println("  TEXT");
        System.out.println("  FILTER");
        System.out.println("  LINE");
        System.out.println("  LAST_LINE");
        System.out.println("  COUNT");
        System.out.println();

        System.out.println("Example - BUFFER:");
        System.out.println();
        System.out.println("  - type: FILE");
        System.out.println("    op: BUFFER");
        System.out.println("    response: report_raw");
        System.out.println("    action: FILTER");
        System.out.println("    value: \"ERROR\"");
        System.out.println("    name: error_lines");
        System.out.println();

        System.out.println("ASSERT actions:");
        System.out.println();
        System.out.println("  EXISTS");
        System.out.println("  NOT_EXISTS");
        System.out.println("  CONTAINS");
        System.out.println("  NOT_CONTAINS");
        System.out.println("  EQUALS");
        System.out.println("  NOT_EQUALS");
        System.out.println("  COUNT");
        System.out.println();

        System.out.println("Example - filesystem ASSERT:");
        System.out.println();
        System.out.println("  - type: FILE");
        System.out.println("    op: ASSERT");
        System.out.println("    action: EXISTS");
        System.out.println("    path: \"output/report.txt\"");
        System.out.println();

        System.out.println("Example - content ASSERT:");
        System.out.println();
        System.out.println("  - type: FILE");
        System.out.println("    op: ASSERT");
        System.out.println("    response: report_raw");
        System.out.println("    action: CONTAINS");
        System.out.println("    value: \"SUCCESS\"");
        System.out.println();

        System.out.println("Important fields:");
        field("op", "EXEC, BUFFER or ASSERT");
        field("action", "Operation-specific action");
        field("path", "Path for READ, WRITE, APPEND, DELETE, EXISTS");
        field("source", "Source path for COPY and MOVE");
        field("target", "Target path for COPY and MOVE");
        field("response", "Stores READ/EXISTS result or references previously read content");
        field("text", "Text for WRITE and APPEND");
        field("value", "Search/comparison value for BUFFER or ASSERT");
        field("name", "Target buffer name for BUFFER");
        field("expected", "Expected value, e.g. ASSERT COUNT");
        field("encoding", "Optional encoding, default UTF-8");
        field("overwrite", "Allow replacing existing target");
        field("newline", "APPEND: append newline, default false");
        field("missingOk", "DELETE: missing file is accepted, default false");
        field("condition", "Optional execution condition");
        System.out.println();

        System.out.println("Important:");
        System.out.println("  FILE works on the local filesystem of the AGATE process.");
        System.out.println();
        System.out.println("  BUFFER and content ASSERT use a previous READ response.");
        System.out.println("  They do not read directly from path.");
        System.out.println();
        System.out.println("  EXEC EXISTS  -> stores true/false in response");
        System.out.println("  ASSERT EXISTS -> directly validates that the file exists");
    }
    private static void describeCmd() {

        header(
                "CMD",
                "Execute an operating-system command."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: EXEC");
        System.out.println("    command: \"java -version\"");
        System.out.println("    response: java_version");
        System.out.println();
        System.out.println("Important fields:");
        field("op", "EXEC, ASSERT or BUFFER depending on use");
        field("command", "Command to execute");
        field("response", "Stored command result");
        field("outputFile", "Optional output file");
        field("condition", "Optional execution condition");
    }

    private static void describeSql() {

        header(
                "SQL",
                "Execute SQL and validate or buffer SQL results."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: EXEC");
        System.out.println("    command: \"SELECT ID FROM TEST_TABLE\"");
        System.out.println("    response: sql_response");
        System.out.println();
        System.out.println("Important fields:");
        field("op", "EXEC, ASSERT or BUFFER");
        field("command", "SQL statement");
        field("response", "Stored SQL result");
        field("action", "Assertion or buffer operation");
        field("column", "Column used by column-based operations");
        field("expected", "Expected assertion value");
        field("name", "Target buffer for BUFFER operations");
    }

    private static void describeBuffer() {

        header(
                "BUFFER",
                "Store a runtime value in an AGATE buffer."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: BUFFER");
        System.out.println("    op: EXEC");
        System.out.println("    name: filename");
        System.out.println("    value: \"{R[psqlFilename]}\"");
        System.out.println();
        System.out.println("Important fields:");
        field("name", "Buffer name");
        field("value", "Value to store");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  {B[filename]}");
    }

    private static void describeWait() {

        header(
                "WAIT",
                "Pause test execution."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: WAIT");
        System.out.println("    op: EXEC");
        System.out.println("    value: 1000");
        System.out.println();
        System.out.println("Important fields:");
        field("value", "Wait duration according to the AGATE WAIT engine");
    }

    private static void describeOpenShift() {

        header(
                "OC",
                "Execute OpenShift / oc operations."
        );

        System.out.println("Example:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: EXEC");
        System.out.println("    command: \"get pods\"");
        System.out.println("    response: oc_response");
        System.out.println();
        System.out.println("Important fields:");
        field("op", "Operation");
        field("command", "oc command");
        field("response", "Stored command output");
    }

    private static void printUnknown(String type) {

        System.err.println();
        System.err.println(
                "Unknown AGATE DSL type: "
                        + type
        );

        printOverview();
    }

    private static void header(
            String type,
            String description) {

        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println(
                "AGATE DSL: " + type
        );
        System.out.println(
                "============================================================"
        );
        System.out.println(description);
        System.out.println();
    }

    private static void field(
            String name,
            String description) {

        System.out.printf(
                "  %-12s %s%n",
                name,
                description
        );
    }
}