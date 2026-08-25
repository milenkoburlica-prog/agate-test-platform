# AGATE Test Platform

> **One test. Multiple technologies. One execution flow.**

**AGATE** is an open-source enterprise test orchestration platform for automating complex business processes across multiple technical layers.

A single AGATE test can combine:

**REST · SOAP · SQL · OpenShift · Command Line · GUI**

within the same execution flow — using a human-readable YAML DSL.

Instead of assembling separate tools, libraries and scripts for each technology, AGATE models supported technologies as **first-class test steps within one unified execution model**.

---

## Why AGATE?

Enterprise tests rarely stop at a single REST request.

A real business process may require:

```text
REST
  ↓
SQL
  ↓
SOAP
  ↓
WAIT
  ↓
OpenShift
  ↓
CMD
  ↓
GUI
  ↓
REPORT
```

For example:

1. Create an entity through a REST API.
2. Verify the persisted data using SQL.
3. Trigger processing through a SOAP service.
4. Wait for asynchronous processing.
5. Validate the application state in OpenShift.
6. Execute a command-line verification.
7. Verify the final result in the GUI.

**AGATE executes all of these steps as one test case and produces one consolidated test result.**

This is the core idea behind AGATE:

> **The tester describes the business test. AGATE orchestrates the technologies.**

---

# ✨ Key Features

- ✅ Human-readable, no-code YAML test definitions
- ✅ Cross-technology test orchestration
- ✅ REST, SOAP, SQL, CMD and OpenShift as first-class test steps
- ✅ Reusable business and technical test components
- ✅ Data-driven testing
- ✅ Environment-independent test definitions
- ✅ Shared variables and data between different test engines
- ✅ Detailed execution logging and HTML reports
- ✅ CI/CD-friendly execution
- ✅ Extensible engine architecture
- ✅ Deterministic OpenAPI parsing and contract modeling
- ✅ OpenAPI change detection and impact analysis
- 🚧 GUI automation
- 🚧 OpenAPI-driven test generation
- 🚧 AI-assisted test generation and maintenance
- 🔄 Migration path from Tricentis Tosca

---

# 🚀 What Makes AGATE Different?

AGATE is **not intended to be just another REST or YAML test framework**.

Its primary goal is to orchestrate complete enterprise business tests across heterogeneous technologies.

```text
                   AGATE TEST CASE

 ┌──────────────────────────────────────────┐
 │                                          │
 │  REST  →  SQL  →  SOAP  →  WAIT         │
 │                     ↓                    │
 │                   CMD                    │
 │                     ↓                    │
 │                OpenShift                 │
 │                     ↓                    │
 │                    GUI                   │
 │                                          │
 └──────────────────────────────────────────┘
                     │
                     ▼
               Unified Report
```

REST, SOAP, SQL, command-line and OpenShift operations are not external concepts that the tester has to combine manually.

They are part of the **AGATE execution model**.

This allows technical operations to be combined into reusable business-level test scenarios.

---

# 🧪 Example

An AGATE test is defined in YAML:

```yaml
testCases:

  - id: TC_Create_And_Verify_Customer
    description: Create customer and verify backend processing
    priority: HIGH

    steps:

      - type: REST
        op: EXEC
        module: create_customer
        response: customer_response

      - type: SQL
        op: EXEC
        query: >
          SELECT STATUS
          FROM CUSTOMER
          WHERE CUSTOMER_ID = '{B[customerId]}'
        response: customer_db

      - type: SQL
        op: ASSERT
        action: EQUALS
        expected: ACTIVE

      - type: CMD
        op: EXEC
        command: "echo Customer validation completed"
```

The important part is not YAML itself.

The important part is that **different technologies participate in the same business test and exchange data within the same execution context**.

---

# 🏗️ Project Architecture

AGATE separates deterministic test execution, deterministic API contract analysis, optional AI-assisted functionality and migration tooling into dedicated components.

```text
                             AGATE Test Platform
                                     │
          ┌──────────────────────────┼──────────────────────────┐
          │                          │                          │
          ▼                          ▼                          ▼
    agate-server               agate-openapi                agate-ai
          │                          │                          │
          │                    OpenAPI Contract             AI-assisted
          │                       Analysis                  Workflows
          │                          │
          │                ┌─────────┼─────────┐
          │                ▼         ▼         ▼
          │             Parsing    Change     Impact
          │             & Model   Detection  Analysis
          │
          ▼
     Execution Core
          │
    ┌─────┼─────┬─────┬─────┬─────┬─────┐
    ▼     ▼     ▼     ▼     ▼     ▼     ▼
   REST  SOAP   SQL   CMD    OC   WAIT   GUI
          │
          ▼
     Unified Report


                    Additional Components
                            │
                 ┌──────────┴──────────┐
                 ▼                     ▼
       agate-tosca-migrator       agate-client
                 │                (under development)
                 │                     │
                 ▼                     ▼
          Tosca → AGATE DSL      Web Management UI
```

A central architectural principle is:

> **Deterministic where possible. AI where useful.**

`agate-server` executes tests deterministically.  
`agate-openapi` analyzes API contracts deterministically.  
`agate-ai` adds optional AI-assisted workflows on top of those deterministic foundations.

---

# 📦 Modules

## 1️⃣ AGATE Server

`agate-server` is the deterministic execution core of AGATE.

It loads AGATE YAML test suites, resolves configuration and test data, executes the requested technology engines and produces execution reports.

### Core Capabilities

- YAML-based AGATE DSL
- Cross-technology test execution
- Reusable test components
- Shared execution context
- Environment configuration
- User-specific configuration
- Data-driven tests
- Assertions
- Detailed execution logging
- HTML reports
- CI/CD integration

### Native Test Engines

| Engine | Purpose | AGATE Support |
| --- | --- | --- |
| 🌐 REST | REST API execution and validation | **Native** |
| 🏢 SOAP | SOAP service execution and validation | **Native** |
| 🗄️ SQL | Database queries and assertions | **Native** |
| 🖥️ CMD | Command-line execution | **Native** |
| ☸️ OpenShift | OpenShift CLI operations and validation | **Native** |
| ⏳ WAIT | Synchronization and asynchronous workflows | **Native** |
| 📑 JSON | JSON processing and validation | **Native** |
| 📄 PDF | PDF validation | **Native** |
| 📦 BUFFER | Shared runtime data / value handling | **Native** |
| 🌐 GUI | Browser-based UI automation | **Under Development** |

### Why Native Engines Matter

With AGATE, a tester does not need to assemble a separate automation stack before describing a cross-technology business scenario.

For supported engines, the technology is represented directly in the AGATE DSL:

```yaml
- type: REST
```

```yaml
- type: SQL
```

```yaml
- type: SOAP
```

```yaml
- type: CMD
```

```yaml
- type: OC
```

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

`agate-openapi` is the deterministic API contract analysis component of AGATE.

Its purpose is to transform OpenAPI YAML or JSON specifications into an internal AGATE model that can be used for analysis, change detection, impact analysis and later test generation.

### Core Capabilities

- OpenAPI YAML/JSON loading
- Deterministic parsing
- `$ref` resolution
- Endpoint and operation extraction
- HTTP method analysis
- Parameter analysis
- Request-body schema analysis
- Response schema analysis
- Metadata extraction
- Validation constraint extraction
- OpenAPI contract change detection
- Breaking-change detection
- Test impact analysis
- CLI-based inspection

The processing model is intentionally deterministic:

```text
OpenAPI Specification
        │
        ▼
   agate-openapi
        │
        ├── Parse & Resolve $ref
        │
        ├── Build AGATE API Model
        │
        ├── Detect Contract Changes
        │
        └── Analyze Test Impact
        │
        ▼
Deterministic Contract Knowledge
```

The goal is to provide a reliable technical foundation before optional AI functionality is introduced.

### Planned Direction

```text
OpenAPI
   │
   ▼
API Contract
   │
   ▼
Change Detection
   │
   ▼
Impact Analysis
   │
   ▼
Business Contract
   │
   ▼
Test Contract
   │
   ▼
Generated / Maintained AGATE Tests
```

This is the foundation for AGATE's longer-term direction toward **contract-aware enterprise test automation**.

---

## 3️⃣ AGATE AI

### Under Development

`agate-ai` is the optional AI-assisted layer of AGATE.

It explores the use of LLMs for tasks where probabilistic assistance can provide value without making deterministic execution dependent on AI.

Planned and experimental capabilities include:

- AI-assisted test scenario generation
- OpenAPI-based test generation
- Test data generation
- Coverage analysis
- Business contract generation
- Test contract generation
- Test maintenance assistance
- Prompt management
- Local LLM support
- Ollama integration

The architectural principle is:

> **OpenAPI parsing, contract modeling, change detection and impact analysis should not require an LLM.**

AI is used as an assistant on top of deterministic AGATE models.

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

- Projects
- Test suites
- Test executions
- Test data
- Environments
- Reports
- OpenAPI analysis
- AI-assisted workflows

The AGATE Server remains the execution core, allowing tests to run independently from the web interface and making command-line and CI/CD execution possible.

---

# 🔁 Reusable Business Steps

AGATE separates reusable technical operations from business test scenarios.

Common operations can therefore be defined once and reused by multiple tests.

Examples:

```text
Create Customer
      ↓
Verify Customer In Database
      ↓
Activate Contract
      ↓
Wait For Processing
      ↓
Verify Backend Status
```

This allows tests to describe **what the business process does** without repeatedly implementing the underlying technical details.

---

# 📊 Data-Driven Testing

AGATE supports external test data so that test logic and test data can be maintained independently.

The same test scenario can therefore execute against multiple datasets without duplicating the test definition.

This is particularly useful for:

- Positive and negative test cases
- Boundary-value testing
- Different users and roles
- Different environments
- Regression suites

---

# 🌍 Environment Separation

AGATE test definitions are designed to remain independent from the target environment.

Environment-specific configuration can be externalized:

```text
DEV
TEST
INTEGRATION
STAGING
PRODUCTION
```

The same business test can therefore be executed against different environments without modifying its test logic.

---

# 📊 Reporting

AGATE generates execution reports for test runs.

Reports provide information about:

- Executed test cases
- Individual test steps
- Execution status
- Assertion results
- Errors
- Execution timing

The goal is to provide one consolidated view even when a test crosses several technical layers.

---

# 🆚 How Is AGATE Different?

Several excellent open-source testing frameworks already exist.

AGATE does not attempt to replace every specialized testing tool.

Instead, its focus is **cross-technology enterprise test orchestration combined with deterministic contract analysis**.

| Capability | AGATE | Karate | Robot Framework | Step CI | Tavern | Schemathesis |
| --- | --- | --- | --- | --- | --- | --- |
| REST | 🟢 **Native** | 🟢 Native | 🟡 Library | 🟢 Native | 🟢 Native | 🟢 Native |
| SOAP | 🟢 **Native** | 🟢 Native | 🟡 Library | 🟢 Native | 🟡 Extension | 🔴 |
| SQL / Database | 🟢 **Native** | 🟡 Integration | 🟡 Library | 🔴 | 🟡 Python | 🔴 |
| CMD / Shell | 🟢 **Native** | 🟡 Integration | 🟢 Process Library | 🔴 | 🟡 Python | 🔴 |
| OpenShift | 🟢 **Native** | 🟡 Custom Integration | 🟡 Library / CLI | 🔴 | 🔴 | 🔴 |
| GUI / Browser | 🚧 | 🟢 Native | 🟡 Browser/Selenium Library | 🔴 | 🔴 | 🔴 |
| Cross-technology scenario | 🟢 **Core concept** | 🟢 Possible | 🟢 Possible | 🟡 API focus | 🟡 API focus | 🔴 API focus |
| Reusable business steps | 🟢 **Native** | 🟢 | 🟢 Keywords | 🟢 | 🟡 | 🔴 Different approach |
| Data-driven testing | 🟢 **Native** | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 Generated data |
| Deterministic OpenAPI model | 🟢 **agate-openapi** | 🟡 Ecosystem | 🟡 Extensions | 🟡 | 🔴 | 🟢 Core API-contract focus |
| OpenAPI-driven generation | 🚧 | 🟡 Ecosystem | 🟡 Extensions | 🟡 | 🔴 | 🟢 **Core strength** |
| Contract change analysis | 🟢 **agate-openapi** | 🔴 Not core | 🔴 Not core | 🔴 | 🔴 | 🟡 Contract focus |
| Change → impacted tests | 🟢 **AGATE Impact Analysis** | 🔴 | 🔴 | 🔴 | 🔴 | 🟡 Different approach |
| Tosca migration | 🟢 **AGATE Migrator** | 🔴 | 🔴 | 🔴 | 🔴 | 🔴 |
| Local LLM integration | 🚧 **Ollama** | 🔴 Not core | 🟡 Extensible | 🔴 | 🔴 | 🔴 |

### Legend

- 🟢 Native / first-class capability or standard AGATE component
- 🟡 Available through libraries, extensions or custom integration
- 🔴 Not a primary capability
- 🚧 Under development

> **Note:** A library-based approach does not necessarily mean that testers have to write code. Frameworks such as Robot Framework provide a large ecosystem of ready-to-use libraries and keywords. AGATE takes a more opinionated approach by modeling its supported technologies directly as first-class steps of one unified execution model.

---

# ⚖️ AGATE vs. Robot Framework

Robot Framework is a mature and highly extensible generic automation framework with a large library ecosystem.

AGATE follows a different approach.

For its supported technologies, REST, SOAP, SQL, CMD and OpenShift are modeled directly as AGATE test steps rather than requiring the tester to assemble a set of technology-specific libraries.

```text
Robot Framework

Generic Automation Framework
          │
          ▼
       Keywords
          │
          ▼
       Libraries
          │
    ┌─────┼─────┐
    ▼     ▼     ▼
   API    DB    Browser ...


AGATE

Enterprise Test Orchestration
          │
          ▼
      Business Test
          │
    ┌─────┼─────┬─────┬─────┐
    ▼     ▼     ▼     ▼     ▼
   REST  SOAP   SQL   CMD    OC
          │
          ▼
   Unified Execution
```

Robot Framework's library ecosystem is a major strength.

AGATE's goal is different:

> **The tester should not have to assemble a test automation technology stack before describing the business test.**

---

# 🔬 Contract-Aware Testing Direction

AGATE's long-term direction connects deterministic API contract analysis with executable cross-technology tests.

```text
OpenAPI Specification
        │
        ▼
   agate-openapi
        │
        ▼
   API Contract Model
        │
        ▼
   Change Detection
        │
        ▼
    Impact Analysis
        │
        ▼
Business / Test Contract
        │
        ▼
     AGATE Tests
        │
   ┌────┼────┬────┬────┐
   ▼    ▼    ▼    ▼    ▼
 REST  SQL  SOAP  OC   GUI
        │
        ▼
   Unified Report
```

This is intended to answer not only:

> **Can this API call be tested?**

but also:

> **What changed in the contract, which tests are affected, and which business scenarios should be executed?**

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

Build the project:

```bash
cd agate-server
./mvnw clean package
```

On Windows:

```cmd
mvnw.cmd clean package
```

See the module documentation and demo suites for execution examples.

---

# 🎯 Vision

AGATE aims to become a lightweight, open and vendor-independent platform for **enterprise test automation and intelligent test engineering**.

The long-term vision combines deterministic contract analysis, cross-technology test orchestration and optional AI assistance.

```text
                    BUSINESS REQUIREMENTS
                    Pflichtenheft / Specs
                            │
                            ▼
                    ┌───────────────┐
                    │   Local AI    │
                    │ Ollama / LLM  │
                    └───────┬───────┘
                            │
                            ▼
                     BUSINESS CONTRACT
                            │
                            │
        OpenAPI             │
           │                │
           ▼                │
     API CONTRACT ──────────┤
           │                │
           └────────┬───────┘
                    ▼
               TEST CONTRACT
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
   Deterministic         AI-assisted
   Test Generation       Test Design
          │                   │
          └─────────┬─────────┘
                    ▼
                AGATE TESTS
                    │
       ┌────────────┼─────────────┐
       ▼            ▼             ▼
     APIs       Databases    Infrastructure
       │            │             │
       └────────────┼─────────────┘
                    ▼
               UI / Systems
                    │
                    ▼
              Unified Report
```

AGATE follows a simple architectural principle:

> **Deterministic where possible. AI where useful.**

Deterministic components such as `agate-openapi` are responsible for facts that can be reliably derived from technical contracts:

- API endpoints and operations
- parameters
- request and response schemas
- validation constraints
- contract changes
- breaking changes
- test impact analysis

AI is intended for tasks that require semantic understanding rather than simple structural analysis.

For example, a local LLM can help analyze business specifications and requirements to identify:

- business rules
- preconditions
- expected behavior
- dependencies between business processes
- positive and negative scenarios
- boundary conditions
- missing test coverage
- candidate test data
- relationships between business requirements and technical API contracts

The resulting knowledge can be represented as a **Business Contract** and combined with the deterministic **API Contract** to derive a **Test Contract**.

```text
Technical truth                    Business meaning
     │                                   │
     ▼                                   ▼
OpenAPI / API Contract       Requirements / Business Contract
     │                                   │
     └────────────────┬──────────────────┘
                      ▼
                 TEST CONTRACT
                      │
                      ▼
                 AGATE TESTS
```

## Local AI First

AGATE's initial AI direction is based on **local LLM execution using Ollama**.

This is particularly important for enterprise environments where API specifications, business requirements, test data or internal system documentation should not be sent to external AI services.

```text
Enterprise Documents
OpenAPI Specifications
Test Definitions
        │
        ▼
   Local Ollama
        │
        ▼
   Local LLM
        │
        ▼
  AGATE AI Services
```

The goal is not to let an LLM control test execution.

The goal is to use AI as an **engineering assistant** for understanding requirements, designing tests and maintaining test assets, while keeping test execution deterministic, reproducible and transparent.

Ultimately, AGATE aims to connect:

> **Business requirements → API contracts → business contracts → test contracts → executable cross-technology tests → deterministic results**

with AI helping where human-level semantic interpretation is useful and deterministic components remaining responsible wherever exact and reproducible behavior is required.

# 🤝 Feedback and Contributions

AGATE is a young project and feedback from real automation engineers and testers is especially valuable.

If you are interested in:

- Cross-technology test automation
- Enterprise integration testing
- OpenAPI-driven testing
- Contract-aware test automation
- Tosca migration
- Vendor-independent test automation

try AGATE and let us know what works — and what does not.

Issues, discussions and pull requests are welcome.

If you find the project useful, consider giving the repository a ⭐. It helps other testers and automation engineers discover AGATE.

---

# 📄 License

AGATE Test Platform is released under the MIT License.