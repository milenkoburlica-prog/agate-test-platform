@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ============================================================
rem AGATE Tosca Migration
rem
rem Usage:
rem   startToscaMigration.bat migrate <appID> <baseFileName>
rem   startToscaMigration.bat clean   <appID> <template.yaml> <template.csv>
rem
rem Examples:
rem   startToscaMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
rem   startToscaMigration.bat migrate DMP11 tcd_dmp_11_getAdminPatientenInformationen
rem   startToscaMigration.bat clean DMP11 dmp_11_getAdminPatientenInformationen.yaml tcd_dmp_11_getAdminPatientenInformationen.csv
rem ============================================================

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "MAIN_CLASS=at.co.svc.tosca.main.Main"

rem ------------------------------------------------------------
rem Resolve Java
rem ------------------------------------------------------------
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH.
    echo         Please install/configure Java before running the migration.
    exit /b 1
)

rem ------------------------------------------------------------
rem Resolve application classpath
rem
rem Preferred:
rem   target\classes + target\dependency\*
rem
rem Fallback:
rem   target\quarkus-app\quarkus-run.jar
rem
rem If your project uses another packaging layout, adjust CLASSPATH.
rem ------------------------------------------------------------
set "CLASSPATH="

if exist "target\classes" (
    set "CLASSPATH=target\classes"
    if exist "target\dependency" (
        set "CLASSPATH=!CLASSPATH!;target\dependency\*"
    )
)

if not defined CLASSPATH (
    if exist "target\quarkus-app\quarkus-run.jar" (
        set "QUARKUS_RUNNER=target\quarkus-app\quarkus-run.jar"
    )
)

rem ------------------------------------------------------------
rem Validate command
rem ------------------------------------------------------------
if "%~1"=="" goto :usage

set "COMMAND=%~1"

if /I "%COMMAND%"=="migrate" goto :migrate
if /I "%COMMAND%"=="clean"   goto :clean
if /I "%COMMAND%"=="help"    goto :usage
if /I "%COMMAND%"=="-h"      goto :usage
if /I "%COMMAND%"=="--help"  goto :usage

echo [ERROR] Unknown command: %COMMAND%
echo.
goto :usage_error


:migrate
rem ------------------------------------------------------------
rem migrate <appID> <baseFileName>
rem ------------------------------------------------------------
if "%~2"=="" (
    echo [ERROR] Missing appID.
    echo.
    goto :usage_error
)

if "%~3"=="" (
    echo [ERROR] Missing baseFileName.
    echo.
    goto :usage_error
)

set "APP_ID=%~2"
set "BASE_FILE=%~3"
set "TSU_FILE=tsu\%BASE_FILE%.tsu"

echo.
echo ============================================================
echo AGATE TOSCA MIGRATION
echo ============================================================
echo Command       : migrate
echo Application   : %APP_ID%
echo Base file     : %BASE_FILE%
echo TSU file      : %TSU_FILE%
echo ============================================================
echo.

if not exist "%TSU_FILE%" (
    echo [ERROR] TSU file does not exist:
    echo         %CD%\%TSU_FILE%
    echo.
    echo Copy the exported Tosca TSU file into:
    echo         %CD%\tsu
    exit /b 1
)

call :runJava migrate "%APP_ID%" "%BASE_FILE%"
set "RC=%ERRORLEVEL%"

if not "%RC%"=="0" (
    echo.
    echo [ERROR] Migration failed with exit code %RC%.
    exit /b %RC%
)

echo.
echo [SUCCESS] Migration completed.
echo.
exit /b 0


:clean
rem ------------------------------------------------------------
rem clean <appID> <template.yaml> <template.csv>
rem ------------------------------------------------------------
if "%~2"=="" (
    echo [ERROR] Missing appID.
    echo.
    goto :usage_error
)

if "%~3"=="" (
    echo [ERROR] Missing template YAML file name.
    echo.
    goto :usage_error
)

if "%~4"=="" (
    echo [ERROR] Missing template CSV file name.
    echo.
    goto :usage_error
)

set "APP_ID=%~2"
set "YAML_FILE=%~3"
set "CSV_FILE=%~4"
set "TEMPLATE_DIR=migration\data\%APP_ID%\template"
set "YAML_PATH=%TEMPLATE_DIR%\%YAML_FILE%"
set "CSV_PATH=%TEMPLATE_DIR%\%CSV_FILE%"

echo.
echo ============================================================
echo AGATE TOSCA CLEANUP
echo ============================================================
echo Command       : clean
echo Application   : %APP_ID%
echo Template YAML : %YAML_FILE%
echo Template CSV  : %CSV_FILE%
echo Template dir  : %TEMPLATE_DIR%
echo ============================================================
echo.

if not exist "%TEMPLATE_DIR%" (
    echo [ERROR] Template directory does not exist:
    echo         %CD%\%TEMPLATE_DIR%
    exit /b 1
)

if not exist "%YAML_PATH%" (
    echo [ERROR] Template YAML does not exist:
    echo         %CD%\%YAML_PATH%
    exit /b 1
)

if not exist "%CSV_PATH%" (
    echo [ERROR] Template CSV does not exist:
    echo         %CD%\%CSV_PATH%
    exit /b 1
)

call :runJava clean "%APP_ID%" "%YAML_FILE%" "%CSV_FILE%"
set "RC=%ERRORLEVEL%"

if not "%RC%"=="0" (
    echo.
    echo [ERROR] Cleanup failed with exit code %RC%.
    exit /b %RC%
)

echo.
echo [SUCCESS] Cleanup completed.
echo.
exit /b 0


:runJava
rem ------------------------------------------------------------
rem Run at.co.svc.tosca.main.Main
rem ------------------------------------------------------------
if defined CLASSPATH (
    java -cp "%CLASSPATH%" %MAIN_CLASS% %*
    exit /b %ERRORLEVEL%
)

if defined QUARKUS_RUNNER (
    rem Quarkus runner fallback.
    rem This works only if the configured Main class is packaged as the app entry point.
    java -jar "%QUARKUS_RUNNER%" %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Compiled application was not found.
echo.
echo Expected one of:
echo   target\classes
echo   target\quarkus-app\quarkus-run.jar
echo.
echo Build the project first.
echo For Maven, for example:
echo   mvn clean package
echo.
echo If dependencies are not copied to target\dependency, use:
echo   mvn dependency:copy-dependencies -DoutputDirectory=target\dependency
exit /b 1


:usage
echo.
echo AGATE Tosca Migration
echo.
echo Usage:
echo   startToscaMigration.bat migrate ^<appID^> ^<baseFileName^>
echo   startToscaMigration.bat clean   ^<appID^> ^<template.yaml^> ^<template.csv^>
echo.
echo Examples:
echo   startToscaMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
echo   startToscaMigration.bat migrate DMP11 tcd_dmp_11_getAdminPatientenInformationen
echo.
echo   startToscaMigration.bat clean DMP11 dmp_11_getAdminPatientenInformationen.yaml tcd_dmp_11_getAdminPatientenInformationen.csv
echo.
echo Expected input:
echo   tsu\^<baseFileName^>.tsu
echo.
echo Clean expects:
echo   migration\data\^<appID^>\template\^<template.yaml^>
echo   migration\data\^<appID^>\template\^<template.csv^>
echo.
exit /b 0


:usage_error
call :usage
exit /b 1
