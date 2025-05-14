package com.dreamypatisiel.devdevdev.domain.service.notification;

import static com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService.UNREAD_NOTIFICATION_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import java.io.IOException;
import java.time.LocalDateTime;
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
class MockNotificationServiceTest {

    @MockBean
    TimeProvider timeProvider;

    @MockBean
    private MemberProvider memberProvider;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private SseEmitterRepository sseEmitterRepository;

    @SpyBean
    private NotificationService notificationService;

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 50, 100})
    @DisplayName("읽지 않은 알림이 존재할 때, SSE 구독 등록 후 알림을 전송한다.")
    void addClientAndSendNotificationHaveUnreadNotification(long unreadNotificationCount) throws Exception {
        // given
        Authentication mockAuthentication = mock(Authentication.class);
        Member mockMember = mock(Member.class);
        SseEmitter realEmitter = new SseEmitter();
        SseEmitter spyEmitter = spy(realEmitter);

        given(timeProvider.getLocalDateTimeNow()).willReturn(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        given(memberProvider.getMemberByAuthentication(mockAuthentication)).willReturn(mockMember);
        given(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).willReturn(unreadNotificationCount);
        given(notificationService.createSseEmitter(anyLong())).willReturn(spyEmitter);

        String notificationMessage = String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount);
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto(notificationMessage,
                timeProvider.getLocalDateTimeNow());

        // when
        SseEmitter resultEmitter = notificationService.addClientAndSendNotification(mockAuthentication);

        // then
        verify(memberProvider).getMemberByAuthentication(mockAuthentication);
        verify(sseEmitterRepository).save(mockMember, resultEmitter);
        verify(notificationRepository).countByMemberAndIsReadIsFalse(mockMember);
        verify(resultEmitter).send(notificationMessageDto);
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

        given(memberProvider.getMemberByAuthentication(mockAuthentication)).willReturn(mockMember);
        given(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).willReturn(0L);
        given(notificationService.createSseEmitter(anyLong())).willReturn(spyEmitter);

        // when
        SseEmitter resultEmitter = notificationService.addClientAndSendNotification(mockAuthentication);

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

        given(timeProvider.getLocalDateTimeNow()).willReturn(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        given(memberProvider.getMemberByAuthentication(mockAuthentication)).willReturn(mockMember);
        given(notificationRepository.countByMemberAndIsReadIsFalse(mockMember)).willReturn(unreadNotificationCount);
        given(notificationService.createSseEmitter(anyLong())).willReturn(spyEmitter);

        String notificationMessage = String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount);
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto(notificationMessage,
                timeProvider.getLocalDateTimeNow());

        doThrow(new IOException()).when(spyEmitter).send(notificationMessageDto);

        // when // then
        assertThatCode(() -> notificationService.addClientAndSendNotification(mockAuthentication))
                .doesNotThrowAnyException();

        verify(memberProvider).getMemberByAuthentication(mockAuthentication);
        verify(sseEmitterRepository).save(mockMember, spyEmitter);
        verify(notificationRepository).countByMemberAndIsReadIsFalse(mockMember);
        verify(spyEmitter).send(notificationMessageDto);
        verify(sseEmitterRepository).remove(mockMember);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 10, 50, 100})
    @DisplayName("구독자가 이미 존재하는 경우 기존의 SSEmitter 를 반환합니다.")
    void addClientAndSendNotificationIsExistMember(Long unreadNotificationCount) throws IOException {
        // given
        Authentication mockAuthentication = mock(Authentication.class);
        Member mockMember = mock(Member.class);
        SseEmitter realEmitter = new SseEmitter();
        SseEmitter spyEmitter = spy(realEmitter);

        given(timeProvider.getLocalDateTimeNow()).willReturn(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        given(memberProvider.getMemberByAuthentication(mockAuthentication)).willReturn(mockMember);
        given(sseEmitterRepository.save(mockMember, spyEmitter)).willReturn(spyEmitter);
        given(sseEmitterRepository.existByMember(mockMember)).willReturn(true);
        given(sseEmitterRepository.findByMemberId(mockMember)).willReturn(spyEmitter);

        // when
        SseEmitter sseEmitter = notificationService.addClientAndSendNotification(mockAuthentication);

        // then
        assertThat(sseEmitter).isEqualTo(spyEmitter);
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
        notificationService.sendNotification(mockMessageDto, mockMember);

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
        assertThatCode(() -> notificationService.sendNotification(mockMessageDto, mockMember))
                .doesNotThrowAnyException();

        verify(mockEmitter).send(mockMessageDto);
        verify(sseEmitterRepository).remove(mockMember);
    }
}