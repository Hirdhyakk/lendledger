#!/usr/bin/env bash
# Stop whatever is listening on the API gateway port (default 8080).
set -euo pipefail
PORT="${SERVER_PORT:-8080}"
PIDS=$(lsof -ti ":$PORT" -sTCP:LISTEN 2>/dev/null || true)
if [[ -z "$PIDS" ]]; then
  echo "Nothing listening on port $PORT."
  exit 0
fi
echo "Stopping process(es) on port $PORT: $PIDS"
kill $PIDS
sleep 1
if lsof -ti ":$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Still listening; sending SIGKILL..."
  kill -9 $(lsof -ti ":$PORT" -sTCP:LISTEN) 2>/dev/null || true
fi
echo "Port $PORT is free."
