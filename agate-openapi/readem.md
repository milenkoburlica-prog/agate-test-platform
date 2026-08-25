# AGATE OpenAPI

AGATE OpenAPI analyzes an OpenAPI specification deterministically and derives
technical API test cases from the API contract.

The OpenAPI specification describes the technical API contract, but it does not
contain the complete business contract or application state model.

AGATE therefore generates a deterministic test skeleton that can be completed by
the tester with business-specific knowledge such as:

- required test preconditions
- database setup
- reusable preparation steps
- business state
- additional assertions
- post-checks

The generated AGATE artifacts consist mainly of:

```text
data/<appId>/
|
+-- template/
|   +-- <testSuite>.csv
|   +-- <testSuite>.yaml
|
+-- modules/
    +-- rest/
        +-- <moduleName>/
            +-- metdata.json
            +-- request.json
```
The project currently provides four command line tools.

1. startOpenApi.bat

All OpenAPI CLI tools should normally be started through:

startOpenApi.bat

General syntax:

startOpenApi.bat <command> <arguments>

Available commands:

openapi
phase1
phase3
impact

Help:

startOpenApi.bat help
2. OpenAPI CLI

CLI:

AgateOpenApiCli

Start through:

startOpenApi.bat openapi <arguments>

This CLI is used for the basic deterministic OpenAPI parsing and inspection.

Example:

startOpenApi.bat openapi src/test/resources/test-openapi-v2.yaml

Depending on the supported options of the current CLI implementation, additional
operation filters can be supplied.

Example:

startOpenApi.bat openapi src/test/resources/test-openapi-v2.yaml POST /users

The purpose of this CLI is primarily technical inspection of the deterministic
AGATE OpenAPI model.

3. Phase 1 CLI

CLI:

AgatePhase1Cli

Start through:

startOpenApi.bat phase1 <arguments>

Phase 1 transforms the parsed OpenAPI model into the normalized AGATE operation
model.

Example:

startOpenApi.bat phase1 src/test/resources/test-openapi-v2.yaml POST /users

The output can be used to verify the normalized API operation including request
parameters, request body and expected responses.

4. Phase 3 CLI

CLI:

AgatePhase3Cli

Start through:

startOpenApi.bat phase3 <arguments>

Phase 3 creates executable AGATE artifacts.

List generated test cases

Example:

startOpenApi.bat phase3 --list src/test/resources/test-openapi-v2.yaml POST /users

Example result:

TC001_Baseline_valid_request
TC002_Full_request
TC003_Missing_required_body_property_role
...
Show one generated test

Example:

startOpenApi.bat phase3 --test src/test/resources/test-openapi-v2.yaml POST /users TC003_Missing_required_body_property_role
Generate CSV

Example:

startOpenApi.bat phase3 --csv src/test/resources/test-openapi-v2.yaml POST /users

The CSV contains one column per testcase and one row per request value.

Example:

testcaseName;TC001_Baseline_valid_request;TC002_Full_request
testcaseDescription;Baseline valid request;Full request
apiEndpoint;/users;/users
statusCode;201;201
username;A1A1;A1A1
Generate a complete AGATE application

Example:

startOpenApi.bat phase3 --generate demo src/test/resources/change/test-constraints-v1.yaml

This creates:

data/demo/
|
+-- template/
|   +-- POST_users.csv
|   +-- POST_users.yaml
|   +-- GET_users_id.csv
|   +-- GET_users_id.yaml
|
+-- modules/
    +-- rest/
        +-- POST_users/
        |   +-- metdata.json
        |   +-- request.json
        |
        +-- GET_users_id/
            +-- metdata.json
            +-- request.json

For a complete public OpenAPI specification, for example PetStore:

startOpenApi.bat phase3 --generate petstore https://petstore3.swagger.io/api/v3/openapi.json

All operations from the specification are processed.

5. OpenAPI Change and Impact Analysis

CLI:

AgateOpenApiImpactCli

Start through:

startOpenApi.bat impact --impact <old-openapi> <new-openapi> <application-directory>

Example:

startOpenApi.bat impact --impact src/test/resources/change/test-constraints-v1.yaml src/test/resources/change/test-constraints-v2.yaml data/demo

The impact analysis compares:

old OpenAPI contract
        +
new OpenAPI contract
        +
current AGATE artifacts

and determines which existing AGATE tests or artifacts are affected.

Typical actions are:

CHANGE_CSV_VALUE
REGENERATE_BOUNDARY_VALUE
REVIEW_EXPECTED_OUTCOME
ADD_TEST_CASE
ADD_CSV_ROW
REMOVE_CSV_ROW
ADD_YAML_FIELD
REMOVE_YAML_FIELD
ADD_REQUEST_JSON_FIELD
REMOVE_REQUEST_JSON_FIELD
CHANGE_REQUEST_JSON_FIELD

Example:

IMPACT 001

  change    : request.body.username.minLength
  artifact  : CSV
  testcase  : TC002_Full_request
  current   : AAAA
  expected  : 5
  action    : CHANGE_CSV_VALUE
  reason    : Length 4 is below minLength 5

A changed boundary testcase can be reported as:

testcase  : TC003_username_minimum_string_length
current   : AAA
expected  : 5
action    : REGENERATE_BOUNDARY_VALUE

A newly introduced contract value can require a completely new testcase:

change    : request.query.language.coverage.enum_value
testcase  : language_valid_enum_value_it
expected  : it
action    : ADD_TEST_CASE
reason    : New enum value it is not covered
6. Impact Analysis Workflow

A typical migration from API V1 to API V2 is:

Step 1 - Generate or use the current V1 AGATE artifacts
startOpenApi.bat phase3 --generate demo src/test/resources/change/test-constraints-v1.yaml
Step 2 - Run impact analysis
startOpenApi.bat impact --impact src/test/resources/change/test-constraints-v1.yaml src/test/resources/change/test-constraints-v2.yaml data/demo

Example:

OPEN IMPACTS : 32
Step 3 - Modify the existing AGATE artifacts

The tester updates the existing:

CSV
YAML template
request.json

according to the reported impacts.

Business-specific preconditions and post-checks remain under tester control.

Step 4 - Run the same analysis again
startOpenApi.bat impact --impact src/test/resources/change/test-constraints-v1.yaml src/test/resources/change/test-constraints-v2.yaml data/demo

The number of unresolved impacts should decrease:

32
-> 20
-> 8
-> 1
-> 0

The target state is:

OPEN IMPACTS : 0

STATUS : ALIGNED

This means that AGATE no longer detects a deterministic technical mismatch
between the current test artifacts and the new OpenAPI contract.

It does not mean that the complete business behavior has been validated.
Business preconditions, business state and business-specific post-checks remain
the responsibility of the tester.

7. Impact Summary

The impact CLI also provides an aggregated summary.

Example:

============================================================
IMPACT SUMMARY
============================================================

OPEN IMPACTS : 32

BY ACTION
------------------------------------------------------------
CHANGE_CSV_VALUE              : 14
REGENERATE_BOUNDARY_VALUE     : 16
REVIEW_EXPECTED_OUTCOME       : 1
ADD_TEST_CASE                 : 1

BY OPERATION
------------------------------------------------------------
POST:/users                   : 16
GET:/users/{id}               : 16

BY ARTIFACT
------------------------------------------------------------
CSV                           : 32
YAML                          : 0
REQUEST_JSON                  : 0
METDATA_JSON                  : 0

============================================================
STATUS : CHANGES REQUIRED
============================================================

This allows the tester to see immediately:

how much migration work remains
which API operations are affected
which artifact types must be changed
whether existing tests must be changed or new tests must be added
8. Exit Codes

The Impact CLI uses exit codes that can later also be used in CI/CD pipelines.

0   no open impacts
1   invalid command line arguments
2   execution error
10  unresolved impacts found

Example:

startOpenApi.bat impact --impact old.yaml new.yaml data/demo

echo %ERRORLEVEL%

An exit code of:

10

means that the comparison itself succeeded, but AGATE found unresolved impacts.

9. Design Principle

AGATE OpenAPI is intentionally deterministic.

The OpenAPI specification is treated as the technical API contract.

AGATE derives deterministic technical test cases and migration impacts from this
contract.

AGATE does not invent business behavior that is not described by the API
contract.

The tester remains the owner of business knowledge and can extend the generated
skeleton with:

business preconditions
database state
external system preparation
reusable AGATE steps
business assertions
post-checks

---

# `startOpenApi.bat`

Ja bih ga stavio direktno u root:

```text
agate-openapi/
|
+-- pom.xml
+-- README.md
+-- startOpenApi.bat
+-- src/
+-- data/

Kompletan BAT:

@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ============================================================
rem AGATE OpenAPI CLI Launcher
rem ============================================================

set "SCRIPT_DIR=%~dp0"

pushd "%SCRIPT_DIR%" >nul


rem ============================================================
rem Main classes
rem ============================================================

set "CLI_OPENAPI=at.co.svc.agate.openapi.cli.AgateOpenApiCli"
set "CLI_PHASE1=at.co.svc.agate.openapi.phase1.cli.AgatePhase1Cli"
set "CLI_PHASE3=at.co.svc.agate.openapi.phase3.cli.AgatePhase3Cli"
set "CLI_IMPACT=at.co.svc.agate.openapi.impact.cli.AgateOpenApiImpactCli"


rem ============================================================
rem Argument check
rem ============================================================

if "%~1"=="" (
    call :usage
    set "RESULT=1"
    goto :end
)


set "COMMAND=%~1"
shift


rem ============================================================
rem Help
rem ============================================================

if /I "%COMMAND%"=="help" (
    call :usage
    set "RESULT=0"
    goto :end
)


if /I "%COMMAND%"=="--help" (
    call :usage
    set "RESULT=0"
    goto :end
)


if /I "%COMMAND%"=="-h" (
    call :usage
    set "RESULT=0"
    goto :end
)


rem ============================================================
rem Resolve CLI
rem ============================================================

if /I "%COMMAND%"=="openapi" (

    set "MAIN_CLASS=%CLI_OPENAPI%"

    goto :execute
)


if /I "%COMMAND%"=="phase1" (

    set "MAIN_CLASS=%CLI_PHASE1%"

    goto :execute
)


if /I "%COMMAND%"=="phase3" (

    set "MAIN_CLASS=%CLI_PHASE3%"

    goto :execute
)


if /I "%COMMAND%"=="impact" (

    set "MAIN_CLASS=%CLI_IMPACT%"

    goto :execute
)


echo.
echo ERROR: Unknown command "%COMMAND%"
echo.

call :usage

set "RESULT=1"

goto :end


rem ============================================================
rem Execute selected CLI
rem ============================================================

:execute

set "CLI_ARGS="


:collectArgs

if "%~1"=="" goto :run


if defined CLI_ARGS (

    set "CLI_ARGS=!CLI_ARGS! %1"

) else (

    set "CLI_ARGS=%1"
)


shift

goto :collectArgs


:run

echo.
echo ============================================================
echo AGATE OPENAPI
echo ============================================================
echo.
echo command    : %COMMAND%
echo main class : %MAIN_CLASS%
echo arguments  : %CLI_ARGS%
echo.


call mvn -q ^
    -DskipTests ^
    -Dexec.mainClass="%MAIN_CLASS%" ^
    -Dexec.args="%CLI_ARGS%" ^
    org.codehaus.mojo:exec-maven-plugin:3.5.0:java


set "RESULT=%ERRORLEVEL%"


echo.

if "%RESULT%"=="0" (

    echo AGATE OpenAPI finished successfully.

) else if "%RESULT%"=="10" (

    echo AGATE OpenAPI finished with unresolved impacts.

) else (

    echo AGATE OpenAPI finished with exit code %RESULT%.
)


goto :end


rem ============================================================
rem Usage
rem ============================================================

:usage

echo.
echo ============================================================
echo AGATE OPENAPI
echo ============================================================
echo.
echo Usage:
echo.
echo   startOpenApi.bat ^<command^> [arguments]
echo.
echo Commands:
echo.
echo   openapi
echo       Basic OpenAPI parsing / inspection
echo.
echo   phase1
echo       Normalized Phase 1 operation model
echo.
echo   phase3
echo       Test generation / CSV / YAML / application generation
echo.
echo   impact
echo       OpenAPI V1 -^> V2 impact analysis
echo.
echo Examples:
echo.
echo   startOpenApi.bat openapi ^
src/test/resources/test-openapi-v2.yaml
echo.
echo   startOpenApi.bat phase1 ^
src/test/resources/test-openapi-v2.yaml POST /users
echo.
echo   startOpenApi.bat phase3 ^
--list src/test/resources/test-openapi-v2.yaml POST /users
echo.
echo   startOpenApi.bat phase3 ^
--csv src/test/resources/test-openapi-v2.yaml POST /users
echo.
echo   startOpenApi.bat phase3 ^
--generate demo src/test/resources/change/test-constraints-v1.yaml
echo.
echo   startOpenApi.bat phase3 ^
--generate petstore https://petstore3.swagger.io/api/v3/openapi.json
echo.
echo   startOpenApi.bat impact ^
--impact src/test/resources/change/test-constraints-v1.yaml ^
src/test/resources/change/test-constraints-v2.yaml ^
data/demo
echo.
echo ============================================================

exit /b 0


rem ============================================================
rem End
rem ============================================================

:end

popd >nul

endlocal & exit /b %RESULT%         
            