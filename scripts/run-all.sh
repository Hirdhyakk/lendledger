#!/usr/bin/env bash
# Start infra + all backend services + frontend in one command.
# Press Ctrl+C to stop everything, or run: ./scripts/stop-all.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
chmod +x scripts/*.sh scripts/lib/*.sh 2>/dev/null || true

SKIP_BUILD=false
NO_FRONTEND=false
FOLLOW_LOGS=false

for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=true ;;
    --no-frontend) NO_FRONTEND=true ;;
    --follow-logs) FOLLOW_LOGS=true ;;
    -h|--help)
      echo "Usage: ./scripts/run-all.sh [options]"
      echo ""
      echo "  Starts Docker (Postgres+Redis), all 5 Java services, and Vite frontend."
      echo ""
      echo "Options:"
      echo "  --skip-build     Skip mvn install (use existing JARs)"
      echo "  --no-frontend    Backend only"
      echo "  --follow-logs    Tail combined logs after startup (Ctrl+C stops all)"
      echo ""
      echo "Stop:  Ctrl+C  or  ./scripts/stop-all.sh"
      exit 0
      ;;
    *) echo "Unknown option: $arg (try --help)"; exit 1 ;;
  esac
done

wait_for_url() {
  local name="$1"
  local url="$2"
  local tries="${3:-90}"
  for ((i = 1; i <= tries; i++)); do
    if curl -sf --connect-timeout 2 "$url" >/dev/null 2>&1; then
      echo "  OK   $name"
      return 0
    fi
    sleep 1
  done
  echo "  FAIL $name did not become ready ($url)"
  echo "       Check logs/$(echo "$name" | tr ' ' '-' | tr '[:upper:]' '[:lower:]').log or logs/*.log"
  return 1
}

cleanup() {
  echo ""
  echo "==> Shutting down..."
  "$ROOT/scripts/stop-all.sh"
}
trap cleanup EXIT INT TERM

echo "╔══════════════════════════════════════════════════════════╗"
echo "║              LendLedger — starting full stack            ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Stop any previous run-all processes
"$ROOT/scripts/stop-all.sh" 2>/dev/null || true
mkdir -p logs

if [[ "$SKIP_BUILD" != "true" ]]; then
  echo "==> Building Maven modules (first run may take a minute)..."
  ./scripts/bootstrap.sh
else
  echo "==> Skipping Maven build (--skip-build)"
fi

echo ""
echo "==> Starting Postgres + Redis..."
./scripts/start-infra.sh

echo ""
echo "==> Starting backend services..."
LIB="$ROOT/scripts/lib"
"$LIB/start-jar-bg.sh" services/auth-service auth-service 8081
sleep 2
"$LIB/start-jar-bg.sh" services/loan-service loan-service 8082
sleep 1
"$LIB/start-jar-bg.sh" services/payment-service payment-service 8083
sleep 1
"$LIB/start-jar-bg.sh" services/notification-service notification-service 8084
sleep 1
"$LIB/start-jar-bg.sh" gateway/api-gateway api-gateway 8080

echo ""
echo "==> Waiting for services..."
FAIL=0
wait_for_url "auth-service" "http://localhost:8081/actuator/health" || FAIL=1
wait_for_url "loan-service" "http://localhost:8082/actuator/health" || FAIL=1
wait_for_url "payment-service" "http://localhost:8083/actuator/health" || FAIL=1
wait_for_url "notification-service" "http://localhost:8084/actuator/health" || FAIL=1
wait_for_url "api-gateway" "http://localhost:8080/actuator/health" || FAIL=1

if [[ "$FAIL" -ne 0 ]]; then
  echo ""
  echo "Some services failed to start. Inspect logs/ then re-run."
  exit 1
fi

if [[ "$NO_FRONTEND" != "true" ]]; then
  echo ""
  echo "==> Starting frontend (Vite)..."
  if lsof -ti :5173 -sTCP:LISTEN >/dev/null 2>&1; then
    lsof -ti :5173 -sTCP:LISTEN | xargs kill 2>/dev/null || true
    sleep 1
  fi
  (
    cd "$ROOT/frontend"
    npm install --silent 2>/dev/null || npm install
    nohup npm run dev >>"$ROOT/logs/frontend.log" 2>&1 &
    echo $! >"$ROOT/logs/frontend.pid"
  )
  wait_for_url "frontend" "http://localhost:5173" 30 || FAIL=1
fi

# Don't run cleanup trap on normal success — only on Ctrl+C
trap - EXIT INT TERM

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    All services up                       ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "  UI:       http://localhost:5173"
echo "  API:      http://localhost:8080/api"
echo "  Login:    admin@lendledger.local / password"
echo ""
echo "  Logs:     lendledger/logs/*.log"
echo "  Verify:   ./scripts/check-local.sh && ./scripts/smoke-local.sh"
echo "  Stop:     ./scripts/stop-all.sh   (or Ctrl+C if using --follow-logs)"
echo ""

if [[ "$FOLLOW_LOGS" == "true" ]]; then
  trap cleanup EXIT INT TERM
  echo "Following logs (Ctrl+C stops all services)..."
  tail -f logs/*.log
else
  echo "Services run in the background. Use --follow-logs to stream logs in this terminal."
fi
