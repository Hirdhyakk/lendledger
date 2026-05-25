#!/usr/bin/env bash
# Clear all borrowers, loans, payments, notifications. Re-creates demo logins on next auth-service start.
set -euo pipefail
cd "$(dirname "$0")/.."

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Start Docker Desktop first."
  exit 1
fi

echo "==> Stopping app traffic recommended (Ctrl+C on run-*.sh terminals)."
echo "==> Truncating Postgres data..."
docker compose exec -T postgres psql -U lendledger -d lendledger -f - < docker/reset-data.sql

echo "==> Flushing Redis..."
docker compose exec -T redis redis-cli FLUSHALL >/dev/null

echo ""
echo "Done. All borrowers, loans, ledger rows, and logins removed."
echo ""
echo "Next:"
echo "  1) Restart auth-service (re-seeds admin@lendledger.local + borrower1/2): ./scripts/run-auth.sh"
echo "  2) Restart loan, payment, notification, gateway if they were stopped"
echo "  3) In browser: Logout or clear site data for localhost:5173"
echo "  4) Login as admin@lendledger.local / password — tables should be empty"
