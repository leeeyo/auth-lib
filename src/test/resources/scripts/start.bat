@echo off
setlocal
for %%f in (..\build\libs\*.jar) do set JAR=%%f
if not defined JAR (
  echo JAR file not found!
  exit /b 1
)
start /b java -jar "%JAR%" > app.log 2>&1
echo %! > app.pid
echo Application started.
endlocal