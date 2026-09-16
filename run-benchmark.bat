@echo off
chcp 65001 >nul
cls

echo [1/3] Building FastAIRag...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Build failed! & pause & exit /b %ERRORLEVEL% )

echo [2/3] Building Benchmark Uber-JAR...
cd examples\Benchmark
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Benchmark build failed! & pause & exit /b %ERRORLEVEL% )

echo [3/3] Running JMH Benchmarks...
java -jar target\benchmarks.jar

cd ..\..
pause
