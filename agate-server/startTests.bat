@echo off

:: Ensure the working directory is the folder where this .bat file is located
cd /d "%~dp0"


:: ============================================================
:: MODE SELECTION
:: ============================================================

if /I "%~1"=="instantiate" goto :INSTANTIATE
if /I "%~1"=="validate" goto :VALIDATE
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

REM
REM Examples:
REM
REM startTests.bat Milenko ECS_SYST_AUT1 MUHI Instance_CheckStatus
REM startTests.bat Milenko ECS_SYST_AUT1 CRS CRS_V3.yaml
REM
REM Instantiation:
REM startTests.bat instantiate MUHI CheckStatus.yaml CheckStatus.csv
REM startTests.bat instantiate DMP21 "DMP SS12 V11 getBetreutePatienten.yaml" "TCD_DMP SS12 V11 getBetreutePatienten.csv"
REM
REM Validation:
REM startTests.bat validate data\MUHI\CheckStatus.yaml
REM startTests.bat validate "data\DMP21\DMP SS12 V11 getBetreutePatienten.yaml"
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
echo  User      : %USER_NAME%
echo  Instance  : %INSTANCE%
echo  App       : %APP_NAME%
echo  Test Suite: %TEST_SUITE%
echo  Test Case : %TEST_CASE%
echo  Priority  : %TEST_PRIORITY%
echo ======================================================================
echo.

java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar ^
    "%USER_NAME%" ^
    "%INSTANCE%" ^
    "%APP_NAME%" ^
    "%TEST_SUITE%" ^
    "%TEST_CASE%" ^
    "%TEST_PRIORITY%"

goto :EOF


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

java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar ^
    instantiate ^
    "%APP_NAME%" ^
    "%TEMPLATE_FILE%" ^
    "%DATA_FILE%"

goto :EOF


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

goto :EOF


:: ============================================================
:: MODE 3: YAML VALIDATION
:: ============================================================

:VALIDATE

set "VALIDATION_FILE=%~2"

if "%VALIDATION_FILE%"=="" goto :VALIDATE_USAGE

echo ======================================================================
echo                     AGATE YAML VALIDATION
echo ======================================================================
echo  File : %VALIDATION_FILE%
echo ======================================================================
echo.

java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar ^
    validate ^
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
echo ERROR: Missing YAML file.
echo.
echo Usage:
echo   startTests.bat validate ^<yamlFile^>
echo.
echo Examples:
echo   startTests.bat validate data\MUHI\CheckStatus.yaml
echo   startTests.bat validate "data\DMP21\DMP SS12 V11 getBetreutePatienten.yaml"
echo.

exit /b 1


:: ============================================================
:: MODE 4: DSL DESCRIPTION
:: ============================================================

:DESCRIBE

set "DSL_TYPE=%~2"

echo ======================================================================
echo                     AGATE DSL DESCRIPTION
echo ======================================================================

if "%DSL_TYPE%"=="" (
    echo.
    java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar ^
        describe
) else (
    echo  Type : %DSL_TYPE%
    echo ======================================================================
    echo.

    java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar ^
        describe ^
        "%DSL_TYPE%"
)

goto :EOF


:: ============================================================
:: END
:: ============================================================

:EOF

echo.
echo ======================================================================
echo             Execution finished.
echo ======================================================================
echo.
