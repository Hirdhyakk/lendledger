#!/usr/bin/env bash
# Wake Render free-tier services before using the app (cold start ~30–90s each).
# Usage: ./scripts/wake-prod.sh
# Override URLs: AUTH_URL=... LOAN_URL=... ./scripts/wake-prod.sh
set -euo pipefail

AUTH_URL="${AUTH_URL:-https://lendledger-auth.onrender.com}"
LOAN_URL="${LOAN_URL:-https://lendledger-loan.onrender.com}"
PAYMENT_URL="${PAYMENT_URL:-https://lendledger-payment.onrender.com}"
NOTIFICATION_URL="${NOTIFICATION_URL:-https://lendledger-notification.onrender.com}"
GATEWAY_URL="${GATEWAY_URL:-https://lendledger-gateway.onrender.com}"
TIMEOUT="${TIMEOUT:-120}"

wake_one() {
  local name="$1"
  local base="${2%/}"
  local url="${base}/actuator/health"
  printf "  %-12s " "$name"
  if code=$(curl -sf -o /dev/null -w "%{http_code}" --max-time "$TIMEOUT" "$url" 2>/dev/null); then
    echo "UP (HTTP $code)"
    return 0
  else
    echo "FAILED or timeout (${TIMEOUT}s) — open $url in browser and retry"
    return 1
  fi
}

echo "Waking LendLedger on Render (timeout ${TIMEOUT}s per service)..."
echo ""

failed=0
wake_one "auth" "$AUTH_URL" || failed=$((failed + 1))
wake_one "loan" "$LOAN_URL" || failed=$((failed + 1))
wake_one "payment" "$PAYMENT_URL" || failed=$((failed + 1))
wake_one "notification" "$NOTIFICATION_URL" || failed=$((failed + 1))
wake_one "gateway" "$GATEWAY_URL" || failed=$((failed + 1))

echo ""
if [[ "$failed" -gt 0 ]]; then
  echo "Some services did not respond. Wait 1–2 min and run again, or check Render dashboard."
  exit 1
fi

echo "All services UP. Warming API login via gateway..."
if curl -sf --max-time "$TIMEOUT" -X POST "${GATEWAY_URL%/}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@lendledger.local","password":"password"}' >/dev/null 2>&1; then
  echo "Login endpoint OK — open https://lendledger-gamma.vercel.app"
else
  echo "Health OK but login failed — wait 30s and run this script again."
  exit 1
fi
