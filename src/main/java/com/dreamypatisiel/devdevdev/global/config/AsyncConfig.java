package com.dreamypatisiel.devdevdev.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    /**
     * Heartbeat 전송용 ThreadPoolTaskExecutor를 생성합니다.
     * <p>
     * 동작 흐름:
     * <ul>
     *   <li>요청이 들어오면 CorePoolSize 내에서 스레드를 생성해 즉시 실행합니다.</li>
     *   <li>CorePoolSize를 초과하면 요청은 Queue에 저장됩니다.</li>
     *   <li>Queue가 꽉 차면 MaxPoolSize까지 스레드를 추가 생성합니다.</li>
     *   <li>MaxPoolSize까지 모두 사용되면 거절 정책이 발동하여 작업이 거절됩니다.</li>
     * </ul>
     *
     * @return heartbeat 전송을 위한 Executor
     * @Author: 장세웅
     * @Since: 2025.04.30
     */
    @Bean("heartbeatExecutor")
    public Executor heartbeatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncHeartbeat-");
        executor.initialize();

        return executor;
    }
}
