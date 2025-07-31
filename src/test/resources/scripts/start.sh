#!/bin/bash
JAR_NAME=$(ls ../build/libs/*.jar | head -n 1)
if [ -z "$JAR_NAME" ]; then
  echo "JAR file not found!"
  exit 1
fi
nohup java -jar "$JAR_NAME" > app.log 2>&1 &
echo $! > app.pid
echo "Application started with PID $(cat app.pid)"