@echo off
set JAR=bin\Project1-Build-1.0.0.jar
set CONF=conf\application.properties
set LOG=log\app.log
if not exist %JAR% (
  echo JAR file not found in bin!
  exit /b 1
)
if not exist %CONF% (
  echo application.properties not found in conf!
  exit /b 1
)
if not exist log (
  mkdir log
)
start /b java -jar "%JAR%" --spring.config.location="%CONF%" > %LOG% 2>&1
REM PID management is limited in batch; recommend using PowerShell for robust PID handling 