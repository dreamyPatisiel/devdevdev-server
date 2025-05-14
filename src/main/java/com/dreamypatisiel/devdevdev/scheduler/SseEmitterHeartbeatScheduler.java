package com.dreamypatisiel.devdevdev.scheduler;

import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class SseEmitterHeartbeatScheduler {

    private final SseEmitterRepository sseEmitterRepository;
    private final AsyncHeartbeatSender asyncHeartbeatSender;

    // 30초 마다 실행
    @Scheduled(fixedRate = 30_000)
    public void scheduleHeartbeat() {
        Collection<SseEmitter> findSseEmitter = sseEmitterRepository.findAll();
        for (SseEmitter sseEmitter : findSseEmitter) {
            asyncHeartbeatSender.sendHeartbeat(sseEmitter);
        }
    }
}
