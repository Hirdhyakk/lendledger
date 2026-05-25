CREATE TABLE IF NOT EXISTS borrowers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    address TEXT,
    pan_masked VARCHAR(16),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loans (
    id UUID PRIMARY KEY,
    borrower_id UUID NOT NULL REFERENCES borrowers(id),
    principal DECIMAL(19,2) NOT NULL,
    annual_rate DECIMAL(8,4) NOT NULL,
    tenure_months INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    disbursed_at TIMESTAMP,
    outstanding_principal DECIMAL(19,2) NOT NULL,
    created_by_admin_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS emi_installments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL REFERENCES loans(id),
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    emi_amount DECIMAL(19,2) NOT NULL,
    principal_component DECIMAL(19,2) NOT NULL,
    interest_component DECIMAL(19,2) NOT NULL,
    paid_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    UNIQUE(loan_id, installment_no)
);

CREATE INDEX idx_loans_borrower ON loans(borrower_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_emi_loan_status ON emi_installments(loan_id, status);
CREATE INDEX idx_emi_due_date ON emi_installments(due_date);
