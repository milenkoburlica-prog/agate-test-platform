# AGATE JSON Engine

The JSON Engine validates existing JSON files by evaluating one or more paths against expected values.

It supports:

- object navigation,
- deeply nested arrays,
- complex keys with special characters,
- optional root prefixes,
- dynamic AGATE placeholders,
- regex-style matching,
- and aggregated validation of multiple assertions within one step.

The canonical operation is:

```text
ASSERT
```

> This document describes JSON-specific behavior. General AGATE test structure, YAML rules, placeholders and conditions are documented separately.

---

# 1. Quick Reference

```yaml
- type: JSON
  op: ASSERT
  file: 'C:\tmp\api_response.json'
  condition: "'{B[run_validation]}' == 'true'"
  parameters:
    path1: '$.status'
    value1: '{B[expected_status]}'

    path2: "$['metadata.info']['env.type']"
    value2: '{B[environment]}'

    path3: '$.data.users[0].id'
    value3: '1001'

    path4: '$.data.session_token'
    value4: 'Session-.*'
```

---

# 2. Purpose

Use the JSON Engine when a test must validate the contents of an existing JSON file on the filesystem.

Typical use cases:

- validate generated API-response files,
- validate deeply nested JSON structures,
- validate values inside arrays,
- validate keys that contain dots or other special characters,
- validate multiple JSON paths in one step,
- use dynamic placeholders as expected values,
- use regex-style matching for flexible expectations.

---

# 3. Operation

The documented JSON operation is:

```text
ASSERT
```

Example:

```yaml
- type: JSON
  op: ASSERT
  file: 'output/response.json'
  parameters:
    path1: '$.status'
    value1: 'SUCCESS'
```

---

# 4. Core Parameters

| Field | Required | Description |
|---|---:|---|
| `file` | Yes | Absolute or relative path to the JSON file. |
| `parameters` | Yes | Numbered path/value pairs: `path1/value1`, `path2/value2`, ... |
| `condition` | No | Optional common AGATE execution condition. |

Canonical structure:

```yaml
- type: JSON
  op: ASSERT
  file: '...'
  condition: '...'
  parameters:
    path1: '...'
    value1: '...'
    path2: '...'
    value2: '...'
```

---

# 5. Aggregated Validation Model

The JSON Engine does **not** stop after the first failed path validation.

Instead:

```text
read file
   |
   v
evaluate path1
evaluate path2
evaluate path3
...
   |
   v
collect all failures
   |
   v
throw one summarized error at the end
```

This is a collection / batch validation model.

Important distinction:

```text
JSON Engine
    -> collects all path failures inside one ASSERT step

PDF / SOAP / REST assertions
    -> may use fail-fast semantics depending on the engine/operation
```

Do not assume JSON ASSERT is fail-fast.

---

# 6. file

`file` identifies the JSON file to validate.

Example:

```yaml
file: 'C:\tmp\api_response.json'
```

Relative paths are also supported.

Example:

```yaml
file: 'output/api_response.json'
```

For Windows paths, prefer single-quoted YAML strings according to the shared YAML rules.

---

# 7. condition

The JSON step supports the common AGATE `condition`.

Example:

```yaml
condition: "'{B[environment]}' == 'PROD'"
```

If the condition evaluates to false, the JSON step is skipped according to the common AGATE condition behavior.

---

# 8. parameters

All JSON validations are defined inside:

```yaml
parameters:
```

Each validation is represented by one numbered pair:

```text
pathN
valueN
```

Example:

```yaml
parameters:
  path1: '$.status'
  value1: 'SUCCESS'

  path2: '$.data.id'
  value2: '1001'
```

The numbering must remain paired.

Conceptually:

```text
path1  <-> value1
path2  <-> value2
path3  <-> value3
```

Do not generate an unmatched path without the corresponding value.

---

# 9. Sequential Numbering

For multiple validations, continue numbering:

```yaml
parameters:
  path1: '$.status'
  value1: 'SUCCESS'

  path2: '$.environment'
  value2: 'PROD'

  path3: '$.data.users[0].id'
  value3: '1001'
```

Do not restart numbering inside the same JSON step.

---

# 10. JSON Path Syntax Overview

The JSON Engine uses its own optimized path tokenizer (`getValueByPath`).

Supported navigation patterns include:

```text
dot notation
nested arrays
quoted complex keys
optional root prefix
```

This is not described as full Jayway JsonPath.

Do not assume unsupported advanced JsonPath features.

---

# 11. Standard Object Navigation

Use dot notation for normal object fields.

Example JSON:

```json
{
  "status": "SUCCESS",
  "data": {
    "user": {
      "id": 1001
    }
  }
}
```

Valid paths:

```text
$.status
$.data.user.id
```

Example:

```yaml
path1: '$.data.user.id'
value1: '1001'
```

---

# 12. Optional Root Prefix

The leading `$` and following `.` are optional and normalized automatically.

These forms may therefore address the same field:

```text
$.status
status
```

and:

```text
$.data.user.id
data.user.id
```

For generated tests, prefer the explicit `$` form for readability.

Preferred:

```yaml
path1: '$.status'
```

---

# 13. Nested Arrays

Direct sequential array indexes are supported.

Example JSON:

```json
{
  "logs": [
    [
      {"level": "INFO"},
      {"level": "ERROR"}
    ]
  ]
}
```

Example path:

```text
$.logs[0][1].level
```

The tokenizer supports directly consecutive array indices.

---

# 14. Array Element Access

Example:

```json
{
  "data": {
    "users": [
      {
        "id": 1001,
        "name": "Anna"
      }
    ]
  }
}
```

Path:

```text
$.data.users[0].id
```

YAML:

```yaml
path1: '$.data.users[0].id'
value1: '1001'
```

---

# 15. Complex Keys

Keys containing dots, special characters or purely numeric names should be addressed with bracket notation.

Single-quoted key:

```text
$['event.code']
```

Double-quoted key:

```text
$["10"]
```

Nested example:

```text
$['metadata.info']['env.type']
```

Example YAML:

```yaml
path1: "$['metadata.info']['env.type']"
value1: 'PROD'
```

---

# 16. Numeric Keys

Purely numeric object keys should use quoted bracket notation.

Example JSON:

```json
{
  "10": {
    "status": "OK"
  }
}
```

Path:

```text
$["10"].status
```

---

# 17. Expected Values

Each `valueN` is the expected value for the corresponding `pathN`.

Example:

```yaml
parameters:
  path1: '$.status'
  value1: 'SUCCESS'
```

Expected values may be:

- static strings,
- dynamically resolved AGATE placeholders,
- regex-style values containing `*`.

---

# 18. Dynamic Placeholder Values

Example:

```yaml
variables:
  expected_status: 'SUCCESS'
```

Validation:

```yaml
parameters:
  path1: '$.status'
  value1: '{B[expected_status]}'
```

The placeholder is resolved before comparison.

---

# 19. Regex / Wildcard Behavior

If a `valueN` contains:

```text
*
```

the engine automatically switches to regex matching.

Conceptually:

```text
actualResolved.matches(expectedResolved)
```

Example:

```yaml
path1: '$.data.session_token'
value1: 'Session-.*'
```

This means:

```text
the actual value must match the regular expression Session-.*
```

Important:

```text
* is not treated as a simple glob wildcard.
It activates regex matching.
```

Therefore the full expected string must be valid regex syntax.

---

# 20. Static Comparison

If `valueN` contains no `*`, the engine performs the normal expected-value comparison.

Example:

```yaml
path1: '$.status'
value1: 'SUCCESS'
```

---

# 21. Complete Example

```yaml
testCases:

  - id: 'TC_JSON_VALIDATION'
    description: 'Umfassende Validierung einer API-Response-Datei'
    stage: '*'
    priority: HIGH

    variables:
      expected_status: 'SUCCESS'
      environment: 'PROD'

    steps:

      - type: JSON
        op: ASSERT
        file: 'C:\tmp\api_response.json'

        parameters:

          # Standard object field
          path1: '$.status'
          value1: '{B[expected_status]}'

          # Complex keys
          path2: "$['metadata.info']['env.type']"
          value2: '{B[environment]}'

          # Nested array
          path3: '$.data.users[0].id'
          value3: '1001'

          # Regex-style validation
          path4: '$.data.session_token'
          value4: 'Session-.*'
```

---

# 22. File Loading

The engine reads the complete JSON file into memory.

Documented implementation behavior:

```text
Files.readAllBytes(...)
```

This happens once per JSON step.

All numbered validations inside that step operate on the in-memory JSON content.

---

# 23. Performance Best Practice

Because the file is read once per JSON step:

```text
many path/value checks in one JSON ASSERT
```

are preferable to:

```text
many separate JSON ASSERT steps against the same file
```

Preferred:

```yaml
- type: JSON
  op: ASSERT
  file: 'large.json'
  parameters:
    path1: '$.status'
    value1: 'SUCCESS'
    path2: '$.environment'
    value2: 'PROD'
    path3: '$.data.id'
    value3: '1001'
```

Less efficient:

```yaml
- type: JSON
  op: ASSERT
  file: 'large.json'
  parameters:
    path1: '$.status'
    value1: 'SUCCESS'

- type: JSON
  op: ASSERT
  file: 'large.json'
  parameters:
    path1: '$.environment'
    value1: 'PROD'
```

when both checks can be grouped safely.

---

# 24. Aggregated Error Reporting

When multiple paths fail, the engine continues evaluating the remaining validations.

At the end, it throws one summarized exception containing all failures.

Example:

```text
RuntimeException: JSON Assertion failed for file: C:\tmp\api_response.json
  - Path [$.status] failed! Expected: [SUCCESS], Actual: [FAILED]
  - Path [$.data.users[0].id] failed! Expected: [1001], Actual: [9999]
```

This behavior is intentional.

---

# 25. Infrastructure Errors

Some errors are not ordinary validation failures.

Examples:

```text
JSON ASSERT operation requires 'file' parameter
JSON file not found
```

These indicate setup / infrastructure problems and may abort immediately.

Do not confuse them with aggregated path validation failures.

---

# 26. Path Type Mismatch

A path evaluation can fail if the path expects the wrong JSON data type.

Example path:

```text
$.users[0]
```

but actual JSON contains:

```json
{
  "users": {
    "id": 1001
  }
}
```

Here the path expects an array but encounters an object.

The engine logs the evaluation problem and marks the path as failed.

---

# 27. Troubleshooting Checklist

## File not found

Check:

- `file` is present,
- path is correct,
- previous test step created the file,
- relative path resolves from the expected working directory,
- placeholders inside the path resolved correctly.

---

## Path returns unexpected value

Check:

- object vs array structure,
- array indexes,
- quoted complex keys,
- optional root prefix normalization,
- exact nesting.

---

## Several paths fail

This is expected behavior for aggregated validation.

The engine continues and reports all failures at the end.

---

## Regex validation fails unexpectedly

If the expected value contains `*`, verify that the entire value is valid regex syntax.

Example:

```yaml
value1: 'Session-.*'
```

Do not assume shell-style wildcard semantics.

---

## Complex key cannot be resolved

Use bracket notation:

```text
$['metadata.info']
```

instead of:

```text
$.metadata.info
```

when the dot is part of the key name.

---

# 28. Best Practices

1. Group validations against the same JSON file into one JSON step.
2. Use sequential `pathN/valueN` pairs.
3. Prefer the explicit `$` root prefix for generated tests.
4. Use dot notation for normal object fields.
5. Use brackets for keys containing dots, special characters or numeric names.
6. Use explicit array indices for nested arrays.
7. Use AGATE placeholders directly in `valueN`.
8. Remember that `*` activates regex matching.
9. Do not assume full Jayway JsonPath support.
10. Do not generate wildcard-selection syntax unless current JSON Engine documentation explicitly supports it.
11. Prefer single-quoted Windows file paths.
12. Treat file-not-found errors as infrastructure/setup issues.
13. Treat multiple path failures as normal aggregated validation output.
14. For large files, combine checks into one step where reasonable.

---

# 29. AI Guidance

When generating JSON tests:

1. Use `type: JSON`.
2. Use `op: ASSERT`.
3. Always provide `file`.
4. Always provide `parameters`.
5. Generate numbered pairs:
   - `path1` + `value1`
   - `path2` + `value2`
   - etc.
6. Never generate a path without its matching value.
7. Prefer `$`-prefixed paths for readability.
8. Use dot notation for normal keys.
9. Use bracket notation for keys with dots, special characters or numeric names.
10. Nested array indexes such as `[0][10]` are supported.
11. Do not assume full Jayway JsonPath feature compatibility.
12. If expected contains `*`, treat it as regex matching, not glob matching.
13. Do not invent actions such as `EQUALS`, `CONTAINS`, `COUNT` for JSON Engine steps.
14. JSON comparison logic is encoded through `pathN/valueN`, not per-item action fields.
15. Do not add `response`, `source`, `selector`, `name`, `expected` or `action` fields to JSON Engine steps.
16. Remember that JSON validation is aggregated inside one step.
17. Do not describe JSON validation as fail-fast for ordinary path mismatches.
18. File/setup errors may still abort immediately.
19. Prefer one JSON step with many path/value checks when validating the same file.
20. Follow shared AGATE YAML and condition rules.

---

# 30. Critical Distinction for AI

Do not confuse the JSON Engine with REST BODY ASSERT.

REST validation:

```yaml
- type: REST
  op: ASSERT
  response: rest_response
  source: BODY
  path: '$.status'
  action: EQUALS
  expected: 'SUCCESS'
```

JSON file validation:

```yaml
- type: JSON
  op: ASSERT
  file: 'response.json'
  parameters:
    path1: '$.status'
    value1: 'SUCCESS'
```

These are different engines with different syntax.

---

# 31. Short Reference

```text
JSON
└── ASSERT
    ├── file           [required]
    ├── condition      [optional]
    └── parameters     [required]
        ├── path1
        ├── value1
        ├── path2
        ├── value2
        └── ...

Path syntax:
├── $.field
├── $.parent.child
├── $.array[0]
├── $.array[0][10]
├── $['event.code']
└── $["10"]

Comparison:
├── normal value
└── value containing * -> regex matching

Execution:
read file once
    |
evaluate all path/value pairs
    |
collect failures
    |
report all failures together
```
