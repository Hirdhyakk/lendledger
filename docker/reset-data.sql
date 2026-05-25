-- Wipe all business data (schemas stay; Flyway history kept).
-- Re-seed admin demo users by restarting auth-service after this script.

TRUNCATE TABLE
  payment.repayment_allocations,
  payment.repayments,
  payment.ledger_entries,
  payment.idempotency_keys
RESTART IDENTITY CASCADE;

TRUNCATE TABLE
  loan.emi_installments,
  loan.loans,
  loan.borrowers
RESTART IDENTITY CASCADE;

TRUNCATE TABLE notification.notification_logs RESTART IDENTITY CASCADE;

TRUNCATE TABLE auth.refresh_tokens, auth.users RESTART IDENTITY CASCADE;
