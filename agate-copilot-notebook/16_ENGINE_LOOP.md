# LOOP Engine

The `LOOP` engine repeatedly executes a group of nested AGATE steps while a condition remains true.

Typical use cases include:

- polling asynchronous backend processing
- repeated database checks
- repeated REST or SOAP status checks
- waiting for a technical state instead of using a fixed `WAIT`
- synchronization between test steps

`LOOP` does **not** use an `op` property.

Supported modes:

```text
WHILE
DO_WHILE
```

---

## Basic syntax

```yaml
- type: LOOP
  mode: DO_WHILE
  condition: "{B[status]} != 'READY'"
  maxIterations: 20
  timeoutMs: 60000
  steps:

    - type: REST
      op: EXEC
      command: demo.status
      response: status_response

    - type: WAIT
      value: "2000"
```

---

# Parameters

| Property | Required | Description |
|---|---:|---|
| `type` | yes | Must be `LOOP` |
| `mode` | yes | `WHILE` or `DO_WHILE` |
| `condition` | yes | Condition controlling whether another iteration is executed |
| `maxIterations` | yes | Maximum number of iterations |
| `timeoutMs` | yes | Maximum total runtime of the complete loop in milliseconds |
| `steps` | yes | Child steps executed during one iteration |
| `id` | no | Optional step identifier |

---

# Condition semantics

The LOOP condition is a **continuation condition**.

```text
condition = true
    → execute another iteration

condition = false
    → terminate the LOOP successfully
```

Example:

```yaml
condition: "{B[count]} == 0"
```

This means:

```text
count == 0
    → continue polling

count != 0
    → exit LOOP
```

LOOP uses the general AGATE Condition Handling syntax.

Examples:

```yaml
condition: "{B[count]} == 0"
```

```yaml
condition: "{B[status]} != 'READY'"
```

```yaml
condition: "{B[status]} == 'PENDING' OR {B[status]} == 'RUNNING'"
```

```yaml
condition: "NOT ({B[status]} == 'DONE' OR {B[status]} == 'SUCCESS')"
```

---

# WHILE

`WHILE` checks the condition **before** executing the child steps.

```yaml
- type: LOOP
  mode: WHILE
  condition: "{B[continue_processing]} == 'true'"
  maxIterations: 10
  timeoutMs: 30000
  steps:

    - type: CMD
      op: EXEC
      command: "echo processing"
```

Execution:

```text
Condition
   │
   ├── false → LOOP ends
   │
   └── true
        ↓
     Child steps
        ↓
     Condition
```

A `WHILE` loop can therefore execute **zero times**.

Use `WHILE` when the value used by the condition already exists before the LOOP starts.

Example:

```yaml
- type: BUFFER
  op: EXEC
  name: continue_processing
  value: "true"

- type: LOOP
  mode: WHILE
  condition: "{B[continue_processing]} == 'true'"
  maxIterations: 10
  timeoutMs: 30000
  steps:
    ...
```

---

# DO_WHILE

`DO_WHILE` executes the complete child-step list first and evaluates the condition afterwards.

```yaml
- type: LOOP
  mode: DO_WHILE
  condition: "{B[result_count]} == 0"
  maxIterations: 30
  timeoutMs: 130000
  steps:

    - type: SQL
      op: EXEC
      command: "SELECT ..."
      response: result

    - type: SQL
      op: BUFFER
      response: result
      row: "0"
      column: "0"
      name: result_count

    - type: WAIT
      value: "4000"
```

Execution:

```text
Child steps
    ↓
Condition
    │
    ├── true → next iteration
    │
    └── false → LOOP ends
```

A `DO_WHILE` loop executes at least **one iteration**.

`DO_WHILE` is the preferred mode for polling when the condition value is created inside the LOOP.

---

# Recommended polling pattern

The recommended AGATE polling pattern is:

```text
ENGINE EXEC
     ↓
ENGINE BUFFER
     ↓
WAIT
     ↓
LOOP condition
```

Example:

```yaml
- id: wait_until_patient_registered
  type: LOOP
  mode: DO_WHILE
  condition: "{B[patient_registered_count]} == 0"
  maxIterations: 30
  timeoutMs: 130000
  steps:

    - type: SQL
      op: EXEC
      command: >
        SELECT COUNT(*)
        FROM DMP_BETREUUNGSVERHAELTNIS
        WHERE FK_SV_NUMMER = '{R[Request.SVNR]}'
          AND BETREUUNGSSTATUS = 'Ein'
      response: patient_registered_response

    - type: SQL
      op: BUFFER
      response: patient_registered_response
      row: "0"
      column: "0"
      name: patient_registered_count

    - type: WAIT
      value: "4000"
```

Possible execution:

```text
Iteration 1

SQL     → 0
BUFFER  → patient_registered_count = 0
WAIT    → 4000 ms
Condition: 0 == 0 → true


Iteration 2

SQL     → 0
BUFFER  → patient_registered_count = 0
WAIT    → 4000 ms
Condition: 0 == 0 → true


Later iteration

SQL     → 1
BUFFER  → patient_registered_count = 1
WAIT    → 4000 ms
Condition: 1 == 0 → false

LOOP ends successfully.
```

---

# Why ENGINE → BUFFER → Condition?

Prefer:

```text
ENGINE
   ↓
RESPONSE
   ↓
BUFFER
   ↓
CONDITION
```

Instead of coupling the LOOP directly to an engine-specific response structure.

Example:

```yaml
- type: SQL
  op: EXEC
  command: "SELECT COUNT(*) FROM ..."
  response: result

- type: SQL
  op: BUFFER
  response: result
  row: "0"
  column: "0"
  name: result_count
```

Then:

```yaml
condition: "{B[result_count]} == 0"
```

The LOOP condition is now independent of whether the value originally came from:

```text
SQL
REST
SOAP
CMD
OC
FILE
...
```

---

# Safety guards

Every LOOP should define:

```yaml
maxIterations: ...
timeoutMs: ...
```

They protect against different problems.

## maxIterations

Limits the maximum number of loop iterations.

```yaml
maxIterations: 30
```

If the condition still requires another iteration after the maximum number of iterations has been reached, the LOOP fails.

`maxIterations` is a safety guard, not a normal exit condition.

---

## timeoutMs

Limits the maximum total runtime of the LOOP.

```yaml
timeoutMs: 130000
```

The timeout includes the execution time of all child steps, including:

```text
SQL
REST
SOAP
CMD
OC
FILE
CALL
WAIT
...
```

---

# maxIterations and timeoutMs

Both safety guards should normally be used together.

Example:

```yaml
maxIterations: 30
timeoutMs: 130000
```

with:

```yaml
- type: WAIT
  value: "4000"
```

The maximum WAIT time alone is:

```text
30 × 4000 ms = 120000 ms
```

But SQL execution, buffer processing, condition evaluation and logging also require time.

Therefore:

```yaml
timeoutMs: 130000
```

is safer than:

```yaml
timeoutMs: 120000
```

---

# Important DO_WHILE behavior

`DO_WHILE` executes the **complete child-step list** before evaluating the condition.

Example:

```yaml
steps:

  - type: SQL
    ...

  - type: SQL
    op: BUFFER
    ...

  - type: WAIT
    value: "4000"
```

Even when SQL already finds the expected state:

```text
SQL    → 1
BUFFER → 1
```

the following step is still executed:

```text
WAIT 4000
```

Only afterwards is the condition evaluated:

```text
Condition → false
LOOP ends
```

---

# No BREAK step

The LOOP engine has no separate:

```yaml
- type: BREAK
```

step.

Termination is controlled through the LOOP condition.

Instead of thinking:

```text
BREAK when count > 0
```

define the continuation condition:

```yaml
condition: "{B[count]} == 0"
```

As long as:

```text
count == 0
```

the LOOP continues.

When:

```text
count != 0
```

the LOOP ends.

---

# LOOP vs WAIT

`WAIT` and `LOOP` solve different problems.

| WAIT | LOOP |
|---|---|
| waits for a fixed duration | waits for a condition |
| no repetition | repeatedly executes child steps |
| no condition | condition-controlled |
| may wait unnecessarily long | can finish as soon as the target state is reached |
| may be too short | checks the actual system state |

Static waiting:

```yaml
- type: WAIT
  value: "15000"
```

means:

```text
Always wait 15 seconds.
```

Polling:

```yaml
- type: LOOP
  mode: DO_WHILE
  condition: "{B[ready]} != 'true'"
  maxIterations: 20
  timeoutMs: 60000
  steps:
    ...
```

means:

```text
Keep checking until ready == true.
```

---

# Best practices

For polling, prefer:

```yaml
mode: DO_WHILE
```

when the condition value is generated inside the LOOP.

Prefer:

```text
ENGINE EXEC
→ BUFFER
→ WAIT
→ CONDITION
```

Choose a polling interval appropriate for the target system, for example:

```yaml
value: "1000"
```

```yaml
value: "2000"
```

```yaml
value: "4000"
```

Always define both safety guards:

```yaml
maxIterations: ...
timeoutMs: ...
```

Keep LOOP conditions simple and move technical response extraction into buffers.

---

# AI generation rules

When generating AGATE LOOP steps:

1. Prefer `DO_WHILE` for polling.
2. Use `WHILE` only when the condition value already exists before the LOOP.
3. Always generate `maxIterations`.
4. Always generate `timeoutMs`.
5. Prefer:

```text
ENGINE EXEC
→ ENGINE BUFFER
→ WAIT
→ LOOP condition
```

6. Treat the LOOP condition as a **continuation condition**:
   - `true` → another iteration
   - `false` → successful exit
7. Do not generate a `BREAK` step.
8. Use the general AGATE Condition Handling syntax for conditions.
9. Remember that the complete `DO_WHILE` body executes before the condition is checked.