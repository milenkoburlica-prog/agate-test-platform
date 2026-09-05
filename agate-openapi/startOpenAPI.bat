@echo off
setlocal

rem ============================================================
rem AGATE OpenAPI
rem ============================================================

set "JAR=target\agate-openapi-2.0.0-SNAPSHOT-jar-with-dependencies.jar"


rem ------------------------------------------------------------
rem Check JAR
rem ------------------------------------------------------------

if not exist "%JAR%" (
    echo.
    echo ============================================================
    echo ERROR: AGATE OpenAPI JAR not found
    echo ============================================================
    echo.
    echo Expected:
    echo   %JAR%
    echo.
    echo Build the project first:
    echo   mvn clean package
    echo.
    exit /b 1
)


rem ------------------------------------------------------------
rem Help
rem ------------------------------------------------------------

if "%~1"=="" goto HELP

if /I "%~1"=="help" goto HELP
if /I "%~1"=="--help" goto HELP
if /I "%~1"=="-h" goto HELP


rem ------------------------------------------------------------
rem Execute AGATE OpenAPI
rem ------------------------------------------------------------

java -jar -Djava.util.logging.manager=org.jboss.logmanager.LogManager "%JAR%" %*

exit /b %ERRORLEVEL%


rem ============================================================
rem HELP
rem ============================================================

:HELP

echo.
echo ============================================================
echo AGATE OpenAPI
echo ============================================================
echo.
echo Usage:
echo.
echo   startOpenAPI.bat COMMAND [arguments]
echo.
echo.
echo ------------------------------------------------------------
echo 1. OPENAPI CONTRACT MODEL
echo ------------------------------------------------------------
echo.
echo   model ^<OPENAPI_SOURCE^>
echo.
echo       Parse an OpenAPI specification and display
echo       the AGATE OpenAPI contract model.
echo.
echo   model-json ^<OPENAPI_SOURCE^>
echo.
echo       Parse an OpenAPI specification and output
echo       the AGATE contract model as JSON.
echo.
echo.
echo Examples:
echo.
echo   startOpenAPI.bat model ^
echo     src/test/resources/test-openapi-v1.yaml
echo   startOpenAPI.bat model-json ^
echo     src/test/resources/test-openapi-v1.yaml
echo   startOpenAPI.bat model ^
echo     https://petstore3.swagger.io/api/v3/openapi.yaml
echo.
echo.
echo ------------------------------------------------------------
echo 2. DETERMINISTIC TEST GENERATION
echo ------------------------------------------------------------
echo.
echo   phase1 ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Build the deterministic operation model.
echo.
echo   phase2 ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Generate deterministic technical test cases.
echo.
echo   list ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       List generated test cases.
echo.
echo   test ^<TEST_ID^> ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Display one generated test case.
echo.
echo   dsl ^<TEST_ID^> ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Generate AGATE DSL for one test case.
echo.
echo   csv ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Generate CSV test data.
echo.
echo   yaml ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo.
echo       Generate AGATE YAML.
echo.
echo   generate ^<APP_ID^> ^<OPENAPI_SOURCE^>
echo.
echo       Generate AGATE artifacts for the complete
echo       OpenAPI specification.
echo.
echo.
echo Examples:
echo.
echo   startOpenAPI.bat phase1 ^
echo     src/test/resources/test-openapi-v1.yaml ^
echo     GET "/users/{id}"
echo.
echo   startOpenAPI.bat phase2 ^
echo     src/test/resources/test-openapi-v13-phase2-final.yaml ^
echo     POST /validation-demo
echo.
echo   startOpenAPI.bat list ^
echo     src/test/resources/test-openapi-v1.yaml ^
echo     GET "/users/{id}"
echo.
echo   startOpenAPI.bat csv ^
echo     src/test/resources/test-openapi-v2.yaml ^
echo     POST /users
echo.
echo   startOpenAPI.bat generate ^
echo     petstore ^
echo     https://petstore3.swagger.io/api/v3/openapi.json
echo.
echo.
echo ------------------------------------------------------------
echo 3. CONTRACT CHANGE / IMPACT ANALYSIS
echo ------------------------------------------------------------
echo.
echo   changes ^<OLD_OPENAPI^> ^<NEW_OPENAPI^>
echo.
echo       Compare two OpenAPI contracts.
echo.
echo   impact ^<OLD_OPENAPI^> ^<NEW_OPENAPI^> ^<APP_DIRECTORY^>
echo.
echo       Analyze contract changes and their impact
echo       on existing AGATE test artifacts.
echo.
echo.
echo Example:
echo.
echo   startOpenAPI.bat impact ^
echo     src/test/resources/change/test-constraints-v1.yaml ^
echo     src/test/resources/change/test-constraints-v2.yaml ^
echo     data/demo
echo.
echo.
echo ------------------------------------------------------------
echo GENERAL
echo ------------------------------------------------------------
echo.
echo   help
echo   --help
echo   -h
echo.
echo ============================================================

exit /b 0