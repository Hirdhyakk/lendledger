package com.lendledger.loan.repository;

import com.lendledger.loan.domain.BorrowerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BorrowerRepository extends JpaRepository<BorrowerEntity, UUID> {
    Optional<BorrowerEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
