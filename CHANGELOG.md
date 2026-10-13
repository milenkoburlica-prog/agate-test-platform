# Changelog

Development notes moved from the original project README. Entries below retain their original wording and reflect the status at the time they were written (including items marked as planned).

## 2026-10-04

### AGATE Studio added

* AGATE Studio is a visual GUI test designer that records browser interactions, turns them into editable YAML tests, and lets users run and debug them with Playwright.
* Written in JavaScript, with a Node.js/Express backend and an HTML/CSS/JavaScript frontend.
* Currently runs as a standalone application; integration with the Java/Quarkus-based agate-server is planned.

---

## 2026-10-03

### Copilot Notebook Integration

* Added structured AGATE Markdown documentation for use with Microsoft Copilot Notebook.
* Added central knowledge index and engine catalog to improve document retrieval.
* Added dedicated documentation for AGATE variables, condition handling and reusable modules.
* Added engine-specific reference documents for SQL, CMD, OC, FILE, WAIT, SOAP, REST, PDF, JSON and BUFFER.
* Added AI-generation rules to distinguish documented AGATE syntax, user-provided inputs, test expectations, assumptions and real documentation gaps.
* Improved guidance for deterministic test generation and multi-engine test scenarios.
* Added retrieval rules so Copilot verifies all relevant engine documents before reporting missing documentation.

---

## 2026-09-30

### Step-by-Step Debugging

* Planned interactive debug mode for AGATE test execution.
* Planned step-by-step execution of test cases and reusable modules.
* Planned support for pausing before individual steps and continuing execution manually.
* Planned inspection of current variables, buffers, responses and reusable parameters during debugging.
* Example: startTests.bat Tester1 Demos Demo "soap_engine_demo.yaml" --debug

---

## 2026-09-29

### Project-Based Test Organization

* Planned project concept for organizing AGATE test assets.
* Test cases, test data, reusable modules and settings will be organized by project instead of relying on the current global `data` folder structure.
* Planned project-specific configuration and environment handling.
* Planned clearer separation of independent applications and test projects.

---

## 2026-09-27

### Reusable Module Organization

* Planned extension of the reusable module concept beyond simple technical reuse.
* Reusable modules will also be used to structure complex test cases into clear logical sections such as preparation, business execution, validation and cleanup.
* Planned support for application-local reusable module hierarchies using dot notation.
* Example:

  * `reusable.orders.preparation.clean_database`
  * `reusable.orders.business.change_priority`
  * `reusable.orders.validation.verify_result`
* Planned support for shared reusable modules that can be used across multiple applications.
* Shared modules will use a dedicated namespace such as:

  * `shared.ssh_execute`
  * `shared.file_transfer`
  * `shared.database.execute_sql`
* The goal is to keep the main test case focused on the business flow while moving repeatable technical implementation details into reusable modules.

---

## 2026-09-13

### Test Suite Validation

* Added optional pre-execution validation for AGATE test suite YAML files.
* Added validation for YAML structure, supported engine types and engine-specific schemas.
* Added data-flow validation for buffers, responses and runtime variable references.
* Added tester-friendly validation messages with error codes, affected fields and hints.
* Added CLI support for validating test suites without executing them.
* Example:

```bat
startTests.bat validate demo variables_demo.yam
```

---

## 2026-09-11

### Error Handling Improvements

* Improved error reporting across AGATE with tester-friendly messages instead of developer-oriented exceptions.
* Added structured error details such as `Reason`, `Expected`, `Actual`, `Field` and `Hint`.
* Improved error reporting for reusable test steps and startup configuration validation.

---

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

---

## 2026-09-05 

### OpenAPI Contract Evolution 

* Added agate-openapi. 
* Added CLI support for comparing OpenAPI contract versions using `changes`. 
* Added classification of detected changes as `INFO`, `REVIEW` and `BREAKING`. 
* Added OpenAPI test impact analysis for existing AGATE applications. 
* Added mapping of contract changes to CSV, YAML and REST request artifacts. 
* Added detection of affected test cases and incompatible test data.

---

## 2026-09-04

### ASSERT Improvements

* Extended ASSERT support across AGATE test execution.
* Added reference-based comparison of expected and actual responses.
* Added support for ignoring selected fields during structured response comparison.
