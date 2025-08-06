#!/bin/bash
JAR_NAME="bin/Project1-1.0.0.jar"
CONF_DIR="conf/application.properties"
LOG_DIR="logs/app.log"

if [ ! -f "$JAR_NAME" ]; then
  echo "JAR file not found in bin!"
  exit 1
fi
if [ ! -f "$CONF_DIR" ]; then
  echo "application.properties not found in conf!"
  exit 1
fi
nohup java -jar "$JAR_NAME" --spring.config.location="$CONF_DIR" > "$LOG_DIR" 2>&1 &
echo $! > app.pid
echo "Application started with PID $(cat app.pid)"
echo "http://localhost:9090/login"