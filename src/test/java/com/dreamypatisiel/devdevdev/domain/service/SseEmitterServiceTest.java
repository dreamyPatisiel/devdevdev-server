package com.dreamypatisiel.devdevdev.domain.service;

import static com.dreamypatisiel.devdevdev.domain.service.SseEmitterService.UNREAD_NOTIFICATION_FORMAT;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootTest
@Transactional
class SseEmitterServiceTest {

    @MockBean
    private MemberProvider memberProvider;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private SseEmitterRepository sseEmitterRepository;

    @SpyBean
    private SseEmitterService sseEmitterService;

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 50, 100})
    @DisplayName("읽지 않은 알림이 존재할 때, SSE 구독 등록 후 알림을 전송한다.")
    void addClientAndSendNotificationHaveUnreadNotification(long unreadNotificationCount) throws Exception {
        // given
        Authentication mockAuthentication = mock(Authentication.class);
        Member mockMember = mock(Member.class);
        SseEmitter realEmitter = new SseEmitter();
        SseEmitter spyEmitter = spy(realEmitter);

        when(memberProvider.getMemberByAuthentication(mockAuthentication)).thenReturn(mockMember);
        when(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).thenReturn(unreadNotificationCount);
        when(sseEmitterService.createSseEmitter(anyLong())).thenReturn(spyEmitter);

        // when
        SseEmitter resultEmitter = sseEmitterService.addClientAndSendNotification(mockAuthentication);

        // then
        verify(memberProvider).getMemberByAuthentication(mockAuthentication);
        verify(sseEmitterRepository).save(mockMember, resultEmitter);
        verify(notificationRepository).countByMemberAndIsReadIsFalse(mockMember);
        verify(resultEmitter).send(String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount));
        verify(sseEmitterRepository, never()).remove(mockMember);
    }

    @Test
    @DisplayName("읽지 않은 알림이 없을 때, 구독자만 등록하고 알림은 전송하지 않는다.")
    void addClientAndSendNotificationHaveNotUnreadNotification() throws IOException {
        // given
        Authentication mockAuthentication = mock(Authentication.class);
        Member mockMember = mock(Member.class);
        SseEmitter realEmitter = new SseEmitter();
        SseEmitter spyEmitter = spy(realEmitter);

        when(memberProvider.getMemberByAuthentication(mockAuthentication)).thenReturn(mockMember);
        when(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).thenReturn(0L);
        when(sseEmitterService.createSseEmitter(anyLong())).thenReturn(spyEmitter);

        // when
        SseEmitter resultEmitter = sseEmitterService.addClientAndSendNotification(mockAuthentication);

        // then
        verify(memberProvider).getMemberByAuthentication(mockAuthentication);
        verify(sseEmitterRepository).save(mockMember, resultEmitter);
        verify(notificationRepository).countByMemberAndIsReadIsFalse(mockMember);
        verify(resultEmitter, never()).send(String.format(UNREAD_NOTIFICATION_FORMAT, any()));
        verify(sseEmitterRepository, never()).remove(mockMember);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 50, 100})
    @DisplayName("알림 전송 중 예외가 발생하면 구독자를 제거한다.")
    void addClientAndSendNotificationIoException(Long unreadNotificationCount) throws IOException {
        // given
        Authentication mockAuthentication = mock(Authentication.class);
        Member mockMember = mock(Member.class);
        SseEmitter realEmitter = new SseEmitter();
        SseEmitter spyEmitter = spy(realEmitter);

        given(memberProvider.getMemberByAuthentication(mockAuthentication)).willReturn(mockMember);
        given(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).willReturn(unreadNotificationCount);
        given(sseEmitterService.createSseEmitter(anyLong())).willReturn(spyEmitter);
        doThrow(new IOException()).when(spyEmitter).send(anyString());

        // when // then
        assertThatCode(() -> sseEmitterService.addClientAndSendNotification(mockAuthentication))
                .doesNotThrowAnyException();

        verify(memberProvider).getMemberByAuthentication(mockAuthentication);
        verify(sseEmitterRepository).save(mockMember, spyEmitter);
        verify(notificationRepository).countByMemberAndIsReadIsFalse(mockMember);
        verify(spyEmitter).send(String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount));
        verify(sseEmitterRepository).remove(mockMember);
    }

    @Test
    @DisplayName("구독자에게 알림을 전송합니다.")
    void sendNotification() throws IOException {
        // given
        Member mockMember = mock(Member.class);
        NotificationMessageDto mockMessageDto = mock(NotificationMessageDto.class);

        // SseEmitter를 모킹해줍니다.
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(sseEmitterRepository.findByMemberId(mockMember)).willReturn(mockEmitter);

        // when
        sseEmitterService.sendNotification(mockMessageDto, mockMember);

        verify(mockEmitter).send(mockMessageDto);
        verify(sseEmitterRepository, never()).remove(mockMember);
    }

    @Test
    @DisplayName("구독자에게 알림 전송 중 예외가 발생하면 구독자를 제거합니다.")
    void sendNotificationIoException() throws IOException {
        // given
        Member mockMember = mock(Member.class);
        NotificationMessageDto mockMessageDto = mock(NotificationMessageDto.class);

        // SseEmitter를 모킹해줍니다.
        SseEmitter mockEmitter = mock(SseEmitter.class);
        given(sseEmitterRepository.findByMemberId(mockMember)).willReturn(mockEmitter);

        doThrow(new IOException()).when(mockEmitter).send(mockMessageDto);

        // when // then
        assertThatCode(() -> sseEmitterService.sendNotification(mockMessageDto, mockMember))
                .doesNotThrowAnyException();

        verify(mockEmitter).send(mockMessageDto);
        verify(sseEmitterRepository).remove(mockMember);
    }
}