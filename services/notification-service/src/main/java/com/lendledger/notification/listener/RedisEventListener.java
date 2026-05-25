package com.lendledger.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendledger.common.Constants;
import com.lendledger.notification.service.NotificationService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class RedisEventListener implements MessageListener {
    private final NotificationService notificationService;
    private final ObjectMapper mapper;

    public RedisEventListener(NotificationService notificationService, ObjectMapper mapper) {
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            Map<String, Object> payload = mapper.readValue(message.getBody(), Map.class);
            UUID userId = UUID.fromString(payload.get("userId").toString());
            String template = switch (channel) {
                case Constants.CHANNEL_LOAN_DISBURSED -> "LOAN_DISBURSED";
                case Constants.CHANNEL_PAYMENT_RECEIVED -> "PAYMENT_RECEIVED";
                case Constants.CHANNEL_LOAN_OVERDUE -> "LOAN_OVERDUE";
                default -> "UNKNOWN";
            };
            notificationService.sendMock(userId, template, new String(message.getBody()));
        } catch (Exception e) {
            // log and continue
        }
    }
}
