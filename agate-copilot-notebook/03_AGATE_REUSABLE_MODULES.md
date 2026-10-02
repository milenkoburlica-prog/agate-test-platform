# AGATE Reusable Modules

Reusable Modules allow commonly used AGATE step sequences to be stored in separate YAML files and invoked through:

```yaml
type: CALL
```

Typical use cases include login flows, setup/cleanup sequences, shared validations and technical preparation.

## 1. CALL Syntax

```yaml
- type: CALL
  command: 'reusable.demo_reusable'
  condition: "..."
  parameters:
    verbose: 'true'
    command: 'mvn --version'
```

Core fields:

```text
type        -> CALL
command     -> logical reusable path
condition   -> optional
parameters  -> optional R-buffer parameters
```

---

## 2. File Resolution

`command` uses dot notation.

Example:

```text
reusable.demo_reusable
```

resolves relative to the current suite to:

```text
./reusable/demo_reusable.yaml
```

The reusable YAML contains a flat `steps:` list.

Example:

```yaml
steps:
  - type: CMD
    op: EXEC
    command: '{R[command]}'
    response: resp1
```

---

## 3. B Buffer vs R Buffer

Reusable Modules can access two distinct value sources.

### B Buffer

```text
{B[name]}
```

Refers to the test-case/global runtime context.

Reusable modules can read and write that context.

### R Buffer

```text
{R[name]}
```

Refers specifically to parameters passed through the caller's `parameters:` block.

Caller:

```yaml
- type: CALL
  command: 'reusable.demo_reusable'
  parameters:
    command: 'mvn --version'
```

Reusable:

```yaml
command: '{R[command]}'
```

These are not the same value source.

---

## 4. Example Difference

Main test:

```yaml
variables:
  command: 'java -version'
```

CALL:

```yaml
- type: CALL
  command: 'reusable.demo_reusable'
  parameters:
    command: 'mvn --version'
```

Inside reusable:

```text
{B[command]} -> java -version
{R[command]} -> mvn --version
```

---

## 5. Missing Optional R Buffer

The supplied documentation states that an unset optional R-buffer does not automatically have to produce an exception when assigned into an already initialized target.

Example concept:

```text
target B value = not-set
R parameter omitted
-> existing target may remain not-set
```

If a reusable parameter is mandatory, validate that requirement explicitly in test logic.

---

## 6. Verbose Logging

Reusable modules suppress detailed internal DSL logging by default.

Default:

```text
verbose = false
```

Enable detailed reusable-step logging with:

```yaml
parameters:
  verbose: 'true'
```

Use this mainly for troubleshooting.

---

## 7. Parameter Reference

```text
type
    required
    must be CALL

command
    required
    logical path to reusable YAML

condition
    optional
    common AGATE condition

parameters
    optional
    key/value map exposed through {R[...]}

parameters.verbose
    optional
    true/false
    default false
```

---

## 8. Reusable File Example

```yaml
steps:

  - type: CMD
    op: EXEC
    command: '{B[command]}'
    response: resp1

  - type: CMD
    op: ASSERT
    response: resp1
    action: EXITCODE
    expected: 0

  - type: CMD
    op: EXEC
    command: '{R[command]}'
    response: resp2
```

---

## 9. Caller Example

```yaml
testCases:

  - id: 'TC_REUSABLE_DEMO'
    description: 'Reusable module demo'
    stage: '*'
    priority: HIGH

    variables:
      command: 'java -version'

    steps:

      - type: CALL
        command: 'reusable.demo_reusable'
        parameters:
          verbose: 'true'
          command: 'mvn --version'
```

---

## 10. AI Guidance

1. Use `type: CALL` for reusable modules.
2. Do not add `op` unless future CALL documentation explicitly defines it.
3. Resolve `command` as reusable dot notation.
4. Use `{R[...]}` for CALL parameters.
5. Use `{B[...]}` for the shared test-case/runtime context.
6. Never treat `{R[...]}` and `{B[...]}` as interchangeable.
7. Preserve the default `verbose: false` behavior unless detailed logging is requested.
8. If a reusable parameter must be mandatory, do not assume it is automatically enforced; add explicit validation where appropriate.
9. The reusable file contains `steps:`, not a full `testCases:` structure.
10. Engine-specific syntax inside reusable modules still comes from the matching engine documentation.
