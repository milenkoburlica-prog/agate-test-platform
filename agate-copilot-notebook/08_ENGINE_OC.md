# AGATE OC Engine

The OC Engine provides direct interaction with OpenShift clusters from AGATE tests.

It supports:

- executing shell commands inside running pods,
- automatic pod discovery by pod/service prefix,
- optional namespace selection,
- lazy OpenShift login,
- automatic exit-code validation,
- explicit expected non-zero exit codes,
- timeouts,
- storing command output locally,
- copying files from the AGATE host into a pod,
- copying files from a pod back to the AGATE host,
- extracting output via `BUFFER`,
- and validating output or exit codes via `ASSERT`.

> This document describes the OC Engine itself. General AGATE concepts such as buffers, placeholders, conditions, templates and re-instantiation are documented separately.

---

## 1. Quick Reference

The OC Engine supports:

```text
EXEC
PUT
GET
BUFFER
ASSERT
```

### EXEC

```yaml
- type: OC
  op: EXEC
  pod: "..."
  namespace: "..."            # Optional
  command: "..."
  response: "..."             # Optional
  expectedExitCode: 0         # Optional, default: 0
  checkExitCode: true         # Optional, default: true
  timeout: 30                 # Optional, seconds, default: 30
  outputFile: "..."           # Optional, local file on AGATE side
  condition: "..."            # Optional
```

### PUT

```yaml
- type: OC
  op: PUT
  pod: "..."
  namespace: "..."            # Optional
  from: "..."                 # Local path
  to: "..."                   # Remote path in pod
  response: "..."
  expectedExitCode: 0
  checkExitCode: true
  timeout: 30
  condition: "..."
```

### GET

```yaml
- type: OC
  op: GET
  pod: "..."
  namespace: "..."            # Optional
  from: "..."                 # Remote path in pod
  to: "..."                   # Local path
  response: "..."
  expectedExitCode: 0
  checkExitCode: true
  timeout: 30
  condition: "..."
```

### ASSERT

```yaml
- type: OC
  op: ASSERT
  response: "..."
  action: <EXITCODE|CONTAINS|NOT_CONTAINS|EQUALS|NOT_EQUALS|COUNT>
  value: "..."
  expected: "..."
  condition: "..."
```

### BUFFER

```yaml
- type: OC
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
| `EXEC` | Executes a shell command inside a running OpenShift pod. |
| `PUT` | Copies a local file from the AGATE host into a pod. |
| `GET` | Copies a file from a pod to the local AGATE host. |
| `BUFFER` | Extracts information from a stored OC result. |
| `ASSERT` | Validates exit code or output from a stored OC result. |

---

# 3. EXEC – Execute a Command in a Pod

`EXEC` runs a command inside a running pod.

Example:

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "java -version"
  response: java_out
```

The engine captures:

- exit code,
- stdout/stderr,
- timeout status,
- execution duration.

If `response` is defined, the result can later be processed by `BUFFER` and `ASSERT`.

## 3.1 EXEC Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `pod` | Yes | – | Base name or prefix of the target pod. |
| `command` | Yes | – | Shell command executed inside the pod. |
| `namespace` | No | configured environment | OpenShift namespace. |
| `response` | No | – | Name under which the result is stored. |
| `expectedExitCode` | No | `0` | Expected process exit code. |
| `checkExitCode` | No | `true` | Enables/disables automatic exit-code validation. |
| `timeout` | No | `30` | Maximum execution time in seconds. |
| `outputFile` | No | – | Local file where command output is additionally stored. |
| `condition` | No | – | Optional execution condition. |

---

# 4. Pod Discovery

The value in `pod` does not need to be the complete generated OpenShift pod name.

Example:

```yaml
pod: "ecsrvaum2bebatch"
```

A real pod may look like:

```text
ecsrvaum2bebatch-779444f45b-fxbt9
```

The OC Engine resolves a matching running pod at runtime.

This keeps dynamically generated Kubernetes/OpenShift suffixes out of test cases.

---

# 5. Namespace

The namespace may be specified explicitly:

```yaml
- type: OC
  op: EXEC
  namespace: "kvw-int-app"
  pod: "kvw-pst-pst"
  command: "pwd"
```

If no `namespace` is provided, AGATE uses the configured OpenShift environment, for example:

```text
{E[env.openShift.namespace]}
```

This allows the same YAML test to run against different stages without hard-coding the namespace.

---

# 6. Lazy Login

The OC Engine can detect a missing or expired OpenShift session and perform login using configured environment credentials.

Example configuration:

```properties
DEMOS.openShift.namespace=os_demo_system
DEMOS.openShift.username=os_user
DEMOS.openShift.password=os_pwd
```

As a result, tests normally do not need an explicit `oc login` step.

---

# 7. Automatic Exit-Code Validation

For `EXEC`, the default behavior is:

```yaml
expectedExitCode: 0
checkExitCode: true
```

Therefore:

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "java -version"
```

is functionally equivalent to:

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "java -version"
  expectedExitCode: 0
  checkExitCode: true
```

An unexpected non-zero exit code fails the step automatically.

## 7.1 Expected Non-Zero Exit Code

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "sh -c 'exit 7'"
  response: exit_result
  expectedExitCode: 7
```

The step succeeds if exit code `7` is actually returned.

## 7.2 Disable Automatic Exit-Code Validation

For negative tests:

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "ls /file/that/does/not/exist"
  response: failed_command
  checkExitCode: false
```

The exit code is still stored and can be asserted later:

```yaml
- type: OC
  op: ASSERT
  response: failed_command
  action: EXITCODE
  expected: 2
```

Two common strategies therefore exist:

```text
EXEC + expectedExitCode
```

for immediate validation, and:

```text
EXEC + checkExitCode: false
ASSERT EXITCODE
```

for negative or deferred validation.

---

# 8. Timeout

`timeout` defines the maximum duration of an OC operation.

The value is specified in seconds.

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "sleep 10"
  timeout: 1
```

If the command exceeds the timeout, the step fails.

Default:

```text
30 seconds
```

---

# 9. outputFile

For `OC / EXEC`, `outputFile` stores the stdout/stderr of the executed command in a local file on the AGATE machine.

```yaml
- type: OC
  op: EXEC
  pod: "my-service"
  command: "java -version"
  response: java_version
  outputFile: "output/java-version.txt"
```

Important distinction:

```text
OC command      -> executes inside the pod
command paths   -> refer to the pod filesystem
OC outputFile   -> local file on the AGATE host
```

`outputFile` does **not** copy a file out of the pod.

It only persists command output locally.

For actual file transfer, use `GET` or `PUT`.

## 9.1 Relative outputFile Paths

A relative path such as:

```yaml
outputFile: "output/oc-output.txt"
```

is resolved relative to the local working directory of the AGATE process.

It is not relative to the pod working directory.

---

# 10. PUT – Copy a Local File into a Pod

`PUT` copies a local file from the AGATE host into a pod.

```yaml
- type: OC
  op: PUT
  pod: "my-service"
  from: "C:\\tmp\\test.txt"
  to: "/tmp/test.txt"
  response: put_result
```

Direction:

```text
from -> local AGATE filesystem
to   -> remote pod filesystem
```

Internally, the transfer uses `oc cp`.

## 10.1 PUT Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `pod` | Yes | – | Target pod. |
| `namespace` | No | configured environment | Target namespace. |
| `from` | Yes | – | Local source path. |
| `to` | Yes | – | Destination path inside the pod. |
| `response` | No | – | Stored transfer result. |
| `expectedExitCode` | No | `0` | Expected transfer exit code. |
| `checkExitCode` | No | `true` | Automatic exit-code validation. |
| `timeout` | No | `30` | Timeout in seconds. |
| `condition` | No | – | Optional execution condition. |

---

# 11. GET – Copy a File from a Pod

`GET` copies a file from a pod to the local AGATE host.

```yaml
- type: OC
  op: GET
  pod: "my-service"
  from: "/tmp/test.txt"
  to: "C:\\tmp\\test.txt"
  response: get_result
```

Direction:

```text
from -> remote pod filesystem
to   -> local AGATE filesystem
```

`GET` also uses `oc cp`.

## 11.1 GET Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `pod` | Yes | – | Source pod. |
| `namespace` | No | configured environment | Namespace. |
| `from` | Yes | – | Source path inside the pod. |
| `to` | Yes | – | Local destination path. |
| `response` | No | – | Stored transfer result. |
| `expectedExitCode` | No | `0` | Expected transfer exit code. |
| `checkExitCode` | No | `true` | Automatic exit-code validation. |
| `timeout` | No | `30` | Timeout in seconds. |
| `condition` | No | – | Optional execution condition. |

---

# 12. Exit-Code Validation for PUT and GET

`PUT` and `GET` use the same exit-code model as `EXEC`.

Default:

```yaml
expectedExitCode: 0
checkExitCode: true
```

Example:

```yaml
- type: OC
  op: PUT
  pod: "my-service"
  from: "C:\\tmp\\test.txt"
  to: "/tmp/test.txt"
  response: put_result
```

The transfer already fails automatically if `oc cp` returns an unexpected non-zero exit code.

An additional explicit assertion is possible:

```yaml
- type: OC
  op: ASSERT
  response: put_result
  action: EXITCODE
  expected: 0
```

but is usually not required for a normal happy path.

---

# 13. Windows Paths with PUT and GET

Local Windows paths can be written normally in the DSL:

```yaml
from: "C:\\tmp\\test.txt"
```

or:

```yaml
to: "C:\\tmp\\test.txt"
```

The OC Engine handles the Windows-path requirements for `oc cp`.

The test does not need to build special `oc cp` syntax manually.

Remote paths remain standard Linux paths:

```text
/tmp/test.txt
/cloud/pst/output/report.txt
```

---

# 14. ASSERT

`OC / ASSERT` validates an exit code or textual output from a stored OC result.

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
| `EXITCODE` | Validates the exit code. | `expected` |
| `CONTAINS` | Checks whether text is present. | `value` |
| `NOT_CONTAINS` | Checks whether text is absent. | `value` |
| `EQUALS` | Exact comparison after trim. | `value` |
| `NOT_EQUALS` | Inequality comparison after trim. | `value` |
| `COUNT` | Counts occurrences of a search term. | `value`, `expected` |

## 14.1 EXITCODE

```yaml
- type: OC
  op: ASSERT
  response: java_out
  action: EXITCODE
  expected: 0
```

## 14.2 CONTAINS

```yaml
- type: OC
  op: ASSERT
  response: java_out
  action: CONTAINS
  value: "openjdk"
```

## 14.3 NOT_CONTAINS

```yaml
- type: OC
  op: ASSERT
  response: java_out
  action: NOT_CONTAINS
  value: "ERROR"
```

## 14.4 EQUALS / NOT_EQUALS

```yaml
- type: OC
  op: ASSERT
  response: pod_output
  action: EQUALS
  value: "/deployments"
```

## 14.5 COUNT

```yaml
- type: OC
  op: ASSERT
  response: java_out
  action: COUNT
  value: "OpenJDK"
  expected: 2
```

---

# 15. BUFFER

`OC / BUFFER` extracts information from a stored OC result and stores it under `name`.

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: TEXT
  name: java_version
```

The extracted value can later be referenced as:

```text
{B[java_version]}
```

Use a different `name` from the original `response` so that the original OC result, including its exit code, remains available.

---

## 15.1 BUFFER Actions

Supported actions:

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
| `FILTER` | Stores lines containing a search term. | `value` |
| `LINE` | Extracts a line from the beginning. `0` = first line. | `value` optional, default `0` |
| `LAST_LINE` | Extracts relative from the end. `0` = last line. | `value` optional, default `0` |
| `COUNT` | Counts occurrences of a search term. | `value` |

### TEXT

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: TEXT
  name: java_version
```

### FILTER

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: FILTER
  value: "OpenJDK"
  name: openjdk_lines
```

### LINE

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: LINE
  value: 0
  name: first_line
```

### LAST_LINE

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: LAST_LINE
  value: 0
  name: last_line
```

### COUNT

```yaml
- type: OC
  op: BUFFER
  response: java_out
  action: COUNT
  value: "OpenJDK"
  name: openjdk_count
```

---

# 16. Real Usage Examples

Only OC Engine steps are shown unless a local CMD step is necessary to demonstrate file transfer.

## 16.1 Execute a Command in a Pod

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "java -version"
  response: java_out

- type: OC
  op: ASSERT
  response: java_out
  action: EXITCODE
  expected: 0

- type: OC
  op: ASSERT
  response: java_out
  action: CONTAINS
  value: "openjdk"

- type: OC
  op: BUFFER
  response: java_out
  action: TEXT
  name: extracted_version
```

Pattern:

```text
OC EXEC
   |
   +--> ASSERT
   |
   +--> BUFFER
```

---

## 16.2 Expected Non-Zero Exit Code

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "sh -c 'exit 7'"
  response: expected_exit_7
  expectedExitCode: 7

- type: OC
  op: ASSERT
  response: expected_exit_7
  action: EXITCODE
  expected: 7
```

---

## 16.3 Negative Command with Deferred Validation

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "ls /file/that/does/not/exist"
  response: failed_command
  checkExitCode: false

- type: OC
  op: ASSERT
  response: failed_command
  action: NOT_EQUALS
  value: ""

- type: OC
  op: ASSERT
  response: failed_command
  action: EXITCODE
  expected: 2
```

---

## 16.4 Explicit Timeout

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "sleep 10"
  timeout: 1
```

---

## 16.5 Persist Command Output Locally

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "echo AGATE OC outputFile test"
  response: output_file_test
  outputFile: "output/oc-output.txt"

- type: OC
  op: ASSERT
  response: output_file_test
  action: CONTAINS
  value: "AGATE OC outputFile test"
```

---

## 16.6 PUT – Local File to Pod

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "rm -f /tmp/{B[filename]}"
  response: remote_cleanup

- type: OC
  op: PUT
  pod: "{B[service]}"
  from: "C:\\tmp\\{B[filename]}"
  to: "/tmp/{B[filename]}"
  response: put_res

- type: OC
  op: ASSERT
  response: put_res
  action: EXITCODE
  expected: 0
```

---

## 16.7 Verify Remote File Content

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "cat /tmp/{B[filename]}"
  response: remote_file_content

- type: OC
  op: ASSERT
  response: remote_file_content
  action: CONTAINS
  value: "foo"
```

---

## 16.8 GET – Pod File to Local AGATE Host

```yaml
- type: OC
  op: GET
  pod: "{B[service]}"
  from: "/tmp/{B[filename]}"
  to: "C:\\tmp\\{B[filename]}"
  response: get_res

- type: OC
  op: ASSERT
  response: get_res
  action: EXITCODE
  expected: 0
```

---

## 16.9 Pod Working Directory

```yaml
- type: OC
  op: EXEC
  pod: "{B[service]}"
  command: "pwd"
  response: pwd_out

- type: OC
  op: ASSERT
  response: pwd_out
  action: EXITCODE
  expected: 0

- type: OC
  op: BUFFER
  response: pwd_out
  action: TEXT
  name: pod_working_directory
```

---

# 17. Troubleshooting and Best Practices

## Pod Not Found

Check:

- is the namespace correct?
- is the `pod` prefix correct?
- does a matching pod exist?
- is the pod running?

Because AGATE resolves the actual pod dynamically, the test should normally use the stable service/pod prefix, not the generated full pod name.

---

## Unexpected Exit Code

Default:

```yaml
checkExitCode: true
expectedExitCode: 0
```

If another exit code is expected:

```yaml
expectedExitCode: 2
```

or, for a negative test:

```yaml
checkExitCode: false
```

followed by an explicit `ASSERT EXITCODE`.

---

## Timeout

Long-running commands can use an explicit timeout:

```yaml
timeout: 120
```

Only increase it when the operation genuinely requires more time.

---

## PUT / GET Fails

Check:

- local path,
- remote path,
- namespace,
- pod,
- read/write permissions,
- target directory existence.

The `oc cp` exit code is validated automatically by default.

---

## Do Not Confuse outputFile and GET

```text
outputFile
    = persists stdout/stderr of an OC EXEC locally

GET
    = copies an actual file from the pod to the AGATE host
```

---

## Prefer Pod Prefixes Over Generated Pod Names

Good:

```yaml
pod: "ecsrvaum2bebatch"
```

Avoid hard-coding:

```text
ecsrvaum2bebatch-779444f45b-fxbt9
```

Generated pod suffixes change across deployments.

---

# 18. AI Guidance

When generating or analyzing OC Engine steps:

1. **Use `EXEC` for commands inside pods.**
2. **Use `PUT` for local → pod file transfer.**
3. **Use `GET` for pod → local file transfer.**
4. **Do not use `outputFile` as a substitute for GET.**
5. **Prefer the stable pod/service prefix instead of a generated full pod name.**
6. **Use the configured namespace when no explicit namespace is required.**
7. **Assume default exit-code checking unless explicitly disabled.**
8. **Use `response` when later ASSERT/BUFFER processing is needed.**
9. **Treat local and remote paths differently.**
   - Windows/local paths belong to the AGATE host.
   - Linux paths belong to the pod.
10. **Do not invent explicit `oc login` steps when lazy login is available through environment configuration.**
11. **Do not redefine shared AGATE concepts here.**
   Buffers, placeholders, conditions, templates and re-instantiation are documented separately.

---

# 19. Short Reference

```text
OC
├── EXEC
│   ├── pod
│   ├── namespace
│   ├── command
│   ├── response
│   ├── expectedExitCode   [default: 0]
│   ├── checkExitCode      [default: true]
│   ├── timeout            [default: 30s]
│   └── outputFile
│
├── PUT
│   ├── pod
│   ├── namespace
│   ├── from               [local]
│   ├── to                 [remote]
│   ├── response
│   ├── expectedExitCode   [default: 0]
│   ├── checkExitCode      [default: true]
│   └── timeout            [default: 30s]
│
├── GET
│   ├── pod
│   ├── namespace
│   ├── from               [remote]
│   ├── to                 [local]
│   ├── response
│   ├── expectedExitCode   [default: 0]
│   ├── checkExitCode      [default: true]
│   └── timeout            [default: 30s]
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
