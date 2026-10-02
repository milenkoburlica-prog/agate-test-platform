# AGATE REST Engine

The REST Engine executes HTTP-based REST API calls and supports response validation, JSONPath-based assertions, response buffering, semantic reference comparison, endpoint substitution and automatic retry handling.

The canonical operation modes are:

```text
EXEC
ASSERT
BUFFER
```

> This document describes REST-specific behavior. General AGATE test structure, YAML rules, placeholders and conditions are documented separately.

---

# 1. Quick Reference

## EXEC

```yaml
- type: REST
  op: EXEC
  command: 'rest.jsonplaceholder.typicode.todos.1'
  endpoint: '{B[apiEndpoint]}'
  response: response_rest1
```

## ASSERT

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: STATUS
  action: EQUALS
  expected: '200'
```

BODY example:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  path: '$.title'
  action: EQUALS
  expected: 'delectus aut autem'
```

## BUFFER

```yaml
- type: REST
  op: BUFFER
  response: response_rest1
  source: BODY
  path: '$.security.token'
  name: auth_token
```

---

# 2. Operations

Supported REST operations:

```text
EXEC
ASSERT
BUFFER
```

If `op` is omitted, the documented default is:

```text
EXEC
```

For AI-generated tests, prefer writing `op: EXEC` explicitly for readability and consistency.

---

# 3. EXEC

`REST / EXEC` resolves a REST command definition and executes the HTTP request.

Canonical fields:

| Field | Required | Description |
|---|---:|---|
| `command` | Yes | Dot-path to the REST request definition (`metadata.json` / `request.json`). |
| `endpoint` | No | Replaces `{{endpoint}}` inside the URL from `metadata.json`. |
| `response` | Yes | Context key under which the full REST response is stored. |
| `condition` | No | Optional common AGATE condition. |

Example:

```yaml
- type: REST
  op: EXEC
  command: 'rest.simulatord.remove_card.remove_card_request'
  endpoint: 'http://{E[users.serverip]}'
  response: remove_card_response
  parameters:
    card_slot: 'baseContact'
```

---

# 4. Command Mapping

The user does not normally define HTTP `method`, `url`, `headers` or raw `body` directly in the REST YAML step.

Instead:

```text
command
   |
   v
REST request definition
├── metadata.json
└── request.json
```

Example `metadata.json`:

```json
{
  "url": "{{endpoint}}/posts",
  "method": "POST",
  "headers": {}
}
```

Example `request.json`:

```json
{
  "hello": "girl"
}
```

The engine derives:

```text
method  <- metadata.json
url     <- metadata.json
headers <- metadata.json
body    <- request.json
```

For new tests, do not invent direct REST YAML fields such as:

```text
method
url
headers
body
```

when the command-based model is used.

---

# 5. endpoint

`endpoint` replaces `{{endpoint}}` in the URL loaded from `metadata.json`.

Example:

```yaml
endpoint: 'https://{E[users.ginoip]}'
```

The engine removes a trailing `/` from the endpoint value before substitution.

Example:

```text
endpoint = https://api.example.com/
metadata URL = {{endpoint}}/api/v1/users

resolved = https://api.example.com/api/v1/users
```

---

# 6. parameters

Existing AGATE REST tests use `parameters` to provide dynamic values to REST request definitions.

Example:

```yaml
- type: REST
  op: EXEC
  command: 'rest.simulatord.insert_card.insert_card_request'
  endpoint: 'http://{E[users.serverip]}'
  response: insert_card_response
  parameters:
    card_slot: 'baseContact'
    cdfName: '{B[cdfName]}'
```

Use documented/project-established parameter names from the corresponding REST command definition.

Do not invent request parameter names without source information.

---

# 7. NULL and EMPTY in Headers

The REST Engine documents special handling for header values:

```text
{NULL}
    -> the header is omitted completely

{EMPTY}
    -> the header is sent with an empty string value
```

These are not equivalent.

Do not replace `{NULL}` with `{EMPTY}` or vice versa without understanding the intended request semantics.

---

# 8. Automatic Retry

The REST Engine has an integrated retry mechanism for transient connection problems such as:

```text
Connection reset
header parser received no bytes
```

Documented behavior:

```text
wait 10 ms
retry
maximum 3 attempts
```

The user does not need to model this retry manually for those specific transport errors.

---

# 9. Response Object

`REST / EXEC` stores a REST response object under `response`.

Conceptually:

```text
RestResponse
├── STATUS
├── BODY
└── HEADERS
```

Example:

```yaml
response: users_data
```

Later steps reference:

```yaml
response: users_data
```

---

# 10. ASSERT

`REST / ASSERT` validates a previously stored REST response.

Supported sources:

```text
STATUS
BODY
HEADERS
```

Example:

```yaml
- type: REST
  op: ASSERT
  response: users_data
  source: STATUS
  action: EQUALS
  expected: '200'
```

---

# 11. ASSERT on STATUS

Example:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: STATUS
  action: EQUALS
  expected: '200'
```

Use the response key from a previous `REST / EXEC`.

---

# 12. ASSERT on BODY

BODY assertions use Jayway JsonPath.

Example:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  path: '$.title'
  action: EQUALS
  expected: 'delectus aut autem'
```

Important:

```text
REST BODY ASSERT uses full JsonPath evaluation.
```

This is different from REST BODY BUFFER behavior.

---

# 13. BODY ASSERT Actions

Documented BODY assertion actions include:

```text
EXISTS
COUNT
CONTAINS
EQUALS
VERIFY
NOT_EQUALS
```

The compact engine reference also includes actions such as:

```text
IS_EMPTY
```

Use only actions documented for the current REST Engine.

---

# 14. EXISTS

`EXISTS` verifies that a JSONPath exists.

Example:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  path: '$.title'
  action: EXISTS
```

When a wildcard returns multiple values and `expected` is supplied, EXISTS succeeds if at least one returned element matches the expected value.

Conceptual example:

```yaml
- type: REST
  op: ASSERT
  response: users_data
  source: BODY
  path: '$.users[*].name'
  action: EXISTS
  expected: 'Chanda'
```

---

# 15. CONTAINS

With arrays:

```yaml
- type: REST
  op: ASSERT
  response: users_data
  source: BODY
  path: '$.users[*].name'
  action: CONTAINS
  expected: '{B[search_term]}'
```

If JsonPath returns an array, the engine checks whether the expected value is contained in that result.

If the path returns a scalar, normal string containment is used.

---

# 16. EQUALS / VERIFY / NOT_EQUALS

These perform direct value comparisons.

Example:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  path: '$.title'
  action: EQUALS
  expected: 'delectus aut autem'
```

Important wildcard rule:

```text
EQUALS / VERIFY / NOT_EQUALS must not be used on a JsonPath that returns multiple values.
```

Bad:

```yaml
path: '$.items[*].id'
action: EQUALS
```

Preferred alternatives:

```yaml
path: '$.items[0].id'
action: EQUALS
```

or:

```yaml
path: '$.items[*].id'
action: CONTAINS
```

---

# 17. COUNT

COUNT works on JSON arrays.

Example:

```yaml
- type: REST
  op: ASSERT
  response: users_data
  source: BODY
  path: '$.users[*].name'
  action: COUNT
  expected: '2'
```

Optional `value` filters before counting.

Example:

```yaml
- type: REST
  op: ASSERT
  response: users_data
  source: BODY
  path: '$.users[*].status'
  action: COUNT
  value: 'ACTIVE'
  expected: '3'
```

Meaning:

```text
count only elements equal to ACTIVE
```

---

# 18. ASSERT on HEADERS

For HEADERS:

```text
path = exact HTTP header name
```

Example pattern:

```yaml
- type: REST
  op: ASSERT
  response: response_rest1
  source: HEADERS
  path: 'Content-Type'
  action: CONTAINS
  expected: 'application/json'
```

---

# 19. MATCH_REFERENCE

`MATCH_REFERENCE` compares the complete JSON response body against a persisted reference.

Example:

```yaml
- id: verify_todo_response
  type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  action: MATCH_REFERENCE
```

For MATCH_REFERENCE:

```text
path     not required
expected not required
```

Use MATCH_REFERENCE for full-response regression validation.

Use ordinary JsonPath assertions when only individual fields matter.

---

# 20. Reference Creation

On the first run, if no reference exists, the current REST response is stored as a new JSON reference.

The first run does not fail solely because the reference was newly created.

The tester must review the generated reference.

An existing reference is not automatically overwritten.

Conceptual path:

```text
<yaml-directory>/
└── references/
    └── <yaml-name>/
        └── <testcase>__<step-id>.json
```

Best practice:

```text
MATCH_REFERENCE ASSERT steps should always have a stable explicit id.
```

---

# 21. Semantic JSON Comparison

MATCH_REFERENCE compares JSON semantically, not as raw text.

Ignored formatting differences include:

```text
whitespace
line breaks
indentation
property order inside JSON objects
```

Meaningful differences include:

```text
changed values
missing properties
additional properties
different data types
different array contents
```

---

# 22. MATCH_REFERENCE ignore

Dynamic technical fields can be excluded.

Example:

```yaml
- id: verify_customer_response
  type: REST
  op: ASSERT
  response: customer_response
  source: BODY
  action: MATCH_REFERENCE
  ignore:
    - '$.timestamp'
    - '$.requestId'
    - '$.transactionId'
```

Use `ignore` only for values whose differences have no business meaning.

---

# 23. MATCH_REFERENCE unordered

Use `unordered` for arrays whose order is not semantically relevant.

Example:

```yaml
- id: verify_readers_response
  type: REST
  op: ASSERT
  response: response_1
  source: BODY
  action: MATCH_REFERENCE
  unordered:
    - path: '$.readers'
      matchBy: '$.id'
```

`path` identifies the array.

`matchBy` identifies a stable key inside each array element.

---

# 24. Root Arrays

A JSON response whose root itself is an array is also supported.

Example:

```yaml
- id: verify_posts_response
  type: REST
  op: ASSERT
  response: posts_response
  source: BODY
  action: MATCH_REFERENCE
  unordered:
    - path: '$'
      matchBy: '$.id'
```

---

# 25. ignore + unordered

Both can be combined.

Example:

```yaml
- id: verify_readers_response
  type: REST
  op: ASSERT
  response: readers_response
  source: BODY
  action: MATCH_REFERENCE
  ignore:
    - '$.timestamp'
    - '$.requestId'
  unordered:
    - path: '$.readers'
      matchBy: '$.id'
```

---

# 26. BUFFER

`REST / BUFFER` extracts data from a previously stored REST response.

Example:

```yaml
- type: REST
  op: BUFFER
  response: auth_res
  source: BODY
  path: '$.security.token'
  name: my_token
```

Supported sources:

```text
STATUS
BODY
HEADERS
```

---

# 27. BUFFER on STATUS

Stores the HTTP status code.

Example:

```yaml
- type: REST
  op: BUFFER
  response: response_rest1
  source: STATUS
  name: actual_status
```

---

# 28. BUFFER on HEADERS

For HEADERS:

```text
path = header name
```

Example:

```yaml
- type: REST
  op: BUFFER
  response: response_rest1
  source: HEADERS
  path: 'Content-Type'
  name: content_type
```

---

# 29. BUFFER on BODY

Example:

```yaml
- type: REST
  op: BUFFER
  response: response_rest1
  source: BODY
  path: '$.status.baseContact.token.value'
  name: token
```

Important distinction:

```text
ASSERT BODY
    -> Jayway JsonPath

BUFFER BODY
    -> Jackson-based extraction with automatic path conversion
```

The engine converts simple dot notation such as:

```text
$.data.user
```

conceptually to:

```text
/data/user
```

for Jackson-style lookup.

---

# 30. BUFFER Path Limitation

Because BODY BUFFER uses simplified Jackson path conversion, complex JsonPath constructs may fail.

Problematic example:

```text
$.['metadata.info']
```

or other paths with special-key syntax / advanced JsonPath features.

For complex JSON structures:

```text
prefer REST ASSERT with full Jayway JsonPath
```

if validation rather than extraction is sufficient.

Do not assume that every JsonPath supported by REST ASSERT is also supported by REST BUFFER.

---

# 31. BUFFER COUNT

REST BUFFER can use:

```yaml
action: COUNT
```

to store the size of a JSON array or object.

Example:

```yaml
- type: REST
  op: BUFFER
  response: users_data
  source: BODY
  path: '$.users'
  action: COUNT
  name: user_count
```

---

# 32. selector in Existing Tests

Existing SVC REST tests may contain:

```yaml
selector: JSON_PATH
```

Example:

```yaml
- type: REST
  op: BUFFER
  name: token
  source: BODY
  selector: JSON_PATH
  path: 'status.baseContact.token.value'
  response: token_response
```

and:

```yaml
- type: REST
  op: ASSERT
  source: BODY
  selector: JSON_PATH
  path: '$.title'
  action: EQUALS
  expected: 'delectus aut autem'
  response: response_rest1
```

However, the current compact REST reference defines canonical REST behavior using:

```text
source + path
```

and does not require `selector`.

Therefore:

```text
For newly generated REST tests, prefer the canonical documented form without selector.
```

Preserve `selector` only when maintaining existing project tests where it is intentionally used.

---

# 33. Suspicious Legacy BUFFER Fields

Some historical/example REST BUFFER steps contain fields such as:

```yaml
action: EQUALS
expected: 'delectus aut autem'
```

together with:

```yaml
op: BUFFER
```

Example:

```yaml
- type: REST
  op: BUFFER
  name: title
  source: BODY
  action: EQUALS
  path: '$.title'
  expected: 'delectus aut autem'
  response: response_rest1
```

The current REST reference defines BUFFER primarily as extraction and documents `action` specifically for `COUNT`.

Therefore, for new generated tests:

```text
Do not add ASSERT-style action/expected fields to REST BUFFER.
```

Preferred:

```yaml
- type: REST
  op: BUFFER
  response: response_rest1
  source: BODY
  path: '$.title'
  name: title
```

Then validate separately if needed:

```yaml
- type: BUFFER
  op: ASSERT
  name: title
  action: EQUALS
  expected: 'delectus aut autem'
```

or use a direct REST ASSERT.

---

# 34. Conditions

REST steps support the common AGATE `condition` field.

Example:

```yaml
- type: REST
  op: EXEC
  condition: "'{B[GinoCardSlot]}' == 'baseContact'"
  endpoint: 'https://{E[users.ginoip]}'
  command: 'rest.gino.v2.status.cardtoken_vpsig_basecontact_request'
  response: token_response
```

Follow shared AGATE condition/YAML rules.

---

# 35. Real SVC Example – Conditional Card Token

```yaml
- type: REST
  op: EXEC
  condition: "'{B[GinoCardSlot]}' == 'baseContact'"
  endpoint: 'https://{E[users.ginoip]}'
  command: 'rest.gino.v2.status.cardtoken_vpsig_basecontact_request'
  response: cardtoken_response

- type: REST
  op: ASSERT
  condition: "'{B[GinoCardSlot]}' == 'baseContact'"
  response: cardtoken_response
  source: STATUS
  action: EQUALS
  expected: '200'

- type: REST
  op: BUFFER
  condition: "'{B[GinoCardSlot]}' == 'baseContact'"
  response: cardtoken_response
  source: BODY
  path: '$.status.baseContact.token.value'
  name: token
```

---

# 36. Real SVC Example – Request Parameters

```yaml
- type: REST
  op: EXEC
  endpoint: 'http://{E[users.serverip]}'
  command: 'rest.simulatord.insert_card.insert_card_request'
  response: insert_card_response
  parameters:
    card_slot: 'baseContact'
    cdfName: '{B[cdfName]}'
```

---

# 37. Real Example – Full Response Regression

```yaml
- type: REST
  op: EXEC
  command: 'rest.jsonplaceholder.typicode.todos.1'
  endpoint: '{B[apiEndpoint]}'
  response: response_rest1

- type: REST
  op: ASSERT
  response: response_rest1
  source: STATUS
  action: EQUALS
  expected: '200'

- id: verify_todo_response
  type: REST
  op: ASSERT
  response: response_rest1
  source: BODY
  action: MATCH_REFERENCE
```

---

# 38. Troubleshooting

## BUFFER reports `Path not found`

Cause:

```text
REST BUFFER uses simplified Jackson-based path conversion.
```

Advanced JsonPath syntax may not be supported.

Use a simpler path or reconsider whether ASSERT with full JsonPath is more appropriate.

---

## ASSERT reports multiple elements

Example problem:

```yaml
path: '$.items[*].id'
action: EQUALS
```

`EQUALS`, `VERIFY` and `NOT_EQUALS` require a scalar result.

Use:

```yaml
path: '$.items[0].id'
```

or:

```yaml
action: CONTAINS
```

---

## Connection reset

The engine automatically retries documented transient connection failures.

Do not immediately add manual retry logic unless the test requirement explicitly requires a different retry model.

---

# 39. AI Guidance

When generating REST tests:

1. Use `EXEC`, `ASSERT` and `BUFFER`.
2. Prefer explicit `op: EXEC`, even though EXEC is the documented default.
3. Use `command` to reference the REST request definition.
4. Do not invent direct YAML `method`, `url`, `headers` or `body` for the command-based model.
5. Use `endpoint` only as the base endpoint substitution when required.
6. Use `parameters` only when parameter names are known from the request definition or requirement.
7. Use `STATUS`, `BODY` or `HEADERS` as response sources.
8. Remember that BODY ASSERT uses Jayway JsonPath.
9. Remember that BODY BUFFER uses simpler Jackson-style path conversion.
10. Do not assume every ASSERT JsonPath is valid for BUFFER.
11. Use `CONTAINS` instead of `EQUALS` when a wildcard returns multiple values.
12. Use `COUNT` with optional `value` for filtered array counting.
13. Use MATCH_REFERENCE for full-response regression checks.
14. Give MATCH_REFERENCE steps a stable explicit `id`.
15. Use `ignore` only for truly dynamic technical values.
16. Use `unordered` when array order is not semantically meaningful.
17. Root arrays may use `unordered.path: '$'`.
18. Prefer canonical REST syntax without `selector` for newly generated tests.
19. Do not copy ASSERT-style `action/expected` into REST BUFFER unless the documented action is specifically applicable, such as `COUNT`.
20. Follow shared AGATE YAML and condition rules.
21. Treat `{NULL}` and `{EMPTY}` header values differently.
22. Do not manually implement retries for the documented transient transport errors unless explicitly required.
23. Do not report `selector` as mandatory just because migrated tests contain it.
24. Do not invent missing request metadata; report it as a business/configuration assumption if necessary.

---

# 40. Short Reference

```text
REST
├── EXEC
│   ├── command         [required]
│   ├── endpoint        [optional]
│   ├── response        [required]
│   ├── parameters      [project/request-definition dependent]
│   └── condition       [optional]
│
├── ASSERT
│   ├── response        [required]
│   ├── source
│   │   ├── STATUS
│   │   ├── BODY
│   │   └── HEADERS
│   ├── path
│   ├── action
│   ├── expected
│   ├── value           [COUNT filter]
│   ├── MATCH_REFERENCE
│   ├── ignore
│   └── unordered
│
└── BUFFER
    ├── response        [required]
    ├── source
    │   ├── STATUS
    │   ├── BODY
    │   └── HEADERS
    ├── path
    ├── name            [required]
    └── action: COUNT   [optional]
```
