$jar = Get-ChildItem ..\build\libs\*.jar | Select-Object -First 1
if (-not $jar) {
  Write-Host "JAR file not found!"
  exit 1
}
Start-Process java -ArgumentList "-jar `"$($jar.FullName)`"" -RedirectStandardOutput app.log -RedirectStandardError app.log -NoNewWindow
Start-Sleep -Seconds 2
$proc = Get-Process | Where-Object { $_.Path -eq $jar.FullName }
if ($proc) {
  $proc.Id | Out-File app.pid
  Write-Host "Application started with PID $($proc.Id)"
}