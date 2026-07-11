@echo off
cd examples/Pal
call mvn -q compile exec:java -D"exec.args"="%*"
cd ../..
