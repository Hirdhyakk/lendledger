package com.lendledger.payment.repository;

import com.lendledger.payment.domain.RepaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepaymentRepository extends JpaRepository<RepaymentEntity, UUID> {
    Optional<RepaymentEntity> findByIdempotencyKey(String key);
    List<RepaymentEntity> findByLoanIdOrderByCreatedAtDesc(UUID loanId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RepaymentEntity r WHERE r.createdAt >= :from AND r.createdAt < :to")
    BigDecimal sumAmountBetween(Instant from, Instant to);
}
