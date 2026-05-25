package com.lendledger.loan.repository;

import com.lendledger.loan.domain.EmiInstallmentEntity;
import com.lendledger.loan.domain.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmiInstallmentRepository extends JpaRepository<EmiInstallmentEntity, UUID> {
    List<EmiInstallmentEntity> findByLoanIdOrderByInstallmentNo(UUID loanId);

    @Query("SELECT e FROM EmiInstallmentEntity e WHERE e.loanId = :loanId AND e.status IN :statuses ORDER BY e.installmentNo")
    List<EmiInstallmentEntity> findByLoanIdAndStatusIn(UUID loanId, List<EmiStatus> statuses);

    @Query("SELECT e FROM EmiInstallmentEntity e WHERE e.dueDate < :today AND e.status <> 'PAID'")
    List<EmiInstallmentEntity> findOverdueCandidates(LocalDate today);

    long countByLoanIdAndStatusNot(UUID loanId, EmiStatus status);
}
