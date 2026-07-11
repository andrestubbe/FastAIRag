@echo off
echo [FastAIRag] Running Pal CLI Demo...
cd examples/Pal
call mvn compile exec:java
cd ../..
pause
