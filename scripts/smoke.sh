#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
API="${GATEWAY_URL}/api"

echo "Smoke test against $API"

login=$(curl -sf -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@lendledger.local","password":"password"}')
token=$(echo "$login" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -sf "$API/auth/me" -H "Authorization: Bearer $token" | head -c 200
echo ""
curl -sf "$API/admin/dashboard/stats" -H "Authorization: Bearer $token" | head -c 200
echo ""
echo "Smoke test OK"
