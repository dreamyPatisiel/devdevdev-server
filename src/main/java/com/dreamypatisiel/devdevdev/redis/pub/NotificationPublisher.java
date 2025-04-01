package com.dreamypatisiel.devdevdev.redis.pub;

public interface NotificationPublisher {
    <T> void publish(String channel, T message);
}
