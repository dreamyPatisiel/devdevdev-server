package com.dreamypatisiel.devdevdev.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@ExtendWith(MockitoExtension.class)
class AsyncHeartbeatSenderTest {

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private AsyncHeartbeatSender asyncHeartbeatSender;

    @Test
    @DisplayName("성공적으로 heartbeat을 전송한다.")
    void sendHeartbeat() throws IOException {
        // given
        SseEmitter sseEmitter = mock(SseEmitter.class); // 여기 수정
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        given(timeProvider.getLocalDateTimeNow()).willReturn(now);

        // when
        asyncHeartbeatSender.sendHeartbeat(sseEmitter);

        // then
        // SseEventBuilder 타입의 인자를 가로채기 위한 캡처 설정
        ArgumentCaptor<SseEventBuilder> captor = ArgumentCaptor.forClass(SseEventBuilder.class);

        // send()에 넘긴 builder를 캡처
        verify(sseEmitter, times(1)).send(captor.capture());

        // builder를 다시 꺼냄
        SseEmitter.SseEventBuilder captorValueEvent = captor.getValue();
        assertThat(captorValueEvent).isNotNull();
    }
}