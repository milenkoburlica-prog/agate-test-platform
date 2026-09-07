@echo off
chcp 65001 > nul
:: Ensure the working directory is the folder where this .bat file is located
cd /d "%~dp0"

:: The first argument must be the command (migrate, clean, csv, etc.)
set COMMAND=%1
:: Other parameters are variable
set P1=%2
set P2=%3
set P3=%4
set P4=%5

REM Start Examples.
REM 
REM startMigration.bat migrate MUHI2 MUHI1_T_anspruchPruefen
REM startMigration.bat migrate MUHI2 MUHI1_TCD_anspruchPruefen
REM startMigration.bat clean MUHI2 anspruchPruefen.yaml MUHI1_T_anspruchPruefen.csv
REM 
REM startTests.bat instantiate MUHI2 anspruchPruefen.yaml MUHI1_T_anspruchPruefen.csv
REM startTests.bat Milenko ECS_SYST_AUT1 MUHI2 Instance_anspruchPruefen.yaml

echo ======================================================================
echo              Starting Agate Migration/Clean Utility
echo ======================================================================
echo  Command    : %COMMAND%
echo  Param 1    : %P1%
echo  Param 2    : %P2%
echo ======================================================================
echo.

:: Run the Fat JAR and forward all arguments
:: Kada proslediš 'clean MUHI', Java će unutar main-a sama da sklopi punu putanju
java -jar target/agate-tosca-migration-svc-1.0.0-SNAPSHOT-jar-with-dependencies.jar %COMMAND% %P1% %P2% %P3% %P4%

echo.
echo ======================================================================
echo              Execution finished.
echo ======================================================================
echo.