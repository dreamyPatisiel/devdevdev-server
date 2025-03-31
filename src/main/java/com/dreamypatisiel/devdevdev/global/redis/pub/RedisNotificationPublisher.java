package com.dreamypatisiel.devdevdev.global.redis.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisNotificationPublisher implements NotificationPublisher {

    private final RedisTemplate<?, ?> redisTemplate;

    @Override
    public <T> void publish(String channel, T message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
