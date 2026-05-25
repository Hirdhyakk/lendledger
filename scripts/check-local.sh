#!/usr/bin/env bash
# Quick readiness check before smoke-local.sh
set -euo pipefail

ok=0
fail=0

check() {
  local name="$1"
  local url="$2"
  if curl -sf --connect-timeout 2 "$url" >/dev/null 2>&1; then
    echo "OK   $name"
    ok=$((ok + 1))
  else
    echo "FAIL $name  ($url)"
    fail=$((fail + 1))
  fi
}

infra_check() {
  local name="$1"
  if nc -z localhost "$2" 2>/dev/null; then
    echo "OK   $name (localhost:$2)"
    ok=$((ok + 1))
  else
    echo "FAIL $name (localhost:$2 not accepting connections)"
    fail=$((fail + 1))
  fi
}

echo "=== Infrastructure ==="
if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
  echo "FAIL Docker — open Docker Desktop, then: ./scripts/start-infra.sh"
  fail=$((fail + 1))
else
  COMPOSE_FILE="$(dirname "$0")/../docker-compose.yml"
  if docker compose -f "$COMPOSE_FILE" ps --status running 2>/dev/null | grep -q postgres; then
    echo "OK   Docker Compose containers running"
    ok=$((ok + 1))
  else
    echo "FAIL Docker Compose — run: ./scripts/start-infra.sh"
    fail=$((fail + 1))
  fi
fi

if command -v nc >/dev/null 2>&1; then
  infra_check "Postgres" 5432
  infra_check "Redis" 6379
else
  echo "WARN nc not installed — skipping port checks"
fi

echo ""
echo "=== Services ==="
check "auth-service         :8081" "http://localhost:8081/actuator/health"
check "loan-service         :8082" "http://localhost:8082/actuator/health"
check "payment-service      :8083" "http://localhost:8083/actuator/health"
check "notification-service :8084" "http://localhost:8084/actuator/health"
check "api-gateway          :8080" "http://localhost:8080/actuator/health"

echo ""
if [[ $fail -eq 0 ]]; then
  echo "All checks passed. Run: ./scripts/smoke-local.sh"
  exit 0
fi
if [[ $fail -gt 0 ]]; then
  echo ""
  echo "Recovery order:"
  echo "  1) Docker Desktop ON  →  ./scripts/start-infra.sh"
  echo "  2) One terminal each  →  run-auth, run-loan, run-payment, run-notification, run-gateway"
  echo "  3) ./scripts/check-local.sh  →  ./scripts/smoke-local.sh"
fi
echo "$fail check(s) failed."
exit 1
