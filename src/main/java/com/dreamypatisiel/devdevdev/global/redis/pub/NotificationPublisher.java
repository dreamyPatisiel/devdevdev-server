package com.dreamypatisiel.devdevdev.global.redis.pub;

public interface NotificationPublisher {
    <T> void publish(String channel, T message);
}
