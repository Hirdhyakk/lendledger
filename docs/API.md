# LendLedger API

Base URL: `http://localhost:8080/api` (via gateway)

## Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | Public | Register borrower |
| POST | `/auth/login` | Public | Login → access + refresh tokens |
| POST | `/auth/refresh` | Public | Refresh access token |
| GET | `/auth/me` | Bearer | Current user |

## Admin

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/admin/borrowers` | ADMIN | Create borrower + user |
| GET | `/admin/borrowers` | ADMIN | List borrowers |
| POST | `/admin/loans` | ADMIN | Create loan (PENDING) |
| POST | `/admin/loans/{id}/approve` | ADMIN | Generate EMI schedule |
| POST | `/admin/loans/{id}/disburse` | ADMIN | Disburse → ACTIVE |
| GET | `/admin/loans` | ADMIN | List loans |
| GET | `/admin/loans/{id}/schedule` | ADMIN | EMI schedule |
| GET | `/admin/dashboard/stats` | ADMIN | Dashboard KPIs |
| GET | `/admin/reports/overdue` | ADMIN | Overdue EMIs |
| GET | `/admin/reports/collections?from=&to=` | ADMIN | Collections sum |

## Borrower

| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/borrower/loans` | BORROWER | My loans |
| GET | `/borrower/loans/{id}/schedule` | BORROWER | EMI schedule |
| POST | `/borrower/loans/{id}/repay` | BORROWER | Repay (header `Idempotency-Key`) |
| GET | `/borrower/loans/{id}/statement` | BORROWER | Ledger + repayments |

## Headers

- `Authorization: Bearer <accessToken>`
- `Idempotency-Key: <uuid>` (repay only)

## Error format

```json
{ "error": { "code": "...", "message": "...", "traceId": "..." } }
```
