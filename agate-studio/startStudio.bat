@echo off
cd /d "%~dp0"
if not exist node_modules (
  echo Run setup.bat first.
  pause
  exit /b 1
)
echo Open http://127.0.0.1:4310 in your browser.
call npm start
pause
