package com.lendledger.payment.repository;

import com.lendledger.payment.domain.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    List<LedgerEntryEntity> findByLoanIdOrderByCreatedAtAsc(UUID loanId);
}
