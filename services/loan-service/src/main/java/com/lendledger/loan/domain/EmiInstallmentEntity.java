package com.lendledger.loan.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emi_installments")
public class EmiInstallmentEntity {
    @Id
    private UUID id;
    @Column(name = "loan_id", nullable = false)
    private UUID loanId;
    @Column(name = "installment_no", nullable = false)
    private int installmentNo;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "emi_amount", nullable = false)
    private BigDecimal emiAmount;
    @Column(name = "principal_component", nullable = false)
    private BigDecimal principalComponent;
    @Column(name = "interest_component", nullable = false)
    private BigDecimal interestComponent;
    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiStatus status = EmiStatus.DUE;

    @PrePersist
    void prePersist() { if (id == null) id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public int getInstallmentNo() { return installmentNo; }
    public void setInstallmentNo(int installmentNo) { this.installmentNo = installmentNo; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    public BigDecimal getPrincipalComponent() { return principalComponent; }
    public void setPrincipalComponent(BigDecimal principalComponent) { this.principalComponent = principalComponent; }
    public BigDecimal getInterestComponent() { return interestComponent; }
    public void setInterestComponent(BigDecimal interestComponent) { this.interestComponent = interestComponent; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public EmiStatus getStatus() { return status; }
    public void setStatus(EmiStatus status) { this.status = status; }
}
