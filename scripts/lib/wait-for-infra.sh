#!/usr/bin/env bash
# Ensure Postgres (5432) and Redis (6379) accept connections before starting a service.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PG_PORT="${DB_PORT:-5432}"
REDIS_PORT="${REDIS_PORT:-6379}"

port_open() {
  nc -z localhost "$1" 2>/dev/null
}

wait_port() {
  local name="$1"
  local port="$2"
  local tries="${3:-30}"
  local i
  for ((i = 1; i <= tries; i++)); do
    if port_open "$port"; then
      return 0
    fi
    sleep 1
  done
  return 1
}

if ! command -v nc >/dev/null 2>&1; then
  echo "WARN: 'nc' not found; skipping port checks."
  exit 0
fi

if port_open "$PG_PORT" && port_open "$REDIS_PORT"; then
  exit 0
fi

echo "Postgres (:$PG_PORT) or Redis (:$REDIS_PORT) is not reachable."

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  echo "Starting infra via Docker Compose..."
  "$ROOT/scripts/start-infra.sh"
else
  echo ""
  echo "ERROR: Docker is not running."
  echo "  1) Open Docker Desktop and wait until it is ready"
  echo "  2) cd lendledger && ./scripts/start-infra.sh"
  echo "  3) Re-run your service script"
  exit 1
fi

echo "Waiting for Postgres on :$PG_PORT..."
if ! wait_port "Postgres" "$PG_PORT" 30; then
  echo "ERROR: Postgres did not open port $PG_PORT in time."
  exit 1
fi

echo "Waiting for Redis on :$REDIS_PORT..."
if ! wait_port "Redis" "$REDIS_PORT" 15; then
  echo "ERROR: Redis did not open port $REDIS_PORT in time."
  exit 1
fi

echo "Infra ready."
