#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if docker info >/dev/null 2>&1; then
  echo "==> Starting PostgreSQL + Redis via Docker Compose..."
  docker compose up -d postgres redis
  echo "Waiting for Postgres..."
  for i in {1..30}; do
    if docker compose exec -T postgres pg_isready -U lendledger >/dev/null 2>&1; then
      echo "Postgres is ready."
      break
    fi
    sleep 1
    if [[ $i -eq 30 ]]; then
      echo "Postgres did not become ready in time."
      exit 1
    fi
  done
  echo "Redis + Postgres running (ports 5432, 6379)."
else
  echo "ERROR: Docker is not running."
  echo ""
  echo "Option A — Start Docker Desktop, then run this script again."
  echo ""
  echo "Option B — Install locally (macOS Homebrew):"
  echo "  brew install postgresql@16 redis"
  echo "  brew services start postgresql@16"
  echo "  brew services start redis"
  echo "  createdb lendledger  # if needed"
  echo "  psql -d postgres -c \"CREATE USER lendledger WITH PASSWORD 'lendledger';\" || true"
  echo "  psql -d postgres -c \"CREATE DATABASE lendledger OWNER lendledger;\" || true"
  echo "  psql -d lendledger -f docker/init-db.sql"
  echo ""
  exit 1
fi
