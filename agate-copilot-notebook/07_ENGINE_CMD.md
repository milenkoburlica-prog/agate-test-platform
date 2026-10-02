# AGATE CMD Engine

The CMD Engine executes local operating-system commands, CLI programs and scripts as part of an AGATE test.

It supports:

- command execution,
- automatic exit-code validation,
- explicit expected non-zero exit codes,
- execution timeouts,
- storing command output under a response,
- optionally writing output to a local file,
- extracting parts of command output into AGATE buffers,
- validating command output,
- and conditional command execution.

> This document describes the CMD Engine itself. General AGATE concepts such as buffers, placeholders, templates, conditions and re-instantiation are documented separately.

---

## 1. Quick Reference

The CMD Engine supports three operations:

```text
EXEC
ASSERT
BUFFER
```

### EXEC

```yaml
- type: CMD
  op: EXEC
  command: "..."
  response: "..."             # Optional: stored CommandResult
  expectedExitCode: 0         # Optional, default: 0
  checkExitCode: true         # Optional, default: true
  timeout: 30                 # Optional, seconds, default: 30
  outputFile: "..."           # Optional: additionally persist output
  condition: "..."            # Optional
```

### ASSERT

```yaml
- type: CMD
  op: ASSERT
  response: "..."
  action: <EXITCODE|CONTAINS|NOT_CONTAINS|EQUALS|NOT_EQUALS|COUNT>
  value: "..."
  expected: "..."
  condition: "..."
```

### BUFFER

```yaml
- type: CMD
  op: BUFFER
  response: "..."
  action: <TEXT|FILTER|LINE|LAST_LINE|COUNT>
  value: "..."
  name: "..."
  condition: "..."
```

---

## 2. Operations

| Operation | Purpose |
|---|---|
| `EXEC` | Executes a local system command, CLI program or script. |
| `ASSERT` | Validates exit code or output from a stored CMD result. |
| `BUFFER` | Extracts data from a stored CMD result and stores it as an AGATE buffer. |

---

# 3. EXEC

`EXEC` runs a local command and captures execution information.

The engine records:

- exit code,
- stdout/stderr,
- timeout status,
- execution duration.

If `response` is defined, the command result can later be used by `BUFFER` and `ASSERT`.

## 3.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `command` | Yes | – | Windows, CLI or script command to execute. |
| `response` | No | – | Name under which the command result is stored. |
| `expectedExitCode` | No | `0` | Expected process exit code. |
| `checkExitCode` | No | `true` | Enables/disables automatic exit-code validation. |
| `timeout` | No | `30` | Maximum execution time in seconds. |
| `outputFile` | No | – | Additionally stores command output in a local file. |
| `condition` | No | – | Optional execution condition. |

## 3.2 Basic example

```yaml
- type: CMD
  op: EXEC
  command: "java --version"
  response: java_version
```

---

# 4. Automatic Exit-Code Validation

By default, AGATE behaves as if the following values were configured:

```yaml
expectedExitCode: 0
checkExitCode: true
```

Therefore:

```yaml
- type: CMD
  op: EXEC
  command: "java --version"
```

is functionally equivalent to:

```yaml
- type: CMD
  op: EXEC
  command: "java --version"
  expectedExitCode: 0
  checkExitCode: true
```

If the command returns another exit code, the CMD step fails automatically.

## 4.1 Expected non-zero exit code

```yaml
- type: CMD
  op: EXEC
  command: "exit /b 7"
  response: exit_result
  expectedExitCode: 7
```

The step succeeds when the command returns exit code `7`.

## 4.2 Disable automatic exit-code validation

```yaml
- type: CMD
  op: EXEC
  command: "exit /b 5"
  response: failed_command
  checkExitCode: false
```

The exit code can then be validated explicitly:

```yaml
- type: CMD
  op: ASSERT
  response: failed_command
  action: EXITCODE
  expected: 5
```

Two common patterns therefore exist:

```text
EXEC + expectedExitCode
```

for direct validation, and:

```text
EXEC + checkExitCode: false
ASSERT EXITCODE
```

for negative tests or deferred validation.

> Older or migrated SVC tests may contain `ignoreExitCode`. The current CMD documentation uses `checkExitCode`. Unless the current engine implementation explicitly confirms an alias, AI-generated tests should use `checkExitCode`.

---

# 5. Timeout

The maximum execution duration is defined with `timeout`.

The value is specified in seconds.

```yaml
- type: CMD
  op: EXEC
  command: "ping 127.0.0.1 -n 10 > nul"
  timeout: 1
```

If execution exceeds the timeout, the CMD step fails.

Default:

```text
30 seconds
```

---

# 6. outputFile

`outputFile` stores the command output in a local file in addition to keeping the execution result available through `response`.

```yaml
- type: CMD
  op: EXEC
  command: "echo AGATE CMD outputFile test"
  response: output_file_test
  outputFile: "output/cmd-output.txt"
```

The output can still be validated through `response`:

```yaml
- type: CMD
  op: ASSERT
  response: output_file_test
  action: CONTAINS
  value: "AGATE CMD outputFile test"
```

`outputFile` is additional persistence. It does not replace `response`.

---

# 7. Working Directory (WORKDIR)

The effective working directory depends on the executed command.

## 7.1 Absolute script or program path

```yaml
- type: CMD
  op: EXEC
  command: "C:\\Services\\App\\run.bat"
```

The parent directory becomes the effective working directory:

```text
C:\Services\App
```

This allows scripts to resolve relative configuration files, libraries and local resources from their own directory.

## 7.2 System commands and relative commands

For commands such as:

```yaml
command: "java --version"
```

or:

```yaml
command: "dir"
```

no special working directory is derived. The command runs in the working directory of the AGATE process.

---

# 8. Relative outputFile Paths

A relative `outputFile` path is resolved relative to the effective working directory of the CMD step.

```yaml
- type: CMD
  op: EXEC
  command: "C:\\Services\\App\\run.bat"
  outputFile: "output/result.txt"
```

Result:

```text
C:\Services\App\output\result.txt
```

For a system command:

```yaml
- type: CMD
  op: EXEC
  command: "java --version"
  outputFile: "output/java-version.txt"
```

the file is written relative to the AGATE process working directory.

---

# 9. ASSERT

`CMD / ASSERT` validates the exit code or textual output of a previously stored command result.

Supported actions:

```text
EXITCODE
CONTAINS
NOT_CONTAINS
EQUALS
NOT_EQUALS
COUNT
```

| Action | Description | Required fields |
|---|---|---|
| `EXITCODE` | Validates the process exit code. | `expected` |
| `CONTAINS` | Checks whether output contains text. | `value` |
| `NOT_CONTAINS` | Checks whether output does not contain text. | `value` |
| `EQUALS` | Exact comparison after trimming. | `value` |
| `NOT_EQUALS` | Checks for inequality after trimming. | `value` |
| `COUNT` | Counts occurrences of a search term. | `value`, `expected` |

## 9.1 EXITCODE

```yaml
- type: CMD
  op: ASSERT
  response: java_version
  action: EXITCODE
  expected: 0
```

## 9.2 CONTAINS

```yaml
- type: CMD
  op: ASSERT
  response: java_version
  action: CONTAINS
  value: "OpenJDK"
```

## 9.3 NOT_CONTAINS

```yaml
- type: CMD
  op: ASSERT
  response: java_version
  action: NOT_CONTAINS
  value: "ERROR"
```

## 9.4 EQUALS / NOT_EQUALS

```yaml
- type: CMD
  op: ASSERT
  response: command_result
  action: EQUALS
  value: "SUCCESS"
```

## 9.5 COUNT

```yaml
- type: CMD
  op: ASSERT
  response: java_version
  action: COUNT
  value: "OpenJDK"
  expected: 2
```

---

# 10. BUFFER

`CMD / BUFFER` extracts information from the output of a previous `CMD / EXEC`.

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: TEXT
  name: java_version_text
```

The generated value can later be referenced as:

```text
{B[java_version_text]}
```

> Use a different `name` from the original `response` so the stored command result, including its exit code, remains intact.

## 10.1 BUFFER Actions

```text
TEXT
FILTER
LINE
LAST_LINE
COUNT
```

| Action | Description | Parameters |
|---|---|---|
| `TEXT` | Stores the complete trimmed output. | – |
| `FILTER` | Stores only lines containing a search term. | `value` |
| `LINE` | Extracts a line from the beginning. `0` = first line. | `value` optional, default `0` |
| `LAST_LINE` | Extracts relative from the end. `0` = last line. | `value` optional, default `0` |
| `COUNT` | Counts occurrences of a search term. | `value` |

## 10.2 TEXT

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: TEXT
  name: complete_output
```

## 10.3 FILTER

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: FILTER
  value: "Runtime"
  name: runtime_lines
```

## 10.4 LINE

Default first line:

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: LINE
  name: first_line
```

Explicit line index:

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: LINE
  value: 1
  name: second_line
```

## 10.5 LAST_LINE

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: LAST_LINE
  name: last_line
```

One line before the last:

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: LAST_LINE
  value: 1
  name: previous_line
```

## 10.6 COUNT

```yaml
- type: CMD
  op: BUFFER
  response: java_version
  action: COUNT
  value: "OpenJDK"
  name: openjdk_count
```

---

# 11. Using CMD Buffer Values

```yaml
- type: CMD
  op: EXEC
  command: "echo %OS%"
  response: os_result

- type: CMD
  op: BUFFER
  response: os_result
  action: TEXT
  name: os_version

- type: CMD
  op: EXEC
  condition: "'{B[os_version]}' == 'Windows_NT'"
  command: "ver"
  response: windows_version
```

Pattern:

```text
CMD EXEC
   |
   v
CMD BUFFER
   |
   v
AGATE Buffer
   |
   v
condition / later step
```

---

# 12. Complete Demo

```yaml
testCases:
  - id: TC_CMD_Engine_Demo
    description: CMD Engine Demo
    stage: "*"
    priority: HIGH

    steps:
      - type: CMD
        op: EXEC
        command: "java --version"
        response: java_version_raw

      - type: CMD
        op: BUFFER
        response: java_version_raw
        action: TEXT
        name: java_version

      - type: CMD
        op: BUFFER
        response: java_version_raw
        action: FILTER
        value: "Runtime"
        name: java_runtime_version

      - type: CMD
        op: BUFFER
        response: java_version_raw
        action: LINE
        name: java_first_line

      - type: CMD
        op: BUFFER
        response: java_version_raw
        action: LAST_LINE
        name: java_last_line

      - type: CMD
        op: ASSERT
        response: java_version_raw
        action: EXITCODE
        expected: 0

      - type: CMD
        op: ASSERT
        response: java_version_raw
        action: CONTAINS
        value: "OpenJDK"

      - type: CMD
        op: ASSERT
        response: java_version_raw
        action: COUNT
        value: "OpenJDK"
        expected: 1

      - type: CMD
        op: EXEC
        command: "exit /b 7"
        response: expected_exit_7
        expectedExitCode: 7

      - type: CMD
        op: EXEC
        command: "exit /b 5"
        response: unchecked_exit_5
        checkExitCode: false

      - type: CMD
        op: ASSERT
        response: unchecked_exit_5
        action: EXITCODE
        expected: 5

      - type: CMD
        op: EXEC
        command: "echo timeout test"
        response: timeout_test
        timeout: 5

      - type: CMD
        op: EXEC
        command: "echo AGATE CMD outputFile test"
        response: output_file_test
        outputFile: "output/cmd-output.txt"

      - type: CMD
        op: ASSERT
        response: output_file_test
        action: CONTAINS
        value: "AGATE CMD outputFile test"
```

---

# 13. Real SVC Usage Patterns

Only CMD Engine steps are shown below.

## 13.1 Execute a Windows/SSH helper command

```yaml
- type: CMD
  op: EXEC
  condition: "('true' == 'true') AND ('{B[L_UmgebungsTyp]}'=='legacy')"
  command: |
    cd /d "{B[SSHLogDir]}" && "{B[G_UMGEBUNGSSCRIPT_SCRIPT_07]}" {B[TOOL_PATH_PLINK]} {B[SSHHostUser]} "{B[SSHHostPassword]}" {B[SSHHostName]} {B[L_Command]} {B[SSHLogDir]}\{B[SSHLogFile]}
  response: cmd_response
```

This demonstrates:

- conditional execution,
- extensive use of AGATE buffers,
- command composition,
- execution of an existing helper script,
- explicit directory switching inside the command.

> Older migrated variants may contain `ignoreExitCode: false`. The canonical current documentation uses `checkExitCode`.

## 13.2 PowerShell command with outputFile

```yaml
- type: CMD
  op: EXEC
  condition: "(('true' == 'true') AND ('{B[L_UmgebungsTyp]}'=='legacy')) AND (('{B[L_Option]}' == 'verify-exact') || ('{B[L_Option]}' == 'buffer'))"
  command: |
    powershell -Command "(Get-Content -Path {B[SSHLogDir]}\{B[SSHLogFile]} -Raw | Measure-Object -Line).Lines"
  response: cmd_response
  outputFile: '{B[SSHLogDir]}\{B[SSHLogFile]}_Count'
  timeout: 3000
```

## 13.3 Transform file content with PowerShell

```yaml
- type: CMD
  op: EXEC
  condition: "(('true' == 'true') AND ('{B[L_UmgebungsTyp]}'=='legacy')) AND (('{B[L_Option]}' == 'verify-exact') || ('{B[L_Option]}' == 'buffer'))"
  command: |
    powershell -Command "(Get-Content -Encoding UTF8 -Path {B[SSHLogDir]}\{B[SSHLogFile]}_Count) | Out-File -Encoding UTF8 -FilePath {B[SSHLogDir]}\{B[SSHLogFile]}_Count -NoNewline"
  response: cmd_response
  timeout: 3000
```

## 13.4 Extract a subset of file lines

```yaml
- type: CMD
  op: EXEC
  condition: "('{B[SSHFILE_LINE_COUNTER]}' != '0') AND ('{B[SSHFILE_LINE_COUNTER]}' != '1')"
  command: |
    powershell -Command "Get-Content -Encoding UTF8 -Path {B[SSHLogDir]}\{B[SSHLogFile]} | Select-Object -First ([Math]::Max(0, (Get-Content -Encoding UTF8 -Path {B[SSHLogDir]}\{B[SSHLogFile]}).Length - 1)) | Out-File -Encoding UTF8 -FilePath {B[SSHLogDir]}\{B[SSHLogFile]}_1"
  response: cmd_response
  timeout: 3000
```

These examples show a key AGATE use case: orchestrating existing scripts and CLI-based workflows without reimplementing them in Java.

---

# 14. Troubleshooting and Best Practices

## Unexpected exit code

Because `checkExitCode: true` is the default, an unexpected non-zero exit code fails the step.

Use:

```yaml
expectedExitCode: 7
```

when that non-zero code is expected.

For negative tests:

```yaml
checkExitCode: false
```

and validate the exit code explicitly if needed.

## ASSERT fails unexpectedly

CLI programs often return additional whitespace, line breaks, version information or several output lines.

Use:

```text
FILTER
LINE
LAST_LINE
```

to isolate relevant output before further processing.

## Condition is unexpectedly skipped

For string comparisons, quote resolved values:

```yaml
condition: "'{B[my_var]}' == 'value'"
```

## Large command output

For large outputs:

- extract only relevant data with `FILTER`, `LINE`, `LAST_LINE` or `COUNT`,
- or persist the complete output with `outputFile`.

## outputFile does not replace response

Use `response` when later AGATE steps need the command result.

Use `outputFile` when the output additionally needs to exist as a local file.

---

# 15. AI Guidance

When generating or analyzing CMD Engine steps:

1. **Use only documented fields.** Prefer `checkExitCode` / `expectedExitCode` over legacy or migration-specific fields unless current implementation confirms them.
2. **Assume automatic exit-code checking by default.** Default: `expectedExitCode: 0`, `checkExitCode: true`.
3. **Use `response` when output or exit code is needed later.**
4. **Use `outputFile` only for additional file persistence.**
5. **Do not confuse CMD BUFFER with the generic BUFFER Engine.**
6. **Preserve PowerShell pipelines and complex commands as one command when that is the intended executable expression.**
7. **Do not invent OS-specific behavior.** Current examples and documentation are Windows-oriented.
8. **Understand AGATE data flow.** CMD output can be converted into buffers and consumed by later engines or conditions.
9. **Do not redefine shared AGATE concepts here.** Buffers, placeholders, conditions, templates and re-instantiation are documented separately.

---

# 16. Short Reference

```text
CMD
├── EXEC
│   ├── command
│   ├── response
│   ├── expectedExitCode   [default: 0]
│   ├── checkExitCode      [default: true]
│   ├── timeout            [default: 30s]
│   ├── outputFile
│   └── condition
│
├── BUFFER
│   ├── TEXT
│   ├── FILTER
│   ├── LINE
│   ├── LAST_LINE
│   └── COUNT
│
└── ASSERT
    ├── EXITCODE
    ├── CONTAINS
    ├── NOT_CONTAINS
    ├── EQUALS
    ├── NOT_EQUALS
    └── COUNT
```

---

# OS-Specific Command Guidance

The CMD Engine defines how AGATE executes a command. The command text itself belongs to the local operating-system/runtime environment.

These are different concerns:

```text
AGATE syntax
    -> type: CMD
    -> op: EXEC
    -> command: '...'

Operating-system command
    -> type
    -> dir
    -> copy
    -> cat
    -> ls
    -> rm
```

## Known Operating System

If the target operating system is known from the requirement or project documentation, generate commands appropriate for that operating system.

## Unknown Operating System

If the operating system is not specified:

- do not claim an OS-specific command is portable,
- if using a Windows-specific command such as `type`, state the Windows runtime as an environment assumption,
- if using a Unix/Linux-specific command such as `cat`, state the Unix/Linux runtime as an environment assumption.

Example:

```yaml
- type: CMD
  op: EXEC
  command: 'type "output\user-{B[user_id]}.txt"'
  response: cmd_file_output
```

Valid assumption:

```text
The local CMD runtime is Windows and supports the `type` command.
```

Do not describe `type` as an AGATE command or AGATE feature.

## AI Rule

1. Keep AGATE CMD syntax separate from shell/OS syntax.
2. Do not invent cross-platform behavior.
3. Use the known runtime when available.
4. If runtime is unknown and an OS-specific command is necessary, make that one concise environment assumption.
5. Do not add an assumption when the operating system is already provided by the requirement or project documentation.
