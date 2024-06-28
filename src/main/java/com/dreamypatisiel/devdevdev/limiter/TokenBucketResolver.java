package com.dreamypatisiel.devdevdev.limiter;

import com.dreamypatisiel.devdevdev.limiter.exception.LimiterException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBucketResolver {

    private static final int MIN_CONSUME_TOKENS = 1;
    public static final String TOO_MANY_REQUEST_MESSAGE = "너무 많은 요청을 했습니다. 잠시 후 다시 시도해 주세요.";

    private final BucketConfiguration bucketConfiguration;
    private final LettuceBasedProxyManager lettuceBasedProxyManager;

    public void checkBucketCounter(String key) {
        Bucket bucket = bucket(key);
        // 버킷의 토큰을 소비할 수 없으면
        if (!bucket.tryConsume(MIN_CONSUME_TOKENS)) {
            throw new LimiterException(TOO_MANY_REQUEST_MESSAGE);
        }
    }

    private Bucket bucket(String key) {
        return lettuceBasedProxyManager.builder()
                .build(key.getBytes(), bucketConfiguration);
    }
}
