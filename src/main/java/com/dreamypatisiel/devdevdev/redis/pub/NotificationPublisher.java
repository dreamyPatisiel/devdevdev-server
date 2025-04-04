package com.dreamypatisiel.devdevdev.redis.pub;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;

public interface NotificationPublisher {
    <T> void publish(NotificationType channel, T message);
}
