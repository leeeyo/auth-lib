@echo off
if exist app.pid (
  set /p PID=<app.pid
  taskkill /PID %PID% /F
  del app.pid
  echo Application stopped.
) else (
  echo Application not running.
)