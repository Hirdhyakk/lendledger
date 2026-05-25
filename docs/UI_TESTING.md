# LendLedger — UI testing guide

Test the full loan lifecycle in the browser. Allow **~2 minutes** after starting all services before the first login (Flyway + seed data).

## Before you start

### One command (recommended)

```bash
cd lendledger
./run.sh
```

Open http://localhost:5173 — login `admin@lendledger.local` / `password`.  
Stop everything: `./scripts/stop-all.sh`

### Manual setup (separate terminals)

1. **Docker Desktop** must be running (PostgreSQL + Redis).
2. Run once from `lendledger/`:
   ```bash
   chmod +x scripts/*.sh
   ./scripts/bootstrap.sh
   ./scripts/start-infra.sh
   ```
3. Start **5 backend terminals** (order matters for first run):

   | # | Command | Ready when you see |
   |---|---------|-------------------|
   | 1 | `./scripts/run-auth.sh` | `Started AuthServiceApplication` |
   | 2 | `./scripts/run-loan.sh` | `Started LoanServiceApplication` |
   | 3 | `./scripts/run-payment.sh` | `Started PaymentServiceApplication` |
   | 4 | `./scripts/run-notification.sh` | `Started NotificationServiceApplication` |
   | 5 | `./scripts/run-gateway.sh` | `Started ApiGatewayApplication` |

4. Start frontend:
   ```bash
   ./scripts/run-frontend.sh
   ```
5. Open **http://localhost:5173**

6. Optional API check:
   ```bash
   ./scripts/smoke-local.sh
   ```

---

## Demo accounts

| Email | Password | Role |
|-------|----------|------|
| admin@lendledger.local | password | ADMIN |
| borrower1@lendledger.local | password | BORROWER |

---

## Flow A — Admin: full loan lifecycle

### A1. Login as admin

1. Go to `/login`
2. Email: `admin@lendledger.local`, Password: `password`
3. You should land on **Admin Dashboard** with KPI cards (active loans, overdue, outstanding).

### A2. Create a borrower

1. **Borrowers** in the nav
2. Fill: email `newborrower@test.com`, full name, optional phone/address/PAN
3. Click **Create borrower**
4. Confirm the row appears in the table

### A3. Create and approve a loan

1. **Loans** in the nav
2. Select the new borrower, principal e.g. `50000`, rate `12`, tenure `6` months
3. **Create** → status **PENDING**
4. Click **Approve** on that row → status **APPROVED** (EMI schedule generated in DB)

### A4. Disburse loan

1. Click **Disburse** → status **ACTIVE**
2. This calls payment-service (ledger DISBURSE entry) and publishes a Redis event

### A5. View EMI schedule

1. Click the loan id link → schedule table with 6 rows, status **DUE**

### A6. Reports

1. **Reports** in the nav
2. Set date range → see **Collected** total (0 until borrower pays)
3. **Overdue** table (empty until due dates pass)
4. **Export CSV** for overdue list

---

## Flow B — Borrower: pay EMI

Use the borrower you created, or `borrower1@lendledger.local` / `password` on an **ACTIVE** loan (create one via Flow A first).

### B1. Login as borrower

1. Logout → login as borrower email / `password`
2. **My Loans** dashboard lists active loans

### B2. Pay EMI

1. **Schedule & Pay** on a loan
2. Enter amount (e.g. first EMI amount from schedule), payment ref e.g. `MOCK-UPI-001`
3. **Pay EMI** — idempotency key is sent automatically
4. Refresh schedule → first EMI should show **PAID** or **PARTIAL**

### B3. Statement

1. **Statement** link
2. See **DISBURSE** and **REPAYMENT** ledger rows + repayment list

### B4. Idempotency (optional)

1. Pay again with the **same** amount quickly — each click uses a **new** idempotency key (normal success)
2. To test duplicate protection, use API tools with the **same** `Idempotency-Key` header twice (see `docs/API.md`)

---

## Flow C — Admin after payment

1. Login as admin again
2. **Reports** → collections should include today’s repayment
3. **Dashboard** → outstanding may decrease

---

## Troubleshooting UI

| Symptom | Fix |
|---------|-----|
| Login fails / network error | Gateway not running on 8080; run `./scripts/run-gateway.sh` last |
| 401 on all pages | Clear `sessionStorage`, login again |
| Empty borrowers/loans | Loan/auth service down; check terminals |
| Disburse fails | Payment service must be up **before** disburse; loan-service logs show connection errors |
| CORS error | Use Vite dev server (5173), not opening `index.html` directly |
| Frontend works, API 502 | Start services in order; wait for Flyway migrations |

---

## Quick API test (no UI)

```bash
./scripts/smoke-local.sh
```

---

## Run automated tests

```bash
cd lendledger
mvn test
```

Expected: **EmiCalculatorTest** (3), **JwtTokenProviderTest** (1), **PaymentServiceDisburseTest** (1) — all pass.
