# ============================================================================
# --- Configuration ---
# ============================================================================
$jarFile = "bin/Project1-1.0.0.jar"
$configFile = "conf/application.properties"
$logFile = "logs/app.log"
$pidFile = "app.pid"

# ============================================================================
# --- Pre-flight Checks ---
# ============================================================================
Write-Host "--- Checking Prerequisites ---" -ForegroundColor Cyan

# Check if app is already running
if (Test-Path $pidFile) {
    $runningPid = Get-Content $pidFile
    if (Get-Process -Id $runningPid -ErrorAction SilentlyContinue) {
        Write-Host "[ERROR] Application is already running with PID $runningPid." -ForegroundColor Red
        exit 1
    } else {
        Write-Host "[INFO] Stale PID file found. Deleting it." -ForegroundColor Yellow
        Remove-Item $pidFile
    }
}

# Check for required files
if (-not (Test-Path $jarFile)) {
    Write-Host "[ERROR] JAR file not found at: $jarFile" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $configFile)) {
    Write-Host "[ERROR] Config file not found at: $configFile" -ForegroundColor Red
    exit 1
}

# Create log directory if it doesn't exist
if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
}

# ============================================================================
# --- Start Application ---
# ============================================================================
Write-Host "--- Starting Application ---" -ForegroundColor Cyan
# Start Java in a hidden cmd window and get the cmd process object
$command = "java -jar `"$jarFile`" --spring.config.location=`"$configFile`""
$process = Start-Process cmd.exe -ArgumentList "/c $command" -NoNewWindow -PassThru

# ============================================================================
# --- Find and Save PID ---
# ============================================================================
Write-Host "Waiting for application to initialize (max 30 seconds)..." -ForegroundColor Yellow
$javaProcess = $null
for ($i = 0; $i -lt 30; $i++) {
    # Find the Java process that was started by our cmd.exe process
    # This is the most reliable way to find the correct PID.
    $javaProcess = Get-CimInstance Win32_Process -Filter "ParentProcessId = $($process.Id) AND Name = 'java.exe'"
    if ($null -ne $javaProcess) {
        break
    }
    Start-Sleep -Seconds 1
}

if ($null -ne $javaProcess) {
    $javaProcess.ProcessId | Out-File -FilePath $pidFile -Encoding ascii
    Write-Host "--------------------------------------------------" -ForegroundColor Green
    Write-Host "✅ Application started successfully!" -ForegroundColor Green
    Write-Host "   - PID: $($javaProcess.ProcessId)"
    Write-Host "   - Log File: $logFile"
    Write-Host "   - http://localhost:9090/login" -ForegroundColor Green
    Write-Host "--------------------------------------------------" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Could not find application PID after 30 seconds." -ForegroundColor Red
    Write-Host "Please check '$logFile' for startup errors." -ForegroundColor Red
}
