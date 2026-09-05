## Generate AGATE Tests from OpenAPI

The primary purpose of `agate-openapi` is to generate an AGATE test application directly from an OpenAPI specification.

In the normal AGATE workflow, users typically only need the `generate` command.

```bash
startOpenAPI.bat generate <APP_ID> <OPENAPI_SOURCE>
```

`OPENAPI_SOURCE` can be:

* a local OpenAPI YAML file
* a local OpenAPI JSON file
* a remote OpenAPI YAML URL
* a remote OpenAPI JSON URL

### Examples

Generate from a local JSON specification:

```bash
startOpenAPI.bat generate petstoredelme resources\petstore3\openapi.json
```

Generate from a local YAML specification:

```bash
startOpenAPI.bat generate petstoredelme resources\petstore3\openapi.yaml
```

Generate directly from a remote YAML specification:

```bash
startOpenAPI.bat generate petstoredelme https://petstore3.swagger.io/api/v3/openapi.yaml
```

Generate directly from a remote JSON specification:

```bash
startOpenAPI.bat generate petstoredelme https://petstore3.swagger.io/api/v3/openapi.json
```

The input format does not have to be specified explicitly. AGATE loads and parses both YAML and JSON OpenAPI specifications automatically.

### What is generated?

AGATE analyzes the complete OpenAPI specification and generates deterministic technical tests for the available API operations.

The main generated artifacts are:

```text
OpenAPI Specification
        │
        ▼
   agate-openapi
        │
        ├── CSV test data
        │
        ├── AGATE YAML test template
        │
        └── REST modules
```

For example:

```text
data/
└── petstoredelme/
    │
    ├── template/
    │   ├── GET_pet_petId.csv
    │   ├── GET_pet_petId.yaml
    │   ├── POST_pet.csv
    │   ├── POST_pet.yaml
    │   └── ...
    │
    └── modules/
        └── rest/
            ├── GET_pet_petId/
            │   ├── metdata.json
            │   └── request.json
            │
            ├── POST_pet/
            │   ├── metdata.json
            │   └── request.json
            │
            └── ...
```

The generated YAML templates and CSV files provide a deterministic technical test baseline derived directly from the OpenAPI contract.

Testers can then extend these generated tests with application-specific test data, business preconditions and additional validations where required.

---

## OpenAPI Contract Change Detection

`agate-openapi` can compare two versions of an OpenAPI specification and detect contract changes between them.

This can be used to identify changes before existing tests or API consumers are affected.

```bash
startOpenAPI.bat changes <OLD_OPENAPI> <NEW_OPENAPI>
```

For example:

```bash
startOpenAPI.bat changes resources\petstore3\openapi.yaml resources\petstore4\openapi.yaml
```

AGATE classifies detected contract changes by severity:

```text
INFO
    A compatible contract change was detected.

REVIEW
    The change should be reviewed.

BREAKING
    The change can break existing API consumers or tests.
```

AGATE can currently detect changes such as:

* new API operations
* request parameters becoming required
* request body properties becoming required
* new optional request body properties
* enum changes

For example:

```text
============================================================
AGATE OPENAPI CHANGE DETECTION
============================================================

old source : resources\petstore3\openapi.yaml
new source : resources\petstore4\openapi.yaml

changes    : 7
breaking   : 4
review     : 2

------------------------------------------------------------
PUT:/pet
------------------------------------------------------------

CHANGE 002
  severity    : BREAKING
  type        : MODIFIED
  location    : request.body.status
  property    : required
  old         : false
  new         : true
  description : required changed

CHANGE 003
  severity    : REVIEW
  type        : ADDED
  location    : request.body.microchipId
  new         : FieldContract{required=false, type='string', format='null'}
  description : New optional request body property

------------------------------------------------------------
GET:/pet/findByStatus
------------------------------------------------------------

CHANGE 006
  severity    : BREAKING
  type        : MODIFIED
  location    : request.query.status
  property    : enum
  old         : [available, pending, sold]
  new         : [available, pending]
  description : enum changed
```

The change detector works on the OpenAPI contract itself. Existing AGATE test artifacts are not required for this analysis.

Conceptually:

```text
Old OpenAPI ──┐
              ├── Contract Change Detection
New OpenAPI ──┘
                       │
                       ▼
                Detected Changes
                       │
             ┌─────────┼─────────┐
             ▼         ▼         ▼
            INFO     REVIEW    BREAKING
```

---

## OpenAPI Test Impact Analysis

Detecting a contract change answers one question:

> What changed in the API?

AGATE impact analysis goes one step further:

> Which existing AGATE tests and test artifacts are affected by that change?

The `impact` command compares the old and new OpenAPI contracts and analyzes the generated AGATE application:

```bash
startOpenAPI.bat impact <OLD_OPENAPI> <NEW_OPENAPI> <APP_DIRECTORY>
```

For example:

```bash
startOpenAPI.bat impact resources\petstore3\openapi.yaml resources\petstore4\openapi.yaml data\petstore3
```

The application directory should point to an existing AGATE application, for example one previously created with:

```bash
startOpenAPI.bat generate petstore3 resources\petstore3\openapi.yaml
```

The analysis maps OpenAPI contract changes to concrete AGATE test artifacts.

For example:

```text
============================================================
AGATE OPENAPI IMPACT ANALYSIS
============================================================

old contract : resources\petstore3\openapi.yaml
new contract : resources\petstore4\openapi.yaml
application  : data\petstore3

API changes  : 7
OPEN IMPACTS : 7

------------------------------------------------------------
PUT:/pet
------------------------------------------------------------

IMPACT 001
  change    : request.body.microchipId
  artifact  : CSV
  file      : data\petstore3\template\PUT_pet.csv
  location  : row: microchipId
  action    : ADD_CSV_ROW
  reason    : New API field is missing from current CSV

IMPACT 002
  change    : request.body.microchipId
  artifact  : YAML
  file      : data\petstore3\template\PUT_pet.yaml
  location  : REST EXEC: microchipId
  action    : ADD_YAML_FIELD
  reason    : New request body field is missing from YAML template

IMPACT 003
  change    : request.body.microchipId
  artifact  : REQUEST_JSON
  file      : data\petstore3\modules\rest\PUT_pet\request.json
  location  : $.microchipId
  action    : ADD_REQUEST_JSON_FIELD
  reason    : New request body field is missing from request.json
```

Impact analysis can also identify existing test data that is no longer compatible with the new contract.

For example, if an enum changes from:

```text
[available, pending, sold]
```

to:

```text
[available, pending]
```

and an existing test case uses `sold`, AGATE reports the affected test case:

```text
IMPACT 007
  change    : request.query.status.enum
  artifact  : CSV
  file      : data\petstore3\template\GET_pet_findByStatus.csv
  location  : row: status
  testcase  : TC005_status_valid_enum_value
  current   : sold
  expected  : [available, pending]
  action    : REVIEW_EXPECTED_OUTCOME
  reason    : Value sold is not part of new enum [available, pending]
```

The impact report therefore connects API contract evolution directly with existing AGATE tests:

```text
Old OpenAPI ──┐
              ├── Change Detection
New OpenAPI ──┘
                       │
                       ▼
                  API Changes
                       │
                       ▼
                Impact Analysis
                       │
                       ├── CSV
                       ├── YAML
                       ├── request.json
                       └── individual test cases
```

Typical impact actions include:

```text
ADD_CSV_ROW
ADD_YAML_FIELD
ADD_REQUEST_JSON_FIELD
REVIEW_EXPECTED_OUTCOME
```

The impact analyzer reports the required or recommended actions. It does not automatically modify the existing AGATE test artifacts.

This keeps contract analysis deterministic while leaving changes to existing test assets under tester control.

---

## OpenAPI Lifecycle

Together, the three main OpenAPI capabilities provide a simple workflow for creating and maintaining AGATE tests:

```text
                 OpenAPI
                    │
                    ▼
                generate
                    │
                    ▼
             AGATE Test Application
                    │
                    │
        API contract evolves
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
     Old OpenAPI          New OpenAPI
          │                   │
          └─────────┬─────────┘
                    ▼
                 changes
                    │
                    ▼
          What changed in the API?
                    │
                    ▼
                  impact
                    │
                    ▼
       Which existing AGATE tests
            are affected?
```

In short:

```text
generate  → OpenAPI → AGATE tests

changes   → Old OpenAPI vs New OpenAPI
            → contract changes
            → breaking changes

impact    → Contract changes + existing AGATE application
            → affected artifacts
            → affected test cases
            → recommended actions
```

---

## Advanced / Developer Commands

The commands in this section are intended primarily for development, debugging and deeper inspection of the OpenAPI processing pipeline.

They are **not required for the normal AGATE workflow**.

For regular test generation, the `generate` command is normally sufficient:

```bash
startOpenAPI.bat generate <APP_ID> <OPENAPI_SOURCE>
```

### Inspect the OpenAPI Contract Model

The `model` command displays how AGATE interprets the complete OpenAPI specification.

```bash
startOpenAPI.bat model <OPENAPI_SOURCE>
```

Examples:

```bash
startOpenAPI.bat model resources\petstore3\openapi.yaml
startOpenAPI.bat model resources\petstore3\openapi.json
startOpenAPI.bat model https://petstore3.swagger.io/api/v3/openapi.yaml
startOpenAPI.bat model https://petstore3.swagger.io/api/v3/openapi.json
```

This command is useful for verifying that endpoints, parameters, request bodies, responses, schemas and OpenAPI constraints are interpreted correctly.

---

### Phase 1 – Inspect an Operation Model

Phase 1 selects a single API operation from the OpenAPI contract and builds the internal AGATE Operation Model used for test generation.

```bash
startOpenAPI.bat phase1 <OPENAPI_SOURCE> <METHOD> <PATH>
```

Examples:

```bash
startOpenAPI.bat phase1 resources\petstore3\openapi.yaml GET /pet/{petId}
startOpenAPI.bat phase1 resources\petstore3\openapi.json GET /pet/{petId}
startOpenAPI.bat phase1 https://petstore3.swagger.io/api/v3/openapi.yaml GET /pet/{petId}
startOpenAPI.bat phase1 https://petstore3.swagger.io/api/v3/openapi.json GET /pet/{petId}
```

Conceptually:

```text
OpenAPI Specification
        │
        ▼
  Contract Model
        │
        ▼
  Operation Model
```

---

### Phase 2 – Inspect Deterministic Test Generation

Phase 2 shows the technical test cases that AGATE can deterministically derive from a selected API operation.

```bash
startOpenAPI.bat phase2 <OPENAPI_SOURCE> <METHOD> <PATH>
```

Examples:

```bash
startOpenAPI.bat phase2 resources\petstore3\openapi.yaml GET /pet/{petId}
startOpenAPI.bat phase2 resources\petstore3\openapi.json GET /pet/{petId}
startOpenAPI.bat phase2 https://petstore3.swagger.io/api/v3/openapi.yaml GET /pet/{petId}
startOpenAPI.bat phase2 https://petstore3.swagger.io/api/v3/openapi.json GET /pet/{petId}
```

Phase 2 does not invent business behavior. It derives technical test cases from information available in the OpenAPI contract.

---

### List Generated Test Cases

The `list` command displays the generated test cases for a selected API operation.

```bash
startOpenAPI.bat list <OPENAPI_SOURCE> <METHOD> <PATH>
```

Example:

```bash
startOpenAPI.bat list resources\petstore3\openapi.yaml GET /pet/{petId}
```

Example output:

```text
========================================
AGATE PHASE 3 - TEST LIST
========================================

identity : GET:/pet/{petId}
tests    : 2

TC001_Baseline_valid_request
TC002_Missing_required_parameter_petId

========================================
```

---

### Inspect a Generated Test Case

The `test` command displays the details of a single generated test case.

```bash
startOpenAPI.bat test <TEST_ID> <OPENAPI_SOURCE> <METHOD> <PATH>
```

Example:

```bash
startOpenAPI.bat test TC001_Baseline_valid_request resources\petstore3\openapi.yaml GET /pet/{petId}
```

This can be used to inspect the generated request data and expected technical outcome before AGATE artifacts are created.

---

### Generate CSV for a Single Operation

The `csv` command outputs the generated CSV test data for one selected API operation.

```bash
startOpenAPI.bat csv <OPENAPI_SOURCE> <METHOD> <PATH>
```

Example:

```bash
startOpenAPI.bat csv resources\petstore3\openapi.yaml GET /pet/{petId}
```

This is mainly useful for inspecting or debugging the CSV generation independently of the complete application generation.

---

### Generate YAML for a Single Operation

The `yaml` command outputs the generated AGATE YAML template for one selected API operation.

```bash
startOpenAPI.bat yaml <OPENAPI_SOURCE> <METHOD> <PATH>
```

Example:

```bash
startOpenAPI.bat yaml resources\petstore3\openapi.yaml GET /pet/{petId}
```

This is mainly useful for inspecting or debugging YAML generation independently of the complete application generation.

---

### OpenAPI Processing Pipeline

The advanced commands expose the individual stages that are normally executed automatically by `generate`:

```text
OpenAPI Specification
        │
        ▼
     model
        │
        ▼
OpenAPI Contract Model
        │
        ▼
     phase1
        │
        ▼
Operation Model
        │
        ▼
     phase2
        │
        ▼
Deterministic Test Cases
        │
        ▼
   Phase 3
        │
        ├── list
        ├── test
        ├── csv
        └── yaml
```

For normal AGATE usage, these individual steps do not need to be executed manually. The `generate` command runs the required processing pipeline and creates the complete AGATE test artifacts from the OpenAPI specification.
