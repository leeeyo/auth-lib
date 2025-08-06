@echo off
setlocal

:: ============================================================================
:: --- Configuration ---
:: ============================================================================
set "JAR_NAME=bin\Project1-1.0.0.jar"
set "MAIN_CLASS=com.digi.auth.Project1Application"

set "PID_FILE=app.pid"
set "LOG_FILE=logs/app.log"
set "JAVA_OPTS=-Xms512m -Xmx1024m"
set "APP_ARGS=--spring.config.location=conf/application.properties"

:: ============================================================================
:: --- Pre-flight Checks ---
:: ============================================================================
echo Checking prerequisites...

if exist "%PID_FILE%" (
    set /p "PID=" < "%PID_FILE%"
    tasklist /FI "PID eq %PID%" 2>nul | find "%PID%" >nul
    if not errorlevel 1 (
        echo [ERROR] Application is already running with PID %PID%.
        exit /b 1
    ) else (
        echo [INFO] Stale PID file found. Deleting it.
        del "%PID_FILE%"
    )
)

if not exist "%JAR_NAME%" (
    echo [ERROR] JAR file not found at: %JAR_NAME%
    exit /b 1
)

if not exist "logs" mkdir "logs"

:: ============================================================================
:: --- Start Application ---
:: ============================================================================
echo Starting the application...
start "JavaApp" /B java %JAVA_OPTS% -jar "%JAR_NAME%" %APP_ARGS%
:: ============================================================================
:: --- Find and Save PID ---
:: ============================================================================
echo Waiting for application to initialize (max 30 seconds)...
set "PID="
for /L %%i in (1,1,30) do (
    if not defined PID (
        for /f "tokens=1,2" %%a in ('jps -l') do (
            if "%%b"=="%JAR_NAME%" set "PID=%%a"
            if "%%b"=="%MAIN_CLASS%" set "PID=%%a"
        )
        if defined PID (
            goto :PID_FOUND
        )
        timeout /t 1 /nobreak >nul
    )
)

:PID_FOUND
if not defined PID (
    echo [ERROR] Could not find application PID after 30 seconds.
    echo Please check '%LOG_FILE%' for startup errors.
    exit /b 1
)

:: --- FINAL FIX: Sanitize the PID variable to remove any hidden whitespace ---
for /f "tokens=*" %%p in ("%PID%") do set "PID=%%p"

:: --- Use PowerShell to force creation of a clean ANSI/ASCII file ---
powershell -Command "Set-Content -Path '%PID_FILE%' -Value '%PID%' -Encoding Ascii -NoNewline"

echo --------------------------------------------------
echo Application started successfully!
echo   - PID: %PID%
echo   - Log File: %LOG_FILE%
echo "http://localhost:9090/login"
echo --------------------------------------------------

endlocal
exit /b 0