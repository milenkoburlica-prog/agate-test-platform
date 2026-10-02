# AGATE Test Structure & General Rules

This document defines the common structure and organization of AGATE test cases.

It is intentionally separate from:

- YAML syntax rules,
- engine-specific syntax,
- buffer/placeholder details,
- reusable test documentation.

The purpose of this document is to teach an AI assistant how a complete AGATE test is structured.

---

# 1. Root Structure

An AGATE test file uses the root element:

```yaml
testCases:
```

One file may contain one or more test cases below this element.

Example:

```yaml
testCases:

  - id: 'TC_001'
    description: 'Simple AGATE test'
    stage: '*'
    priority: HIGH
    variables: {}
    steps:
      ...
```

When asked to generate a complete AGATE test, generate the full `testCases:` structure rather than only a `steps:` fragment unless the user explicitly requests only steps.

---

# 2. Test Case Structure

A typical AGATE test case contains:

```yaml
testCases:

  - id: '...'
    description: '...'
    stage: '*'
    priority: HIGH
    variables: {}
    steps:
      ...
```

The commonly used top-level test-case fields are:

| Field | Purpose |
|---|---|
| `id` | Unique or descriptive identifier of the test case. |
| `description` | Human-readable description of the test. |
| `stage` | Defines the environment/stage applicability. `'*'` is commonly used for all stages. |
| `priority` | Test priority, e.g. `HIGH`. |
| `variables` | Test-local initial values. Can be `{}` when no initial variables are required. |
| `steps` | Ordered list of AGATE test steps. |

---

# 3. Example

```yaml
testCases:

  - id: 'Gutfall: Ungültige SvNummer angegeben. Kein Fehler weil die SV-Nummer eine gültige Prüfziffer hat'
    description: 'DMP SS12 v11 getAdminPatientenInformationen'
    stage: '*'
    priority: HIGH
    variables: {}

    steps:

      # --- Test data: Set test data ---

      - type: BUFFER
        op: EXEC
        name: SVNR
        value: '0000000000'

      - type: BUFFER
        op: EXEC
        name: B_Karte
        value: '645031'

      - type: BUFFER
        op: EXEC
        name: B_OrdinationsId
        value: '15682076'
```

This demonstrates a complete AGATE test case rather than an isolated engine fragment.

---

# 4. Test Case ID

`id` identifies the test case.

It can be technical:

```yaml
id: 'TC_01_Demo_SQL'
```

or descriptive:

```yaml
id: 'Gutfall: Ungültige SvNummer angegeben'
```

For generated tests, prefer a stable and meaningful identifier.

Examples:

```yaml
id: 'TC_SQL_FILE_CMD_OC_DEMO'
```

```yaml
id: 'TC_KAMS_Report_Validation'
```

Do not invent an existing project-specific naming convention if none is documented.

---

# 5. Description

`description` should explain the business or technical purpose of the test.

Example:

```yaml
description: 'DMP SS12 v11 getAdminPatientenInformationen'
```

or:

```yaml
description: 'Validates database data and file transfer to OpenShift'
```

Avoid meaningless descriptions such as:

```yaml
description: 'Test'
```

when a more precise description can be derived from the requirement.

---

# 6. Stage

A commonly used value is:

```yaml
stage: '*'
```

This means the test is not restricted to one specific stage.

When the requested stage is unknown, do not invent a stage-specific value.

Prefer:

```yaml
stage: '*'
```

if this matches the established AGATE test pattern.

If a specific stage is explicitly required, use the documented project value.

---

# 7. Priority

A typical AGATE test uses:

```yaml
priority: HIGH
```

Use the priority supplied by the requirement when available.

Do not invent a project-specific priority classification beyond documented examples.

---

# 8. Variables

`variables` contains initial values defined at test-case level.

If no initial values are needed:

```yaml
variables: {}
```

Example with variables:

```yaml
variables:
  service: 'my-service'
  filename: 'test.txt'
  baseDir: 'output/file-demo'
```

These values can later be referenced through the normal AGATE dynamic-value mechanism.

---

# 9. Steps

All executable test logic is defined below:

```yaml
steps:
```

Steps are executed in their defined order unless control-flow features such as conditions or loops alter execution.

Example:

```yaml
steps:

  - type: SQL
    op: EXEC
    command: 'SELECT ID FROM CUSTOMER'
    response: customer_result

  - type: SQL
    op: ASSERT
    response: customer_result
    source: ROW_COUNT
    action: GREATER_THAN_OR_EQUAL
    expected: '1'
```

Each step belongs to an AGATE engine.

Typical examples include:

```text
SQL
CMD
OC
FILE
BUFFER
REST
SOAP
CALL
WAIT
LOOP
...
```

The valid fields of each step are defined by the corresponding engine documentation.

---

# 10. Test Data Initialization

Test data is often prepared at the beginning of the `steps` section.

Example:

```yaml
# --- Test data: Set test data ---

- type: BUFFER
  op: EXEC
  name: SVNR
  value: '0000000000'

- type: BUFFER
  op: EXEC
  name: B_Karte
  value: '645031'
```

This makes test inputs explicit and keeps later business steps readable.

A generated AGATE test should prefer explicit initialization when the requirement provides concrete values.

---

# 11. Section Comments

Comments may be used to make larger test cases easier to understand.

Preferred style:

```yaml
# --- Test data ---

# --- Preparation ---

# --- Business execution ---

# --- Validation ---

# --- Cleanup ---
```

Comments should describe logical test phases and should not overwhelm the actual DSL.

---

# 12. Typical Test Organization

A larger AGATE test can follow this logical structure:

```text
Test data
    |
    v
Preparation
    |
    v
Business execution
    |
    v
Validation
    |
    v
Cleanup
```

Example:

```yaml
testCases:

  - id: 'TC_Example'
    description: 'Example structured AGATE test'
    stage: '*'
    priority: HIGH
    variables: {}

    steps:

      # --- Test data ---

      ...

      # --- Preparation ---

      ...

      # --- Business execution ---

      ...

      # --- Validation ---

      ...

      # --- Cleanup ---

      ...
```

Not every test needs all phases.

Only include phases required by the actual scenario.

---

# 13. Complete Test vs. Step Fragment

When the user asks:

```text
Create an AGATE test
```

generate:

```yaml
testCases:
  - id: ...
    description: ...
    stage: '*'
    priority: HIGH
    variables: {}
    steps:
      ...
```

Do not return only:

```yaml
steps:
  ...
```

unless the user explicitly asks for:

- only the steps,
- a reusable step fragment,
- an engine example,
- or a partial YAML snippet.

---

# 14. Engine Documentation Has Precedence

This document defines test structure.

It does not redefine engine syntax.

For example:

```yaml
- type: SQL
  ...
```

must follow the SQL Engine documentation.

```yaml
- type: FILE
  ...
```

must follow the FILE Engine documentation.

```yaml
- type: CMD
  ...
```

must follow the CMD Engine documentation.

```yaml
- type: OC
  ...
```

must follow the OC Engine documentation.

Do not infer unsupported fields from another engine just because the overall step structure looks similar.

---

# 15. General Generation Rules

When generating a complete AGATE test:

1. Start with `testCases:`.
2. Create a meaningful `id`.
3. Add a useful `description`.
4. Use the requested `stage`; otherwise use the established generic stage pattern when appropriate.
5. Set the requested priority; otherwise follow documented examples.
6. Add `variables: {}` when no test-local variables are required.
7. Put executable logic under `steps:`.
8. Keep steps in the logical execution order.
9. Use section comments for larger tests where helpful.
10. Initialize known test data explicitly.
11. Use only documented engine fields and actions.
12. Do not generate only a `steps:` fragment when a complete test was requested.
13. Do not invent business values, table names, paths, pod names or expected results without clearly marking them as assumptions.
14. Do not mark something as an assumption if it is already defined by AGATE documentation.

---

# 16. Preferred Example for AI Generation

```yaml
testCases:

  - id: 'TC_SQL_FILE_CMD_OC_DEMO'
    description: 'Cross-engine demonstration using SQL, FILE, CMD and OC'
    stage: '*'
    priority: HIGH

    variables:
      service: 'my-service'

    steps:

      # --- Database validation ---

      - type: SQL
        op: EXEC
        command: |
          SELECT ID, STATUS
          FROM USERS
          WHERE STATUS = 'ACTIVE'
        response: user_result

      - type: SQL
        op: ASSERT
        response: user_result
        source: ROW_COUNT
        action: GREATER_THAN_OR_EQUAL
        expected: '1'

      - type: SQL
        op: BUFFER
        response: user_result
        row: 0
        column: 'ID'
        name: user_id

      # --- Local file processing ---

      - type: FILE
        op: EXEC
        action: WRITE
        path: 'output/user-{B[user_id]}.txt'
        text: 'User-ID: {B[user_id]}'
        overwrite: true
```

This example demonstrates the expected shape of a complete AGATE test.

---

# 17. AI Guidance

An AI assistant working with AGATE should understand the distinction between:

```text
AGATE test structure
        vs.
engine-specific syntax
        vs.
YAML syntax
```

Use:

- this document for test-case structure,
- `DSL_YAML_RULES` for YAML/style rules,
- engine files for engine-specific fields and actions.

When asked to create a complete test, combine all three layers.

---

# 18. Assumptions vs. Inputs vs. Test Expectations

When generating AGATE tests, do not classify every external or unknown value as an assumption.

The assistant must distinguish between four categories.

## 18.1 User-Provided Input

A value explicitly supplied by the user or task description is an input, not an assumption.

Example requirement:

```text
Use pod my-service.
```

Generated YAML:

```yaml
pod: 'my-service'
```

This is not an assumption.

Another example:

```text
Store the remote output in buffer remote_file_content.
```

Generated YAML:

```yaml
name: remote_file_content
```

Again, this is user-provided input.

---

## 18.2 Test Expectation

A condition that the test is supposed to verify is a test expectation.

It must not be converted into an assumption merely because the assistant cannot know in advance whether the tested system will satisfy it.

Example:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: '1'
```

This means:

```text
The test expects at least one result row.
```

It does NOT mean:

```text
Assumption: At least one result row exists.
```

The purpose of the test is to verify the expectation. If the system does not satisfy it, the test should fail.

Another example:

```yaml
- type: FILE
  op: ASSERT
  response: file_content
  action: CONTAINS
  value: 'ACTIVE'
```

This expresses an expected result. Do not list `The file contains ACTIVE` as an assumption.

---

## 18.3 Assumption

An assumption is information that is required to construct the test but is not provided by:

- the user,
- the AGATE documentation,
- the engine documentation,
- or an explicitly supplied test requirement.

Example:

```sql
SELECT ID, STATUS
FROM USERS
WHERE STATUS = 'ACTIVE'
```

If the requirement only says to find a user with `ID` and `STATUS`, then the following may legitimately be assumptions:

```text
A table USERS exists.
USERS contains columns ID and STATUS.
```

Another valid assumption:

```text
The local runtime supports the Windows command type.
```

when a generated CMD step uses:

```yaml
command: 'type "output\user.txt"'
```

and the operating system was not specified.

---

## 18.4 Documented AGATE Behavior

Behavior explicitly defined by the AGATE documentation must never be listed as an assumption.

Examples:

```text
FILE ASSERT EXISTS uses path.
FILE content ASSERT uses response.
FILE BUFFER operates on a previous READ response.
OC resolves a pod prefix to a running pod.
CMD supports response + ASSERT.
SQL BUFFER extracts a cell into a buffer.
```

If these rules are documented in the supplied AGATE knowledge files, they are facts within the scope of test generation.

---

## 18.5 Decision Rule

Before adding an item to an `Assumptions` section, classify it:

```text
Was it explicitly provided by the user?
    YES -> Input, not assumption.

Is it something the test is supposed to verify?
    YES -> Test expectation, not assumption.

Is it explicitly documented by AGATE?
    YES -> Documented behavior, not assumption.

Is it required to construct the test but none of the above applies?
    YES -> Assumption.
```

---

## 18.6 Critical AI Rule

A test expectation MUST NOT be converted into an assumption simply because the assistant cannot know whether the system will satisfy it.

Bad:

```text
Assumption:
At least one ACTIVE user exists.
```

when the generated test already contains:

```yaml
source: ROW_COUNT
action: GREATER_THAN_OR_EQUAL
expected: '1'
```

Good:

```text
Expected behavior:
The query must return at least one ACTIVE user.
```

If no such user exists, the test should fail.

---

## 18.7 Documentation Gaps

Only report a documentation gap when the required syntax or behavior is genuinely absent from the provided AGATE documentation.

Do not report a documentation gap when:

- the syntax is already shown in an engine file,
- the requested value was explicitly supplied by the user,
- the requirement is a test expectation,
- or the missing information is actually a business/environment assumption.

Example of a real documentation gap:

```text
The requirement asks AGATE to create a directory,
but no supplied engine documentation defines a directory-creation action.
```

Example of something that is NOT a documentation gap:

```text
The FILE ASSERT syntax is unknown.
```

if the supplied FILE Engine documentation already defines it.

---

# 19. AI Output Discipline

When generating a complete AGATE test:

1. Generate the YAML first from documented AGATE rules.
2. Add an `Assumptions` section only when actual assumptions are necessary.
3. Do not duplicate test expectations as assumptions.
4. Do not repeat user-provided values as assumptions.
5. Do not describe documented AGATE behavior as assumptions.
6. Report a documentation gap only when no supplied AGATE document defines the required capability.
7. Keep assumptions short, concrete and external to the DSL itself.

Preferred example:

```text
Assumptions:
1. A table USERS exists with columns ID and STATUS.
2. The local runtime supports the Windows command type.
3. The local output directory already exists.
```

Do not add:

```text
4. At least one ACTIVE user exists.
5. The pod prefix my-service is correct.
6. FILE ASSERT EXISTS uses path.
```

because these are respectively:

```text
test expectation
user-provided input
documented AGATE behavior
```

---

# 20. Deterministic Test Construction

AI-generated tests should prefer deterministic test logic over avoidable assumptions.

A test should constrain or identify the intended business entity as early as the documented engine syntax allows.

Example requirement:

```text
Find an ACTIVE user and validate its ID and STATUS.
```

Preferred SQL:

```sql
SELECT ID, STATUS
FROM USERS
WHERE STATUS = 'ACTIVE'
```

Then:

```text
assert ROW_COUNT >= 1
extract row 0
validate/use the returned values
```

Avoid:

```sql
SELECT ID, STATUS
FROM USERS
```

followed by:

```text
Assumption: row 0 is the intended ACTIVE user.
```

The second form creates uncertainty that is caused by the generated implementation itself.

## 20.1 Do Not Manufacture Assumptions

Before adding an assumption, ask:

1. Is the information absent from the user requirement?
2. Is it absent from AGATE documentation?
3. Is it genuinely external/business/environment information?
4. Could the generated test be made deterministic without inventing syntax?

If the test can be improved using documented syntax, improve the test instead of adding an assumption.

## 20.2 Business Condition vs Test Expectation

A business condition can often be expressed in the execution step and then independently verified.

Example:

```sql
SELECT ID, STATUS
FROM USERS
WHERE STATUS = 'ACTIVE'
```

followed by:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: '1'
```

This is preferable to relying on unspecified row ordering.

## 20.3 Selection Must Be Explicit When Ordering Matters

Do not assume that `row: 0` refers to a meaningful business entity unless the query or requirement makes that deterministic.

If the requirement identifies a user by ID, VSNR, CIN, status, date or other key, use that criterion in the query whenever possible.

If no selection criterion is provided, that absence may be a valid business-data assumption.

## 20.4 AI Self-Check Before Final Output

Before returning a generated test, check:

- Is every selected row deterministically identified?
- Does any assumption exist only because the generated query is too broad?
- Did I introduce arbitrary ordering dependence?
- Can a documented filter remove that ambiguity?
- Are assumptions limited to genuine missing external information?

If the answer reveals avoidable ambiguity, revise the test before returning it.


---

# 21. Missing Information Must Not Block Generation

AGATE test generation should be best-effort.

If external information is missing but the requested test structure is still clear, generate the complete YAML and place assumptions after the test.

Examples of external details that may be assumed when necessary:

- example table names,
- example column names,
- existence of a local directory,
- operating-system command availability,
- sample endpoint names,
- sample pod names,
- sample test-data identifiers.

These assumptions must be:

```text
explicit
minimal
easy to replace
outside the YAML semantics
```

They must not be presented as documented AGATE facts.

## 21.1 Generate First, Assumptions Second

Do:

```text
<complete YAML>

Assumptions:
1. ...
2. ...
```

Do not respond with:

```text
I cannot generate the test because the table name is missing.
```

when a clearly labeled example assumption is sufficient.

## 21.2 Missing External Details vs Missing DSL Syntax

These are different:

```text
Missing table name
    -> generate with an explicit assumption

Missing directory existence
    -> generate with an explicit assumption

Unknown OS command
    -> generate with an explicit environment assumption

Unknown AGATE field/action syntax
    -> do NOT invent syntax
```

Only missing AGATE DSL syntax should prevent generation of that specific unsupported construct.

## 21.3 Best-Effort Rule

When a user asks for a complete AGATE test, the default objective is:

```text
produce a runnable-looking complete test
using documented AGATE syntax
with clearly marked replaceable assumptions
```

Do not defer generation to a later answer merely because assumptions are required.

---

# 22. Documentation Gap Classification

A difference between AGATE components is not automatically a documentation gap.

AGATE has multiple layers:

```text
shared DSL
engine-specific DSL
control-flow semantics
runtime/environment details
business/test-data details
```

Each layer may support different capabilities.

## 22.1 Engine-Specific Capability Is Valid

Example requirement:

```text
Assert that SQL ROW_COUNT >= 1.
```

Shared `condition` does not support:

```text
>=
```

But SQL ASSERT documents:

```text
GREATER_THAN_OR_EQUAL
```

Therefore the correct solution is:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: '1'
```

This is **not** a documentation gap.

The shared `condition` evaluator is simply not the feature used for this requirement.

## 22.2 Do Not Compare Unrelated DSL Layers

Do not reason like this:

```text
condition cannot express >=
therefore AGATE cannot express >=
```

Instead ask:

```text
Which AGATE feature owns this requirement?
```

Examples:

```text
conditional step execution
    -> shared condition syntax

SQL row count comparison
    -> SQL ASSERT

CMD exit-code validation
    -> CMD ASSERT

REST response validation
    -> REST ASSERT

FILE existence validation
    -> FILE ASSERT
```

Use the syntax owned by the relevant feature.

## 22.3 When a Real Documentation Gap Exists

Report a documentation gap only if all of the following are true:

1. the requested capability is required,
2. the responsible AGATE component is identified,
3. its documentation does not define the necessary syntax or behavior,
4. no other documented AGATE feature covers the requirement.

Example of a possible real gap:

```text
Requirement needs operation X.
Relevant engine documentation has no operation/action capable of X.
```

Then state the gap precisely.

## 22.4 External Missing Information Is Not a Documentation Gap

Examples:

```text
table name missing
column name missing
directory may not exist
OS not specified
pod name not provided
endpoint not provided
```

These are:

```text
business assumptions
test-data assumptions
environment assumptions
```

They are not missing AGATE documentation.

## 22.5 AI Self-Check

Before reporting a documentation gap:

1. Identify the responsible AGATE component.
2. Check its authoritative document.
3. Check whether another documented AGATE feature already handles the requirement.
4. Distinguish syntax gaps from environment/business assumptions.
5. Only then report a gap.

Preferred final structure:

```text
<complete YAML>

Assumptions:
...

Documentation gaps:
None.
```

when all required AGATE operations are documented.

---

# 23. Requirement Relevance Rule for Documentation Gaps

A missing or undocumented capability is relevant only if the current user requirement actually needs that capability.

Do not report hypothetical, unused or alternative capabilities as documentation gaps.

## 23.1 Required Capability Test

Before reporting a documentation gap, ask:

```text
Does the requested test actually require this missing capability?
```

If the answer is no:

```text
do not report a documentation gap
```

Example requirement:

```text
Verify that a local file exists.
```

Documented solution:

```yaml
- type: FILE
  op: ASSERT
  action: EXISTS
  path: 'output/user-{B[user_id]}.txt'
```

Suppose FILE also documents:

```text
EXEC EXISTS -> stores a Boolean response
```

but does not document how that Boolean response can be reused as `{B[...]}`.

This is **not** a documentation gap for the requirement above, because the requirement is already fully satisfied by:

```text
FILE ASSERT EXISTS
```

The test does not need to convert the Boolean response into a B-variable.

---

## 23.2 Alternative Capability Is Not Automatically Required

Do not reason like this:

```text
AGATE supports method A
method B might also be useful
method B reuse semantics are undocumented
therefore documentation gap
```

Instead reason:

```text
Does method A completely satisfy the requirement?
```

If yes:

```text
use method A
report no gap
```

---

## 23.3 Prefer the Simplest Documented Construct That Fully Satisfies the Requirement

If several documented approaches are possible, prefer the simplest one that directly satisfies the user's request.

Examples:

```text
Need to verify file existence
    -> FILE ASSERT EXISTS

Need SQL row count >= 1
    -> SQL ASSERT / ROW_COUNT / GREATER_THAN_OR_EQUAL

Need remote command output as a buffer
    -> OC BUFFER action TEXT

Need to validate local file content
    -> FILE READ + FILE ASSERT
```

Do not introduce extra intermediate mechanisms unless the requirement needs them.

---

## 23.4 Do Not Create Gaps From Unused Responses

A response object may expose capabilities that the current test does not consume.

Example:

```text
FILE EXEC EXISTS
    -> Boolean response
```

If the current test only needs:

```text
file exists?
```

then:

```yaml
- type: FILE
  op: ASSERT
  action: EXISTS
  path: '...'
```

is sufficient.

The undocumented reuse of the Boolean response is irrelevant to that test.

---

## 23.5 Requirement-to-Capability Mapping

Before generating the test, map each user requirement to one documented AGATE capability.

Example:

```text
Requirement:
query ACTIVE user
    -> SQL EXEC

verify at least one row
    -> SQL ASSERT ROW_COUNT

extract ID
    -> SQL BUFFER

write local file
    -> FILE WRITE

verify local file exists
    -> FILE ASSERT EXISTS

read local file
    -> FILE READ

verify CMD output contains ID
    -> CMD ASSERT CONTAINS

copy file to pod
    -> OC PUT

verify remote content
    -> OC EXEC + OC ASSERT

store remote output
    -> OC BUFFER TEXT
```

If every requirement maps to a documented capability:

```text
Documentation gaps: None.
```

Do not add hypothetical gaps for other unused capabilities.

---

## 23.6 Final Documentation Gap Decision

Report a documentation gap only if all of the following are true:

1. The current user requirement needs capability X.
2. Capability X belongs to a known AGATE component.
3. The authoritative documentation for that component does not define how to express X.
4. No other documented AGATE construct satisfies the same requirement.
5. The missing information is AGATE syntax/behavior, not external environment/business information.

If any of these conditions is false:

```text
do not report a documentation gap
```

---

# 24. Assumption Relevance Rule

Only list assumptions that are genuinely external and actually relevant to the generated test.

## Valid assumptions

Examples:

```text
The example schema contains USERS(ID, STATUS).
The local output directory already exists.
The local CMD runtime is Unix/Linux and supports cat.
A running OpenShift pod matching the provided prefix exists.
```

## Not assumptions

Do not list as assumptions:

```text
values explicitly given by the user
test expectations explicitly requested by the user
documented AGATE behavior
properties made deterministic by the generated test itself
```

Example:

If the user requirement says:

```text
STATUS = ACTIVE
```

then:

```text
ACTIVE is the required status
```

is a test expectation, not an assumption.

If the user provides:

```text
pod: my-service
```

then:

```text
the pod prefix is my-service
```

is input, not an assumption.

A valid environment assumption may instead be:

```text
A running pod matching the provided prefix my-service exists.
```

---

# 25. Final AI Self-Check

Before returning a complete generated test, perform this checklist.

## Syntax

- Did I use only documented AGATE operations, fields and actions?
- Did I consult the authoritative engine document for every engine?

## Determinism

- Did I constrain business selection where the requirement allows it?
- Am I relying on arbitrary row ordering?
- Can a documented filter remove that ambiguity?

## Assumptions

- Is each assumption truly external?
- Did the user already provide this information?
- Is it actually a test expectation rather than an assumption?
- Did I add an assumption only to compensate for a weak implementation?

## Documentation gaps

- Is the missing capability actually required?
- Is there already a documented alternative that satisfies the requirement?
- Am I reporting a hypothetical or unused capability as a gap?
- Is the issue really AGATE syntax rather than business/environment information?

## Output structure

Preferred:

```text
<complete YAML>

Assumptions:
1. ...

Documentation gaps:
None.
```

If and only if a real required AGATE capability is undocumented:

```text
Documentation gaps:
- <precise required missing capability>
```

Never create a gap merely because an unused alternative mechanism is not documented.

