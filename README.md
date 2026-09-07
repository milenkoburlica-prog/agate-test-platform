# AGATE Test Platform

> **One test. Multiple technologies. One execution flow.**

**AGATE** is an open-source enterprise test orchestration platform for automating complex business processes across multiple technical layers.

A single AGATE test can combine:

**REST · SOAP · SQL · CMD · FILE · OpenShift · CALL · JSON · PDF · WAIT**

within the same execution flow — using a human-readable YAML DSL.

Instead of assembling separate tools, libraries and scripts for each technology, AGATE models supported technologies as **first-class test steps within one unified execution model**.


This is the core idea behind AGATE:

> **The tester describes the business test. AGATE orchestrates the technologies.**

---

# ✨ Key Features

### Available Today

* ✅ Human-readable YAML test definitions
* ✅ Cross-technology test execution
* ✅ REST, SOAP, SQL, CMD, FILE, OpenShift, JSON, PDF and WAIT test steps and CALL reusable test steps.
* ✅ YAML templates and CSV-based test data
* ✅ Assertions and reference-based response validation
* ✅ Detailed execution logging and HTML reports
* ✅ Deterministic test generation from OpenAPI specifications
* ✅ OpenAPI contract change and breaking-change detection 
* ✅ OpenAPI test impact Analysis
* 🔄 Migration from Tricentis Tosca to AGATE

### Under Development

* 🚧 GUI automation
* 🚧 AI-assisted test generation and maintenance
* 🚧 Local LLM / Ollama integration
* 🚧 AGATE web client


> **Status legend:**
> ✅ Available / implemented
> 🚧 Under active development
> 💡 Planned concept — not implemented yet
> 🔄 Available as a migration approach/service

---

# 🚀 What Makes AGATE Different?

AGATE is **not intended to be just another REST or YAML test framework**.

Its primary goal is to orchestrate complete enterprise business tests across heterogeneous technologies.

```text
                              AGATE TEST CASE
                                    │
                                    ▼
                              Execution Core
                                    │
 ┌────────┬───────┬──────┬──────┬──────┬───────────┬──────┬──────┬──────┬──────┬──────┐
 ▼        ▼       ▼      ▼      ▼      ▼           ▼      ▼      ▼      ▼      ▼
REST     SOAP     SQL    CMD    FILE  OpenShift    WAIT   JSON    PDF    CALL   GUI
                                    │
                                    ▼
                              Unified Report
```

REST, SOAP, SQL, CMD, FILE, OpenShift, WAIT, JSON and PDF operations — together with reusable `CALL` steps — are not external concepts that the tester has to combine manually.

They are part of the **AGATE execution model**.

This allows technical operations to be combined into reusable business-level test scenarios.

---

# 🧪 Example

An AGATE test is defined in a human-readable YAML DSL.

A key concept is that the test case describes the **test flow**, while technical request definitions are kept in reusable modules.

For example:

```yaml
testCases:

  - id: TC_Create_And_Verify_Customer
    description: Create a customer and verify the response
    stage: '*'
    priority: HIGH

    variables:
      customerName: "John Doe"
      jsonplaceholder.endpoint: "https://jsonplaceholder.typicode.com"

    steps:

      - type: REST
        op: EXEC
        command: rest.jsonplaceholder.posts
        endpoint: "{B[jsonplaceholder.endpoint]}"
        response: create_customer

      - type: REST
        op: ASSERT
        response: create_customer
        source: STATUS
        action: EQUALS
        expected: 200

      - type: REST
        op: ASSERT
        response: create_customer
        source: BODY
        action: MATCH_REFERENCE
```

The REST step does not need to contain the complete HTTP request.

Instead, the `command` references a reusable AGATE module:

```text
rest.jsonplaceholder.posts
```

Conceptually:

```text
Test Case
   │
   │ command: rest.jsonplaceholder.posts
   ▼
REST Module
   │
   ├── metadata.json
   │      method
   │      path
   │      headers
   │      technical configuration
   │
   └── request.json
          │
          └── parameterized request body
```

For SOAP services, the same principle applies:

```text
SOAP Module
   │
   ├── metadata.json
   │
   └── request.xml
```

## Test Flow vs. Technical Request

This separation is intentional.

The YAML test case should primarily describe the **business and test flow**:

```text
Create customer
      │
      ▼
Validate REST response
      │
      ▼
Query database
      │
      ▼
Execute backend command
      │
      ▼
Validate generated file
```

The technical details of a potentially large REST or SOAP request do not have to be embedded directly into that flow.

Instead, they remain encapsulated in reusable modules.

A parameterized `request.json` could for example contain:

```json
{
  "name": "{B[customerName]}",
  "email": "{B[email]}",
  "customerType": "{XL[customerType]}"
}
```

At runtime, AGATE resolves the placeholders using the current execution context and test data.

This keeps even complex REST and SOAP tests compact and readable.

## Why This Matters

Enterprise REST and SOAP requests can contain hundreds of fields.

Embedding those structures directly into every test case would make test definitions large, repetitive and difficult to maintain.

AGATE therefore separates:

```text
WHAT is being tested
        │
        │  YAML Test Case
        ▼
Test flow, data and assertions


HOW the technical request looks
        │
        │  AGATE Module
        ▼
metadata.json + request.json / request.xml
```

The same module can be reused by many test cases while each test supplies different data through AGATE variables, environment configuration or CSV test data.

This also means that test artifacts do not necessarily have to be created manually.

They can originate from different engineering workflows:

```text
                         AGATE Test Artifacts
                                ▲
                                │
               ┌────────────────┼────────────────┐
               │                │                │
               │                │                │
          Manual Design    AGATE OpenAPI    Tosca Migration
               │                │                │
               └────────────────┼────────────────┘
                                │
                                ▼
                    metadata.json
                    request.json / request.xml
                    YAML test definitions
                    CSV test data
                                │
                                ▼
                         agate-server
                                │
                                ▼
                           Execution
```

For example, `agate-openapi` can deterministically derive REST modules and test artifacts from an OpenAPI contract.

A migration workflow can transform existing test assets into the same native AGATE structures.

From the perspective of `agate-server`, the origin of the artifact is secondary: execution uses the same AGATE DSL and module model.

## One Test – Multiple Technologies

The module concept also combines naturally with AGATE's cross-technology execution model.

A single test can therefore remain compact while orchestrating multiple technologies:

```text
                    AGATE Test Case
                          │
          ┌───────────────┼────────────────┐
          ▼               ▼                ▼
     REST Module       SOAP Module        SQL
          │               │                │
 metadata.json      metadata.json          │
 request.json       request.xml            │
          │               │                │
          └───────────────┬┴────────────────┘
                          │
                          ▼
                 Shared Execution Context
                          │
                    ┌─────┼─────┐
                    ▼     ▼     ▼
                   CMD   FILE  OpenShift
                          │
                          ▼
                    Unified Report
```

Values produced by one step can be consumed by subsequent steps through the shared execution context.

This is the important part of the example:

> **AGATE keeps the test flow readable by separating reusable technical request definitions from the business test scenario, while still allowing all technologies to participate in one shared execution flow.**

The tester describes the scenario.
Reusable modules encapsulate the technical requests.
AGATE resolves the data and orchestrates the execution.

---

# 🏗️ Project Architecture

AGATE separates deterministic test execution, deterministic API contract processing, optional AI-assisted functionality and migration tooling into dedicated components.

```text
                              AGATE Test Platform
                                     │
          ┌──────────────────────────┼──────────────────────────┐
          │                          │                          │
          ▼                          ▼                          ▼
    agate-server               agate-openapi          agate-tosca-migrator
          │                          │                          │
          │                     OpenAPI Contract          Tosca Migration
          │                       Processing                    │
          │                          │                          ▼
          │               ┌──────────┼──────────┐        Tosca → AGATE DSL
          │               ▼          ▼          ▼               │
          │            Parsing      Test      Change &          │
          │            & Model   Generation    Impact           │
          │                          │                          │
          │                          ▼                          │
          │                   AGATE Test Artifacts ◄────────────┘
          │                          │
          │                          ▼
          └───────────────────► agate-server
                                     │
                                     ▼
                               Execution Core
                                     │
 ┌────────┬───────┬──────┬──────┬──────┬───────────┬──────┬──────┬──────┬──────┬──────┐
 ▼        ▼       ▼      ▼      ▼      ▼           ▼      ▼      ▼      ▼      ▼
REST     SOAP     SQL    CMD    FILE  OpenShift    WAIT   JSON    PDF    CALL   GUI
                                     │
                                     ▼
                               Unified Report


                               AGATE AI
                                  │
                                  ▼
                              agate-ai
                                  │
                                  ▼
                         AI-assisted Workflows
                                  │
                                  ▼
                    AGATE Test Platform & Artifacts
```

A central architectural principle is:

> **Deterministic where possible. AI where useful.**

`agate-server` executes tests deterministically.

`agate-openapi` analyzes OpenAPI contracts and derives technical test artifacts deterministically.

`agate-tosca-migrator` deterministically transforms supported Tricentis Tosca test structures into AGATE test definitions.

`agate-ai` adds optional AI-assisted workflows where semantic understanding can provide additional value.

---

# 📦 Modules

## 1️⃣ AGATE Server

`agate-server` is the deterministic execution core of AGATE.

It loads AGATE YAML test suites, resolves configuration and test data, executes the requested technology engines and produces execution reports.

### Core Capabilities

* YAML-based AGATE DSL
* Cross-technology test execution
* Reusable test components
* Shared execution context
* Environment configuration
* User-specific configuration
* Data-driven tests
* Assertions
* Detailed execution logging
* HTML reports
* CI/CD integration

### Native Test Engines

| Engine       | Purpose                                    | AGATE Support         |
| ------------ | ------------------------------------------ | --------------------- |
| 🌐 REST      | REST API execution and validation          | **Native**            |
| 🏢 SOAP      | SOAP service execution and validation      | **Native**            |
| 🗄️ SQL      | Database queries and assertions            | **Native**            |
| 🖥️ CMD      | Command-line execution                     | **Native**            |
| 📁 FILE      | Local file operations, content extraction and validation | **Native**            |
| ☸️ OpenShift | OpenShift CLI operations and validation    | **Native**            |
| ⏳ WAIT       | Synchronization and asynchronous workflows | **Native**            |
| 📑 JSON      | JSON processing and validation             | **Native**            |
| 📄 PDF       | PDF validation                             | **Native**            |
| 📦 BUFFER    | Shared runtime data / value handling       | **Native**            |
| 🌐 GUI       | Browser-based UI automation                | **Under Development** |

### Why Native Engines Matter

With AGATE, a tester does not need to assemble a separate automation stack before describing a cross-technology business scenario.

The engines share the same execution context, allowing values produced by one step to be consumed by another.

For example:

```text
REST response
     │
     ▼
Extract customerId
     │
     ▼
SQL verification
     │
     ▼
SOAP processing
     │
     ▼
OpenShift validation
```

---

## 2️⃣ AGATE OpenAPI

`agate-openapi` provides deterministic OpenAPI-driven test generation, contract change detection and test impact analysis.

It accepts OpenAPI specifications in **YAML or JSON format**, either as local files or directly from remote URLs.

Its three primary workflows are:

```text
OpenAPI Specification
        │
        ├── generate
        │      │
        │      ▼
        │  AGATE Test Application
        │
        ├── changes
        │      │
        │      ▼
        │  Contract Changes
        │  Breaking Changes
        │
        └── impact
               │
               ▼
        Affected AGATE Tests
        and Test Artifacts
```

Main Capabilities
* OpenAPI YAML/JSON loading from local files or URLs
* Deterministic OpenAPI parsing
* $ref resolution
* Endpoint and operation extraction
* Parameter, request and response analysis
* Validation constraint extraction
* Deterministic technical test generation
* CSV test-data generation
* AGATE YAML test-template generation
* REST module generation
* Complete AGATE application generation
* OpenAPI contract change detection
* Breaking-change classification
* Test impact analysis for existing AGATE artifacts
* Identification of affected test data and test cases

The generated tests represent a deterministic technical test baseline derived from the OpenAPI contract.

AGATE deliberately does not invent business behavior that is not described by the API contract. Testers can extend the generated baseline with domain-specific test data, business preconditions and additional validations where required.


# 🚀 Getting Started

Clone the repository:

```bash
git clone https://github.com/milenkoburlica-prog/agate-test-platform.git
cd agate-test-platform
```
Execute Existing AGATE Tests
```bash
cd agate-server
startTests.bat DEMOS DEMOS DEMO rest_engine_demo.yaml
```

or execute all suites for the selected application/stage:

```bash
startTests.bat DEMOS DEMOS DEMO
```


Conceptually:

```text
Already have AGATE tests?
        │
        ▼
   agate-server
        │
        ▼
      Execute


Have an OpenAPI specification?
        │
        ▼
   agate-openapi
        │
        ▼
     generate
        │
        ▼
    AGATE tests
        │
        ▼
   agate-server
        │
        ▼
      Execute


API contract changed?
        │
        ▼
   agate-openapi
        │
        ├── changes ──► What changed?
        │
        └── impact  ──► Which existing tests are affected?


Have Tricentis Tosca tests?
        │
        ▼
 agate-tosca-migrator
        │
        ▼
    AGATE tests
        │
        ▼
   agate-server
        │
        ▼
      Execute
```

---


## 3️⃣ AGATE AI

### Under Development

`agate-ai` is the optional AI-assisted layer of AGATE.

It explores the use of LLMs for tasks where semantic interpretation can provide value without making deterministic test execution dependent on AI.

The initial direction is based on **local LLM execution using Ollama**.

Planned and experimental capabilities include:

* AI-assisted test scenario generation
* Coverage analysis
* Business-aware test engineering
* Test maintenance assistance
* Prompt management
* Local LLM support
* Ollama integration

The architectural principle is:

> **OpenAPI parsing, contract modeling, change detection and impact analysis should not require an LLM.**

AI is intended as an engineering assistant on top of deterministic AGATE models.

> **Deterministic where possible. AI where useful.**

---

## 4️⃣ AGATE Tosca Migrator

### Available on Request

Many organizations have invested heavily in **Tricentis Tosca** test assets.

AGATE provides a migration approach for transforming existing Tosca test structures into the native AGATE DSL.

```text
Existing Tosca Tests
        │
        ▼
AGATE Tosca Migrator
        │
        ▼
     AGATE DSL
        │
        ▼
Version-controlled
open test assets
```

The objective is to help organizations preserve existing test investments while transitioning toward an open and vendor-independent test automation architecture.

The Tosca migration component is currently offered as a **customized migration service**.

---

## 5️⃣ AGATE Client

### Under Development

`agate-client` is intended to provide a web-based interface for managing:

* Projects
* Test suites
* Test executions
* Test data
* Environments
* Reports
* OpenAPI analysis
* AI-assisted workflows

The AGATE Server remains the execution core, allowing tests to run independently from the web interface and making command-line and CI/CD execution possible.

---

# 🚀 Getting Started

Clone the repository:

```bash
git clone https://github.com/milenkoburlica-prog/agate-test-platform.git
cd agate-test-platform
```

The main execution engine is located in:

```text
agate-server
```

```cmd
cd agate-server
startTests.bat DEMOS DEMOS DEMO rest_engine_match_reference_demo.yaml
```

See the module documentation and demo suites for execution examples.

```text
agate-openapi
```

```cmd
cd agate-openapi
startOpenAPI.bat generate petstore3 resources\petstore3\openapi.yaml
```


---

# 🤝 Feedback and Contributions

AGATE is a young project and feedback from real automation engineers and testers is especially valuable.

If you are interested in:

* Cross-technology test automation
* Enterprise integration testing
* OpenAPI-driven testing
* API contract analysis
* AI-assisted test engineering
* Tosca migration
* Vendor-independent test automation

try AGATE and let us know what works — and what does not.

Issues, discussions and pull requests are welcome.

If you find the project useful, consider giving the repository a ⭐. It helps other testers and automation engineers discover AGATE.

---

# 📄 License

AGATE Test Platform is released under the MIT License.

---

# 📝 Recent Changes

## 2026-09-04

### ASSERT Improvements

* Extended ASSERT support across AGATE test execution.
* Added reference-based comparison of expected and actual responses.
* Added support for ignoring selected fields during structured response comparison.

## 2026-09-05 

### OpenAPI Contract Evolution 

* Added agate-openapi. 
* Added CLI support for comparing OpenAPI contract versions using `changes`. 
* Added classification of detected changes as `INFO`, `REVIEW` and `BREAKING`. 
* Added OpenAPI test impact analysis for existing AGATE applications. 
* Added mapping of contract changes to CSV, YAML and REST request artifacts. 
* Added detection of affected test cases and incompatible test data.

## 2026-09-08

### CMD, OpenShift and FILE Engine Improvements

* Extended the CMD Engine with configurable `expectedExitCode`, `checkExitCode`, `timeout` and `outputFile` support.
* Added automatic exit-code validation and timeout handling for CMD command execution.
* Extended the OpenShift Engine with the same configurable exit-code and timeout handling for `EXEC`, `PUT` and `GET`.
* Added local `outputFile` support for OpenShift `EXEC` command output.
* Improved OpenShift file-transfer handling for local Windows paths.
* Added the new native FILE Engine for local filesystem operations.
* Added FILE `EXEC` actions: `READ`, `WRITE`, `APPEND`, `COPY`, `MOVE`, `DELETE` and `EXISTS`.
* Added FILE `BUFFER` actions: `TEXT`, `FILTER`, `LINE`, `LAST_LINE` and `COUNT`.
* Added FILE `ASSERT` actions: `EXISTS`, `NOT_EXISTS`, `CONTAINS`, `NOT_CONTAINS`, `EQUALS`, `NOT_EQUALS` and `COUNT`.
* Added shared command execution infrastructure for timeout handling, exit-code validation and command results.
* Added and validated extended CMD, OpenShift and FILE Engine demo scenarios.

