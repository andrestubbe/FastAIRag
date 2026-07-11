@echo off
echo [FastAIRag] Running Pal CLI Demo...
cd examples/Pal
call mvn -q compile exec:java
cd ../..
pause
