#!/bin/bash
if [ -f app.pid ]; then
  kill $(cat app.pid)
  rm app.pid
  echo "Application stopped."
else
  echo "No PID file found. Is the app running?"
fi 