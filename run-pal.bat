@echo off
cd examples/Pal
call mvn -q compile exec:java -D"exec.mainClass"="pal.PalMain" -D"exec.args"="%*" -D"java.library.path"=..\..\..\FastAIVectorDB\build
cd ../..
