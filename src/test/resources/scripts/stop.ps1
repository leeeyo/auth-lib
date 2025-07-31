if (Test-Path app.pid) {
  $pid = Get-Content app.pid
  Stop-Process -Id $pid -Force
  Remove-Item app.pid
  Write-Host "Application stopped."
} else {
  Write-Host "No PID file found. Is the app running?"
}