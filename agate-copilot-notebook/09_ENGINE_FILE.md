# AGATE FILE Engine


# 0. Critical Rules for AI Assistants

The following rules are explicitly documented and MUST NOT be treated as assumptions.

## FILE ASSERT is fully documented

Filesystem assertions:

```yaml
- type: FILE
  op: ASSERT
  action: EXISTS
  path: 'output/report.txt'
```

```yaml
- type: FILE
  op: ASSERT
  action: NOT_EXISTS
  path: 'output/report.txt'
```

Content assertions operate on the `response` of a previous `READ`:

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: CONTAINS
  value: 'CHANGED'
```

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: COUNT
  value: 'CHANGED'
  expected: 2
```

Therefore:

```text
EXISTS / NOT_EXISTS
    -> use path

CONTAINS / NOT_CONTAINS / EQUALS / NOT_EQUALS / COUNT
    -> use response from a previous READ
```

These are documented FILE Engine rules, not assumptions.

## FILE BUFFER is fully documented

`FILE / BUFFER` operates on the `response` of a previous `FILE / EXEC / READ`.

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: 'output/report.txt'
  response: report_raw

- type: FILE
  op: BUFFER
  response: report_raw
  action: TEXT
  name: report_text
```

Supported FILE BUFFER actions are:

```text
TEXT
FILTER
LINE
LAST_LINE
COUNT
```

Example:

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: FILTER
  value: 'ERROR'
  name: error_lines
```

Therefore an AI assistant MUST NOT state that FILE BUFFER syntax or examples are missing from this documentation.

## Default encoding is documented

For `READ`, `WRITE` and `APPEND`:

```text
encoding default = UTF-8
```

This is documented behavior and MUST NOT be listed as an assumption.

## Dynamic placeholders in FILE fields are documented

Standard AGATE placeholders may be used in FILE paths and content.

Example:

```yaml
path: '{B[baseDir]}/{B[filename]}'
```

```yaml
source: '{E[env.ks.testdata.basedir]}\input\file.txt'
```

This is documented AGATE behavior and MUST NOT be listed as an assumption.

## AI rule: distinguish missing business data from documented engine behavior

A valid assumption is something like:

```text
The database table USERS exists and contains ID and STATUS.
```

because this is business/environment information not defined by the engine documentation.

The following are NOT valid assumptions when this document is available:

```text
FILE ASSERT fields are unknown.
FILE BUFFER examples are missing.
UTF-8 default is unknown.
FILE does not document buffer placeholders.
```


The FILE Engine performs file operations on the **local filesystem of the AGATE process**.

It supports:

- reading files,
- writing and overwriting files,
- appending text,
- copying files,
- moving or renaming files,
- deleting files,
- checking file existence,
- extracting file content through `BUFFER`,
- and validating file existence or content through `ASSERT`.

> The FILE Engine always works on the local filesystem visible to the AGATE process. Files inside OpenShift pods belong to the OC Engine.

> This document describes the FILE Engine itself. General AGATE concepts such as buffers, placeholders, conditions, templates and re-instantiation are documented separately.

---

## 1. Quick Reference

The FILE Engine supports three operation modes:

```text
EXEC
BUFFER
ASSERT
```

### READ

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: "..."
  response: "..."
  encoding: "UTF-8"          # Optional, default: UTF-8
  condition: "..."           # Optional
```

### WRITE

```yaml
- type: FILE
  op: EXEC
  action: WRITE
  path: "..."
  text: "..."
  encoding: "UTF-8"          # Optional, default: UTF-8
  overwrite: true            # Optional
  condition: "..."
```

### APPEND

```yaml
- type: FILE
  op: EXEC
  action: APPEND
  path: "..."
  text: "..."
  encoding: "UTF-8"          # Optional, default: UTF-8
  newline: false             # Optional, default: false
  condition: "..."
```

### COPY

```yaml
- type: FILE
  op: EXEC
  action: COPY
  source: "..."
  target: "..."
  overwrite: false           # Optional, default: false
  condition: "..."
```

### MOVE

```yaml
- type: FILE
  op: EXEC
  action: MOVE
  source: "..."
  target: "..."
  overwrite: false           # Optional, default: false
  condition: "..."
```

### DELETE

```yaml
- type: FILE
  op: EXEC
  action: DELETE
  path: "..."
  missingOk: false           # Optional, default: false
  condition: "..."
```

### EXISTS

```yaml
- type: FILE
  op: EXEC
  action: EXISTS
  path: "..."
  response: "..."
  condition: "..."
```

### BUFFER

```yaml
- type: FILE
  op: BUFFER
  response: "..."
  action: <TEXT|FILTER|LINE|LAST_LINE|COUNT>
  value: "..."
  name: "..."
  condition: "..."
```

### ASSERT

```yaml
- type: FILE
  op: ASSERT
  action: <EXISTS|NOT_EXISTS|CONTAINS|NOT_CONTAINS|EQUALS|NOT_EQUALS|COUNT>
  path: "..."
  response: "..."
  value: "..."
  expected: "..."
  condition: "..."
```

---

## 2. Operation Model

The FILE Engine uses:

```text
type   = FILE
op     = execution mode
action = concrete file operation or validation
```

Example:

```yaml
- type: FILE
  op: EXEC
  action: READ
```

Supported `op` values:

| Operation | Purpose |
|---|---|
| `EXEC` | Performs a file operation. |
| `BUFFER` | Extracts information from previously read file content. |
| `ASSERT` | Validates file existence or previously read content. |

---

# 3. EXEC Actions

Supported `EXEC` actions:

```text
READ
WRITE
APPEND
COPY
MOVE
DELETE
EXISTS
```

---

# 4. READ

`READ` reads the complete content of a local file and stores it under `response`.

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: "output/report.txt"
  response: report_raw
```

The stored content can later be used by:

```text
FILE / BUFFER
FILE / ASSERT
```

## 4.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `path` | Yes | – | File to read. |
| `response` | Yes | – | Name under which file content is stored. |
| `encoding` | No | `UTF-8` | Character encoding. |
| `condition` | No | – | Optional execution condition. |

Example with explicit encoding:

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: "output/report.txt"
  response: report_raw
  encoding: UTF-8
```

---

# 5. WRITE

`WRITE` writes text to a local file.

```yaml
- type: FILE
  op: EXEC
  action: WRITE
  path: "output/report.txt"
  text: "AGATE FILE Engine"
```

Multi-line content can be written directly through YAML:

```yaml
- type: FILE
  op: EXEC
  action: WRITE
  path: "output/report.txt"
  text: |
    AGATE FILE Engine
    Line 2
    Line 3
```

Dynamic AGATE values may also be used:

```yaml
- type: FILE
  op: EXEC
  action: WRITE
  path: "output/report.txt"
  text: "{B[report_content]}"
  overwrite: true
```

## 5.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `path` | Yes | – | Target file. |
| `text` | Yes | – | Content to write. |
| `encoding` | No | `UTF-8` | Character encoding. |
| `overwrite` | No | Engine default | Controls replacement of an existing file. |
| `condition` | No | – | Optional execution condition. |

> The provided FILE documentation does not define a fixed literal default value for `WRITE / overwrite`; it documents it as the engine default. Do not invent a value in generated tests if overwrite behavior matters—set it explicitly.

---

# 6. APPEND

`APPEND` adds text to an existing file.

```yaml
- type: FILE
  op: EXEC
  action: APPEND
  path: "output/report.txt"
  text: "additional line"
```

By default, no extra line break is added:

```text
newline = false
```

To append a newline after the text:

```yaml
- type: FILE
  op: EXEC
  action: APPEND
  path: "output/report.txt"
  text: "additional line"
  newline: true
```

## 6.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `path` | Yes | – | Target file. |
| `text` | Yes | – | Text to append. |
| `encoding` | No | `UTF-8` | Character encoding. |
| `newline` | No | `false` | Append a line break after the text. |
| `condition` | No | – | Optional execution condition. |

---

# 7. COPY

`COPY` copies one local file to another local path.

```yaml
- type: FILE
  op: EXEC
  action: COPY
  source: "output/report.txt"
  target: "backup/report.txt"
```

To replace an existing destination:

```yaml
- type: FILE
  op: EXEC
  action: COPY
  source: "output/report.txt"
  target: "backup/report.txt"
  overwrite: true
```

## 7.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `source` | Yes | – | Source file. |
| `target` | Yes | – | Destination file. |
| `overwrite` | No | `false` | Replace an existing destination. |
| `condition` | No | – | Optional execution condition. |

---

# 8. MOVE

`MOVE` moves a local file from `source` to `target`.

```yaml
- type: FILE
  op: EXEC
  action: MOVE
  source: "output/report.txt"
  target: "archive/report.txt"
```

`MOVE` can also be used for renaming:

```yaml
- type: FILE
  op: EXEC
  action: MOVE
  source: "output/report.txt"
  target: "output/report-old.txt"
```

No separate `RENAME` action is required.

## 8.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `source` | Yes | – | Source file. |
| `target` | Yes | – | Destination path. |
| `overwrite` | No | `false` | Replace an existing target. |
| `condition` | No | – | Optional execution condition. |

---

# 9. DELETE

`DELETE` removes a local file.

```yaml
- type: FILE
  op: EXEC
  action: DELETE
  path: "output/report.txt"
```

By default, the step fails if the file does not exist:

```text
missingOk = false
```

For cleanup steps where an already missing file is acceptable:

```yaml
- type: FILE
  op: EXEC
  action: DELETE
  path: "output/report.txt"
  missingOk: true
```

## 9.1 Parameters

| Parameter | Required | Default | Description |
|---|---:|---:|---|
| `path` | Yes | – | File to delete. |
| `missingOk` | No | `false` | Treat missing file as success. |
| `condition` | No | – | Optional execution condition. |

`missingOk: true` is especially useful for idempotent cleanup.

---

# 10. EXEC EXISTS

`EXEC / EXISTS` checks whether a local file exists and stores the result as a Boolean under `response`.

```yaml
- type: FILE
  op: EXEC
  action: EXISTS
  path: "output/report.txt"
  response: report_exists
```

The result can later be used as:

```text
{B[report_exists]}
```

For example:

```yaml
- type: FILE
  op: EXEC
  action: EXISTS
  path: "output/report.txt"
  response: report_exists

- type: FILE
  condition: "'{B[report_exists]}' == 'true'"
  op: EXEC
  action: READ
  path: "output/report.txt"
  response: report
```

Important distinction:

```text
EXEC EXISTS
    -> returns information (true/false)

ASSERT EXISTS
    -> directly validates an expectation
```

---

# 11. Relative and Absolute Paths

The FILE Engine works on the local filesystem of the AGATE process.

Absolute paths are used directly:

```yaml
path: "C:\\tmp\\report.txt"
```

Relative paths are resolved relative to the working directory of the running AGATE process:

```yaml
path: "output/report.txt"
```

If AGATE is started from:

```text
C:\work\projects\agate-server
```

then:

```text
output/report.txt
```

resolves to:

```text
C:\work\projects\agate-server\output\report.txt
```

The same rule applies to:

```text
path
source
target
```

---

# 12. FILE Engine vs. OC Engine

The FILE Engine always works locally.

```text
FILE
    -> local filesystem of the AGATE process
```

Files inside OpenShift pods belong to the OC Engine.

Examples:

```text
FILE READ
    -> read a local file

OC EXEC cat ...
    -> read content inside a pod

OC GET
    -> copy a pod file to the local AGATE host

OC PUT
    -> copy a local file into a pod
```

This separation is important for both test design and AI-generated tests.

---

# 13. BUFFER

`FILE / BUFFER` works on content stored by a previous `FILE / EXEC / READ`.

Example:

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: "output/report.txt"
  response: report_raw

- type: FILE
  op: BUFFER
  response: report_raw
  action: TEXT
  name: report
```

> `FILE / BUFFER` does not read directly from a file path. It operates on the content stored under `response`.

---

# 14. BUFFER Actions

Supported actions:

```text
TEXT
FILTER
LINE
LAST_LINE
COUNT
```

| Action | Purpose | Parameters |
|---|---|---|
| `TEXT` | Stores complete trimmed file content. | – |
| `FILTER` | Stores only lines containing a search term. | `value` |
| `LINE` | Extracts a line from the beginning. | `value` optional, default `0` |
| `LAST_LINE` | Extracts a line relative to the end. | `value` optional, default `0` |
| `COUNT` | Counts occurrences of a search term. | `value` |

## 14.1 TEXT

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: TEXT
  name: report
```

## 14.2 FILTER

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: FILTER
  value: "ERROR"
  name: error_lines
```

Only matching lines are stored.

## 14.3 LINE

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: LINE
  value: 0
  name: first_line
```

Indexing:

```text
0 = first line
1 = second line
2 = third line
...
```

If `value` is omitted, `0` is used.

## 14.4 LAST_LINE

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: LAST_LINE
  value: 0
  name: last_line
```

Indexing:

```text
0 = last line
1 = second-last line
2 = third-last line
...
```

## 14.5 COUNT

```yaml
- type: FILE
  op: BUFFER
  response: report_raw
  action: COUNT
  value: "CHANGED"
  name: changed_count
```

The result can later be referenced as:

```text
{B[changed_count]}
```

---

# 15. ASSERT

The FILE Engine supports two assertion groups:

```text
1. Filesystem Assertions
2. Content Assertions
```

---

# 16. Filesystem Assertions

Filesystem assertions work directly with a local path.

Supported actions:

```text
EXISTS
NOT_EXISTS
```

## EXISTS

```yaml
- type: FILE
  op: ASSERT
  action: EXISTS
  path: "output/report.txt"
```

The step succeeds if the file exists.

## NOT_EXISTS

```yaml
- type: FILE
  op: ASSERT
  action: NOT_EXISTS
  path: "output/report.txt"
```

The step succeeds if the file does not exist.

---

# 17. Content Assertions

Content assertions operate on the `response` of a previous `READ`.

Supported actions:

```text
CONTAINS
NOT_CONTAINS
EQUALS
NOT_EQUALS
COUNT
```

## 17.1 CONTAINS

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: CONTAINS
  value: "CHANGED"
```

## 17.2 NOT_CONTAINS

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: NOT_CONTAINS
  value: "ERROR"
```

## 17.3 EQUALS

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: EQUALS
  value: "AGATE FILE Engine"
```

This checks exact equality after the text normalization defined by the engine.

## 17.4 NOT_EQUALS

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: NOT_EQUALS
  value: "unexpected value"
```

## 17.5 COUNT

```yaml
- type: FILE
  op: ASSERT
  response: report_raw
  action: COUNT
  value: "CHANGED"
  expected: 2
```

---

# 18. EXEC EXISTS vs. ASSERT EXISTS

Both forms intentionally exist.

## EXEC EXISTS

```yaml
- type: FILE
  op: EXEC
  action: EXISTS
  path: "output/report.txt"
  response: exists_result
```

Result:

```text
true / false
```

Use this when later steps need the information.

## ASSERT EXISTS

```yaml
- type: FILE
  op: ASSERT
  action: EXISTS
  path: "output/report.txt"
```

Use this when the existence itself is the test expectation.

Summary:

```text
EXEC EXISTS
    -> produces data

ASSERT EXISTS
    -> validates expectation
```

---

# 19. Placeholders

FILE paths and text may use standard AGATE placeholders.

Examples include:

```text
{B[var]}
{E[env.*]}
{XL[column]}
```

Example:

```yaml
variables:
  baseDir: "output"
  filename: "report.txt"

steps:
  - type: FILE
    op: EXEC
    action: WRITE
    path: "{B[baseDir]}/{B[filename]}"
    text: "AGATE"

  - type: FILE
    op: EXEC
    action: READ
    path: "{B[baseDir]}/{B[filename]}"
    response: report
```

Placeholder semantics are defined in the shared AGATE documentation.

---

# 20. Complete FILE Engine Demo

```yaml
testCases:

  - id: TC_FILE_FULL_DEMO
    description: "FILE Engine - Full Happy Path"
    stage: "*"
    priority: HIGH

    variables:
      baseDir: "output/file-demo"
      filename: "test.txt"
      copyFilename: "test-copy.txt"
      movedFilename: "test-moved.txt"

    steps:

      # WRITE
      - type: FILE
        op: EXEC
        action: WRITE
        path: "{B[baseDir]}/{B[filename]}"
        text: |
          AGATE FILE Engine
          Line 2
          CHANGED
          Line 4
          CHANGED
        response: write_result

      # ASSERT EXISTS
      - type: FILE
        op: ASSERT
        action: EXISTS
        path: "{B[baseDir]}/{B[filename]}"

      # EXEC EXISTS -> boolean response
      - type: FILE
        op: EXEC
        action: EXISTS
        path: "{B[baseDir]}/{B[filename]}"
        response: file_exists

      # READ
      - type: FILE
        op: EXEC
        action: READ
        path: "{B[baseDir]}/{B[filename]}"
        response: file_content

      # Content assertions
      - type: FILE
        op: ASSERT
        response: file_content
        action: CONTAINS
        value: "AGATE FILE Engine"

      - type: FILE
        op: ASSERT
        response: file_content
        action: NOT_CONTAINS
        value: "THIS_TEXT_DOES_NOT_EXIST"

      - type: FILE
        op: ASSERT
        response: file_content
        action: COUNT
        value: "CHANGED"
        expected: 2

      # BUFFER TEXT
      - type: FILE
        op: BUFFER
        response: file_content
        action: TEXT
        name: complete_file

      # BUFFER FILTER
      - type: FILE
        op: BUFFER
        response: file_content
        action: FILTER
        value: "CHANGED"
        name: changed_lines

      # BUFFER LINE
      - type: FILE
        op: BUFFER
        response: file_content
        action: LINE
        value: "0"
        name: first_line

      # BUFFER LAST_LINE
      - type: FILE
        op: BUFFER
        response: file_content
        action: LAST_LINE
        value: "0"
        name: last_line

      # BUFFER COUNT
      - type: FILE
        op: BUFFER
        response: file_content
        action: COUNT
        value: "CHANGED"
        name: changed_count

      # APPEND
      - type: FILE
        op: EXEC
        action: APPEND
        path: "{B[baseDir]}/{B[filename]}"
        text: "APPENDED"
        newline: true

      - type: FILE
        op: EXEC
        action: READ
        path: "{B[baseDir]}/{B[filename]}"
        response: file_after_append

      - type: FILE
        op: ASSERT
        response: file_after_append
        action: CONTAINS
        value: "APPENDED"

      # COPY
      - type: FILE
        op: EXEC
        action: COPY
        source: "{B[baseDir]}/{B[filename]}"
        target: "{B[baseDir]}/{B[copyFilename]}"
        overwrite: true

      - type: FILE
        op: ASSERT
        action: EXISTS
        path: "{B[baseDir]}/{B[copyFilename]}"

      # MOVE
      - type: FILE
        op: EXEC
        action: MOVE
        source: "{B[baseDir]}/{B[copyFilename]}"
        target: "{B[baseDir]}/{B[movedFilename]}"
        overwrite: true

      - type: FILE
        op: ASSERT
        action: NOT_EXISTS
        path: "{B[baseDir]}/{B[copyFilename]}"

      - type: FILE
        op: ASSERT
        action: EXISTS
        path: "{B[baseDir]}/{B[movedFilename]}"

      # DELETE
      - type: FILE
        op: EXEC
        action: DELETE
        path: "{B[baseDir]}/{B[movedFilename]}"

      - type: FILE
        op: ASSERT
        action: NOT_EXISTS
        path: "{B[baseDir]}/{B[movedFilename]}"

      - type: FILE
        op: EXEC
        action: DELETE
        path: "{B[baseDir]}/{B[filename]}"

      # missingOk
      - type: FILE
        op: EXEC
        action: DELETE
        path: "{B[baseDir]}/{B[filename]}"
        missingOk: true
```

---

# 21. Real SVC Usage Pattern

A typical SVC use case is copying prepared test data from a central test-data directory into an application-specific processing directory.

```yaml
- type: FILE
  op: EXEC
  action: COPY
  source: "{E[env.ks.testdata.basedir]}\\ss2\\order\\KartenausstellungEAA\\001\\TESTPROD_EAA_0217_20251110140701.REQ"
  target: "{E[env.ks.testdata.basedir]}\\TxFilesToKams\\TESTPROD_EAA_0217_20251110140701.REQ"
  overwrite: true
```

This demonstrates:

- use of environment placeholders,
- Windows filesystem paths,
- preparation of existing test data,
- explicit overwrite behavior.

The FILE Engine itself remains local. If the destination is inside an OpenShift pod, use `OC / PUT` instead.

---

# 22. Troubleshooting and Best Practices

## File Not Found

Check:

- whether the path is local to the AGATE process,
- whether a relative path resolves from the expected working directory,
- whether placeholders resolve to the intended path,
- whether the file exists before `READ`, `COPY`, `MOVE` or `DELETE`.

---

## Relative Paths

Remember:

```text
relative path
    -> AGATE process working directory
```

Do not assume a relative FILE path refers to:

- a pod filesystem,
- a remote server,
- or the directory of another engine.

---

## Cleanup

For cleanup operations where the file may already be absent, prefer:

```yaml
missingOk: true
```

This makes cleanup idempotent.

---

## BUFFER Requires READ

This is correct:

```text
FILE READ
   |
   v
response
   |
   v
FILE BUFFER
```

Do not generate:

```yaml
- type: FILE
  op: BUFFER
  path: "..."
```

unless the current FILE specification explicitly introduces such a feature.

---

## Content ASSERT Requires response

Content assertions such as:

```text
CONTAINS
NOT_CONTAINS
EQUALS
NOT_EQUALS
COUNT
```

operate on a previously read `response`.

Filesystem assertions:

```text
EXISTS
NOT_EXISTS
```

operate directly on `path`.

---

# 23. AI Guidance

When generating or analyzing FILE Engine steps:

1. **Treat FILE as local filesystem only.**
2. **Use OC for files inside OpenShift pods.**
3. **Use `READ` before content `BUFFER` or content `ASSERT`.**
4. **Do not confuse `EXEC EXISTS` with `ASSERT EXISTS`.**
5. **Use `missingOk: true` for safe cleanup when absence is acceptable.**
6. **Use explicit `overwrite` when replacement behavior matters.**
7. **Assume UTF-8 for READ/WRITE/APPEND unless another encoding is explicitly required.**
8. **Respect relative-path resolution against the AGATE process working directory.**
9. **Use placeholders in paths and text only according to the shared AGATE placeholder rules.**
10. **Do not redefine shared AGATE concepts here.**
    Buffers, placeholders, templates, conditions and re-instantiation are documented separately.

---

# 24. Short Reference

```text
FILE
├── EXEC
│   ├── READ
│   │   ├── path
│   │   ├── response
│   │   └── encoding [default: UTF-8]
│   │
│   ├── WRITE
│   │   ├── path
│   │   ├── text
│   │   ├── encoding [default: UTF-8]
│   │   └── overwrite
│   │
│   ├── APPEND
│   │   ├── path
│   │   ├── text
│   │   ├── encoding [default: UTF-8]
│   │   └── newline [default: false]
│   │
│   ├── COPY
│   │   ├── source
│   │   ├── target
│   │   └── overwrite [default: false]
│   │
│   ├── MOVE
│   │   ├── source
│   │   ├── target
│   │   └── overwrite [default: false]
│   │
│   ├── DELETE
│   │   ├── path
│   │   └── missingOk [default: false]
│   │
│   └── EXISTS
│       ├── path
│       └── response
│
├── BUFFER
│   ├── TEXT
│   ├── FILTER
│   ├── LINE
│   ├── LAST_LINE
│   └── COUNT
│
└── ASSERT
    ├── EXISTS
    ├── NOT_EXISTS
    ├── CONTAINS
    ├── NOT_CONTAINS
    ├── EQUALS
    ├── NOT_EQUALS
    └── COUNT
```

---

# Directory Creation and Parent Paths

The FILE Engine documentation defines file operations. Do not infer undocumented directory-creation behavior.

Example target:

```yaml
path: 'output/user-{B[user_id]}.txt'
```

If the documentation does not explicitly state that missing parent directories are created automatically, do not assume that FILE WRITE creates them.

Valid handling:

```text
Assumption: the `output` directory already exists.
```

or use another documented engine/action to create the directory if such behavior is explicitly available in the knowledge pack.

Do not invent FILE actions such as:

```text
CREATE_DIR
MKDIR
CREATE_DIRECTORIES
ENSURE_PATH
```

unless they are documented.

## AI Rule

When generating FILE WRITE:

1. Treat the file path as documented input.
2. Do not silently assume parent-directory creation.
3. If parent-directory existence matters and no creation behavior is documented, state it as an environment assumption.
4. If another documented engine is intentionally used to create the directory, use that documented syntax instead.
5. Do not turn this into a DSL documentation gap; it is an environment/setup assumption unless AGATE is explicitly expected to provide directory creation.
