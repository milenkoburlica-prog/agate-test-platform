# AGATE SOAP Engine

The SOAP Engine executes SOAP-based web-service calls and supports request templating, response validation, XPath-based data extraction, reference-response comparison, WS-Security and binary file transfer.

The canonical operation modes are:

```text
EXEC
ASSERT
BUFFER
```

> This document describes SOAP-specific behavior. General AGATE structure, YAML rules, placeholders and conditions are documented separately.

---

# 1. Quick Reference

## EXEC

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.auth.1.authenticate_dialog_request'
  endpoint: 'https://{E[env.ecard.service]}'
  response: res_auth
  parameters:
    token: '{B[token]}'
  condition: "..."          # Optional
```

Optional file handling:

```yaml
upload:
  - method: MTOM
    path: "//*[local-name()='attachment']"
    sourceFile: 'attachment.document.zip'

download:
  - method: INLINE
    path: "//*[local-name()='document']/text()"
    targetPath: 'target/downloads/document_{B[id]}.pdf'
```

## ASSERT

```yaml
- type: SOAP
  op: ASSERT
  response: res_auth
  source: STATUS
  action: EQUALS
  expected: '200'
```

BODY example:

```yaml
- type: SOAP
  op: ASSERT
  response: res_auth
  source: BODY
  path: "//*[local-name()='dialogId']/text()"
  action: EQUALS
  expected: '{B[expected_dialog_id]}'
```

## BUFFER

```yaml
- type: SOAP
  op: BUFFER
  response: res_auth
  source: BODY
  path: "//*[local-name()='dialogId']/text()"
  name: dialog_id
```

---

# 2. EXEC

`SOAP / EXEC` creates and sends an HTTP POST request based on the SOAP command configuration.

Canonical fields:

| Field | Required | Description |
|---|---:|---|
| `command` | Yes | Logical path to the SOAP command folder containing `metadata.json` and `request.xml`. |
| `endpoint` | Yes | Base URL used to resolve `{{endpoint}}` in metadata. |
| `response` | Yes | Context key under which the SOAP response is stored. |
| `condition` | No | Optional execution condition. |
| `parameters` | No | Key-value replacements used in the request template. |
| `upload` | No | Outgoing binary document injection. |
| `download` | No | Incoming binary document extraction. |

Example:

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.base.17.getBerechtigungen'
  endpoint: 'https://{E[env.ecard.service]}'
  response: res_perms
```

With parameters:

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.auth.1.set_dialogaddress_request'
  endpoint: 'https://{E[env.ecard.service]}'
  response: res_addr
  parameters:
    dialogId: '{B[dialogid]}'
    ORD_ID: '{B[ordinationid]}'
    taetigkeitsBereich: '{B[taetigkeitsbereichid]}'
```

---

# 3. Command Mapping

`command` identifies a SOAP command directory.

Conceptually:

```text
soap.foo.operation
        |
        v
command directory
├── metadata.json
└── request.xml
```

`metadata.json` defines protocol-level information such as URL, method, HTTP headers and optional authentication.

`request.xml` contains the SOAP request template.

Example `metadata.json`:

```json
{
  "url": "{{endpoint}}/calculator.asmx",
  "method": "POST",
  "headers": {
    "Content-Type": "text/xml;charset=UTF-8",
    "SOAPAction": "\"http://tempuri.org/Add\""
  }
}
```

Example `request.xml`:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <Add xmlns="http://tempuri.org/">
      <intA>{B[intA]}</intA>
      <intB>{B[intB]}</intB>
    </Add>
  </soap:Body>
</soap:Envelope>
```

---

# 4. Parameters and Placeholder Resolution

`parameters` provides values that are resolved into the SOAP request template.

Example:

```yaml
parameters:
  dialogId: '{B[dialogId]}'
  dmpCode: '{R[Request.DMPCode]}'
  svNummer: '{R[Request.SVNR]}'
```

Standard AGATE dynamic tags can also appear directly in documented string fields.

Examples:

```text
{B[...]}
{R[...]}
{E[...]}
{XL[...]}
```

---

# 5. NULL and EMPTY in SOAP Requests

SOAP request preprocessing supports special values such as:

```text
{NULL}
{EMPTY}
```

Important distinction:

```text
{NULL}
    -> the corresponding XML node may be removed during preprocessing

{EMPTY}
    -> use when an empty XML element must remain structurally present
```

Therefore, do not replace `{NULL}` with an empty string unless the test semantics require it.

---

# 6. ASSERT

`SOAP / ASSERT` validates a previously stored SOAP response.

Supported sources:

```text
STATUS
BODY
HEADERS
```

---

# 7. ASSERT on STATUS

`STATUS` validates the HTTP status code.

Example:

```yaml
- type: SOAP
  op: ASSERT
  response: res_auth
  source: STATUS
  action: EQUALS
  expected: '200'
```

Documented numeric actions:

```text
EQUALS
GREATER_THAN
GREATER_OR_EQUALS
LESS_THAN
LESS_OR_EQUALS
```

Example:

```yaml
- type: SOAP
  op: ASSERT
  response: soap_response
  source: STATUS
  action: GREATER_OR_EQUALS
  expected: '200'
```

---

# 8. ASSERT on BODY

BODY assertions use XPath.

Example:

```yaml
- type: SOAP
  op: ASSERT
  response: soap_response
  source: BODY
  path: "//*[local-name()='present']/text()"
  action: EQUALS
  expected: 'false'
```

Documented text actions include:

```text
CONTAINS
NOT_CONTAINS
EQUALS
NOT_EQUALS
EMPTY
NOT_EMPTY
```

Use `path` to identify the value in the XML response.

For namespace-heavy SOAP responses, prefer namespace-independent XPath using `local-name()`.

Example:

```yaml
path: "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='return']/*[local-name()='id']/text()"
```

---

# 9. ASSERT on HEADERS

HEADERS assertions validate HTTP response headers.

For HEADERS:

```text
path = header name
```

Example pattern:

```yaml
- type: SOAP
  op: ASSERT
  response: soap_response
  source: HEADERS
  path: 'Content-Type'
  action: CONTAINS
  expected: 'xml'
```

---

# 10. MATCH_REFERENCE

`MATCH_REFERENCE` compares the complete SOAP XML response against a persisted reference response.

Example:

```yaml
- id: verify_get_admin_response
  type: SOAP
  op: ASSERT
  response: get_admin_response
  source: BODY
  action: MATCH_REFERENCE
```

Use this for regression tests where the complete semantic SOAP response matters.

For single fields, ordinary XPath ASSERTs remain preferable.

---

# 11. Reference Creation

On the first run, if no reference exists, AGATE stores the current SOAP response as a new reference.

The generated reference must be reviewed by the tester.

An existing reference is not automatically overwritten.

Conceptual location:

```text
<yaml-directory>/
└── references/
    └── <yaml-name>/
        └── <testcase>__<step-id>.xml
```

Best practice:

```text
Always give MATCH_REFERENCE ASSERT steps a stable explicit id.
```

Example:

```yaml
- id: verify_patient_response
  type: SOAP
  op: ASSERT
  response: patient_response
  source: BODY
  action: MATCH_REFERENCE
```

---

# 12. Semantic XML Comparison

MATCH_REFERENCE performs semantic XML comparison rather than raw string comparison.

Differences such as these should not alone cause a mismatch:

- formatting,
- indentation,
- irrelevant whitespace,
- XML declaration differences,
- attribute ordering,
- namespace-prefix differences with identical namespace URIs.

Meaningful differences are detected, including:

- changed element values,
- missing or additional elements,
- changed attributes,
- missing or additional attributes,
- changed XML structure.

---

# 13. MATCH_REFERENCE ignore

Dynamic technical fields may be excluded from comparison.

Example:

```yaml
- id: verify_patient_response
  type: SOAP
  op: ASSERT
  response: patient_response
  source: BODY
  action: MATCH_REFERENCE
  ignore:
    - "//*[local-name()='timestamp']"
    - "//*[local-name()='requestId']"
    - "//*[local-name()='transactionId']"
```

Use `ignore` only for genuinely dynamic technical values.

Do not ignore business-relevant values merely to make a test pass.

---

# 14. MATCH_REFERENCE unordered

For XML lists whose element order is not guaranteed:

```yaml
- id: verify_patient_response
  type: SOAP
  op: ASSERT
  response: patient_response
  source: BODY
  action: MATCH_REFERENCE
  unordered:
    - path: "//*[local-name()='persons']/*[local-name()='person']"
      matchBy: "*[local-name()='id']"
```

`path` identifies the repeated elements.

`matchBy` identifies a stable key within each element.

`ignore` and `unordered` can be combined.

---

# 15. BUFFER

`SOAP / BUFFER` extracts data from a stored SOAP response into a context variable.

Important:

```text
name must be different from response
```

Do not overwrite the original response object with a scalar extracted value.

Canonical example:

```yaml
- type: SOAP
  op: BUFFER
  response: res_auth
  source: BODY
  path: "//*[local-name()='dialogId']/text()"
  name: dialogid
```

---

# 16. BUFFER Sources

Supported sources:

```text
STATUS
HEADERS
BODY
```

## STATUS

Stores the HTTP status code.

```yaml
- type: SOAP
  op: BUFFER
  response: soap_response
  source: STATUS
  name: actual_status
```

`path` is not needed for STATUS.

## HEADERS

Extracts a response header.

```yaml
- type: SOAP
  op: BUFFER
  response: soap_response
  source: HEADERS
  path: 'Server'
  name: server_type
```

## BODY

Extracts a value through XPath.

```yaml
- type: SOAP
  op: BUFFER
  response: soap_response
  source: BODY
  path: "//*[local-name()='VertragId']/text()"
  name: contract_id
```

If the XPath does not match, the documented behavior stores:

```text
NULL
```

---

# 17. SOAP BUFFER Does Not Use Text Actions

For SOAP BODY extraction, do not apply CMD/FILE-style actions such as:

```text
LINE
FILTER
COUNT
TEXT
```

SOAP BODY BUFFER returns the string value selected by XPath.

Canonical form:

```yaml
- type: SOAP
  op: BUFFER
  response: response
  source: BODY
  path: "//*[local-name()='id']/text()"
  name: extracted_id
```

---

# 18. Constraints

Constraints filter XML list results before extracting the target value.

Example:

```yaml
- type: SOAP
  op: BUFFER
  name: isocode
  response: response
  source: BODY
  path: "//*[local-name()='Country']/*[local-name()='Code']/text()"
  constraints:
    - path: "//*[local-name()='Country']/*[local-name()='Name']/text()"
      action: EQUALS
      expected: 'Botswana'
```

Constraint fields:

```text
path
action
expected
```

This allows selection by business key rather than fixed list position.

Real SVC-style pattern:

```yaml
- type: SOAP
  op: BUFFER
  name: MeldungsID
  source: BODY
  response: meldunganlegen_response
  path: "//*[local-name()='quittung']/*[local-name()='id']/text()"
  constraints:
    - path: "//*[local-name()='quittung']/*[local-name()='meldungsdaten']/*[local-name()='svtCode']/text()"
      action: EQUALS
      expected: '{XL[RueckgabeSVT1]}'
```

---

# 19. XPath Best Practices

Prefer explicit text extraction when extracting scalar text:

```yaml
path: "//*[local-name()='AddResult']/text()"
```

Without `/text()`, XPath string evaluation on an element may concatenate descendant text values.

Use namespace-independent paths when service prefixes may differ:

```yaml
//*[local-name()='dialogId']/text()
```

---

# 20. Legacy / Migrated selector Field

Some migrated SVC tests contain:

```yaml
selector: XPATH
```

or:

```yaml
selector: XML_PATH
```

together with SOAP BUFFER.

Example observed in migrated tests:

```yaml
- type: SOAP
  op: BUFFER
  name: MeldungsID
  source: BODY
  selector: XML_PATH
  path: "..."
  response: meldunganlegen_response
```

However, the canonical current SOAP documentation defines BODY extraction using:

```text
source + path
```

and does not require `selector`.

Therefore, when generating new SOAP tests:

```text
Prefer the canonical documented form without selector.
```

Only preserve `selector` when maintaining an existing project/test where that field is intentionally used and known to be supported.

---

# 21. Upload

Outgoing binary files can be injected into SOAP requests.

Example:

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.foo.anfrage.25'
  endpoint: 'https://services.example'
  response: upload_response
  upload:
    - method: MTOM
      path: "//*[local-name()='attachment']"
      sourceFile: 'attachment.foo.document.zip'
```

Fields:

```text
method      MTOM | INLINE
path        XPath target node in request XML
sourceFile  file to inject
```

For MTOM, the engine builds a multipart request and adjusts the HTTP Content-Type accordingly.

---

# 22. Download

Incoming binary data can be extracted from SOAP responses.

Example:

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.dbas.amp.13'
  endpoint: 'https://services.example'
  response: data_response
  download:
    - method: MTOM
      path: "//*[local-name()='druckaufbereitung']/text()"
      targetPath: 'target/downloads/datenblatt_{B[svNummer]}.pdf'
```

Fields:

```text
method      INLINE | MTOM
path        XPath to binary/base64 content
targetPath  local target file
```

The documented default extraction method is `INLINE`.

---

# 23. Download Memory Optimization

When large Base64 content is extracted through `download`, the engine may replace the original Base64 value in the stored XML response with a file reference:

```text
file:<targetPath>
```

This reduces memory usage and keeps logs readable.

A later ASSERT or BUFFER may inspect that file reference through XPath.

Example:

```yaml
- type: SOAP
  op: ASSERT
  response: data_response
  source: BODY
  path: "//*[local-name()='druckaufbereitung']/text()"
  action: CONTAINS
  expected: 'file:target/downloads/'
```

---

# 24. WS-Security

SOAP authentication is configured declaratively in `metadata.json`.

Documented authentication type:

```text
WSS_USERNAME_TOKEN_DIGEST
```

Example:

```json
{
  "url": "{{endpoint}}/services/SOAPReceiverService",
  "method": "POST",
  "headers": {
    "Content-Type": "text/xml;charset=UTF-8",
    "SOAPAction": "\"\""
  },
  "auth": {
    "type": "WSS_USERNAME_TOKEN_DIGEST",
    "username": "<username>",
    "password": "<password>"
  }
}
```

The engine injects the WS-Security header automatically.

When metadata authentication is enabled, do not manually duplicate the generated WS-Security header in `request.xml`.

---

# 25. Real SVC Example – Basic SOAP Call

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.base.17.getBerechtigungen'
  endpoint: 'https://{E[env.ecard.service]}'
  response: res_perms

- type: SOAP
  op: ASSERT
  response: res_perms
  source: STATUS
  action: EQUALS
  expected: '200'
```

---

# 26. Real SVC Example – Authentication and Buffer

```yaml
- type: SOAP
  op: EXEC
  command: 'soap.auth.1.authenticate_dialog_request'
  endpoint: 'https://{E[env.ecard.service]}'
  response: res_auth
  parameters:
    token: '{B[token]}'

- type: SOAP
  op: ASSERT
  response: res_auth
  source: STATUS
  action: EQUALS
  expected: '200'

- type: SOAP
  op: BUFFER
  name: dialogId
  response: res_auth
  source: BODY
  path: "//*[local-name()='dialogId']/text()"
```

---

# 27. Real SVC Example – Regression Validation

```yaml
- id: doausschreibung_request
  type: SOAP
  op: EXEC
  endpoint: 'https://{E[env.ecard.service]}'
  command: 'soap.dmp.11.doausschreibung_request_v11'
  response: doausschreibung_request_v11_response
  parameters:
    dialogId: '{B[dialogId]}'
    ausschreibeGrund: 'AV'
    dmpCode: '01'
    svNummer: '{NULL}'
    cardToken: '{B[token]}'

- id: verify_doausschreibung_response
  type: SOAP
  op: ASSERT
  response: doausschreibung_request_v11_response
  source: BODY
  action: MATCH_REFERENCE
  ignore:
    - "//*[local-name()='ausschreibeZeitstempel']"
```

---

# 28. Troubleshooting

## URL or Action not found

Check that:

- the command mapping is valid,
- `metadata.json` is present,
- its URL mapping resolves correctly,
- the command path points to the intended SOAP definition.

## Required empty XML element disappears

If:

```xml
<Node>{NULL}</Node>
```

causes the node to be removed, use:

```text
{EMPTY}
```

when the empty node itself must remain.

## Constraint BUFFER returns NULL

Check the XPath structure carefully.

Avoid path shapes that make list-element detection ambiguous.

## XML processing fails because of special characters

Unescaped `&` and malformed XML content can break DOM parsing.

Ensure payload values are valid XML content.

## Unexpected concatenated text

When extracting scalar text, prefer:

```text
/text()
```

at the end of XPath.

---

# 29. AI Guidance

When generating SOAP tests:

1. Use only `EXEC`, `ASSERT` and `BUFFER`.
2. Prefer `command + endpoint + response` for EXEC.
3. Use `parameters` for request-template values.
4. Use `STATUS`, `BODY` or `HEADERS` as documented response sources.
5. Use XPath for BODY ASSERT and BODY BUFFER.
6. Prefer `local-name()` XPath expressions for namespace-independent matching.
7. Prefer `/text()` when extracting scalar text.
8. Never use CMD/FILE BUFFER actions (`TEXT`, `LINE`, `FILTER`, `COUNT`) for SOAP BODY BUFFER.
9. Keep `name` different from `response`.
10. Use `MATCH_REFERENCE` for full-response regression checks.
11. Give MATCH_REFERENCE steps a stable explicit `id`.
12. Use `ignore` only for truly dynamic technical fields.
13. Use `unordered` when list ordering is not semantically significant.
14. Use `constraints` for dynamic selection from XML lists.
15. Use `upload`/`download` only with documented `MTOM` or `INLINE` semantics.
16. Prefer current canonical SOAP syntax over migrated legacy fields.
17. Do not add `selector` to new SOAP BUFFER steps unless project-specific documentation explicitly requires it.
18. Treat `{NULL}` and `{EMPTY}` differently.
19. Follow shared AGATE YAML quoting rules.
20. Do not invent metadata/authentication types that are not documented.

---

# 30. Short Reference

```text
SOAP
├── EXEC
│   ├── command
│   ├── endpoint
│   ├── response
│   ├── parameters
│   ├── condition
│   ├── upload
│   └── download
│
├── ASSERT
│   ├── response
│   ├── source
│   │   ├── STATUS
│   │   ├── BODY
│   │   └── HEADERS
│   ├── path
│   ├── action
│   ├── expected
│   ├── MATCH_REFERENCE
│   ├── ignore
│   └── unordered
│
└── BUFFER
    ├── response
    ├── source
    │   ├── STATUS
    │   ├── BODY
    │   └── HEADERS
    ├── path
    ├── name
    └── constraints
```
