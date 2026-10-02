# AGATE WAIT Engine

The WAIT Engine pauses test execution for a defined amount of time.

It is intentionally simple and has no `op`, `action`, `response` or `assert` model.

A WAIT step consists primarily of:

```yaml
- type: WAIT
  value: '2s'
```

---

# 1. Quick Reference

```yaml
- type: WAIT
  value: '1000'
  condition: "..."   # Optional
```

Supported fields:

```text
id
type
condition
value
```

The required field is:

```text
value
```

---

# 2. Purpose

Use WAIT when a test must explicitly pause before continuing.

Typical examples:

- wait for asynchronous processing,
- wait between polling iterations,
- allow an external system time to react,
- delay execution before the next validation,
- preserve timing behavior migrated from another test system.

Example:

```yaml
- type: WAIT
  value: '2s'
```

---

# 3. Supported Time Formats

WAIT supports milliseconds, seconds and minutes.

Examples:

```yaml
- type: WAIT
  value: '1000'
```

Digits without a unit are interpreted as milliseconds.

Equivalent:

```yaml
- type: WAIT
  value: '1000ms'
```

Other supported forms:

```yaml
- type: WAIT
  value: '200ms'
```

```yaml
- type: WAIT
  value: '200 ms'
```

```yaml
- type: WAIT
  value: '2s'
```

```yaml
- type: WAIT
  value: '2 s'
```

```yaml
- type: WAIT
  value: '1m'
```

```yaml
- type: WAIT
  value: '1 m'
```

Supported unit suffixes:

```text
ms
s
m
```

---

# 4. Unit Semantics

```text
1000
    -> 1000 milliseconds

1000ms
    -> 1000 milliseconds

2s
    -> 2 seconds

1m
    -> 1 minute
```

Whitespace between number and unit is allowed.

Examples:

```text
200ms
200 ms
2s
2 s
1m
1 m
```

---

# 5. Dynamic WAIT Values

WAIT supports dynamic AGATE placeholders.

Example:

```yaml
- type: WAIT
  value: '{B[wait_time]}'
```

The placeholder is resolved before the WAIT value is parsed.

Example setup:

```yaml
- type: BUFFER
  op: EXEC
  name: wait_time
  value: '5s'

- type: WAIT
  value: '{B[wait_time]}'
```

The resulting WAIT duration is:

```text
5 seconds
```

---

# 6. condition

WAIT supports the common AGATE `condition` field.

Example:

```yaml
- type: WAIT
  condition: "'{B[status]}' == 'PENDING'"
  value: '5s'
```

If the condition evaluates to `false`, the WAIT step is skipped.

The common AGATE condition and YAML rules apply.

---

# 7. Validation Rules

A WAIT step must contain a valid `value`.

## Missing value

Invalid:

```yaml
- type: WAIT
```

Validator error:

```text
AGATE-V800
```

Meaning:

```text
WAIT value is missing.
```

---

## Invalid or negative value

Invalid:

```yaml
- type: WAIT
  value: '-1000'
```

Invalid:

```yaml
- type: WAIT
  value: 'abc'
```

Validator error:

```text
AGATE-V802
```

Meaning:

```text
WAIT value is invalid.
```

---

# 8. Valid Value Pattern

A static WAIT value follows the logical form:

```text
<number>
<number>ms
<number>s
<number>m
```

with optional whitespace between the number and unit.

Conceptually:

```text
^\d+\s*(ms|s|m)?$
```

Examples that are valid:

```text
1000
200ms
200 ms
2s
2 s
1m
1 m
```

Examples that are invalid:

```text
-1000
1.5s
abc
2h
five seconds
```

---

# 9. WAIT Has No op

Do not generate:

```yaml
- type: WAIT
  op: EXEC
  value: '2s'
```

Preferred and documented form:

```yaml
- type: WAIT
  value: '2s'
```

WAIT is a direct step type.

---

# 10. WAIT Has No action

Do not generate:

```yaml
- type: WAIT
  action: WAIT
  value: '2s'
```

Use:

```yaml
- type: WAIT
  value: '2s'
```

---

# 11. WAIT Has No response

WAIT does not produce a reusable execution response.

Do not generate:

```yaml
- type: WAIT
  value: '2s'
  response: wait_result
```

There is no need for a `response`.

---

# 12. WAIT Inside Polling Flows

WAIT is particularly useful between repeated checks.

Example pattern:

```text
check state
    |
    v
not finished
    |
    v
WAIT
    |
    v
check again
```

Example inside a loop:

```yaml
- type: LOOP
  mode: DO_WHILE
  condition: "'{B[processing_done]}' == 'false'"
  maxIterations: 30
  timeoutMs: 130000
  steps:

    - type: SQL
      op: EXEC
      command: |
        SELECT STATUS
        FROM PROCESSING_STATE
      response: processing_result

    - type: SQL
      op: BUFFER
      response: processing_result
      row: 0
      column: 'STATUS'
      name: processing_status

    - type: WAIT
      value: '4s'
```

The LOOP syntax itself is documented separately.

---

# 13. Migration Example

A migrated external test system may express a wait duration in milliseconds.

Example source meaning:

```text
Duration = 5000 ms
```

AGATE equivalent:

```yaml
- type: WAIT
  value: '5000'
```

or more readable:

```yaml
- type: WAIT
  value: '5s'
```

Prefer the readable unit form in manually maintained tests unless the exact millisecond representation is important.

---

# 14. Recommended Style

Prefer explicit units for human-readable tests.

Preferred:

```yaml
- type: WAIT
  value: '5s'
```

instead of:

```yaml
- type: WAIT
  value: '5000'
```

Both represent the same duration.

Milliseconds remain useful when the test requires fine-grained timing:

```yaml
- type: WAIT
  value: '250ms'
```

---

# 15. Best Practices

## Use WAIT only when timing is really required

A fixed delay can make tests slower and less deterministic.

Prefer checking a real state or condition when the corresponding engine supports it.

Use WAIT when:

- the target system genuinely needs time,
- polling requires a pause between iterations,
- timing behavior is part of the scenario,
- or migration semantics require preservation of a wait.

---

## Prefer readable units

Good:

```yaml
value: '5s'
```

Less readable:

```yaml
value: '5000'
```

---

## Avoid unnecessarily long waits

Bad:

```yaml
- type: WAIT
  value: '5m'
```

unless the business flow genuinely requires such a delay.

For repeated state checks, prefer a loop with shorter waits where possible.

---

# 16. AI Guidance

When generating WAIT steps:

1. Use `type: WAIT`.
2. Do not add `op`.
3. Do not add `action`.
4. Do not add `response`.
5. Always provide `value`.
6. Prefer readable units such as `ms`, `s` or `m`.
7. Digits without a unit mean milliseconds.
8. Do not generate negative durations.
9. Dynamic values such as `{B[wait_time]}` may be used.
10. Use `condition` only when conditional waiting is required.
11. Do not invent hour (`h`) support.
12. Do not use decimal values such as `1.5s`.
13. When polling, use WAIT as the pause between checks rather than as a substitute for the actual validation.
14. Follow the shared AGATE YAML rules for quoting and conditions.

---

# 17. Examples

## 500 milliseconds

```yaml
- type: WAIT
  value: '500ms'
```

## 2 seconds

```yaml
- type: WAIT
  value: '2s'
```

## 1 minute

```yaml
- type: WAIT
  value: '1m'
```

## Dynamic value

```yaml
- type: WAIT
  value: '{B[wait_time]}'
```

## Conditional WAIT

```yaml
- type: WAIT
  condition: "'{B[state]}' == 'PENDING'"
  value: '3s'
```

---

# 18. Short Reference

```text
WAIT
├── value       [required]
└── condition   [optional]

Supported values:
├── 1000        -> milliseconds
├── 1000ms
├── 2s
└── 1m

Also valid with whitespace:
├── 200 ms
├── 2 s
└── 1 m

Not supported:
├── negative values
├── decimal values
├── hours
├── op
├── action
└── response
```
