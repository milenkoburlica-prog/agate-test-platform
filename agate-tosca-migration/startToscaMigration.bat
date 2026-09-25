@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem ============================================================
rem AGATE Tosca Migration
rem
rem Usage:
rem   startToscaMigration.bat migrate <appID> <baseFileName> [--cleanup-dry-run|--cleanup]
rem
rem   startToscaMigration.bat clean <appID> <testsuite.yaml>
rem       Cleans a generated testcase YAML and all reusable YAML
rem       files under:
rem         data\<appID>\<testsuite.yaml>
rem         data\<appID>\reusable\*.yaml
rem
rem   startToscaMigration.bat clean <appID> <template.yaml> <template.csv>
rem       Runs the existing template cleanup under:
rem         migration\data\<appID>\template\
rem
rem Examples:
rem   startToscaMigration.bat migrate PST PST-004_Report
rem   startToscaMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
rem
rem   startToscaMigration.bat clean PST PST-004_Report.yaml
rem
rem   startToscaMigration.bat clean DMP11 ^
rem       dmp_11_getAdminPatientenInformationen.yaml ^
rem       tcd_dmp_11_getAdminPatientenInformationen.csv
rem ============================================================


rem ============================================================
rem BASE PATHS
rem ============================================================

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "JAR_FILE=target\agate-tosca-migration-svc-1.0.0-SNAPSHOT-jar-with-dependencies.jar"


rem ============================================================
rem TOSCA TRANSLATION CONFIG
rem
rem Default:
rem   <project>\agate-acme-config\acme-tosca-translations.yaml
rem
rem Can be overridden externally with:
rem
rem   set AGATE_TOSCA_TRANSLATION_CONFIG=C:\path\customer.yaml
rem ============================================================

if defined AGATE_TOSCA_TRANSLATION_CONFIG (
    set "TOSCA_TRANSLATION_CONFIG=%AGATE_TOSCA_TRANSLATION_CONFIG%"
    set "TOSCA_TRANSLATION_CONFIG_SOURCE=environment"
) else (
    set "TOSCA_TRANSLATION_CONFIG=%SCRIPT_DIR%agate-acme-config\acme-tosca-translations.yaml"
    set "TOSCA_TRANSLATION_CONFIG_SOURCE=default"
)


rem ============================================================
rem PRE-CHECKS
rem ============================================================

rem ------------------------------------------------------------
rem Check Java
rem ------------------------------------------------------------

where java >nul 2>&1

if errorlevel 1 (
    echo.
    echo ============================================================
    echo [ERROR] Java was not found in PATH.
    echo ============================================================
    echo.
    exit /b 1
)


rem ------------------------------------------------------------
rem Check JAR
rem ------------------------------------------------------------

if not exist "%JAR_FILE%" (
    echo.
    echo ============================================================
    echo [ERROR] AGATE Tosca Migration JAR was not found.
    echo ============================================================
    echo.
    echo Expected:
    echo   %SCRIPT_DIR%%JAR_FILE%
    echo.
    echo Run:
    echo   mvn clean package
    echo.
    exit /b 1
)


rem ============================================================
rem COMMAND ROUTING
rem ============================================================

if "%~1"=="" goto :usage

if /I "%~1"=="migrate" goto :migrate
if /I "%~1"=="clean"   goto :clean
if /I "%~1"=="help"    goto :usage
if /I "%~1"=="-h"      goto :usage
if /I "%~1"=="--help"  goto :usage

echo.
echo [ERROR] Unknown command: %~1
goto :usage_error


rem ============================================================
rem MIGRATE
rem ============================================================

:migrate

if "%~2"=="" (
    echo.
    echo [ERROR] Missing appID.
    goto :usage_error
)

if "%~3"=="" (
    echo.
    echo [ERROR] Missing baseFileName.
    goto :usage_error
)


rem ------------------------------------------------------------
rem Optional migration cleanup mode
rem ------------------------------------------------------------

set "MIGRATION_CLEANUP_MODE="
set "MIGRATION_CLEANUP_JAVA_OPT="

if not "%~5"=="" (
    echo.
    echo [ERROR] Too many arguments for 'migrate'.
    goto :usage_error
)

if not "%~4"=="" (
    if /I "%~4"=="--cleanup-dry-run" (
        set "MIGRATION_CLEANUP_MODE=DRY_RUN"
        set "MIGRATION_CLEANUP_JAVA_OPT=-Dagate.migration.cleanup=dry-run"
    ) else if /I "%~4"=="--cleanup" (
        set "MIGRATION_CLEANUP_MODE=CLEANUP"
        set "MIGRATION_CLEANUP_JAVA_OPT=-Dagate.migration.cleanup=cleanup"
    ) else (
        echo.
        echo [ERROR] Unknown migrate option: %~4
        echo         Supported:
        echo           --cleanup-dry-run
        echo           --cleanup
        exit /b 1
    )
)


rem ------------------------------------------------------------
rem Check Tosca translation configuration
rem ------------------------------------------------------------

if not exist "%TOSCA_TRANSLATION_CONFIG%" (
    echo.
    echo ============================================================
    echo [ERROR] Tosca translation configuration was not found.
    echo ============================================================
    echo.
    echo Config:
    echo   %TOSCA_TRANSLATION_CONFIG%
    echo.
    echo Default:
    echo   %SCRIPT_DIR%agate-acme-config\acme-tosca-translations.yaml
    echo.
    echo Override example:
    echo.
    echo   set AGATE_TOSCA_TRANSLATION_CONFIG=C:\path\customer.yaml
    echo.
    exit /b 1
)


echo.
echo ============================================================
echo AGATE TOSCA MIGRATION
echo ============================================================
echo Command       : migrate
echo Application   : %~2
echo Base file     : %~3
echo.
echo JAR           : %JAR_FILE%
echo Config        : %TOSCA_TRANSLATION_CONFIG%
echo Config source : %TOSCA_TRANSLATION_CONFIG_SOURCE%
if defined MIGRATION_CLEANUP_MODE echo Cleanup mode  : %~4
echo ============================================================
echo.

goto :execute


rem ============================================================
rem CLEAN
rem
rem Two variants are supported:
rem
rem 1) Testcase cleanup:
rem      clean <appID> <testsuite.yaml>
rem
rem    Processes:
rem      data\<appID>\<testsuite.yaml>
rem      data\<appID>\reusable\*.yaml
rem
rem 2) Template cleanup:
rem      clean <appID> <template.yaml> <template.csv>
rem
rem    Runs the existing template cleanup.
rem ============================================================

:clean

if "%~2"=="" (
    echo.
    echo [ERROR] Missing appID.
    goto :usage_error
)

if "%~3"=="" (
    echo.
    echo [ERROR] Missing YAML file.
    goto :usage_error
)


rem ------------------------------------------------------------
rem Testcase cleanup
rem
rem Exactly:
rem   clean <appID> <testsuite.yaml>
rem ------------------------------------------------------------

if "%~4"=="" goto :clean_testcase


rem ------------------------------------------------------------
rem Template cleanup
rem
rem Exactly:
rem   clean <appID> <template.yaml> <template.csv>
rem ------------------------------------------------------------

if not "%~5"=="" (
    echo.
    echo [ERROR] Too many arguments for 'clean'.
    goto :usage_error
)

goto :clean_template


rem ============================================================
rem CLEAN TESTCASE
rem ============================================================

:clean_testcase

echo.
echo ============================================================
echo AGATE TOSCA TESTCASE CLEANUP
echo ============================================================
echo Command       : clean
echo Mode          : testcase
echo Application   : %~2
echo Test Suite    : %~3
echo.
echo Test File     : data\%~2\%~3
echo Reusable      : data\%~2\reusable\*.yaml
echo.
echo JAR           : %JAR_FILE%
echo ============================================================
echo.

goto :execute


rem ============================================================
rem CLEAN TEMPLATE
rem ============================================================

:clean_template

echo.
echo ============================================================
echo AGATE TOSCA TEMPLATE CLEANUP
echo ============================================================
echo Command       : clean
echo Mode          : template
echo Application   : %~2
echo Template YAML : %~3
echo Template CSV  : %~4
echo.
echo JAR           : %JAR_FILE%
echo ============================================================
echo.

goto :execute


rem ============================================================
rem EXECUTE
rem ============================================================

:execute

if /I "%~1"=="migrate" (
    java ^
        "-Dagate.tosca.translation.config=%TOSCA_TRANSLATION_CONFIG%" ^
        %MIGRATION_CLEANUP_JAVA_OPT% ^
        -jar "%JAR_FILE%" ^
        migrate "%~2" "%~3"
) else (
    java ^
        "-Dagate.tosca.translation.config=%TOSCA_TRANSLATION_CONFIG%" ^
        -jar "%JAR_FILE%" ^
        %*
)

set "RC=%ERRORLEVEL%"

echo.

if not "%RC%"=="0" (
    echo ============================================================
    echo [ERROR] AGATE Tosca Migration failed.
    echo Exit code: %RC%
    echo ============================================================
    exit /b %RC%
)

echo ============================================================
echo [SUCCESS] AGATE Tosca Migration completed.
echo ============================================================

exit /b 0


rem ============================================================
rem USAGE
rem ============================================================

:usage

echo.
echo ============================================================
echo AGATE Tosca Migration
echo ============================================================
echo.
echo Usage:
echo.
echo   startToscaMigration.bat migrate ^<appID^> ^<baseFileName^> [--cleanup-dry-run^|--cleanup]
echo.
echo   startToscaMigration.bat clean ^<appID^> ^<testsuite.yaml^>
echo.
echo       Cleans:
echo         data\^<appID^>\^<testsuite.yaml^>
echo         data\^<appID^>\reusable\*.yaml
echo.
echo   startToscaMigration.bat clean ^<appID^> ^<template.yaml^> ^<template.csv^>
echo.
echo       Runs the existing template cleanup under:
echo         migration\data\^<appID^>\template\
echo.
echo Examples:
echo.
echo   startToscaMigration.bat migrate PST PST-004_Report
echo.
echo   startToscaMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
echo.
echo   startToscaMigration.bat migrate DMP31 "T_DMP SS12 V11 getBetreutePatienten" --cleanup-dry-run
echo.
echo   startToscaMigration.bat migrate DMP31 "T_DMP SS12 V11 getBetreutePatienten" --cleanup
echo.
echo   startToscaMigration.bat clean PST PST-004_Report.yaml
echo.
echo   startToscaMigration.bat clean DMP11 template.yaml template.csv
echo.
echo Tosca translation config:
echo.
echo   Default:
echo     %SCRIPT_DIR%agate-acme-config\acme-tosca-translations.yaml
echo.
echo   Override:
echo     set AGATE_TOSCA_TRANSLATION_CONFIG=C:\path\customer.yaml
echo.
echo Current config:
echo     %TOSCA_TRANSLATION_CONFIG%
echo.
echo JAR:
echo.
echo   %JAR_FILE%
echo.
echo ============================================================

exit /b 0


:usage_error

echo.
call :usage
exit /b 1