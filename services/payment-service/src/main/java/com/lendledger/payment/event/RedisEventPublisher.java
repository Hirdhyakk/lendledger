package com.lendledger.payment.event;

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

    public void paymentReceived(Map<String, Object> payload) {
        try {
            redis.convertAndSend(Constants.CHANNEL_PAYMENT_RECEIVED, mapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish payment event", e);
        }
    }
}
