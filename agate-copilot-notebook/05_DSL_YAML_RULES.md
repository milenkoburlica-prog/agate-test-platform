# AGATE YAML & DSL Rules

This document defines common YAML and AGATE DSL rules that apply across all AGATE engines.

Engine-specific documentation such as SQL, CMD, OC and FILE describes the fields and actions of the corresponding engine.
This document describes how AGATE YAML should be written so that placeholders, conditions, commands, paths and dynamic values are represented consistently and safely.

---

# 1. General Principle

AGATE uses a decoupled execution model.

A step executes an operation and can store its result under a freely chosen name such as `response`.

That stored result can then be reused by later steps without executing the original operation again.

Typical flow:

```text
EXEC
  |
  v
response
  |
  +--> ASSERT
  |
  +--> BUFFER
  |
  +--> later engine step
```

Example:

```yaml
- type: CMD
  op: EXEC
  command: 'java --version'
  response: java_version

- type: CMD
  op: ASSERT
  response: java_version
  action: CONTAINS
  value: 'OpenJDK'

- type: CMD
  op: BUFFER
  response: java_version
  action: TEXT
  name: java_version_text
```

---

# 2. Preferred YAML Quoting Style

For AGATE tests, **single quotes (`'...'`) are preferred for ordinary string values whenever possible**.

This is especially recommended for:

- Windows paths,
- shell commands,
- values containing backslashes,
- values containing double quotes,
- AGATE placeholders,
- file names and paths,
- URLs and other strings containing YAML-sensitive characters.

Preferred:

```yaml
command: 'type "C:\tmp\result.txt"'
```

instead of:

```yaml
command: "type \"C:\\tmp\\result.txt\""
```

Both forms may be valid YAML, but the single-quoted form is easier to read and less error-prone.

---

# 3. Why Single Quotes Are Preferred

In YAML double-quoted strings, backslash is an escape character.

Example:

```yaml
command: "C:\\Scripts\\run.bat"
```

With single quotes, backslashes are literal:

```yaml
command: 'C:\Scripts\run.bat'
```

## 3.1 Double quotes inside single-quoted strings

```yaml
command: 'echo "Hello"'
```

is preferred over:

```yaml
command: "echo \"Hello\""
```

## 3.2 Single quote inside a single-quoted YAML string

Use doubled single quotes:

```yaml
value: 'It''s valid YAML'
```

---

# 4. Global Feature: condition

`condition` is an optional AGATE field for conditional execution.

If the condition evaluates to `false`, the step is skipped (`SKIPPED`) and the test continues.

Before evaluation, AGATE resolves dynamic values used in the expression.

Example:

```yaml
- type: CMD
  op: EXEC
  condition: "'{B[os_version_var]}' == 'Windows_NT'"
  command: '{B[command]}'
  response: windows_version
```

---

# 5. Quoting String Values in Conditions

Strings inside conditions must be explicitly quoted.

Preferred pattern:

```yaml
condition: "'{B[status]}' == 'ACTIVE'"
```

Another example:

```yaml
condition: "('{B[count]}' == '1') AND ('{B[type]}' == 'A')"
```

The outer YAML string uses double quotes so the expression itself can contain single-quoted string values.

---

# 6. Variable Naming Rules

To avoid ambiguous expression evaluation, variable names should follow these rules.

## Allowed characters

Use only:

```text
A-Z
a-z
0-9
_
```

## First character

Start with a letter or underscore.

Good:

```text
customer_id
_result
B_VSNR
```

Avoid:

```text
3_test
2026_status
```

## Avoid mathematical operators

Do not use:

```text
-
+
/
```

Avoid:

```text
customer-id
CardToken.SVNR-Karte
a+b
```

Prefer:

```text
customer_id
CardToken_SVNR_Karte
a_b
```

## Avoid special/grouping characters

Avoid characters such as:

```text
(
)
[
]
?
@
```

Examples to avoid:

```text
(1)
Kunde@Daten
customer[1]
```

---

# 7. Dynamic AGATE Tags

AGATE resolves dynamic tags before engine execution.

Known tag families include:

```text
{B[...]}   Buffer value
{R[...]}   Reusable/runtime parameter
{T[...]}   Test-related dynamic value
{E[...]}   Environment value
```

Examples:

```yaml
command: '{B[command]}'
path: '{E[env.ks.testdata.basedir]}\input\test.txt'
text: '{R[content]}'
```

Dynamic tags may be used in AGATE fields and are resolved before engine execution.

> Dynamic resolution does not make an undocumented engine field valid. Engine documentation still defines which fields exist.

---

# 8. Quote Dynamic Tags

As an AGATE style rule, complete dynamic-tag values should be quoted.

Preferred:

```yaml
command: '{B[command]}'
path: '{B[baseDir]}/{B[filename]}'
value: '{R[SVNR]}'
```

Avoid:

```yaml
command: {B[command]}
```

Quoting avoids ambiguity with YAML flow syntax.

---

# 9. Windows Paths

For Windows paths, prefer single quotes.

Preferred:

```yaml
path: 'C:\tmp\report.txt'
command: 'C:\Scripts\run.bat'
from: 'C:\tmp\test.txt'
```

With double quotes, backslashes must be escaped:

```yaml
path: "C:\\tmp\\report.txt"
```

---

# 10. Commands Containing Double Quotes

Prefer:

```yaml
command: 'type "C:\Program Files\App\result.txt"'
```

or:

```yaml
command: 'echo "Hello AGATE"'
```

instead of heavily escaped double-quoted strings.

---

# 11. YAML-Sensitive Characters

Strings containing characters such as:

```text
:
{
}
#
[
]
```

should be quoted when they represent literal command text, paths, URLs or placeholders.

Examples:

```yaml
command: 'C:\Tools\run.bat'
url: 'https://example.org/api'
value: '{B[user_id]}'
```

---

# 12. Multi-Line Commands

Use YAML block notation for long or complex commands:

```yaml
- type: CMD
  op: EXEC
  command: |
    cd C:\Services\App
    run_install.bat --mode=silent --dir="C:\Program Files\App"
  response: install_out
```

This is especially useful for:

- long PowerShell commands,
- command chains,
- commands with nested quotes.

Example:

```yaml
- type: CMD
  op: EXEC
  command: |
    powershell -Command "(Get-Content -Path C:\tmp\result.txt -Raw | Measure-Object -Line).Lines"
  response: line_count
```

---

# 13. Prefer Readability Over Escaping

Preferred:

```yaml
command: 'type "output\user-{B[user_id]}.txt"'
```

Less preferred:

```yaml
command: "type \"output\\user-{B[user_id]}.txt\""
```

---

# 14. YAML Boolean and Numeric Values

Use native YAML values when a field is explicitly boolean or numeric.

Boolean:

```yaml
overwrite: true
checkExitCode: false
missingOk: true
```

Numeric:

```yaml
timeout: 30
expectedExitCode: 0
row: 0
```

Do not quote these unless the engine documentation explicitly expects a string.

---

# 15. Response Names and Buffer Names

Use descriptive and stable names.

Good:

```yaml
response: java_version_raw
name: java_version
```

```yaml
response: user_result
name: user_id
```

Avoid reusing the same identifier for different data types unless explicitly documented.

---

# 16. Typical Cross-Engine Data Flow

```text
SQL EXEC
   |
   v
SQL BUFFER -> user_id
   |
   v
FILE WRITE
   |
   v
CMD EXEC
   |
   v
OC PUT / EXEC
```

Example:

```yaml
- type: SQL
  op: BUFFER
  response: user_result
  row: 0
  column: 'ID'
  name: user_id

- type: FILE
  op: EXEC
  action: WRITE
  path: 'output/user-{B[user_id]}.txt'
  text: 'User-ID: {B[user_id]}'
  overwrite: true

- type: CMD
  op: EXEC
  command: 'type "output\user-{B[user_id]}.txt"'
  response: local_content

- type: OC
  op: PUT
  pod: 'my-service'
  from: 'output\user-{B[user_id]}.txt'
  to: '/tmp/user-{B[user_id]}.txt'
  response: put_result
```

---

# 17. Engine Fields vs. Dynamic Resolution

These are separate concepts.

Dynamic resolution may resolve:

```text
{B[...]}, {R[...]}, {T[...]}, {E[...]}
```

inside a field.

But engine documentation defines which fields exist.

Valid:

```yaml
- type: FILE
  op: EXEC
  action: READ
  path: '{B[file]}'
```

Still invalid if undocumented:

```yaml
- type: FILE
  op: EXEC
  unknownField: '{B[file]}'
```

---

# 18. AI Guidance

When generating AGATE YAML:

1. Prefer single quotes for ordinary strings.
2. Prefer single quotes for Windows paths.
3. Prefer single quotes for commands containing double quotes.
4. Use double quotes for `condition` expressions when the expression contains single-quoted strings.
5. Quote complete dynamic-tag string values.
6. Use `|` for long or complex commands.
7. Use native YAML booleans and numbers for boolean/numeric fields.
8. Never invent engine fields because dynamic tags can be resolved.
9. Use safe variable names containing only letters, digits and underscores.
10. Do not start variable names with digits.
11. Avoid `-`, `+`, `/`, brackets and other special characters in variable names.
12. Preserve engine-specific rules from the corresponding engine documentation.
13. If a required business value or environment detail is unknown, state an assumption instead of inventing it.
14. Do not classify something as an assumption if it is already explicitly documented in the provided AGATE documentation.

---

# 19. Preferred AGATE Style Examples

## Preferred command

```yaml
- type: CMD
  op: EXEC
  command: 'type "C:\tmp\result.txt"'
  response: result
```

## Preferred condition

```yaml
condition: "'{B[status]}' == 'ACTIVE'"
```

## Preferred placeholder path

```yaml
path: '{B[baseDir]}/{B[filename]}'
```

## Preferred Windows path

```yaml
path: 'C:\tmp\report.txt'
```

## Preferred complex command

```yaml
command: |
  powershell -Command "Get-Content -Encoding UTF8 -Path C:\tmp\report.txt"
```

---

# 20. Design Rule

AGATE YAML should optimize for:

```text
readability
predictability
minimal escaping
explicit data flow
documented engine fields only
```

When several YAML representations are technically possible, prefer the representation that is easiest for a tester to read and least likely to be misinterpreted by YAML, the expression engine or an AI assistant.

---

# 21. Direct Placeholder Composition

AGATE dynamic placeholders may be embedded directly into documented string fields.

This means that values from buffers, reusable parameters, environment variables or other supported dynamic sources can be composed directly inside:

- ordinary string fields,
- file paths,
- commands,
- SQL strings,
- text fields,
- and YAML block scalars.

A separate generic `BUFFER` step is **not required** merely to concatenate or assemble text.

Example:

```yaml
- type: FILE
  op: EXEC
  action: WRITE
  path: 'output/user-{B[user_id]}.txt'
  text: |
    User-ID: {B[user_id]}
    Status: {B[user_status]}
  overwrite: true
```

In this example `{B[user_id]}` and `{B[user_status]}` are resolved directly inside the FILE `text` field.

There is no need to create an intermediate generic `BUFFER` only to construct this string.

## 21.1 Direct Composition in Commands

```yaml
- type: CMD
  op: EXEC
  command: 'type "output\user-{B[user_id]}.txt"'
  response: cmd_output
```

## 21.2 Direct Composition in Paths

```yaml
path: 'output/{B[filename]}'
```

```yaml
from: 'output/user-{B[user_id]}.txt'
to: '/tmp/user-{B[user_id]}.txt'
```

## 21.3 Direct Composition in Multi-Line Text

```yaml
text: |
  ID={B[user_id]}
  STATUS={B[user_status]}
  ENV={E[env.name]}
```

All placeholders are resolved as part of the field value before engine execution.

## 21.4 When a Separate BUFFER Is Appropriate

Use a BUFFER step when you actually need to:

- extract data from a previous engine response,
- store a value for reuse,
- normalize or transform a value,
- select a specific line or field,
- count or filter content,
- or preserve an intermediate result.

Do not use BUFFER merely as a workaround for string concatenation.

## 21.5 AI Rule

When generating AGATE YAML:

1. Prefer direct placeholder composition inside documented string fields.
2. Do not introduce a generic BUFFER only to build a string.
3. Do not report a missing generic BUFFER engine if the required value can be expressed directly with placeholders.
4. Only introduce an additional BUFFER when the test logic actually requires extraction, transformation, reuse or normalization.
5. If the target engine field supports a string value, use the placeholders directly in that field.

Bad reasoning:

```text
I need a generic BUFFER to build:
User-ID: <ID>
Status: <STATUS>
```

Preferred:

```yaml
text: |
  User-ID: {B[user_id]}
  Status: {B[user_status]}
```

## 21.6 Critical Distinction

```text
BUFFER
    -> stores/extracts/transforms data

Placeholder composition
    -> embeds already available dynamic values into a field
```

Do not confuse them.
