@echo off
setlocal EnableExtensions

rem ============================================================
rem AGATE OpenAPI - CLI Dispatcher
rem ============================================================

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%" >nul

rem ------------------------------------------------------------
rem Runtime
rem ------------------------------------------------------------

set "RUNNER_JAR=target\agate-openapi-2.0.0-SNAPSHOT-runner.jar"

rem ------------------------------------------------------------
rem Main classes
rem ------------------------------------------------------------

set "CLI_MODEL=at.co.svc.agate.openapi.cli.AgateOpenApiCli"
set "CLI_PHASE1=at.co.svc.agate.openapi.phase1.cli.AgatePhase1Cli"
set "CLI_PHASE2=at.co.svc.agate.openapi.phase2.cli.AgatePhase2Cli"
set "CLI_PHASE3=at.co.svc.agate.openapi.phase3.cli.AgatePhase3Cli"
set "CLI_CHANGES=at.co.svc.agate.openapi.change.cli.AgateOpenApiChangeCli"
set "CLI_IMPACT=at.co.svc.agate.openapi.impact.cli.AgateOpenApiImpactCli"

rem ------------------------------------------------------------
rem Check build
rem ------------------------------------------------------------

if not exist "%RUNNER_JAR%" (
    echo.
    echo ============================================================
    echo ERROR: AGATE OpenAPI runner JAR not found
    echo ============================================================
    echo.
    echo Expected:
    echo   %RUNNER_JAR%
    echo.
    echo Build first:
    echo   mvn clean package -DskipTests
    echo.
    popd >nul
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
rem Route command
rem ------------------------------------------------------------

set "COMMAND=%~1"
set "MAIN="
set "PREFIX="

if /I "%COMMAND%"=="model" (
    set "MAIN=%CLI_MODEL%"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="model-json" (
    set "MAIN=%CLI_MODEL%"
    set "PREFIX=--json"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="phase1" (
    set "MAIN=%CLI_PHASE1%"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="phase2" (
    set "MAIN=%CLI_PHASE2%"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="phase3" (
    set "MAIN=%CLI_PHASE3%"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="list" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--list"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="test" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--test"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="dsl" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--dsl"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="csv" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--csv"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="yaml" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--yaml"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="generate" (
    set "MAIN=%CLI_PHASE3%"
    set "PREFIX=--generate"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="changes" (
    set "MAIN=%CLI_CHANGES%"
    set "PREFIX=--changes"
    goto PREPARE_ARGS
)

if /I "%COMMAND%"=="impact" (
    set "MAIN=%CLI_IMPACT%"
    set "PREFIX=--impact"
    goto PREPARE_ARGS
)

echo.
echo ERROR: Unknown command: %COMMAND%
echo.
goto HELP_ERROR

rem ============================================================
rem Prepare remaining arguments
rem ============================================================

:PREPARE_ARGS
shift
set "ARGS="

:COLLECT_ARGS
if "%~1"=="" goto EXECUTE
set "ARGS=%ARGS% "%~1""
shift
goto COLLECT_ARGS

rem ============================================================
rem Execute
rem ============================================================

:EXECUTE

echo.
echo ============================================================
echo AGATE OpenAPI
echo ============================================================
echo Command : %COMMAND%
echo Main    : %MAIN%
echo JAR     : %RUNNER_JAR%
echo ============================================================
echo.

if defined PREFIX (
    java -Djava.util.logging.manager=org.jboss.logmanager.LogManager ^
         -cp "%RUNNER_JAR%" ^
         %MAIN% %PREFIX% %ARGS%
) else (
    java -Djava.util.logging.manager=org.jboss.logmanager.LogManager ^
         -cp "%RUNNER_JAR%" ^
         %MAIN% %ARGS%
)

set "RC=%ERRORLEVEL%"

echo.
echo ============================================================
echo AGATE OpenAPI finished
echo Exit Code: %RC%
echo ============================================================

popd >nul
exit /b %RC%

rem ============================================================
rem Help
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
echo ------------------------------------------------------------
echo OPENAPI MODEL
echo ------------------------------------------------------------
echo   model ^<OPENAPI_SOURCE^>
echo   model-json ^<OPENAPI_SOURCE^>
echo.
echo ------------------------------------------------------------
echo PHASE 1
echo ------------------------------------------------------------
echo   phase1 ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^> [status] [mediaType] [options]
echo.
echo ------------------------------------------------------------
echo PHASE 2
echo ------------------------------------------------------------
echo   phase2 ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^> [options]
echo.
echo ------------------------------------------------------------
echo PHASE 3
echo ------------------------------------------------------------
echo   phase3 ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   list ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   test ^<TECHNICAL_NAME^> ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   dsl ^<TECHNICAL_NAME^> ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   csv ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   yaml ^<OPENAPI_SOURCE^> ^<METHOD^> ^<PATH^>
echo   generate ^<APP_ID^> ^<OPENAPI_SOURCE^>
echo.
echo Example:
echo   startOpenAPI.bat generate petstore3 resources\petstore3\openapi.yaml
echo.
echo ------------------------------------------------------------
echo CONTRACT CHANGES
echo ------------------------------------------------------------
echo   changes ^<OLD_OPENAPI^> ^<NEW_OPENAPI^>
echo.
echo ------------------------------------------------------------
echo IMPACT ANALYSIS
echo ------------------------------------------------------------
echo   impact ^<OLD_OPENAPI^> ^<NEW_OPENAPI^> ^<APP_DIRECTORY^>
echo.
echo ------------------------------------------------------------
echo BUILD
echo ------------------------------------------------------------
echo   mvn clean package -DskipTests
echo.
echo ============================================================

popd >nul
exit /b 0

:HELP_ERROR
call :HELP
exit /b 1
