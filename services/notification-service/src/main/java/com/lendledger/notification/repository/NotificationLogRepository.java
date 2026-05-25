package com.lendledger.notification.repository;

import com.lendledger.notification.domain.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {}
