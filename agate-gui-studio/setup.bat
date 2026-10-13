@echo off
cd /d "%~dp0"
where node >nul 2>nul
if errorlevel 1 (
  echo Install Node.js 22 or newer, then run setup.bat again.
  pause
  exit /b 1
)
call npm ci
if errorlevel 1 exit /b 1
call npx playwright install chromium
if errorlevel 1 exit /b 1
echo Setup complete. Run startStudio.bat.
pause
