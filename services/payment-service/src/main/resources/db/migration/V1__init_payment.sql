CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    entry_type VARCHAR(32) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repayments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    payment_ref VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repayment_allocations (
    id UUID PRIMARY KEY,
    repayment_id UUID NOT NULL REFERENCES repayments(id),
    emi_id UUID NOT NULL,
    allocated_amount DECIMAL(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    response_body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ledger_loan ON ledger_entries(loan_id);
CREATE INDEX idx_repayments_loan ON repayments(loan_id);
CREATE INDEX idx_repayments_created ON repayments(created_at);
