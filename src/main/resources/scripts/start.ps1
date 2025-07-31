$jar = "bin/Project1-Build-1.0.0.jar"
$conf = "conf/application.properties"
$log = "log/app.log"
if (-not (Test-Path $jar)) {
  Write-Host "JAR file not found in bin!"
  exit 1
}
if (-not (Test-Path $conf)) {
  Write-Host "application.properties not found in conf!"
  exit 1
}
if (-not (Test-Path "log")) {
  New-Item -ItemType Directory -Path "log" | Out-Null
}
$proc = Start-Process java -ArgumentList "-jar `"$jar`" --spring.config.location=`"$conf`"" -RedirectStandardOutput $log -RedirectStandardError $log -NoNewWindow -PassThru
$proc.Id | Out-File app.pid
Write-Host "Application started with PID $($proc.Id)" 