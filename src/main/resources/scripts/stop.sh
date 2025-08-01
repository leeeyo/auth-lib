#!/bin/bash
if [ -f app.pid ]; then
  kill $(cat app.pid)
  rm app.pid
  echo "Application terminated."
else
  echo "Application is not UP "
fi 