# AGATE Test Platform

> **YAML-based, AI-powered no-code test automation platform** for testers and automation engineers.

---

### Overview

**AGATE Test Platform** enables the creation and execution of automated tests without writing programming code.

It provides a clean, human-readable **YAML-based Domain Specific Language (DSL)** for building maintainable test scenarios across enterprise environments.

### Key Features

- ✅ No-code YAML test definitions
- ✅ Reusable test components
- ✅ Environment configuration
- ✅ Data-driven testing
- ✅ Detailed execution reports
- ✅ CI/CD ready
- ✅ Extensible engine architecture
- ✅ AI-assisted test generation

---

# 🏗️ Project Architecture

```text
AGATE Test Studio
│
├── agate-server
│   └── Central Backend
│
├── agate-ai
│   └── AI engine (LLMs, Ollama, Chatbot for Client, test generation based on OpenAPI specification)
│
├── agate-tosca-migrator
│   └── Migration of existing Tosca tests into AGATE DSL
│
└── agate-client (under development)
    └── Web interface for project management, execution and AI workflows
````

---

# 📦 Modules

## 1️⃣ AGATE Server

The core backend responsible for executing AGATE test suites.

### Features

* YAML-based test DSL
* Human-readable test scenarios
* Reusable test components
* Data-driven testing
* Environment configuration
* Detailed execution logging
* Comprehensive reports with log-info
* CI/CD integration

### Supported Engines

* 🌐 REST APIs
* 🏢 SOAP APIs
* 🗄️ SQL Databases
* 🖥️ Command Line (CMD)
* ☸️ OpenShift
* 📄 PDF Validation
* 📑 JSON Validation
* 🏢 Buffer
* 🏢 Wait


---

## 2️⃣ AGATE Tosca Migrator *(Available on Request)*

Migration solution for converting existing **Tricentis Tosca** test cases into the native **AGATE DSL**.

This component is offered as a **customized migration service** and allows organizations to preserve their existing test assets while transitioning to AGATE.

---

## 3️⃣ AGATE AI *(Under Development)*

AI-powered generation of AGATE test scenarios from **OpenAPI specifications**.

Planned capabilities include:

* Automatic YAML test generation based on OpenAPI specification
* Local LLM support
* Local Ollama integration

---

## 4️⃣ AGATE Client *(Under Development)*

A web-based interface for managing:

* Projects
* Test Suites
* Test Executions
* Reports
* AI-assisted workflows

---

# 🎯 Vision

AGATE aims to become a lightweight, vendor-independent alternative for enterprise test automation by combining:

* YAML simplicity
* Modular architecture
* AI-assisted automation
* Open integration capabilities

```
```
