package com.lendledger.loan.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class LoanEntity {
    @Id
    private UUID id;
    @Column(name = "borrower_id", nullable = false)
    private UUID borrowerId;
    @Column(nullable = false)
    private BigDecimal principal;
    @Column(name = "annual_rate", nullable = false)
    private BigDecimal annualRate;
    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;
    @Column(name = "disbursed_at")
    private Instant disbursedAt;
    @Column(name = "outstanding_principal", nullable = false)
    private BigDecimal outstandingPrincipal;
    @Column(name = "created_by_admin_id", nullable = false)
    private UUID createdByAdminId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public UUID getBorrowerId() { return borrowerId; }
    public void setBorrowerId(UUID borrowerId) { this.borrowerId = borrowerId; }
    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }
    public BigDecimal getAnnualRate() { return annualRate; }
    public void setAnnualRate(BigDecimal annualRate) { this.annualRate = annualRate; }
    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public Instant getDisbursedAt() { return disbursedAt; }
    public void setDisbursedAt(Instant disbursedAt) { this.disbursedAt = disbursedAt; }
    public BigDecimal getOutstandingPrincipal() { return outstandingPrincipal; }
    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) { this.outstandingPrincipal = outstandingPrincipal; }
    public UUID getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(UUID createdByAdminId) { this.createdByAdminId = createdByAdminId; }
    public Instant getCreatedAt() { return createdAt; }
}
