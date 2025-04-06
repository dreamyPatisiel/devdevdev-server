package com.dreamypatisiel.devdevdev.redis.pub;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationPublisher {

    private final RedisTemplate<?, ?> redisTemplate;

    @Override
    public <T> void publish(NotificationType channel, T message) {
        redisTemplate.convertAndSend(channel.name(), message);
    }
}
