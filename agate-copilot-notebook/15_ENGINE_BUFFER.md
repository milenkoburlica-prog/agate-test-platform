# AGATE BUFFER Engine

The BUFFER Engine manages local in-memory variables and performs simple text-based validations.

Supported operations:

```text
EXEC
ASSERT
```

Values stored in BUFFER can be referenced later using:

```text
{B[name]}
```

---

# 1. Quick Reference

## EXEC

```yaml
- type: BUFFER
  op: EXEC
  name: application_env
  value: 'Customer-Service-PROD'
```

## ASSERT

```yaml
- type: BUFFER
  op: ASSERT
  name: application_env
  action: EQUALS
  expected: 'Customer-Service-PROD'
```

---

# 2. EXEC

`BUFFER / EXEC` sets or overwrites a value in the current test context.

Fields:

```text
name       required
value      required
condition  optional
```

Example:

```yaml
- type: BUFFER
  op: EXEC
  name: app_env
  value: 'PROD-Server-01'
```

Dynamic composition is supported:

```yaml
- type: BUFFER
  op: EXEC
  name: full_system_string
  value: '{B[local_prefix]}-{B[app_name]}-v2'
```

The dynamic values are resolved before storage.

---

# 3. ASSERT

`BUFFER / ASSERT` validates a previously stored variable.

Fields:

```text
name       required
action     required
expected   action-dependent
condition  optional
```

Failing BUFFER assertions are fail-fast.

---

# 4. Supported ASSERT Actions

```text
EQUALS
NOT_EQUALS
CONTAINS
IS_NULL
IS_NOT_NULL
IS_EMPTY
IS_NOT_EMPTY
```

## EQUALS

```yaml
- type: BUFFER
  op: ASSERT
  name: application_env
  action: EQUALS
  expected: 'Customer-Service-PROD'
```

## NOT_EQUALS

```yaml
- type: BUFFER
  op: ASSERT
  name: application_env
  action: NOT_EQUALS
  expected: 'DEV'
```

## CONTAINS

```yaml
- type: BUFFER
  op: ASSERT
  name: full_system_string
  action: CONTAINS
  expected: 'Payment'
```

## IS_NULL

True when the variable is not defined or explicitly null.

```yaml
- type: BUFFER
  op: ASSERT
  name: optional_value
  action: IS_NULL
```

## IS_NOT_NULL

```yaml
- type: BUFFER
  op: ASSERT
  name: result
  action: IS_NOT_NULL
```

## IS_EMPTY

True when the value is an empty string or null according to the documented BUFFER semantics.

```yaml
- type: BUFFER
  op: ASSERT
  name: result
  action: IS_EMPTY
```

## IS_NOT_EMPTY

```yaml
- type: BUFFER
  op: ASSERT
  name: result
  action: IS_NOT_EMPTY
```

---

# 5. expected Rules

`expected` is required for:

```text
EQUALS
NOT_EQUALS
CONTAINS
```

It is not required for:

```text
IS_NULL
IS_NOT_NULL
IS_EMPTY
IS_NOT_EMPTY
```

---

# 6. condition

Both BUFFER operations support the common AGATE `condition`.

Example:

```yaml
- type: BUFFER
  op: EXEC
  condition: "'{B[application_env]}' == 'Customer-Service-PROD'"
  name: prod_flag
  value: 'active'
```

If the condition is false, the step is skipped.

---

# 7. Case Sensitivity

BUFFER keys are case-sensitive.

Example:

```text
{B[userId]}
```

and:

```text
{B[userid]}
```

are different names.

If a placeholder remains unresolved, verify exact spelling and execution order.

---

# 8. Execution Order

A buffer must be created before a later step consumes it.

Correct:

```yaml
- type: BUFFER
  op: EXEC
  name: token
  value: 'abc'

- type: CMD
  op: EXEC
  command: 'echo {B[token]}'
```

Do not assume future steps can resolve a BUFFER value that has not yet been set.

---

# 9. Example

```yaml
testCases:

  - id: 'TC_BUF_DEMO'
    description: 'Variablenspeicherung, String-Konstruktion und Prüfung'
    stage: '*'
    priority: HIGH

    variables:
      local_prefix: 'PRE-PROD'
      app_name: 'Payment-Processing'

    steps:

      - type: BUFFER
        op: EXEC
        name: application_env
        value: 'Customer-Service-PROD'

      - type: BUFFER
        op: ASSERT
        name: application_env
        action: EQUALS
        expected: 'Customer-Service-PROD'

      - type: BUFFER
        op: EXEC
        name: full_system_string
        value: '{B[local_prefix]}-{B[app_name]}-v2'

      - type: BUFFER
        op: ASSERT
        name: full_system_string
        action: IS_NOT_EMPTY
```

---

# 10. Performance Guidance

BUFFER variables are stored in memory.

Do not store extremely large text blocks, such as multi-megabyte logs, unless they are actually needed by later test steps.

Use BUFFER for compact runtime values, identifiers, status values and intermediate strings.

---

# 11. AI Guidance

1. Use only `EXEC` and `ASSERT`.
2. EXEC requires `name` and `value`.
3. ASSERT requires `name` and `action`.
4. Use `expected` only for actions that require it.
5. Do not invent actions outside the documented list.
6. BUFFER keys are case-sensitive.
7. Ensure a value is initialized before use.
8. Use direct placeholder composition in `value` when needed.
9. Do not use BUFFER merely because another engine already accepts placeholders directly.
10. Do not store unnecessarily large payloads in BUFFER.
