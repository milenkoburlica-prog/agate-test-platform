# AGATE Knowledge Index

This file is the primary entry point for AI assistants and human readers using the AGATE knowledge pack.

Its purpose is to route every AGATE question to the correct authoritative document and to prevent incorrect cross-engine inference.

---

# 1. Shared AGATE Concepts

## `01_AGATE_VARIABLES.md`

Use for:

- `{E[...]}` environment/user variables
- `{B[...]}` local/test-case variables
- `{R[...]}` reusable-module parameters
- `{XL[...]}` Excel/CSV template injection
- STRING / BOOLEAN / random keywords
- `{NULL}` and `{EMPT}`
- `{DATE}` / `{DATETIME}`
- loadtime vs runtime behavior

---

## `02_AGATE_CONDITION_HANDLING.md`

Use for:

- `condition`
- supported comparison operators
- `AND`, `OR`, `NOT`
- parentheses and operator precedence
- NULL / EMPTY handling in conditions
- unsupported operators
- parser limitations

---

## `03_AGATE_REUSABLE_MODULES.md`

Use for:

- `type: CALL`
- reusable YAML modules
- dot-notation command resolution
- `parameters`
- `{R[...]}` vs `{B[...]}` inside reusable modules
- reusable logging / `verbose`

---

## `04_AGATE_RULES.md`

Use for:

- complete AGATE test-case structure
- `testCases`
- `id`, `description`, `stage`, `priority`, `variables`, `steps`
- AI generation rules
- assumptions
- documentation-gap classification
- deterministic test construction

---

## `05_DSL_YAML_RULES.md`

Use for:

- YAML quoting
- paths
- multiline values
- direct placeholder composition
- common DSL-writing conventions

---

# 2. Engine-Specific Documents

Use the corresponding engine document whenever generating, validating or explaining that engine.

```text
06_ENGINE_SQL.md
07_ENGINE_CMD.md
08_ENGINE_OC.md
09_ENGINE_FILE.md
10_ENGINE_WAIT.md
11_ENGINE_SOAP.md
12_ENGINE_REST.md
13_ENGINE_PDF.md
14_ENGINE_JSON.md
15_ENGINE_BUFFER.md
```

The matching engine document owns:

```text
operations
fields
actions
defaults
response semantics
buffer semantics
assertion semantics
engine-specific limitations
```

Do not infer these from another engine.

---

# 3. Source Ownership Rule

Use the following ownership model.

```text
Overall test structure
    -> 04_AGATE_RULES.md

Shared variables / placeholders
    -> 01_AGATE_VARIABLES.md

Shared conditions
    -> 02_AGATE_CONDITION_HANDLING.md

Reusable CALL semantics
    -> 03_AGATE_REUSABLE_MODULES.md

Shared YAML / quoting / composition
    -> 05_DSL_YAML_RULES.md

Engine-specific syntax
    -> corresponding ENGINE_* document
```

---

# 4. AI Test Generation Workflow

When generating a complete AGATE test:

1. Read `04_AGATE_RULES.md`.
2. Identify every shared DSL concept used.
3. Identify every engine used.
4. Retrieve the matching shared documents.
5. Retrieve the matching `ENGINE_*` documents.
6. Generate the complete test using only documented AGATE syntax.
7. Prefer deterministic implementations over avoidable assumptions.
8. List genuine external assumptions after the YAML.
9. Report documentation gaps only when required AGATE syntax is truly undocumented.

---

# 5. Non-Blocking Generation Rule

Missing business, schema, test-data or environment details must not automatically stop test generation.

Preferred behavior:

```text
GENERATE FIRST
EXPLAIN ASSUMPTIONS AFTERWARD
```

Example:

```sql
SELECT ID, STATUS
FROM USERS
WHERE STATUS = 'ACTIVE'
```

followed by:

```text
Assumption:
The example schema contains a USERS table with columns ID and STATUS.
```

This is acceptable when those schema names were not supplied.

Do not refuse to generate the test merely because an external assumption is required.

---

# 6. Missing External Information vs Missing AGATE Syntax

These are different categories.

## External missing information

Examples:

```text
database table name
database column name
directory existence
operating system
pod name
endpoint
test-data identifier
```

Handling:

```text
generate the test
state a minimal assumption
```

## Missing AGATE syntax

Example:

```text
the requirement needs an engine operation/action
but the authoritative engine document does not define such syntax
```

Handling:

```text
do not invent syntax
report the specific documentation gap
```

---

# 7. Documentation Gap Classification

Do not report a documentation gap merely because different AGATE layers support different capabilities.

Example:

```text
Shared condition syntax
    -> does not support >=

SQL ROW_COUNT ASSERT
    -> supports GREATER_THAN_OR_EQUAL
```

This is not a documentation gap.

The SQL-specific requirement is implemented with SQL ASSERT.

Example:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: '1'
```

The global `condition` syntax is irrelevant here.

---

# 8. Engine Retrieval Verification

Before declaring that an engine document is missing, verify availability explicitly.

For each required engine:

1. Search by canonical filename.
2. Search by engine name.
3. Search by related technical terms.
4. Check all available notebook/source documents again.
5. Only after that may the engine document be reported as unavailable.

Do not conclude that a document is missing merely because it was not returned in the first retrieval.

---

# 9. Mandatory Second Retrieval Rule

For multi-engine requests, a second retrieval attempt is mandatory before reporting missing engine documentation.

Example request:

```text
SQL -> FILE -> CMD -> OC
```

The AI must explicitly verify:

```text
06_ENGINE_SQL.md
07_ENGINE_CMD.md
08_ENGINE_OC.md
09_ENGINE_FILE.md
```

If one is not found on the first retrieval pass:

```text
search again
```

using both filename and semantic engine terms.

Only after the second verification may a missing-document statement be made.

---

# 10. Retrieval Search Terms

Use broader retrieval terms when canonical filenames are not returned.

## SQL

Search terms:

```text
SQL
database
query
ROW_COUNT
row
column
datasource
```

Canonical:

```text
06_ENGINE_SQL.md
```

---

## CMD

Search terms:

```text
CMD
command line
shell
local command
exit code
stdout
stderr
```

Canonical:

```text
07_ENGINE_CMD.md
```

---

## OC

Search terms:

```text
OC
OpenShift
pod
namespace
PUT
GET
remote command
oc
```

Canonical:

```text
08_ENGINE_OC.md
```

---

## FILE

Search terms:

```text
FILE
local file
READ
WRITE
APPEND
COPY
MOVE
DELETE
EXISTS
```

Canonical:

```text
09_ENGINE_FILE.md
```

---

## WAIT

Search terms:

```text
WAIT
sleep
delay
pause
```

Canonical:

```text
10_ENGINE_WAIT.md
```

---

## SOAP

Search terms:

```text
SOAP
XML
XPath
WS-Security
MTOM
MATCH_REFERENCE
```

Canonical:

```text
11_ENGINE_SOAP.md
```

---

## REST

Search terms:

```text
REST
HTTP
JsonPath
MATCH_REFERENCE
endpoint
response
```

Canonical:

```text
12_ENGINE_REST.md
```

---

## PDF

Search terms:

```text
PDF
PDFTextStripper
EXIST
NO_EXIST
COUNT
targetPDF
```

Canonical:

```text
13_ENGINE_PDF.md
```

---

## JSON

Search terms:

```text
JSON
path1
value1
JSON file validation
nested arrays
```

Canonical:

```text
14_ENGINE_JSON.md
```

---

## BUFFER

Search terms:

```text
BUFFER
in-memory variable
EXEC
ASSERT
IS_EMPTY
IS_NULL
```

Canonical:

```text
15_ENGINE_BUFFER.md
```

---

# 11. Multi-Engine Retrieval Example

Requirement:

```text
Query a user from DB,
write a local file,
read it with CMD,
copy it to OpenShift,
validate it remotely.
```

Required sources:

```text
04_AGATE_RULES.md
05_DSL_YAML_RULES.md
06_ENGINE_SQL.md
07_ENGINE_CMD.md
08_ENGINE_OC.md
09_ENGINE_FILE.md
```

Do not generate only a skeleton merely because one or more engine files were not surfaced initially.

First perform the retrieval verification process.

---

# 12. Assumption Quality Rule

An assumption should describe genuinely missing external information.

Good examples:

```text
The example schema contains USERS(ID, STATUS).
The output directory already exists.
The local CMD runtime is Unix/Linux and supports cat.
The pod prefix my-service resolves to a running pod.
```

Bad example:

```text
row 0 is probably the ACTIVE user
```

when the SQL query could instead be constrained to:

```sql
WHERE STATUS = 'ACTIVE'
```

Do not manufacture assumptions to compensate for a weaker implementation.

---

# 13. Deterministic Generation Rule

Prefer:

```sql
SELECT ID, STATUS
FROM USERS
WHERE STATUS = 'ACTIVE'
```

over:

```sql
SELECT ID, STATUS
FROM USERS
```

plus an assumption about arbitrary row order.

If the requirement provides a business key or property, use it to constrain selection whenever documented syntax allows.

---

# 14. Do Not Infer Syntax Across Engines

Do not copy fields/actions between engines simply because their names sound similar.

Examples:

```text
REST BODY ASSERT JsonPath
    != JSON Engine path/value syntax

SOAP BUFFER
    != CMD BUFFER actions

FILE ASSERT EXISTS
    != BUFFER ASSERT IS_NOT_NULL

SQL ROW_COUNT action
    != condition operator
```

Always use the authoritative engine document.

---

# 15. AI Final-Answer Pattern

For complete test generation, prefer:

```text
<complete YAML>

Assumptions:
1. ...
2. ...

Documentation gaps:
None.
```

If a real AGATE documentation gap exists:

```text
Documentation gaps:
- <precise missing engine capability>
```

Do not put environment assumptions under documentation gaps.

---

# 16. Final Retrieval Rule

Before saying:

```text
The required engine documentation is missing.
```

the AI must have:

```text
searched by filename
searched by engine name
searched by semantic engine terms
performed a second retrieval pass
checked the engine catalog
```

If those checks were not performed, do not report the document as missing.
