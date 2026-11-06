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

            case "LOOP" ->
                    describeLoop();

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
        System.out.println("  LOOP");
        System.out.println();

        System.out.println("Examples:");
        System.out.println("  startTests.bat describe REST");
        System.out.println("  startTests.bat describe CMD");
        System.out.println("  startTests.bat describe OC");
        System.out.println("  startTests.bat describe LOOP");
        System.out.println();
    }

    private static void describeRest() {

        header(
                "REST",
                "Execute HTTP requests, extract values and validate REST responses."
        );

        operations(
                "EXEC    Execute a configured REST request",
                "BUFFER  Extract a value from a stored REST response",
                "ASSERT  Validate status, body or headers"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: REST");
        System.out.println("    op: EXEC");
        System.out.println("    command: rest.example.get");
        System.out.println("    endpoint: \"{E[env.example.url]}\"");
        System.out.println("    response: rest_response");
        System.out.println();

        System.out.println("Example - BUFFER:");
        System.out.println();
        System.out.println("  - type: REST");
        System.out.println("    op: BUFFER");
        System.out.println("    response: rest_response");
        System.out.println("    source: BODY");
        System.out.println("    path: \"$.title\"");
        System.out.println("    name: title");
        System.out.println();

        System.out.println("Example - ASSERT STATUS:");
        System.out.println();
        System.out.println("  - type: REST");
        System.out.println("    op: ASSERT");
        System.out.println("    response: rest_response");
        System.out.println("    source: STATUS");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: 200");
        System.out.println();

        System.out.println("Example - ASSERT BODY:");
        System.out.println();
        System.out.println("  - type: REST");
        System.out.println("    op: ASSERT");
        System.out.println("    response: rest_response");
        System.out.println("    source: BODY");
        System.out.println("    selector: JSON_PATH");
        System.out.println("    path: \"$.title\"");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: \"Example\"");
        System.out.println();

        System.out.println("Example - MATCH_REFERENCE:");
        System.out.println();
        System.out.println("  - id: verify_response");
        System.out.println("    type: REST");
        System.out.println("    op: ASSERT");
        System.out.println("    response: rest_response");
        System.out.println("    source: BODY");
        System.out.println("    action: MATCH_REFERENCE");
        System.out.println();

        System.out.println("ASSERT sources:");
        list(
                "STATUS",
                "BODY",
                "HEADERS"
        );

        System.out.println("ASSERT actions:");
        list(
                "EQUALS",
                "VERIFY",
                "NOT_EQUALS",
                "CONTAINS",
                "EXISTS",
                "COUNT",
                "IS_EMPTY",
                "IS_NOT_EMPTY",
                "MATCH_REFERENCE"
        );

        System.out.println("Important fields:");
        field("op", "EXEC, BUFFER or ASSERT");
        field("command", "REST request definition used by EXEC");
        field("endpoint", "Optional endpoint override");
        field("response", "Stored REST response or response reference");
        field("source", "STATUS, BODY or HEADERS");
        field("selector", "Optional selector type, e.g. JSON_PATH");
        field("path", "Value path for BODY/HEADERS operations");
        field("action", "Assertion action");
        field("expected", "Expected assertion value");
        field("name", "Target buffer name for BUFFER");
        field("unordered", "Optional MATCH_REFERENCE unordered rules");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  EXEC stores the response under 'response'.");
        System.out.println("  BUFFER reads from a previous response and stores the extracted value");
        System.out.println("  as an AGATE buffer variable.");
        System.out.println("  MATCH_REFERENCE is intended for BODY validation.");
    }

    private static void describeSoap() {

        header(
                "SOAP",
                "Execute SOAP requests, extract values and validate SOAP responses."
        );

        operations(
                "EXEC    Execute a SOAP request",
                "BUFFER  Extract data from a stored SOAP response",
                "ASSERT  Validate status, body or headers"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: SOAP");
        System.out.println("    op: EXEC");
        System.out.println("    endpoint: \"https://{E[env.ecard.service]}\"");
        System.out.println("    command: soap.dmp.11.getdmps_request_v11");
        System.out.println("    response: soap_response");
        System.out.println("    parameters:");
        System.out.println("      dialogId: \"{B[dialogId]}\"");
        System.out.println();

        System.out.println("Example - BUFFER:");
        System.out.println();
        System.out.println("  - type: SOAP");
        System.out.println("    op: BUFFER");
        System.out.println("    response: soap_response");
        System.out.println("    source: BODY");
        System.out.println("    path: \"//*[local-name()='dialogId']/text()\"");
        System.out.println("    name: dialogId");
        System.out.println();

        System.out.println("Example - ASSERT:");
        System.out.println();
        System.out.println("  - type: SOAP");
        System.out.println("    op: ASSERT");
        System.out.println("    response: soap_response");
        System.out.println("    source: BODY");
        System.out.println("    path: \"//*[local-name()='result']/text()\"");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: \"OK\"");
        System.out.println();

        System.out.println("Example - MATCH_REFERENCE:");
        System.out.println();
        System.out.println("  - id: verify_soap_response");
        System.out.println("    type: SOAP");
        System.out.println("    op: ASSERT");
        System.out.println("    response: soap_response");
        System.out.println("    source: BODY");
        System.out.println("    action: MATCH_REFERENCE");
        System.out.println();

        System.out.println("ASSERT sources:");
        list(
                "STATUS",
                "BODY",
                "HEADERS"
        );

        System.out.println("ASSERT actions:");
        list(
                "EQUALS",
                "NOT_EQUALS",
                "CONTAINS",
                "IS_EMPTY",
                "IS_NOT_EMPTY",
                "MATCH_REFERENCE"
        );

        System.out.println("Important fields:");
        field("op", "EXEC, BUFFER or ASSERT");
        field("endpoint", "SOAP endpoint for command-based EXEC");
        field("command", "SOAP request definition");
        field("url", "Direct SOAP URL when direct mode is used");
        field("body", "Direct SOAP body when direct mode is used");
        field("parameters", "Values passed into request template");
        field("response", "Stored SOAP response or response reference");
        field("source", "STATUS, BODY or HEADERS");
        field("path", "XPath for BODY/HEADERS extraction or assertion");
        field("action", "Assertion action");
        field("expected", "Expected assertion value");
        field("name", "Target buffer name for BUFFER");
        field("upload", "Optional SOAP attachment upload configuration");
        field("download", "Optional SOAP attachment download configuration");
        field("unordered", "Optional MATCH_REFERENCE unordered rules");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  SOAP EXEC supports command-based request definitions.");
        System.out.println("  Direct url/body mode is also supported.");
        System.out.println("  BUFFER and ASSERT operate on a previously stored SOAP response.");
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
        field("parameters", "Values passed to the reusable block");
        field("condition", "Optional execution condition");

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

        operations(
                "EXEC    Execute a file operation",
                "BUFFER  Extract data from a previously read file",
                "ASSERT  Validate file existence or file content"
        );

        System.out.println("EXEC actions:");
        list(
                "READ",
                "WRITE",
                "APPEND",
                "COPY",
                "MOVE",
                "DELETE",
                "EXISTS"
        );

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
        list(
                "TEXT",
                "FILTER",
                "LINE",
                "LAST_LINE",
                "COUNT"
        );

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
        list(
                "EXISTS",
                "NOT_EXISTS",
                "CONTAINS",
                "NOT_CONTAINS",
                "EQUALS",
                "NOT_EQUALS",
                "COUNT"
        );

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
        field("response", "Stores READ/EXISTS result or references previous content");
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
        System.out.println("  BUFFER and content ASSERT use a previous READ response.");
        System.out.println("  EXEC EXISTS stores true/false in response.");
        System.out.println("  ASSERT EXISTS directly validates that the file exists.");
    }

    private static void describeCmd() {

        header(
                "CMD",
                "Execute operating-system commands, extract output and validate command results."
        );

        operations(
                "EXEC    Execute an operating-system command",
                "BUFFER  Extract text from a stored command result",
                "ASSERT  Validate exit code or command output"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: EXEC");
        System.out.println("    command: \"java --version\"");
        System.out.println("    response: java_version_raw");
        System.out.println();

        System.out.println("EXEC options:");
        field("expectedExitCode", "Expected process exit code");
        field("checkExitCode", "Enable/disable automatic exit-code validation");
        field("timeout", "Command timeout in milliseconds");
        field("outputFile", "Optional local file for command output");
        System.out.println();

        System.out.println("BUFFER actions:");
        list(
                "TEXT",
                "FILTER",
                "LINE",
                "LAST_LINE",
                "COUNT"
        );

        System.out.println("Example - BUFFER TEXT:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: BUFFER");
        System.out.println("    response: java_version_raw");
        System.out.println("    action: TEXT");
        System.out.println("    name: java_version");
        System.out.println();

        System.out.println("Example - BUFFER FILTER:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: BUFFER");
        System.out.println("    response: java_version_raw");
        System.out.println("    action: FILTER");
        System.out.println("    value: \"Runtime\"");
        System.out.println("    name: java_runtime");
        System.out.println();

        System.out.println("ASSERT actions:");
        list(
                "EXITCODE",
                "CONTAINS",
                "NOT_CONTAINS",
                "COUNT"
        );

        System.out.println("Example - ASSERT EXITCODE:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: ASSERT");
        System.out.println("    response: java_version_raw");
        System.out.println("    action: EXITCODE");
        System.out.println("    expected: 0");
        System.out.println();

        System.out.println("Example - ASSERT CONTAINS:");
        System.out.println();
        System.out.println("  - type: CMD");
        System.out.println("    op: ASSERT");
        System.out.println("    response: java_version_raw");
        System.out.println("    action: CONTAINS");
        System.out.println("    value: \"OpenJDK\"");
        System.out.println();

        System.out.println("Important fields:");
        field("op", "EXEC, BUFFER or ASSERT");
        field("command", "Command to execute");
        field("response", "Stored command result or response reference");
        field("action", "BUFFER or ASSERT action");
        field("value", "Search text or line index");
        field("expected", "Expected value, e.g. exit code or count");
        field("name", "Target buffer name");
        field("expectedExitCode", "Expected exit code for EXEC");
        field("checkExitCode", "Automatic exit-code validation");
        field("timeout", "Timeout in milliseconds");
        field("outputFile", "Optional file receiving command output");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  EXEC stores command output and exit code in 'response'.");
        System.out.println("  BUFFER reads from that stored response.");
        System.out.println("  ASSERT validates that stored response.");
    }

    private static void describeSql() {

        header(
                "SQL",
                "Execute SQL, extract result values and validate database results."
        );

        operations(
                "EXEC    Execute an SQL statement",
                "BUFFER  Extract one cell into an AGATE buffer",
                "ASSERT  Validate row count or result values"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: EXEC");
        System.out.println("    command: |");
        System.out.println("      SELECT ID, STATUS");
        System.out.println("      FROM TEST_TABLE");
        System.out.println("    response: sql_response");
        System.out.println();

        System.out.println("Example - EXEC with constraints:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: EXEC");
        System.out.println("    command: \"SELECT ID, STATUS FROM TEST_TABLE\"");
        System.out.println("    response: sql_response");
        System.out.println("    constraints:");
        System.out.println("      - column: STATUS");
        System.out.println("        action: EQUALS");
        System.out.println("        expected: \"ACTIVE\"");
        System.out.println();

        System.out.println("Example - BUFFER:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: BUFFER");
        System.out.println("    response: sql_response");
        System.out.println("    row: 0");
        System.out.println("    column: STATUS");
        System.out.println("    name: db_status");
        System.out.println();

        System.out.println("ASSERT actions:");
        list(
                "EQUALS",
                "NOT_EQUALS",
                "GREATER_THAN",
                "GREATER_THAN_OR_EQUAL",
                "LESS_THAN",
                "LESS_THAN_OR_EQUAL",
                "BETWEEN",
                "CONTAINS",
                "IS_NULL",
                "IS_NOT_NULL",
                "IS_EMPTY",
                "IS_NOT_EMPTY",
                "DATE_EQUALS",
                "ALL_MATCH",
                "ANY_MATCH"
        );

        System.out.println("Example - ASSERT ROW_COUNT:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: ASSERT");
        System.out.println("    response: sql_response");
        System.out.println("    source: ROW_COUNT");
        System.out.println("    action: GREATER_THAN");
        System.out.println("    expected: 0");
        System.out.println();

        System.out.println("Example - ASSERT cell value:");
        System.out.println();
        System.out.println("  - type: SQL");
        System.out.println("    op: ASSERT");
        System.out.println("    response: sql_response");
        System.out.println("    row: 0");
        System.out.println("    column: STATUS");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: \"ACTIVE\"");
        System.out.println();

        System.out.println("Important fields:");
        field("op", "EXEC, BUFFER or ASSERT");
        field("command", "SQL statement");
        field("response", "Stored SQL result or response reference");
        field("constraints", "Optional EXEC result constraints");
        field("source", "Use ROW_COUNT for row-count assertions");
        field("row", "Result row index, default depends on operation");
        field("column", "Column name or column index");
        field("action", "Assertion operation");
        field("expected", "Expected value or range");
        field("name", "Target buffer name for BUFFER");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  BUFFER extracts one result cell and stores it as {B[name]}.");
        System.out.println("  source: ROW_COUNT validates the number of rows instead of one cell.");
    }

    private static void describeBuffer() {

        header(
                "BUFFER",
                "Create runtime variables and validate buffered values."
        );

        operations(
                "EXEC    Store a value in the AGATE runtime buffer",
                "ASSERT  Validate a previously stored buffer value"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: BUFFER");
        System.out.println("    op: EXEC");
        System.out.println("    name: filename");
        System.out.println("    value: \"{R[psqlFilename]}\"");
        System.out.println();

        System.out.println("Example - ASSERT EQUALS:");
        System.out.println();
        System.out.println("  - type: BUFFER");
        System.out.println("    op: ASSERT");
        System.out.println("    name: filename");
        System.out.println("    action: EQUALS");
        System.out.println("    expected: \"delete.psql\"");
        System.out.println();

        System.out.println("Example - ASSERT IS_NOT_EMPTY:");
        System.out.println();
        System.out.println("  - type: BUFFER");
        System.out.println("    op: ASSERT");
        System.out.println("    name: filename");
        System.out.println("    action: IS_NOT_EMPTY");
        System.out.println();

        System.out.println("Important fields:");
        field("op", "EXEC or ASSERT");
        field("name", "Buffer variable name");
        field("value", "Value stored by EXEC");
        field("action", "Assertion action");
        field("expected", "Expected assertion value");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Usage:");
        System.out.println("  {B[filename]}");
    }

    private static void describeWait() {

        header(
                "WAIT",
                "Pause test execution for a configured duration."
        );

        System.out.println("Examples:");
        System.out.println();
        System.out.println("  - type: WAIT");
        System.out.println("    value: 1000");
        System.out.println();
        System.out.println("  - type: WAIT");
        System.out.println("    value: \"200ms\"");
        System.out.println();
        System.out.println("  - type: WAIT");
        System.out.println("    value: \"2s\"");
        System.out.println();
        System.out.println("  - type: WAIT");
        System.out.println("    value: \"{B[timeout]}\"");
        System.out.println();

        System.out.println("Important fields:");
        field("value", "Wait duration; numeric values are milliseconds");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  Examples:");
        System.out.println("    1000    -> 1000 ms");
        System.out.println("    \"200ms\" -> 200 ms");
        System.out.println("    \"2s\"    -> 2000 ms");
    }

    private static void describeOpenShift() {

        header(
                "OC",
                "Execute commands in OpenShift pods, transfer files, extract output and validate results."
        );

        operations(
                "EXEC    Execute a shell command inside a pod",
                "PUT     Copy a local file into a pod",
                "GET     Copy a file from a pod to the local AGATE system",
                "BUFFER  Extract text from a stored OC result",
                "ASSERT  Validate exit code or OC output"
        );

        System.out.println("Example - EXEC:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: EXEC");
        System.out.println("    pod: \"my-service\"");
        System.out.println("    command: \"java -version\"");
        System.out.println("    response: java_out");
        System.out.println();

        System.out.println("Example - PUT:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: PUT");
        System.out.println("    pod: \"my-service\"");
        System.out.println("    from: \"C:\\\\tmp\\\\test.txt\"");
        System.out.println("    to: \"/tmp/test.txt\"");
        System.out.println("    response: put_result");
        System.out.println();

        System.out.println("Example - GET:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: GET");
        System.out.println("    pod: \"my-service\"");
        System.out.println("    from: \"/tmp/result.txt\"");
        System.out.println("    to: \"C:\\\\tmp\\\\result.txt\"");
        System.out.println("    response: get_result");
        System.out.println();

        System.out.println("BUFFER actions:");
        list(
                "TEXT",
                "FILTER",
                "LINE",
                "LAST_LINE",
                "COUNT"
        );

        System.out.println("Example - BUFFER:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: BUFFER");
        System.out.println("    response: java_out");
        System.out.println("    action: FILTER");
        System.out.println("    value: \"Runtime\"");
        System.out.println("    name: runtime_line");
        System.out.println();

        System.out.println("ASSERT actions:");
        list(
                "EXITCODE",
                "CONTAINS",
                "NOT_CONTAINS",
                "EQUALS",
                "NOT_EQUALS",
                "COUNT"
        );

        System.out.println("Example - ASSERT:");
        System.out.println();
        System.out.println("  - type: OC");
        System.out.println("    op: ASSERT");
        System.out.println("    response: java_out");
        System.out.println("    action: CONTAINS");
        System.out.println("    value: \"OpenJDK\"");
        System.out.println();

        System.out.println("Important fields:");
        field("op", "EXEC, PUT, GET, BUFFER or ASSERT");
        field("pod", "Pod/service base name");
        field("namespace", "Optional namespace; otherwise environment default is used");
        field("command", "Shell command for EXEC");
        field("from", "Source path for PUT/GET");
        field("to", "Target path for PUT/GET");
        field("response", "Stored OC command/transfer result");
        field("action", "BUFFER or ASSERT action");
        field("value", "Search value or line index");
        field("expected", "Expected exit code/count/value");
        field("name", "Target buffer name");
        field("expectedExitCode", "Expected process/transfer exit code");
        field("checkExitCode", "Automatic exit-code validation");
        field("timeout", "Timeout in seconds for OC operations");
        field("outputFile", "Optional local output file for EXEC");
        field("condition", "Optional execution condition");

        System.out.println();
        System.out.println("Important:");
        System.out.println("  PUT: from = local path, to = remote pod path.");
        System.out.println("  GET: from = remote pod path, to = local path.");
        System.out.println("  PUT and GET use the same exit-code handling as EXEC.");
    }

    private static void describeLoop() {

        header(
                "LOOP",
                "Repeat nested AGATE steps using WHILE or DO_WHILE semantics."
        );

        System.out.println("Modes:");
        System.out.println();
        System.out.println("  WHILE     Check condition before executing the body");
        System.out.println("  DO_WHILE  Execute the body first, then check condition");
        System.out.println();

        System.out.println("Example - WHILE:");
        System.out.println();
        System.out.println("  - type: LOOP");
        System.out.println("    mode: WHILE");
        System.out.println("    condition: \"{B[counter]} < 5\"");
        System.out.println("    maxIterations: 10");
        System.out.println("    timeoutMs: 60000");
        System.out.println("    steps:");
        System.out.println("      - type: CMD");
        System.out.println("        op: EXEC");
        System.out.println("        command: \"echo loop\"");
        System.out.println();

        System.out.println("Example - DO_WHILE:");
        System.out.println();
        System.out.println("  - type: LOOP");
        System.out.println("    mode: DO_WHILE");
        System.out.println("    condition: \"{B[patient_registered_count]} == 0\"");
        System.out.println("    maxIterations: 30");
        System.out.println("    timeoutMs: 120000");
        System.out.println("    steps:");
        System.out.println("      - type: SQL");
        System.out.println("        op: EXEC");
        System.out.println("        command: \"SELECT COUNT(*) FROM DMP_BETREUUNGSVERHAELTNIS\"");
        System.out.println("        response: patient_registered_response");
        System.out.println();
        System.out.println("      - type: SQL");
        System.out.println("        op: BUFFER");
        System.out.println("        response: patient_registered_response");
        System.out.println("        row: 0");
        System.out.println("        column: 0");
        System.out.println("        name: patient_registered_count");
        System.out.println();
        System.out.println("      - type: WAIT");
        System.out.println("        value: 4000");
        System.out.println();

        System.out.println("Important fields:");
        field("mode", "WHILE or DO_WHILE");
        field("condition", "Loop continuation condition");
        field("maxIterations", "Maximum number of iterations");
        field("timeoutMs", "Maximum total loop duration in milliseconds");
        field("steps", "Nested AGATE steps executed as loop body");

        System.out.println();
        System.out.println("Execution order:");
        System.out.println("  WHILE    : condition -> body -> condition -> ...");
        System.out.println("  DO_WHILE : body -> condition -> body -> ...");
        System.out.println();
        System.out.println("Important:");
        System.out.println("  A DO_WHILE body executes at least once.");
        System.out.println("  Therefore a buffer created in the body can be used by the");
        System.out.println("  following DO_WHILE condition.");
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

    private static void operations(
            String... operations) {

        System.out.println("Operations:");
        System.out.println();

        for (String operation : operations) {
            System.out.println("  " + operation);
        }

        System.out.println();
    }

    private static void list(
            String... values) {

        for (String value : values) {
            System.out.println("  " + value);
        }

        System.out.println();
    }

    private static void field(
            String name,
            String description) {

        System.out.printf(
                "  %-16s %s%n",
                name,
                description
        );
    }
}
