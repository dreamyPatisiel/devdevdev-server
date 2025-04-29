package com.dreamypatisiel.devdevdev.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterHeartbeatSchedulerTest {

    @Mock
    SseEmitterRepository sseEmitterRepository;

    @Mock
    AsyncHeartbeatSender asyncHeartbeatSender;

    @InjectMocks
    SseEmitterHeartbeatScheduler sseEmitterHeartbeatScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("모든 SseEmitter에 대해 heartbeat을 전송한다.")
    void scheduleHeartbeat() {
        // given
        SseEmitter sseEmitter1 = new SseEmitter();
        SseEmitter sseEmitter2 = new SseEmitter();
        List<SseEmitter> sseEmitters = Arrays.asList(sseEmitter1, sseEmitter2);

        given(sseEmitterRepository.findAll()).willReturn(sseEmitters);

        // when
        sseEmitterHeartbeatScheduler.scheduleHeartbeat();

        // then
        verify(asyncHeartbeatSender, times(1)).sendHeartbeat(sseEmitter1);
        verify(asyncHeartbeatSender, times(1)).sendHeartbeat(sseEmitter2);
    }
}