@echo off
cd examples/Pal
call mvn -q compile exec:java -D"exec.mainClass"="pal.PalIndexBuilder" -D"java.library.path"=..\..\..\FastAIVectorDB\build
cd ../..
pause
