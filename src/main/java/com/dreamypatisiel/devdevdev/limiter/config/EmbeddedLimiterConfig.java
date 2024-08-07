package com.dreamypatisiel.devdevdev.limiter.config;

import com.dreamypatisiel.devdevdev.limiter.LimiterPlan;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Configuration
@DependsOn({"embeddedRedisServerConfig"}) // 빈 초기화 순서 지정
@Profile({"test", "local"})
public class EmbeddedLimiterConfig {

    private static final int MAX_SECONDS = 10;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${bucket.plan}")
    private String bucketPlan;

    @Bean
    public RedisClient redisClient() {

        RedisURI redisURI = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean
    public LettuceBasedProxyManager lettuceBasedProxyManager() {

        ExpirationAfterWriteStrategy expirationAfterWriteStrategy = ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                Duration.ofSeconds(MAX_SECONDS));

        return LettuceBasedProxyManager.builderFor(redisClient())
                .withExpirationStrategy(expirationAfterWriteStrategy)
                .build();
    }

    @Bean
    public BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(LimiterPlan.resolvePlan(bucketPlan))
                .build();
    }
}
