package com.dreamypatisiel.devdevdev.global.redis.config;

import com.dreamypatisiel.devdevdev.global.redis.sub.RedisSubscriber;
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
    private final RedisSubscriber redisSubscriber;

    @PostConstruct
    public void init() {
        redisMessageListenerContainer.addMessageListener(redisSubscriber, new PatternTopic(TECH_ARTICLE_CHANNEL));
    }
}
