@echo off
echo [FastAIRag] Building Pal RAG Index...
cd examples/Pal
call mvn -q compile exec:java -D"exec.mainClass"="pal.PalIndexBuilder"
cd ../..
pause
