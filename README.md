# AGATE Test Platform

> **One test. Multiple technologies. One execution flow.**

**AGATE** is an open-source enterprise test orchestration platform for automating complex business processes across multiple technical layers.

A single AGATE test can combine:

**REST · SOAP · SQL · OpenShift · Command Line · GUI**

within the same execution flow — using a human-readable YAML DSL.

Instead of assembling separate tools, libraries and scripts for each technology, AGATE models them as **first-class test steps within one unified test execution model**.

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

1. Create an entity through a REST API
2. Verify the persisted data using SQL
3. Trigger processing through a SOAP service
4. Wait for asynchronous processing
5. Validate the application state in OpenShift
6. Execute a command-line verification
7. Verify the final result in the GUI

**AGATE executes all of these steps as one test case and produces one consolidated test result.**

This is the core idea behind AGATE:

> **The tester describes the business test. AGATE orchestrates the technologies.**

---

# ✨ Key Features

* ✅ Human-readable, no-code YAML test definitions
* ✅ Cross-technology test orchestration
* ✅ REST, SOAP, SQL, CMD and OpenShift as first-class test steps
* ✅ Reusable business and technical test components
* ✅ Data-driven testing
* ✅ Environment-independent test definitions
* ✅ Shared variables and data between different test engines
* ✅ Detailed execution logging and HTML reports
* ✅ CI/CD friendly execution
* ✅ Extensible engine architecture
* 🚧 GUI automation
* 🚧 OpenAPI-driven test generation and contract analysis
* 🚧 AI-assisted test generation and maintenance
* 🔄 Migration path from Tricentis Tosca

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

```text
                    AGATE Test Platform
                            │
          ┌─────────────────┼──────────────────┐
          │                 │                  │
          ▼                 ▼                  ▼
    agate-server         agate-ai       agate-tosca-migrator
          │
          │
          ▼
     Execution Core
          │
    ┌─────┼─────┬─────┬─────┬─────┬─────┐
    ▼     ▼     ▼     ▼     ▼     ▼     ▼
   REST  SOAP   SQL   CMD    OC   WAIT   GUI
          │
          ▼
      Test Report


    agate-client
    (under development)
          │
          ▼
   Web Management UI
```

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
| ☸️ OpenShift | OpenShift CLI operations and validation    | **Native**            |
| ⏳ WAIT       | Synchronization and asynchronous workflows | **Native**            |
| 📑 JSON      | JSON processing and validation             | **Native**            |
| 📄 PDF       | PDF validation                             | **Native**            |
| 📦 BUFFER    | Shared runtime data / value handling       | **Native**            |
| 🌐 GUI       | Browser-based UI automation                | **Under Development** |

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

* positive and negative test cases
* boundary-value testing
* different users and roles
* different environments
* regression suites

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

* executed test cases
* individual test steps
* execution status
* assertion results
* errors
* execution timing

The goal is to provide one consolidated view even when a test crosses several technical layers.

---

# 🔄 AGATE Tosca Migrator

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

# 🧠 AGATE OpenAPI & AI

### Under Development

AGATE is being extended beyond test execution toward **contract-aware test automation**.

The OpenAPI component is designed to analyze API specifications deterministically and build an internal AGATE representation of the API contract.

The planned processing chain is:

```text
OpenAPI Specification
        │
        ▼
Deterministic Parser
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
Test Generation / Maintenance
        │
        ▼
Executable AGATE Tests
```

Planned and experimental capabilities include:

* OpenAPI YAML/JSON parsing
* `$ref` resolution
* Endpoint and parameter analysis
* Request and response schema analysis
* API contract change detection
* Breaking-change detection
* Test impact analysis
* OpenAPI-based test generation
* AI-assisted test generation
* AI-assisted test maintenance
* Local LLM support
* Ollama integration

A central design principle is to keep deterministic contract analysis separate from optional AI-assisted functionality.

This makes it possible to use AI where it provides value without making the test execution itself dependent on an LLM.

---

# 🖥️ AGATE Client

### Under Development

`agate-client` is intended to provide a web-based interface for managing:

* projects
* test suites
* test executions
* test data
* environments
* reports
* OpenAPI analysis
* AI-assisted workflows

The AGATE Server remains the execution core, allowing tests to run independently from the web interface and making command-line and CI/CD execution possible.

---

# 🆚 How Is AGATE Different?

Several excellent open-source testing frameworks already exist.

AGATE does not attempt to replace every specialized testing tool.

Instead, its focus is **cross-technology enterprise test orchestration**.

| Capability                | AGATE                  | Karate                | Robot Framework             | Step CI      | Tavern       | Schemathesis          |
| ------------------------- | ---------------------- | --------------------- | --------------------------- | ------------ | ------------ | --------------------- |
| REST                      | 🟢 **Native**          | 🟢 Native             | 🟡 Library                  | 🟢 Native    | 🟢 Native    | 🟢 Native             |
| SOAP                      | 🟢 **Native**          | 🟢 Native             | 🟡 Library                  | 🟢 Native    | 🟡 Extension | 🔴                    |
| SQL / Database            | 🟢 **Native**          | 🟡 Integration        | 🟡 Library                  | 🔴           | 🟡 Python    | 🔴                    |
| CMD / Shell               | 🟢 **Native**          | 🟡 Integration        | 🟢 Process Library          | 🔴           | 🟡 Python    | 🔴                    |
| OpenShift                 | 🟢 **Native**          | 🟡 Custom Integration | 🟡 Library / CLI            | 🔴           | 🔴           | 🔴                    |
| GUI / Browser             | 🚧                     | 🟢 Native             | 🟡 Browser/Selenium Library | 🔴           | 🔴           | 🔴                    |
| Cross-technology scenario | 🟢 **Core concept**    | 🟢 Possible           | 🟢 Possible                 | 🟡 API focus | 🟡 API focus | 🔴 API focus          |
| Reusable business steps   | 🟢 **Native**          | 🟢                    | 🟢 Keywords                 | 🟢           | 🟡           | 🔴 Different approach |
| Data-driven testing       | 🟢 **Native**          | 🟢                    | 🟢                          | 🟢           | 🟢           | 🟢 Generated data     |
| OpenAPI-driven generation | 🚧                     | 🟡 Ecosystem          | 🟡 Extensions               | 🟡           | 🔴           | 🟢 **Core strength**  |
| Contract change analysis  | 🚧 **AGATE OpenAPI**   | 🔴 Not core           | 🔴 Not core                 | 🔴           | 🔴           | 🟡 Contract focus     |
| Change → impacted tests   | 🚧 **AGATE direction** | 🔴                    | 🔴                          | 🔴           | 🔴           | 🟡 Different approach |
| Tosca migration           | 🟢 **AGATE Migrator**  | 🔴                    | 🔴                          | 🔴           | 🔴           | 🔴                    |
| Local LLM integration     | 🚧 **Ollama**          | 🔴 Not core           | 🟡 Extensible               | 🔴           | 🔴           | 🔴                    |

**Legend**

* 🟢 Native / first-class capability or standard component
* 🟡 Available through libraries, extensions or custom integration
* 🔴 Not a primary capability
* 🚧 Under development

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

AGATE aims to become a lightweight, open and vendor-independent platform for enterprise test automation.

The long-term direction connects:

```text
              BUSINESS REQUIREMENTS
                       │
                       ▼
                 API CONTRACT
                       │
                       ▼
               BUSINESS CONTRACT
                       │
                       ▼
                 TEST CONTRACT
                       │
                       ▼
                AGATE TESTS
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
        APIs        Databases   Infrastructure
          │            │            │
          └────────────┼────────────┘
                       ▼
                  UI / Systems
                       │
                       ▼
                Unified Report
```

The objective is not simply to execute YAML files.

The objective is to connect **business intent, technical contracts and executable tests** while keeping the resulting automation transparent, version-controlled and vendor-independent.

---

# 🤝 Feedback and Contributions

AGATE is a young project and feedback from real automation engineers and testers is especially valuable.

If you are interested in:

* cross-technology test automation
* enterprise integration testing
* OpenAPI-driven testing
* contract-aware test automation
* Tosca migration
* vendor-independent test automation

try AGATE and let us know what works — and what does not.

Issues, discussions and pull requests are welcome.

If you find the project useful, consider giving the repository a ⭐. It helps other testers and automation engineers discover AGATE.

---

# 📄 License

AGATE Test Platform is released under the MIT License.
