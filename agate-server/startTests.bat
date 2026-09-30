@echo off

:: ============================================================
:: AGATE START SCRIPT
:: ============================================================

set "SCRIPT_DIR=%~dp0"
set "AGATE_JAR=%SCRIPT_DIR%target\agate-server-3.0.0-SNAPSHOT-jar-with-dependencies.jar"

:: IMPORTANT:
:: Do NOT change current working directory here.
:: The current directory is used as AGATE project root
:: when --project is not specified.


:: ============================================================
:: MODE SELECTION
:: ============================================================

if /I "%~1"=="instantiate" goto :INSTANTIATE
if /I "%~1"=="validate" goto :VALIDATE
if /I "%~1"=="analyze" goto :ANALYZE
if /I "%~1"=="describe" goto :DESCRIBE


:: ============================================================
:: MODE 1: STANDARD TEST EXECUTION
:: ============================================================

set "USER_NAME=%~1"
set "INSTANCE=%~2"
set "APP_NAME=%~3"
set "TEST_SUITE=%~4"
set "TEST_CASE=%~5"
set "TEST_PRIORITY=%~6"
set "DEBUG_MODE="

for %%A in (%*) do (
    if /I "%%~A"=="--debug" set "DEBUG_MODE=--debug"
)

REM
REM Debug execution:
REM
REM startTests.bat Tester1 DEMOS demo test.yaml --debug
REM
REM The --debug flag enables interactive Step-by-Step Debugging Phase 1.
REM
REM Debug commands:
REM   [Enter] continue
REM   s       skip current step
REM   v       variables
REM   b       buffers
REM   r       responses
REM   q       quit
REM
REM Examples:
REM
REM Normal execution from project directory:
REM
REM startTests.bat Milenko ECS_SYST_AUT1 MUHI Instance_CheckStatus
REM startTests.bat Milenko ECS_SYST_AUT1 CRS CRS_V3.yaml
REM
REM Explicit project:
REM
REM startTests.bat --project C:\TOSCA_PROJECTS\DMP11 Milenko ECS_SYST_AUT1 DMP11AGATE "Instance_DMP SS12 V11 doAusschreibung.yaml"
REM
REM Instantiation:
REM startTests.bat instantiate MUHI CheckStatus.yaml CheckStatus.csv
REM startTests.bat instantiate DMP21 "DMP SS12 V11 getBetreutePatienten.yaml" "TCD_DMP SS12 V11 getBetreutePatienten.csv"
REM
REM Validation:
REM startTests.bat validate MUHI CheckStatus.yaml
REM startTests.bat validate DMP21 "DMP SS12 V11 getBetreutePatienten.yaml"
REM
REM Report analysis:
REM startTests.bat analyze "ecs_fach\report\ECS_SYST_AUT1\muhi\CheckStatus\Latest_Report.json"
REM
REM Describe:
REM startTests.bat describe
REM startTests.bat describe SOAP
REM startTests.bat describe REST
REM startTests.bat describe CALL
REM

echo ======================================================================
echo             Starting Agate Test Suite via Windows CMD
echo ======================================================================
echo  Current Dir: %CD%
echo  Agate JAR  : %AGATE_JAR%
if defined DEBUG_MODE echo  Debug Mode : ENABLED
echo ======================================================================
echo.

if not exist "%AGATE_JAR%" (
    echo [ERROR] AGATE JAR was not found:
    echo         %AGATE_JAR%
    echo.
    echo Build the project first:
    echo         mvn clean package
    echo.
    exit /b 1
)

java -jar "%AGATE_JAR%" %*

set "EXECUTION_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ======================================================================
echo             Execution finished.
echo  Exit Code : %EXECUTION_EXIT_CODE%
echo ======================================================================
echo.

exit /b %EXECUTION_EXIT_CODE%


:: ============================================================
:: MODE 2: TEST CASE INSTANTIATION
:: ============================================================

:INSTANTIATE

set "APP_NAME=%~2"
set "TEMPLATE_FILE=%~3"
set "DATA_FILE=%~4"

if "%APP_NAME%"=="" goto :INSTANTIATE_USAGE
if "%TEMPLATE_FILE%"=="" goto :INSTANTIATE_USAGE
if "%DATA_FILE%"=="" goto :INSTANTIATE_USAGE

echo ======================================================================
echo             BATCH TEST CASE INSTANTIATION
echo ======================================================================
echo  App           : %APP_NAME%
echo  Template File : %TEMPLATE_FILE%
echo  Data File     : %DATA_FILE%
echo ======================================================================
echo.

if not exist "%AGATE_JAR%" (
    echo [ERROR] AGATE JAR was not found:
    echo         %AGATE_JAR%
    echo.
    exit /b 1
)

java -jar "%AGATE_JAR%" ^
    instantiate ^
    "%APP_NAME%" ^
    "%TEMPLATE_FILE%" ^
    "%DATA_FILE%"

set "INSTANTIATE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ======================================================================
echo             Instantiation finished.
echo  Exit Code : %INSTANTIATE_EXIT_CODE%
echo ======================================================================
echo.

exit /b %INSTANTIATE_EXIT_CODE%


:INSTANTIATE_USAGE

echo.
echo ERROR: Missing parameters.
echo.
echo Usage:
echo   startTests.bat instantiate ^<appName^> ^<templateFile^> ^<dataFile^>
echo.
echo Examples:
echo   startTests.bat instantiate MUHI CheckStatus.yaml CheckStatus.csv
echo   startTests.bat instantiate DMP21 "DMP SS12 V11 getBetreutePatienten.yaml" "TCD_DMP SS12 V11 getBetreutePatienten.csv"
echo.

exit /b 1


:: ============================================================
:: MODE 3: YAML VALIDATION
:: ============================================================

:VALIDATE

set "APP_NAME=%~2"
set "VALIDATION_FILE=%~3"

if "%APP_NAME%"=="" goto :VALIDATE_USAGE
if "%VALIDATION_FILE%"=="" goto :VALIDATE_USAGE

echo ======================================================================
echo                     AGATE YAML VALIDATION
echo ======================================================================
echo  App  : %APP_NAME%
echo  File : %VALIDATION_FILE%
echo ======================================================================
echo.

if not exist "%AGATE_JAR%" (
    echo [ERROR] AGATE JAR was not found:
    echo         %AGATE_JAR%
    echo.
    exit /b 1
)

java -jar "%AGATE_JAR%" ^
    validate ^
    "%APP_NAME%" ^
    "%VALIDATION_FILE%"

set "VALIDATION_EXIT_CODE=%ERRORLEVEL%"

echo.

if "%VALIDATION_EXIT_CODE%"=="0" (
    echo ======================================================================
    echo             VALIDATION SUCCESSFUL
    echo ======================================================================
) else (
    echo ======================================================================
    echo             VALIDATION FAILED
    echo  Exit Code: %VALIDATION_EXIT_CODE%
    echo ======================================================================
)

exit /b %VALIDATION_EXIT_CODE%


:VALIDATE_USAGE

echo.
echo ERROR: Missing application or YAML file.
echo.
echo Usage:
echo   startTests.bat validate ^<appName^> ^<yamlFile^>
echo.
echo Examples:
echo   startTests.bat validate MUHI CheckStatus.yaml
echo   startTests.bat validate DMP21 "DMP SS12 V11 getBetreutePatienten.yaml"
echo.

exit /b 1


:: ============================================================
:: MODE 4: REPORT ANALYSIS
:: ============================================================

:ANALYZE

set "REPORT_FILE=%~2"

if "%REPORT_FILE%"=="" goto :ANALYZE_USAGE

echo ======================================================================
echo                     AGATE REPORT ANALYSIS
echo ======================================================================
echo  Report : %REPORT_FILE%
echo ======================================================================
echo.

if not exist "%AGATE_JAR%" (
    echo [ERROR] AGATE JAR was not found:
    echo         %AGATE_JAR%
    echo.
    exit /b 1
)

java -jar "%AGATE_JAR%" ^
    analyze ^
    "%REPORT_FILE%"

set "ANALYZE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ======================================================================
echo             Analysis finished.
echo  Exit Code : %ANALYZE_EXIT_CODE%
echo ======================================================================
echo.

exit /b %ANALYZE_EXIT_CODE%


:ANALYZE_USAGE

echo.
echo ERROR: Missing JSON report.
echo.
echo Usage:
echo   startTests.bat analyze ^<jsonReport^>
echo.
echo Example:
echo   startTests.bat analyze "ecs_fach\report\ECS_SYST_AUT1\muhi\CheckStatus\Latest_Report.json"
echo.

exit /b 1


:: ============================================================
:: MODE 5: DSL DESCRIPTION
:: ============================================================

:DESCRIBE

set "DSL_TYPE=%~2"

echo ======================================================================
echo                     AGATE DSL DESCRIPTION
echo ======================================================================

if not exist "%AGATE_JAR%" (
    echo [ERROR] AGATE JAR was not found:
    echo         %AGATE_JAR%
    echo.
    exit /b 1
)

if "%DSL_TYPE%"=="" (

    echo.

    java -jar "%AGATE_JAR%" ^
        describe

) else (

    echo  Type : %DSL_TYPE%
    echo ======================================================================
    echo.

    java -jar "%AGATE_JAR%" ^
        describe ^
        "%DSL_TYPE%"
)

set "DESCRIBE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ======================================================================
echo             Description finished.
echo  Exit Code : %DESCRIBE_EXIT_CODE%
echo ======================================================================
echo.

exit /b %DESCRIBE_EXIT_CODE%
