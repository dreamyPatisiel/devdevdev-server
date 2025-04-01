package com.dreamypatisiel.devdevdev.redis.config;

import com.dreamypatisiel.devdevdev.redis.sub.RedisNotificationSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisSubscribeConfig {

    public static final String TECH_ARTICLE_CHANNEL = "tech-article";

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisNotificationSubscriber redisNotificationSubscriber;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(redisNotificationSubscriber,
                new PatternTopic(TECH_ARTICLE_CHANNEL));
    }
}
