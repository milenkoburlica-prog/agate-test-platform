@echo off
setlocal
cd /d "%~dp0"
echo Starting neutral PDF demo service from: %CD%
java --add-modules jdk.httpserver --source 17 src\main\java\demo\DemoServer.java 8767 site
if errorlevel 1 echo ERROR: Service did not start. Check the Java error above; port 8767 may already be in use.
pause
