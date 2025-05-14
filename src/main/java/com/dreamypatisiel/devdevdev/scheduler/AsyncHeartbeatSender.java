package com.dreamypatisiel.devdevdev.scheduler;

import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class AsyncHeartbeatSender {

    private final TimeProvider timeProvider;

    @Async
    public void sendHeartbeat(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(createHeartbeatEvent());
        } catch (IOException e) {
            sseEmitter.complete();
        }
    }

    private SseEmitter.SseEventBuilder createHeartbeatEvent() {
        return SseEmitter.event()
                .name("heartbeat")
                .data(timeProvider.getLocalDateTimeNow());
    }
}
