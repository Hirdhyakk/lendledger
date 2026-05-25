package com.lendledger.notification.config;

import com.lendledger.common.Constants;
import com.lendledger.notification.listener.RedisEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory, RedisEventListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listener, new PatternTopic(Constants.CHANNEL_LOAN_DISBURSED));
        container.addMessageListener(listener, new PatternTopic(Constants.CHANNEL_PAYMENT_RECEIVED));
        container.addMessageListener(listener, new PatternTopic(Constants.CHANNEL_LOAN_OVERDUE));
        return container;
    }
}
