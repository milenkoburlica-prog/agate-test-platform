# AGATE Engine Catalog

This catalog maps AGATE engine names, canonical knowledge documents and recommended retrieval terms.

Its purpose is to improve document discovery in AI/RAG/Notebook environments.

---

# 1. Engine Catalog

| Engine | Canonical Document | Main Purpose |
|---|---|---|
| SQL | `06_ENGINE_SQL.md` | Database query execution, SQL assertions and SQL buffering |
| CMD | `07_ENGINE_CMD.md` | Local command execution, command assertions and output buffering |
| OC | `08_ENGINE_OC.md` | OpenShift pod commands and file transfer |
| FILE | `09_ENGINE_FILE.md` | Local filesystem operations and file-content validation |
| WAIT | `10_ENGINE_WAIT.md` | Delays / waiting |
| SOAP | `11_ENGINE_SOAP.md` | SOAP requests, XML validation, XPath, reference comparison |
| REST | `12_ENGINE_REST.md` | REST requests, HTTP validation, JSON response extraction |
| PDF | `13_ENGINE_PDF.md` | PDF text validation |
| JSON | `14_ENGINE_JSON.md` | JSON file validation |
| BUFFER | `15_ENGINE_BUFFER.md` | Local runtime variables and text assertions |

---

# 2. SQL Engine

Canonical document:

```text
06_ENGINE_SQL.md
```

Retrieve using:

```text
SQL
database
query
SELECT
ROW_COUNT
row
column
datasource
SQL ASSERT
SQL BUFFER
```

Typical requirement language:

```text
query database
read user from DB
validate row count
extract column
store database value
```

---

# 3. CMD Engine

Canonical document:

```text
07_ENGINE_CMD.md
```

Retrieve using:

```text
CMD
command
command line
shell
local runtime
stdout
stderr
exit code
command output
```

Typical requirement language:

```text
run command
execute shell
read file using command
validate command output
```

---

# 4. OC Engine

Canonical document:

```text
08_ENGINE_OC.md
```

Retrieve using:

```text
OC
OpenShift
pod
namespace
remote command
PUT
GET
copy to pod
copy from pod
oc exec
```

Typical requirement language:

```text
run command in pod
copy file to pod
copy file from pod
validate remote output
OpenShift
```

---

# 5. FILE Engine

Canonical document:

```text
09_ENGINE_FILE.md
```

Retrieve using:

```text
FILE
local file
filesystem
READ
WRITE
APPEND
COPY
MOVE
DELETE
EXISTS
file content
```

Typical requirement language:

```text
create file
read file
delete file
append file
copy local file
check file exists
validate file content
```

---

# 6. WAIT Engine

Canonical document:

```text
10_ENGINE_WAIT.md
```

Retrieve using:

```text
WAIT
sleep
pause
delay
wait milliseconds
wait seconds
```

Typical requirement language:

```text
wait
sleep
delay execution
pause test
```

---

# 7. SOAP Engine

Canonical document:

```text
11_ENGINE_SOAP.md
```

Retrieve using:

```text
SOAP
XML
XPath
WS-Security
MTOM
INLINE
MATCH_REFERENCE
SOAP request
SOAP response
```

Typical requirement language:

```text
call SOAP service
validate XML
extract XML value
compare SOAP response
upload/download SOAP attachment
```

---

# 8. REST Engine

Canonical document:

```text
12_ENGINE_REST.md
```

Retrieve using:

```text
REST
HTTP
endpoint
JsonPath
response
status
headers
MATCH_REFERENCE
REST request
REST response
```

Typical requirement language:

```text
call REST API
validate HTTP status
validate JSON response
extract token
compare full response
```

---

# 9. PDF Engine

Canonical document:

```text
13_ENGINE_PDF.md
```

Retrieve using:

```text
PDF
targetPDF
PDFTextStripper
EXIST
NO_EXIST
COUNT
password-protected PDF
```

Typical requirement language:

```text
validate PDF
check text in PDF
count text in PDF
check forbidden text
```

---

# 10. JSON Engine

Canonical document:

```text
14_ENGINE_JSON.md
```

Retrieve using:

```text
JSON
JSON file
path1
value1
nested array
complex key
regex
JSON validation
```

Typical requirement language:

```text
validate JSON file
check JSON path
validate nested JSON
validate generated response file
```

---

# 11. BUFFER Engine

Canonical document:

```text
15_ENGINE_BUFFER.md
```

Retrieve using:

```text
BUFFER
in-memory variable
runtime value
EXEC
ASSERT
IS_NULL
IS_EMPTY
CONTAINS
```

Typical requirement language:

```text
store value
create runtime variable
validate buffer
compare buffer
```

---

# 12. Shared Documents

These are not engine documents.

## Variables

Canonical:

```text
01_AGATE_VARIABLES.md
```

Search terms:

```text
E variable
B variable
R variable
XL variable
DATE
DATETIME
NULL
EMPT
RND
```

---

## Conditions

Canonical:

```text
02_AGATE_CONDITION_HANDLING.md
```

Search terms:

```text
condition
AND
OR
NOT
CONTAINS
IS_EMPTY
NULL
SKIPPED
```

---

## Reusable Modules

Canonical:

```text
03_AGATE_REUSABLE_MODULES.md
```

Search terms:

```text
CALL
reusable
R buffer
parameters
verbose
```

---

## Overall AGATE Rules

Canonical:

```text
04_AGATE_RULES.md
```

Search terms:

```text
testCases
steps
assumptions
documentation gap
test generation
AGATE structure
```

---

## DSL / YAML Rules

Canonical:

```text
05_DSL_YAML_RULES.md
```

Search terms:

```text
YAML
quoting
single quotes
multiline
placeholder composition
paths
```

---

# 13. Retrieval Policy

If an engine document is not found on the first retrieval:

```text
DO NOT immediately report it as missing.
```

Perform another search using:

1. canonical filename,
2. engine name,
3. semantic retrieval terms from this catalog.

Example:

If `08_ENGINE_OC.md` is not found initially, search for:

```text
OC
OpenShift
pod
PUT
GET
remote command
```

Only after this second retrieval attempt may it be considered unavailable.

---

# 14. Multi-Engine Retrieval

For requirements that combine several engines, verify each one explicitly.

Example:

```text
SQL -> FILE -> CMD -> OC
```

must retrieve:

```text
06_ENGINE_SQL.md
09_ENGINE_FILE.md
07_ENGINE_CMD.md
08_ENGINE_OC.md
```

The AI must not stop after retrieving only the shared AGATE documents.

---

# 15. AI Rule

Before generating an AGATE test:

```text
identify engines
retrieve engine docs
retrieve shared docs
generate complete test
```

Before reporting missing documentation:

```text
search filename
search engine name
search semantic terms
repeat retrieval
```

This catalog exists specifically to prevent false conclusions that an engine document is unavailable.
