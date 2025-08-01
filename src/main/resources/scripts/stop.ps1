
# --- Configuration ---
# ============================================================================
$pidFile = "app.pid"

# ============================================================================
# --- Pre-flight Checks ---
# ============================================================================
if (-not (Test-Path $pidFile)) {
    Write-Host "[INFO] PID file not found. Application is likely not running." -ForegroundColor Green
    exit 0
}

# Renamed variable from $pid to $processId to avoid conflict with built-in variable
$processId = Get-Content $pidFile
$process = Get-Process -Id $processId -ErrorAction SilentlyContinue

if ($null -eq $process) {
    Write-Host "[INFO] Process with PID $processId not found (may have already stopped)." -ForegroundColor Yellow
    Write-Host "Deleting stale PID file." -ForegroundColor Yellow
    Remove-Item $pidFile
    exit 0
}

# ============================================================================
# --- Stop Application ---
# ============================================================================
try {
    Write-Host "[INFO] Terminating application for PID $processId..." -ForegroundColor Cyan
    # UPDATED: Go straight to forceful termination.
    # The /F flag forces termination, /T terminates child processes.
    taskkill.exe /PID $processId /F /T | Out-Null
    
    # Short wait to allow the OS to update the process list
    Start-Sleep -Seconds 2

    # Final check to confirm the process is gone
    if (-not (Get-Process -Id $processId -ErrorAction SilentlyContinue)) {
        Write-Host "✅ Application terminated." -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Failed to terminate the application." -ForegroundColor Red
    }

} catch {
    # This block will catch errors if the process was already gone or couldn't be stopped.
    $errorMessage = $_.Exception.Message
    if ($errorMessage -like "*Cannot find a process with the process identifier*") {
         Write-Host "✅ Application terminated." -ForegroundColor Green
    }
    else {
        Write-Host "[ERROR] An error occurred while trying to stop the process." -ForegroundColor Red
        Write-Host $errorMessage -ForegroundColor Red
        # Add a pause so you can read the error if the script is run directly
        Read-Host "Press Enter to exit..."
    }
} finally {
    # Clean up the PID file regardless of what happened
    if (Test-Path $pidFile) {
        Remove-Item $pidFile
    }
}
