package com.lendledger.common;

public final class Constants {
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_ROLE = "X-Role";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_INTERNAL_KEY = "X-Internal-Key";
    public static final String HEADER_IDEMPOTENCY = "Idempotency-Key";

    public static final String CHANNEL_LOAN_DISBURSED = "loan.disbursed";
    public static final String CHANNEL_PAYMENT_RECEIVED = "payment.received";
    public static final String CHANNEL_LOAN_OVERDUE = "loan.overdue";

    private Constants() {}
}
