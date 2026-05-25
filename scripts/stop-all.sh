#!/usr/bin/env bash
# Stop all services started by run-all.sh (and free ports 5173, 8080-8084).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/logs"

echo "==> Stopping LendLedger services..."

if [[ -d "$LOG_DIR" ]]; then
  for pid_file in "$LOG_DIR"/*.pid; do
    [[ -f "$pid_file" ]] || continue
    name=$(basename "$pid_file" .pid)
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      echo "  Stopping $name (pid $pid)"
      kill "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  done
fi

for port in 8081 8082 8083 8084 8080 5173; do
  pids=$(lsof -ti ":$port" -sTCP:LISTEN 2>/dev/null || true)
  if [[ -n "$pids" ]]; then
    echo "  Freeing port $port"
    kill $pids 2>/dev/null || true
  fi
done

sleep 1
echo "Done. Docker (Postgres/Redis) is still running — use: docker compose down"
