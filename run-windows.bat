@echo off
call mvn clean package
if errorlevel 1 exit /b 1
java -jar target\time-off-tracker.jar
