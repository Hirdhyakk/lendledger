package com.lendledger.loan.repository;

import com.lendledger.loan.domain.LoanEntity;
import com.lendledger.loan.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<LoanEntity, UUID> {
    List<LoanEntity> findByBorrowerId(UUID borrowerId);
    List<LoanEntity> findByStatus(LoanStatus status);
}
