#!/bin/bash
if [ -f app.pid ]; then
  kill $(cat app.pid)
  rm app.pid
  echo "Application stopped."
else
  echo "Application not running."
fi