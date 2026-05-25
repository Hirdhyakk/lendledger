# Architecture

## Services

| Service | Port (local) | Schema | Responsibility |
|---------|--------------|--------|----------------|
| api-gateway | 8080 | — | Routing, JWT, CORS, login rate limit |
| auth-service | 8081 | auth | Users, JWT, refresh tokens |
| loan-service | 8082 | loan | Borrowers, loans, EMI, overdue job |
| payment-service | 8083 | payment | Ledger, repayments, idempotency |
| notification-service | 8084 | notification | Redis event consumer, mock notifications |

## Communication

- **Sync:** RestClient + `X-Internal-Key` for service-to-service
- **Async:** Redis pub/sub (`loan.disbursed`, `payment.received`, `loan.overdue`)

## Money conventions

- **DISBURSE** ledger: positive amount (principal out)
- **REPAYMENT** ledger: negative amount (debt reduced)
- EMI: reducing balance, last installment adjusted for rounding

## Security

- Gateway validates JWT, forwards `X-User-Id`, `X-Role`
- Internal APIs require `X-Internal-Key`
- Repayments require `Idempotency-Key`
