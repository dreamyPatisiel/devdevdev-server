package com.dreamypatisiel.devdevdev.limiter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.limiter.exception.LimiterException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@SpringBootTest
class TokenBucketResolverTest {

    @Autowired
    TokenBucketResolver tokenBucketResolver;

    @Autowired
    RedisTemplate<?, ?> redisTemplate;

    @AfterEach
    void tearDown() {
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        DefaultStringRedisConnection defaultStringRedisConnection = new DefaultStringRedisConnection(redisConnection,
                redisSerializer);

        defaultStringRedisConnection.flushAll();
    }

    @Test
    @DisplayName("처리율 제한 장치의 정책을 준수하면 예외가 발생하지 않는다.")
    void checkBucketCounter() {
        // given
        String key = "sampleKey";

        // when // then
        assertThatCode(() -> tokenBucketResolver.checkBucketCounter(key))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("처리율 제한 장치의 정책을 위반하면 예외가 발생한다.")
    void checkBucketCounterException() {
        // given
        String key = "sampleKey";
        long capacity = LimiterPlan.TEST.getLimit().getCapacity();

        for (long i = 1; i < capacity; i++) {
            tokenBucketResolver.checkBucketCounter(key);
        }
        // when // then
        assertThatThrownBy(() -> tokenBucketResolver.checkBucketCounter(key))
                .isInstanceOf(LimiterException.class)
                .hasMessage(TokenBucketResolver.TOO_MANY_REQUEST_MESSAGE);
    }

    @Test
    @DisplayName("key에 따라서 처리율 제한 장치의 정책이 적용된다.")
    void checkBucketCounterAppliedAccordingToKey() {
        String key1 = "key1";
        String key2 = "key2";
        long capacity = LimiterPlan.TEST.getLimit().getCapacity();

        for (long i = 1; i < capacity; i++) {
            tokenBucketResolver.checkBucketCounter(key1);
        }

        // when // then
        assertAll(
                () -> assertThatThrownBy(() -> tokenBucketResolver.checkBucketCounter(key1))
                        .isInstanceOf(LimiterException.class)
                        .hasMessage(TokenBucketResolver.TOO_MANY_REQUEST_MESSAGE),
                () -> assertThatCode(() -> tokenBucketResolver.checkBucketCounter(key2))
                        .doesNotThrowAnyException()
        );
    }
}