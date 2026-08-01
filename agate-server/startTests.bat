@echo off
:: Ensure the working directory is the folder where this .bat file is located
cd /d "%~dp0"

:: Provera da li je prvi parametar "instantiate"
if /I "%1"=="instantiate" goto :INSTANTIATE

:: --- STANDARDNO POKRETANJE TESTOVA ---
set USER_NAME=%1
if "%USER_NAME%"=="" set USER_NAME=""

set INSTANCE=%2
if "%INSTANCE%"=="" set INSTANCE=""

set APP_NAME=%3
if "%APP_NAME%"=="" set APP_NAME=""

set TEST_SUITE=%4
if "%TEST_SUITE%"=="" set TEST_SUITE=""

set TEST_CASE=%5
if "%TEST_CASE%"=="" set TEST_CASE=""

set TEST_PRIORITY=%6
if "%TEST_PRIORITY%"=="" set TEST_PRIORITY=""

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

java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar %USER_NAME% %INSTANCE% %APP_NAME% %TEST_SUITE% %TEST_CASE% %TEST_PRIORITY%
goto :EOF

:: --- MOD ZA INSTANCIRANJE ---
:INSTANTIATE
set APP_NAME=%2
set TEMPLATE_FILE=%3
set DATA_FILE=%4

echo ======================================================================
echo             BATCH TEST CASE INSTANTIATION
echo ======================================================================
echo  App           : %APP_NAME%
echo  Template File : %TEMPLATE_FILE%
echo  Data File     : %DATA_FILE%
echo ======================================================================
echo.

java -jar target/agate-server-2.0.0-SNAPSHOT-jar-with-dependencies.jar instantiate %APP_NAME% %TEMPLATE_FILE% %DATA_FILE%
goto :EOF

:EOF
echo.
echo ======================================================================
echo             Execution finished.
echo ======================================================================
echo.