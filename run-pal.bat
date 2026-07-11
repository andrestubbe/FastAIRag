@echo off
cd examples/Pal
call mvn -q compile exec:java -Dexec.mainClass="pal.PalMain" -Dexec.args="%~1" -Djava.library.path="..\..\..\FastAIVectorDB\build"
cd ../..
