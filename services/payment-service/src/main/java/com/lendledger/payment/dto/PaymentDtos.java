package com.lendledger.payment.dto;

import com.lendledger.payment.domain.LedgerEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record DisburseRequest(@NotNull UUID loanId, @NotNull @DecimalMin("0.01") BigDecimal amount, String reference) {}

    public record RepayRequest(@NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String paymentRef) {}

    public record RepaymentResponse(UUID repaymentId, UUID loanId, BigDecimal amount, String status, List<AllocationResult> allocations) {}

    public record AllocationResult(UUID emiId, BigDecimal allocatedAmount) {}

    public record LedgerEntryResponse(UUID id, UUID loanId, LedgerEntryType entryType, BigDecimal amount, String reference, Instant createdAt) {}

    public record StatementResponse(UUID loanId, List<LedgerEntryResponse> ledger, List<RepaymentSummary> repayments) {}

    public record RepaymentSummary(UUID id, BigDecimal amount, String paymentRef, Instant createdAt) {}

    public record CollectionsReport(BigDecimal totalCollected, Instant from, Instant to) {}

    public record AllocationItem(UUID emiId, BigDecimal allocatedAmount) {}

    public record ApplyAllocationRequest(List<AllocationItem> allocations, BigDecimal totalPrincipalReduced) {}
}
