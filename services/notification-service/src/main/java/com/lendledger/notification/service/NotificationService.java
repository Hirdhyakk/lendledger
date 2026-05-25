package com.lendledger.notification.service;

import com.lendledger.notification.domain.NotificationLogEntity;
import com.lendledger.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationLogRepository repository;

    public NotificationService(NotificationLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void sendMock(UUID userId, String template, String payloadJson) {
        NotificationLogEntity entity = new NotificationLogEntity();
        entity.setUserId(userId);
        entity.setChannel("MOCK_EMAIL");
        entity.setTemplate(template);
        entity.setPayloadJson(payloadJson);
        entity.setStatus("SENT");
        repository.save(entity);
        log.info("MOCK notification template={} userId={} payload={}", template, userId, payloadJson);
    }
}
