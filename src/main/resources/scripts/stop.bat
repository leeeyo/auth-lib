@echo off
setlocal

set "PID_FILE=app.pid"

if not exist "%PID_FILE%" (
    echo [SUCCESS] Application is not running.
    goto :END_SUCCESS
)

:: Read the first line of the file
for /f "usebackq delims=" %%i in ("%PID_FILE%") do set "PID=%%i"

if not defined PID (
    del "%PID_FILE%"
    goto :END_ERROR
)

:: Sanitize the PID variable to remove any hidden whitespace
for /f "tokens=*" %%p in ("%PID%") do set "PID=%%p"

tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
if errorlevel 1 (
    echo [SUCCESS] Application is not running.
    del "%PID_FILE%"
    goto :END_SUCCESS
)

taskkill /PID %PID% >nul 2>&1

for /L %%i in (1,1,10) do (
    tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
    if errorlevel 1 (
        echo [SUCCESS] Application stopped gracefully.
        goto :CLEANUP_AND_EXIT
    )
    timeout /t 1 /nobreak >nul
)

taskkill /F /PID %PID% >nul 2>&1
timeout /t 2 /nobreak >nul

tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
if errorlevel 1 (
    echo [SUCCESS] Application terminated.
    goto :CLEANUP_AND_EXIT
)

echo [ERROR] Failed to stop process with PID [%PID%]. Please check Task Manager.
goto :END_ERROR


:CLEANUP_AND_EXIT
del "%PID_FILE%"
goto :END_SUCCESS

:END_ERROR
endlocal
exit /b 1

:END_SUCCESS
endlocal
exit /b 0