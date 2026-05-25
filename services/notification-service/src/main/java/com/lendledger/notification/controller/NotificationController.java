package com.lendledger.notification.controller;

import com.lendledger.notification.domain.NotificationLogEntity;
import com.lendledger.notification.repository.NotificationLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {
    private final NotificationLogRepository repository;

    public NotificationController(NotificationLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/notifications/logs")
    public List<NotificationLogEntity> logs() {
        return repository.findAll();
    }
}
