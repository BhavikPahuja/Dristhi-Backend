@echo off
cd /d d:\Tekathon\Drishti
call mvnw.cmd -DskipTests=false clean test > build.log 2>&1