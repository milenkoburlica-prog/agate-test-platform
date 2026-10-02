# AGATE Variables and Dynamic Values

AGATE uses several variable sources and runtime keywords. Their scopes and resolution times are different and must not be mixed.

## 1. E Variables

`{E[...]}` reads values from environment configuration.

### env.conf

`env/env.conf` defines environment-specific technical values.

Example configuration:

```text
DEMOS.database.user=D_DB
```

Usage:

```yaml
- type: CMD
  op: EXEC
  command: 'echo {E[env.database.user]}'
  response: buff_out
```

If the selected environment is `DEMOS`, this resolves to `D_DB`.

### users.conf

`env/users.conf` defines user-specific values inside an environment.

Conceptual configuration:

```text
<instance>.<user>.<variable>=<value>
```

Example:

```text
DEMOS.Milenko.staat=AUT
```

Usage:

```yaml
command: 'echo {E[users.staat]}'
```

If an E-variable cannot be resolved, the documentation states that it may remain unresolved in the YAML field instead of producing an explicit dedicated error.

---

## 2. B Variables

`{B[...]}` refers to local test-case variables / buffer values.

Example:

```yaml
variables:
  retryCount: '3'

steps:
  - type: CMD
    op: EXEC
    command: 'echo {B[retryCount]}'
    response: buff_out
```

B values can come from:
- the test case `variables:` block,
- runtime BUFFER operations,
- other engine BUFFER operations that write into the test context.

Use B variables for values local to a test case or values reused across several steps.

---

## 3. R Variables

`{R[...]}` is reserved for reusable-module parameters passed through `type: CALL`.

Caller:

```yaml
- type: CALL
  command: 'reusable.logcheck'
  parameters:
    service: 'doan'
    code: 'ZS-00'
```

Inside the reusable module:

```yaml
command: '{R[service]}'
```

`{R[...]}` is not the same as `{B[...]}`.

---

## 4. XL Variables

`{XL[...]}` is used for Excel/CSV-driven template injection.

The documentation defines it as a specialized template mechanism for reading column values from an external data source while instantiating data-driven tests.

Important:

```text
{XL[...]} is intended for templates / data-driven instantiation.
```

Do not use XL variables as a generic runtime buffer replacement.

---

## 5. STRING and BOOLEAN Keywords

Documented dynamic keywords include:

```text
STRING:NULL
STRING:FIX:N
STRING:EMPTY
STRING:N:EMPTY
BOOLEAN:true
BOOLEAN:false
{RND[N]}
```

Meaning:

```text
STRING:NULL      -> empty value / directive removed
STRING:FIX:7     -> AAAAAAA
STRING:EMPTY     -> one empty/blank value according to resolver behavior
STRING:5:EMPTY   -> five spaces
BOOLEAN:true     -> true
BOOLEAN:false    -> false
{RND[5]}         -> random 5-digit numeric string, first digit not zero
```

Example:

```yaml
command: 'echo USER_{RND[5]}'
```

---

## 6. Runtime Keywords `{NULL}` and `{EMPT}`

These are protocol/runtime keywords and are not the same as the loadtime STRING keywords.

```text
{NULL}
    -> signals a null value to the downstream adapter

{EMPT}
    -> signals an empty string
```

They remain present until the corresponding runtime adapter interprets them.

The supplied documentation explicitly distinguishes this from loadtime keywords.

Use them only where the receiving engine/protocol defines their semantics.

---

## 7. DATE and DATETIME

Basic forms:

```text
{DATE}
{DATETIME}
```

Documented defaults:

```text
{DATE}      -> dd.MM.yyyy
{DATETIME}  -> dd.MM.yyyy HH:mm:ss
```

Extended form:

```text
{DATE[base][offset][format]}
{DATETIME[base][offset][format]}
```

Examples:

```text
{DATE[][][ddMMyyyy]}
{DATE[][+10d][]}
{DATE[22.01.2025][2d][]}
{DATETIME[][-200d][dd-MM-yyyy HH:mm:ss]}
```

The format uses Java `DateTimeFormatter` semantics.

---

## 8. Loadtime vs Runtime

This distinction is important.

Example:

```yaml
variables:
  datetime: '{DATETIME}'
```

Then:

```yaml
command: 'echo {B[datetime]}'
```

uses the value that was resolved when the test case was initialized.

By contrast:

```yaml
command: 'echo {DATETIME}'
```

resolves the current date/time when that step is executed.

Therefore:

```text
B variable containing DATETIME
    -> stable/frozen value for reuse

direct {DATETIME}
    -> freshly resolved runtime value
```

Use a B variable when several steps must share the same timestamp.

Use direct `{DATETIME}` when the actual current time at each step is required.

---

## 9. Variables Block vs Direct Access

Both styles are documented.

Encapsulated:

```yaml
variables:
  db_user: '{E[env.database.user]}'
  current_time: '{DATETIME}'

steps:
  - type: CMD
    op: EXEC
    command: 'echo {B[db_user]} {B[current_time]}'
```

Direct:

```yaml
- type: CMD
  op: EXEC
  command: 'echo {E[env.database.user]} {DATETIME}'
```

Use `variables:` when values are reused or should remain stable.

Use direct access when a value is simple and used only once.

---

## 10. AI Guidance

1. Keep `{E[...]}`, `{B[...]}`, `{R[...]}` and `{XL[...]}` conceptually separate.
2. Do not use `{R[...]}` outside reusable-module parameter semantics.
3. Do not treat `{XL[...]}` as a generic runtime variable.
4. Preserve the distinction between loadtime and runtime values.
5. Do not replace `{NULL}` with `{EMPT}` or vice versa.
6. Prefer B variables for stable values reused across steps.
7. Prefer direct DATE/DATETIME keywords when a fresh runtime value is required.
8. Do not invent variable sources that are not documented.
