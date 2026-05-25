package com.lendledger.loan.dto;

import com.lendledger.loan.domain.EmiStatus;
import com.lendledger.loan.domain.LoanStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class LoanDtos {
    private LoanDtos() {}

    public record CreateBorrowerRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String fullName,
            String phone,
            String address,
            String panMasked
    ) {}

    public record BorrowerResponse(UUID id, UUID userId, String fullName, String email, String address, String panMasked, String status) {}

    public record CreateLoanRequest(
            @NotNull UUID borrowerId,
            @NotNull @DecimalMin("0.01") BigDecimal principal,
            @NotNull @DecimalMin("0") BigDecimal annualRate,
            @Min(1) int tenureMonths
    ) {}

    public record LoanResponse(UUID id, UUID borrowerId, BigDecimal principal, BigDecimal annualRate, int tenureMonths,
                               LoanStatus status, BigDecimal outstandingPrincipal, Instant disbursedAt, Instant createdAt) {}

    public record EmiResponse(UUID id, int installmentNo, LocalDate dueDate, BigDecimal emiAmount,
                              BigDecimal principalComponent, BigDecimal interestComponent,
                              BigDecimal paidAmount, EmiStatus status) {}

    public record DashboardStats(long activeLoans, long overdueEmis, BigDecimal totalOutstanding) {}

    public record OverdueReportItem(UUID loanId, UUID emiId, int installmentNo, LocalDate dueDate,
                                    BigDecimal emiAmount, BigDecimal paidAmount, String borrowerName) {}

    public record AllocationItem(UUID emiId, BigDecimal allocatedAmount) {}

    public record ApplyAllocationRequest(List<AllocationItem> allocations, BigDecimal totalPrincipalReduced) {}

    public record InternalLoanResponse(UUID id, UUID borrowerId, UUID borrowerUserId, LoanStatus status,
                                     BigDecimal outstandingPrincipal, BigDecimal principal) {}
}
