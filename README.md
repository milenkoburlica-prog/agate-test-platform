# AGATE Test Platform

> **One test. Multiple technologies. One execution flow.**

**AGATE** is an open-source test automation and orchestration platform for complex integration and system tests across multiple technologies and existing environments.

A single AGATE test can combine:

**REST · SOAP · SQL · CMD · FILE · OpenShift · GUI · CALL · JSON · PDF · BUFFER**

within one human-readable YAML workflow.

### Why AGATE?

AGATE keeps the tester focused on **business behavior, test design, and software quality** instead of framework code, helper classes, mocks, and technical plumbing.

> **More focus on testing. Less focus on programming the test framework.**

### Why not just JUnit?

JUnit is excellent for unit and component testing, especially when the test environment can be created and controlled directly by the test.

AGATE addresses a different problem: **integration and system testing across existing environments** where databases, backend systems or production-like test data may already exist and cannot simply be recreated inside a local container.

### GUI automation

The **AGATE GUI Engine** uses Playwright for Java to automate browser interactions as part of the same YAML test flow as REST, SQL, and other technologies. It supports **Chromium, Microsoft Edge, and Google Chrome**, with configurable headless execution and browser reuse. Test cases use isolated browser contexts, whether executed individually or within a suite.

For testers who prefer a visual approach, **`agate-gui-studio`** records browser interactions and turns them into editable AGATE YAML tests. The Studio is a standalone test-design tool; the Java GUI Engine performs execution in `agate-server`.

### Migration from Tricentis Tosca

Existing **Tricentis Tosca** test assets can be transformed into native AGATE test definitions through **`agate-tosca-migrator`**. This helps preserve existing regression scenarios while moving towards readable, version-controlled YAML tests and an open execution platform.

Tosca migration is currently provided as a **customized migration approach**, depending on the test structures involved.

### AI-assisted testing

AGATE is largely based on human-readable text artifacts such as **YAML, CSV, JSON, configuration files, and textual execution results**.

This makes it well suited for AI-assisted workflows across the test lifecycle.

> **Text-based test artifacts are easy to understand for both humans and AI-assisted tooling.**

For AI-assisted test creation, the repository includes **`agate-copilot-notebook`**, a collection of Markdown documentation covering AGATE concepts, DSL syntax, engine-specific references, examples, and test-generation guidance. It enables experimentation with Microsoft Copilot Notebook without requiring AI for test execution.

For failure analysis, **`agate-ai-troubleshooter-agent`** provides an AI-powered troubleshooting assistant. It combines deterministic log analysis with LLMs to investigate failed test executions, identify possible root causes, and suggest verifiable corrections across AGATE's test engines.

Both tools are optional and complement AGATE's core test execution capabilities.

### The goal

> **Readable tests. Reusable execution engines. Existing environments. AI-assisted testing. Minimal framework code. No vendor lock-in.**

---

## 🚀 Quick Start

Clone the repository and execute an existing demo test:

```cmd
git clone https://github.com/milenkoburlica-prog/agate-test-platform.git
cd agate-test-platform\agate-server

startTests.bat Tester1 DEMOS DEMO rest_engine_demo.yaml
```

Adjust the user, environment, and demo application arguments to match your configuration. Some examples require running services, databases, or additional test data.

---

## 🧪 What an AGATE Test Looks Like

AGATE tests are written in YAML. Here is a deliberately simple example: **query a database and verify that it contains at least one customer**.

```yaml
testCases:
  - id: TC_Check_Customers
    description: Verify that customers exist
    stage: '*'
    priority: HIGH

    steps:
      - type: SQL
        op: EXEC
        command: "SELECT COUNT(*) AS TOTAL FROM CUSTOMERS"
        response: customers

      - type: SQL
        op: ASSERT
        response: customers
        row: 0
        column: TOTAL
        action: GREATER_THAN
        expected: 0
```

The first step executes a SQL query; the second verifies its result. **No Java test class or database connection code is required in the YAML.**

Database credentials and connection settings are maintained separately in AGATE environment configuration (for example, `agate-server/env/env.conf`) using `database.connectionString`, `database.user`, and `database.password`. Named datasources can be configured when needed. The `CUSTOMERS` table is illustrative; use a table available in your own test database.

More sophisticated tests can combine SQL with REST, SOAP, file checks, browser actions, variables, and reusable `CALL` steps in one execution flow.

**Explore real examples:**

- **Test suites and reusable modules:** [`agate-server/data/demo/`](agate-server/data/demo/). For GUI Test see "agate-gui-demo-server"
- **DSL and engine documentation:** [`agate-documentation/`](agate-documentation/)

Each AGATE component also has its own README for installation, configuration, and detailed usage.

---

## 🏗️ Project Architecture

Different tools can create or migrate AGATE test artifacts. **`agate-server` executes them using the same native DSL**, regardless of how they were created.

```text
                            AGATE TEST PLATFORM

        Manual YAML         agate-openapi       agate-tosca-migrator
             |                    |                     |
             +--------------------+---------------------+
                                  |
       agate-gui-studio -----------+----------- Copilot Notebook
                                  |
                                  v
                        Native AGATE Artifacts
                       YAML · Modules · CSV · JSON
                                  |
                                  v
                             agate-server
                                  |
         +------------------------+------------------------+
         |                        |                        |
     REST · SOAP · SQL       GUI · CMD · FILE       OpenShift · JSON
         |                    PDF · BUFFER           WAIT · LOOP · CALL
         +------------------------+------------------------+
                                  |
                                  v
                          Unified Test Reports

     agate-gui-demo-server: local learning pages for GUI tests
```

`agate-gui-studio` and Copilot Notebook are **alternative ways to help create tests**; they are not runtime dependencies. `agate-gui-demo-server` supplies example web pages for GUI learning and regression scenarios. The Copilot Notebook is a knowledge source for guided authoring, not an automatic test-execution component.

### Platform components

| Component | Purpose |
| --- | --- |
| **`agate-server`** | Executes YAML tests across the native engines; manages reusable steps, configuration, results, and reports. |
| **`agate-openapi`** | Generates test artifacts from OpenAPI contracts and analyzes API changes and their impact on tests. |
| **`agate-tosca-migrator`** | Migrates supported Tosca scenarios into native AGATE artifacts; customized migration approach. |
| **`agate-copilot-notebook`** | Markdown knowledge base for AI-assisted test creation with Microsoft Copilot Notebook. |
| **`agate-ai-troubleshooter-agent`** | AI-powered analysis of failed test executions, root cause investigation, and verifiable fix proposals. |

The guiding principle is:

> **Deterministic where possible. AI where useful.**

---

## ✨ Core Capabilities

| Area | What AGATE provides |
| --- | --- |
| **Test design** | Readable YAML DSL, variables, CSV test data, reusable `CALL` flows, and suite validation. |
| **Integration testing** | Native REST, SOAP, SQL, CMD, and OpenShift operations in a single scenario. |
| **Browser testing** | Playwright-based GUI execution with Chromium, Edge, and Chrome; isolated browser contexts and optional browser reuse. |
| **Files and data** | FILE, JSON, PDF, and BUFFER operations with assertions and value sharing. |
| **Execution control** | WAIT, LOOP, and reusable test steps. |
| **Results** | Structured execution logging and HTML reports. |
| **Test generation** | OpenAPI-driven generation, Tosca migration, GUI recording, and optional AI-assisted authoring. |

---

## 🤝 Feedback and Contributions

AGATE is a growing open-source project. Feedback from testers, automation engineers, and teams working with complex system environments is welcome. Issues and pull requests are appreciated.

If you find AGATE useful, consider giving the repository a ⭐.

## 📄 License

AGATE Test Platform is released under the **MIT License**.
