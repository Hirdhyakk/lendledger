#!/usr/bin/env bash
set -euo pipefail
PORT="${SERVER_PORT:-8080}"
if lsof -ti ":$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "ERROR: Port $PORT is already in use (stale gateway from a prior run?)."
  echo "  ./scripts/stop-gateway.sh"
  echo "  ./scripts/run-gateway.sh"
  exit 1
fi
exec "$(dirname "$0")/lib/run-jar.sh" gateway/api-gateway api-gateway
