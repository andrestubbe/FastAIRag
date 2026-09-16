@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo [1/3] Building FastAIRag...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Build failed! & pause & exit /b %ERRORLEVEL% )

echo [2/3] Compiling Demo...
cd examples\Demo
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Demo compilation failed! & pause & exit /b %ERRORLEVEL% )

echo [3/3] Running Demo...
call mvn exec:java "-Dexec.mainClass=demo.Demo" -q %*
cd ..\..
pause
