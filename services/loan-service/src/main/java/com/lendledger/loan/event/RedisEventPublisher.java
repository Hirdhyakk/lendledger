package com.lendledger.loan.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendledger.common.Constants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisEventPublisher {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisEventPublisher(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public void publish(String channel, Map<String, Object> payload) {
        try {
            redis.convertAndSend(channel, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish event", e);
        }
    }

    public void loanDisbursed(Map<String, Object> payload) {
        publish(Constants.CHANNEL_LOAN_DISBURSED, payload);
    }

    public void loanOverdue(Map<String, Object> payload) {
        publish(Constants.CHANNEL_LOAN_OVERDUE, payload);
    }
}
