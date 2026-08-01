# Agate Test Studio



\## What is Agate Test Studio?



\*\*Do you want to automate tests quickly and efficiently?\*\*



Agate Test Studio is a lightweight test automation framework that requires \*\*no programming skills\*\* and \*\*no expensive software licenses\*\*.



It allows you to validate complex system processes including:



\- REST APIs

\- SOAP services

\- Databases

\- OpenShift environments

\- Command-line based validations



without complex installations or proprietary testing tools.



Agate brings structure, simplicity, and transparency into your test automation landscape.



\---



\# System Requirements



To run Agate Test Studio successfully, the following tools must be available on your machine.



\## Java Runtime Environment (JRE/JDK) 17+



Java is the foundation for running the Agate backend.



Verify your installation:



```cmd

java -version

```



Recommended:



```

OpenJDK 17+

```



\---



\## OpenShift Client (oc.exe)



Required only if your tests interact with OpenShift environments.



The OpenShift client must:



\- be installed

\- be available in the system PATH



Verify:



```cmd

oc version

```



\---



\## SQLPlus



Required only for database test scenarios.



SQLPlus is used by the SQL Engine to execute database validations.



\---



\# Quick Start Guide



\## Running Your First Test



Agate tests are executed directly from the Windows command line using:



```

startTests.bat

```



The general syntax is:



```cmd

startTests.bat \[USER] \[INSTANCE] \[APP] \[TEST\_SUITE] \[TEST\_CASE] \[PRIORITY]

```



\---



\# Examples



\## Execute all tests of an application



```cmd

startTests.bat DEMOS DEMOS demo

```



Runs all available test suites inside the application:



```

data/demo

```



\---



\## Execute a specific test suite



```cmd

startTests.bat Tester1 DEMOS demo cmd\_engine\_demo

```



Runs only:



```

cmd\_engine\_demo.yaml

```



\---



\## Execute a single test case



```cmd

startTests.bat Tester1 DEMOS demo cmd\_engine\_demo "TC Hello World 1"

```



Useful when developing or debugging a single test.



\---



\## Execute only HIGH priority tests



```cmd

startTests.bat Milenko DEMOS demo cmd\_engine\_demo "" HIGH

```



The empty string means:



```

TEST\_CASE = all

PRIORITY = HIGH

```



\---



\## Important



Always execute the script from the correct Agate project directory.



The runtime requires:



```

target/

env/

data/

reports/

```



to be available.



\---



\# Test Reports



During execution, the console shows the live execution status.



For detailed analysis, use the generated HTML report.



After every execution Agate creates reports under:



```

reports/

```



Example:



```

reports/

&#x20;└── ECS\_SYST\_AUT1/

&#x20;     └── demo/

&#x20;          └── cmd\_engine\_demo/

&#x20;               ├── Latest\_Report.html

&#x20;               └── SuiteReport\_20260621\_151756.html

```



\## Report Types



\### Latest\_Report.html



Always points to the newest execution.



Recommended during development.



\---



\### Timestamped Reports



Historical execution reports.



Useful for:



\- regression testing

\- comparisons

\- documentation



\---



\# Environment Configuration



Agate separates technical environment configuration from user-specific test data.



The configuration is located in:



```

env/

```



Contains:



```

env.conf

users.conf

```



\---



\# env.conf



The file:



```

env/env.conf

```



defines the technical environment.



Example:



```properties

DEMOS.server.url=http://localhost:8080

DEMOS.logs.path=/opt/application/logs

```



The general format is:



```

INSTANCE.PARAMETER=VALUE

```



Example:



```

DEMOS.database.url=jdbc:oracle:thin:@localhost

```



Each instance starts with a unique name.



Example:



```

DEMOS

ECS\_SYST\_AUT1

TEST

```



\---



\# users.conf



The file:



```

env/users.conf

```



contains user-specific variables.



Example:



```properties

DEMOS.Tester1.seriennummer=99999090

```



Format:



```

INSTANCE.USER.VARIABLE=VALUE

```



Example:



```properties

ECS\_SYST\_AUT1.Milenko.cardNumber=12345678

```



User variables can be used inside YAML test cases.



\---



\# Application Setup



\## Creating a New Application



In Agate, an application under test is simply a folder below:



```

data/

```



Example:



```

data/

&#x20;├── demo/

&#x20;├── foo/

&#x20;└── MyFirstProject/

```



No code changes or registration are required.



Agate automatically detects new applications.



\---



\# Recommended Application Structure



Example:



```

data/MyFirstProject/



├── hello\_world.yaml



├── modules/

│    └── reusable API definitions



├── reusable/

│    └── common test components



└── template/

&#x20;    └── CSV/XML test data

```



\---



\# Creating Your First Test



Agate tests are defined using YAML files.



Example:



```

hello\_world\_01.yaml

```



\---



\## Empty Test Suite



```yaml

testCases:

```



This is a valid YAML test suite.



However, no test cases will be executed.



\---



\# First Test Case



Example:



```yaml

testCases:



&#x20; - id: TC Hello World 2

&#x20;   description: Hello World 2

&#x20;   stage: "\*"

&#x20;   priority: HIGH



&#x20;   steps:

```



Execute:



```cmd

startTests.bat Milenko ECS\_SYST\_AUT1 MyFirstProject hello\_world\_02

```



Result:



```

TEST RESULT: TC Hello World 2 \[PASSED]



Total Test Cases : 1

Passed           : 1

Failed           : 0

```



A test case without steps is still valid.



\---



\# Test Steps



Every step consists of:



| Element | Description |

|---|---|

| type | Engine used for execution |

| op | Operation executed by the engine |

| response | Stores execution result |



\---



Example:



```yaml

steps:



&#x20; - type: CMD

&#x20;   op: EXEC

&#x20;   command: "java -version"

&#x20;   response: java\_out



&#x20; - type: CMD

&#x20;   op: ASSERT

&#x20;   action: EXITCODE

&#x20;   expected: 0

```



\---



\# Variables



Variables make tests reusable and maintainable.



Example:



```yaml

testCases:



&#x20; - id: TC Hello World 1



&#x20;   variables:

&#x20;     command: "java -version"



&#x20;   steps:



&#x20;     - type: CMD

&#x20;       op: EXEC

&#x20;       command: "{B\[command]}"

```



Variable syntax:



```

{B\[variable\_name]}

```



Example:



```

{B\[command]}

```



will be replaced with:



```

java -version

```



\---



\# Test Execution Flow



A typical execution:



1\. Load environment configuration

2\. Load application

3\. Load YAML test suite

4\. Filter test cases by stage and priority

5\. Execute test steps

6\. Generate HTML report



\---



\# Stage Filtering



The `stage` property defines where a test should run.



Example:



```yaml

stage: "DEMOS"

```



\---



\## Stage Examples



\### Execute everywhere



```yaml

stage: "\*"

```



Wildcard means:



```

all environments

```



\---



\### Execute only in DEMOS



```yaml

stage: "DEMOS"

```



Run:



```cmd

startTests.bat USER DEMOS app suite

```



\---



\## Stage Rules



| YAML stage | Result |

|-|-|

| `\*` | Always executed |

| missing | Skipped |

| empty string | Skipped |

| DEMOS | Runs only on DEMOS |

| demos | Same as DEMOS |



Stage comparison is case-insensitive.



\---



\# Priority Filtering



Priority controls test execution depth.



Supported levels:



```

LOW

MEDIUM

HIGH

CRITICAL

```



Hierarchy:



```

LOW < MEDIUM < HIGH < CRITICAL

```



Example:



```yaml

priority: HIGH

```



\---



\## Priority Rules



| Test Priority | Requested Priority | Result |

|-|-|-|

| HIGH | HIGH | Execute |

| CRITICAL | HIGH | Execute |

| LOW | HIGH | Skip |

| HIGH | empty | Execute |



\---



\# Debugging Failed Tests



If a test fails:



1\. Open:



```

reports/.../Latest\_Report.html

```



2\. Open the failed test case.



3\. Check:



\- failed engine

\- operation (`op`)

\- response values

\- assertion details



The report shows exactly which step failed.



\---



\# Learning Path



\## Quick Start Guide



You have learned:



\- Agate installation requirements

\- environment setup

\- application structure

\- YAML test creation

\- execution and reporting



\---



\## Advanced User Tutorial



Coming soon:



Detailed explanation of:



\- CMD Engine

\- SQL Engine

\- REST Engine

\- SOAP Engine

\- OpenShift Engine



\---



\## Expert User Tutorial



Advanced topics:



\- DSL templates

\- Data Driven Testing (DDT)

\- CSV based test generation

\- Large scale regression testing



\---



\## Tosca Migration Concept



Migration concept for teams moving from Tosca to Agate.



Topics:



\- Tosca TestSet analysis

\- DSL conversion

\- automated migration approach



\---



\# Future Vision



Agate Test Studio is continuously evolving.



Planned and experimental features:



\## Agate Client \& AI Integration



A client-server architecture with integrated AI assistant.



Features:



\- AI generated test cases

\- intelligent test creation

\- natural language interaction



\---



\## Playwright GUI Engine



Future support for:



\- browser automation

\- website capture

\- GUI testing



\---



\# Project Status



Agate Test Studio is a passion-driven open-source project.



The goal is to provide a modern, lightweight alternative to expensive enterprise test automation platforms.



Feedback, ideas, and contributions are welcome.

