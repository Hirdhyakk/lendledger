#!/usr/bin/env bash
# End-to-end API test: borrower → loan → approve → disburse → repay → verify
set -euo pipefail
API="${API:-http://localhost:8080/api}"
RESET="${RESET:-false}"
EMAIL="e2e-$(date +%s)@test.com"
PRINCIPAL=50000
RATE=12
MONTHS=6

pass=0
fail=0

assert() {
  local name="$1"
  local cond="$2"
  if eval "$cond"; then
    echo "  PASS: $name"
    pass=$((pass + 1))
  else
    echo "  FAIL: $name"
    fail=$((fail + 1))
  fi
}

json() { python3 -c "import sys,json; d=json.load(sys.stdin); $1"; }

echo "=== LendLedger E2E API test ==="
echo "API: $API"

if [[ "$RESET" == "true" ]]; then
  echo "==> Resetting database..."
  "$(dirname "$0")/reset-data.sh"
  echo "==> Restarting auth-service to re-seed admin (5s)..."
  lsof -ti :8081 -sTCP:LISTEN 2>/dev/null | xargs kill 2>/dev/null || true
  sleep 2
  ROOT="$(cd "$(dirname "$0")/.." && pwd)"
  java -jar "$ROOT/services/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar" >/tmp/lendledger-auth-e2e.log 2>&1 &
  AUTH_PID=$!
  for i in {1..30}; do
    curl -sf http://localhost:8081/actuator/health >/dev/null 2>&1 && break
    sleep 1
  done
  if ! curl -sf http://localhost:8081/actuator/health >/dev/null 2>&1; then
    echo "FAIL: auth-service did not start after reset"
    exit 1
  fi
  echo "  auth-service PID $AUTH_PID"
fi

echo ""
echo "--- 1) Admin login ---"
LOGIN=$(curl -sf -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"admin@lendledger.local","password":"password"}')
TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
ROLE=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('role',''))" 2>/dev/null || echo "")
assert "admin login returns token" "[[ -n '$TOKEN' ]]"

AUTH="Authorization: Bearer $TOKEN"

echo ""
echo "--- 2) Create borrower ---"
BORROWER=$(curl -sf -X POST "$API/admin/borrowers" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"password\",\"fullName\":\"E2E Test\",\"phone\":\"9000000001\"}")
BORROWER_ID=$(echo "$BORROWER" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
assert "borrower created" "[[ -n '$BORROWER_ID' ]]"

echo ""
echo "--- 3) Create loan (PENDING) ---"
LOAN=$(curl -sf -X POST "$API/admin/loans" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"borrowerId\":\"$BORROWER_ID\",\"principal\":$PRINCIPAL,\"annualRate\":$RATE,\"tenureMonths\":$MONTHS}")
LOAN_ID=$(echo "$LOAN" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
LOAN_STATUS=$(echo "$LOAN" | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])")
assert "loan status PENDING" "[[ '$LOAN_STATUS' == 'PENDING' ]]"

echo ""
echo "--- 4) Approve loan ---"
curl -sf -X POST "$API/admin/loans/$LOAN_ID/approve" -H "$AUTH" >/dev/null
SCHEDULE=$(curl -sf "$API/admin/loans/$LOAN_ID/schedule" -H "$AUTH")
SCHED_COUNT=$(echo "$SCHEDULE" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
assert "EMI schedule has 6 rows" "[[ '$SCHED_COUNT' == '6' ]]"

echo ""
echo "--- 5) Disburse loan (ACTIVE) ---"
curl -sf -X POST "$API/admin/loans/$LOAN_ID/disburse" -H "$AUTH" >/dev/null
LOANS=$(curl -sf "$API/admin/loans" -H "$AUTH")
ACTIVE=$(echo "$LOANS" | python3 -c "import sys,json; l=json.load(sys.stdin); print(next(x['status'] for x in l if x['id']=='$LOAN_ID'))")
assert "loan status ACTIVE" "[[ '$ACTIVE' == 'ACTIVE' ]]"

STATS=$(curl -sf "$API/admin/dashboard/stats" -H "$AUTH")
ACTIVE_LOANS=$(echo "$STATS" | python3 -c "import sys,json; print(json.load(sys.stdin)['activeLoans'])")
OUTSTANDING_TOTAL=$(echo "$STATS" | python3 -c "import sys,json; print(json.load(sys.stdin)['totalOutstanding'])")
assert "dashboard activeLoans >= 1" "[[ $ACTIVE_LOANS -ge 1 ]]"

echo ""
echo "--- 6) Borrower login ---"
BLOGIN=$(curl -sf -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"password\"}")
BTOKEN=$(echo "$BLOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
BAUTH="Authorization: Bearer $BTOKEN"
MY_LOANS=$(curl -sf "$API/borrower/loans" -H "$BAUTH")
MY_COUNT=$(echo "$MY_LOANS" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
LOAN_OUT=$(echo "$MY_LOANS" | python3 -c "import sys,json; l=json.load(sys.stdin); print(l[0]['outstandingPrincipal'])")
assert "borrower sees 1 active loan" "[[ '$MY_COUNT' == '1' ]]"
assert "loan outstanding equals principal after disburse" "python3 -c \"import sys; sys.exit(0 if abs(float('$LOAN_OUT')-$PRINCIPAL)<1 else 1)\""
OUTSTANDING="$LOAN_OUT"

BSCHED=$(curl -sf "$API/borrower/loans/$LOAN_ID/schedule" -H "$BAUTH")
EMI1=$(echo "$BSCHED" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['emiAmount'])")
EMI1_STATUS=$(echo "$BSCHED" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['status'])")
assert "first EMI status DUE" "[[ '$EMI1_STATUS' == 'DUE' ]]"

echo ""
echo "--- 7) Pay full first EMI ---"
IDEM=$(uuidgen 2>/dev/null || python3 -c "import uuid; print(uuid.uuid4())")
REPAY=$(curl -sf -X POST "$API/borrower/loans/$LOAN_ID/repay" -H "$BAUTH" -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $IDEM" \
  -d "{\"amount\":$EMI1,\"paymentRef\":\"MOCK-E2E-001\"}")
BSCHED2=$(curl -sf "$API/borrower/loans/$LOAN_ID/schedule" -H "$BAUTH")
EMI1_STATUS2=$(echo "$BSCHED2" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['status'])")
PAID1=$(echo "$BSCHED2" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['paidAmount'])")
assert "first EMI status PAID after full payment" "[[ '$EMI1_STATUS2' == 'PAID' ]]"

echo ""
echo "--- 8) Reports & statement ---"
TODAY=$(date +%Y-%m-%d)
COLL=$(curl -sf "$API/admin/reports/collections?from=$TODAY&to=$TODAY" -H "$AUTH")
COLLECTED=$(echo "$COLL" | python3 -c "import sys,json; print(json.load(sys.stdin)['totalCollected'])")
assert "collections includes EMI payment" "python3 -c \"import sys; sys.exit(0 if float('$COLLECTED')>=float('$EMI1')-0.01 else 1)\""

STMT=$(curl -sf "$API/borrower/loans/$LOAN_ID/statement" -H "$BAUTH")
LEDGER_LEN=$(echo "$STMT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('ledger',[])))")
REPAY_LEN=$(echo "$STMT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('repayments',[])))")
assert "statement has DISBURSE ledger entry" "[[ $LEDGER_LEN -ge 1 ]]"
assert "statement has repayment row" "[[ $REPAY_LEN -ge 1 ]]"

MY_LOANS2=$(curl -sf "$API/borrower/loans" -H "$BAUTH")
LOAN_OUT2=$(echo "$MY_LOANS2" | python3 -c "import sys,json; l=json.load(sys.stdin); print(l[0]['outstandingPrincipal'])")
assert "loan outstanding decreased after payment" "python3 -c \"import sys; sys.exit(0 if float('$LOAN_OUT2')<float('$OUTSTANDING') else 1)\""

echo ""
echo "--- 9) Partial payment (8600 vs EMI 8627.42) ---"
# Second loan on same borrower for isolated partial test
LOAN2=$(curl -sf -X POST "$API/admin/loans" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"borrowerId\":\"$BORROWER_ID\",\"principal\":10000,\"annualRate\":12,\"tenureMonths\":3}")
LOAN2_ID=$(echo "$LOAN2" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
curl -sf -X POST "$API/admin/loans/$LOAN2_ID/approve" -H "$AUTH" >/dev/null
curl -sf -X POST "$API/admin/loans/$LOAN2_ID/disburse" -H "$AUTH" >/dev/null
BSCHED_P=$(curl -sf "$API/borrower/loans/$LOAN2_ID/schedule" -H "$BAUTH")
EMI_P=$(echo "$BSCHED_P" | python3 -c "import sys,json; print(json.load(sys.stdin)[0]['emiAmount'])")
PARTIAL_AMT=$(python3 -c "print(round(float('$EMI_P') - 27.42, 2))")
curl -sf -X POST "$API/borrower/loans/$LOAN2_ID/repay" -H "$BAUTH" -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $(uuidgen 2>/dev/null || python3 -c 'import uuid;print(uuid.uuid4())')" \
  -d "{\"amount\":$PARTIAL_AMT,\"paymentRef\":\"MOCK-PARTIAL\"}" >/dev/null
BSCHED_P2=$(curl -sf "$API/borrower/loans/$LOAN2_ID/schedule" -H "$BAUTH")
PARTIAL_STATUS=$(echo "$BSCHED_P2" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['status'])")
PARTIAL_PAID=$(echo "$BSCHED_P2" | python3 -c "import sys,json; s=json.load(sys.stdin); print(s[0]['paidAmount'])")
assert "under-payment yields PARTIAL" "[[ '$PARTIAL_STATUS' == 'PARTIAL' ]]"
assert "partial paid amount matches payment" "python3 -c \"import sys; sys.exit(0 if abs(float('$PARTIAL_PAID')-float('$PARTIAL_AMT'))<0.01 else 1)\""

echo ""
echo "=== Results: $pass passed, $fail failed ==="
echo "Test borrower: $EMAIL / password"
echo "Loan ID: $LOAN_ID"
echo "EMI1 amount: $EMI1 | paid: $PAID1 | status: $EMI1_STATUS2"
echo "Outstanding before pay: $OUTSTANDING | after: $LOAN_OUT2 | collected today: $COLLECTED"
echo "Partial test loan $LOAN2_ID: EMI=$EMI_P paid $PARTIAL_AMT → status=$PARTIAL_STATUS (paid=$PARTIAL_PAID)"

if [[ -n "${AUTH_PID:-}" ]]; then
  kill "$AUTH_PID" 2>/dev/null || true
  echo "(Stopped temporary auth-service — restart ./scripts/run-auth.sh in your terminal)"
fi

[[ $fail -eq 0 ]]
