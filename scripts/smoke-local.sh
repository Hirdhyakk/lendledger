#!/usr/bin/env bash
set -euo pipefail
API="${API:-http://localhost:8080/api}"

echo "Testing $API ..."

code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "$API/auth/login" -X POST \
  -H 'Content-Type: application/json' \
  -d '{"email":"x","password":"x"}' 2>/dev/null || echo "000")
if [[ "$code" == "000" ]]; then
  echo ""
  echo "ERROR: Cannot reach gateway at $API"
  echo "  1) ./scripts/stop-gateway.sh  then  ./scripts/run-gateway.sh"
  echo "  2) Start auth, loan, payment, notification (see QUICKSTART.sh)"
  echo "  3) Wait for 'Started ...Application' in each terminal"
  exit 1
fi
if [[ "$code" == "500" || "$code" == "502" || "$code" == "503" ]]; then
  echo ""
  echo "ERROR: Gateway on :8080 returned HTTP $code."
  echo "  Run: ./scripts/check-local.sh"
  echo "  Usually: ./scripts/start-infra.sh  + 4 backend terminals  +  gateway"
  exit 1
fi

login=$(curl -sf -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@lendledger.local","password":"password"}')
token=$(echo "$login" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

echo "OK login"
curl -sf "$API/auth/me" -H "Authorization: Bearer $token" | python3 -m json.tool | head -20

echo "OK me"
curl -sf "$API/admin/dashboard/stats" -H "Authorization: Bearer $token" | python3 -m json.tool

echo "OK dashboard"
echo "Smoke test passed."
