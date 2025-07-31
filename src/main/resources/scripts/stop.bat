@echo off
if exist app.pid (
  set /p PID=<app.pid
  taskkill /PID %PID% /F
  del app.pid
  echo Application stopped.
) else (
  echo No PID file found. Is the app running?
) 