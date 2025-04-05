package com.dreamypatisiel.devdevdev.domain.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SseEmitterService {
    public static final long TIMEOUT = 60 * 1000L;
    public static final String UNREAD_NOTIFICATION_FORMAT = "읽지 않은 알림이 %d개가 있어요.";

    private final MemberProvider memberProvider;

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;

    public SseEmitter createSseEmitter(Long timeout) {
        return new SseEmitter(timeout);
    }

    /**
     * @Note: 실시간 알림을 받을 구독자 추가 및
     * @Author: 장세웅
     * @Since: 2025.03.31
     */
    public SseEmitter addClientAndSendNotification(Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 구독자 생성
        SseEmitter sseEmitter = createSseEmitter(TIMEOUT);
        sseEmitterRepository.save(findMember, sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitterRepository.remove(findMember));
        sseEmitter.onTimeout(() -> sseEmitterRepository.remove(findMember));

        // 회원에게 안읽은 알림이 있는지 조회
        Long unreadNotificationCount = notificationRepository.countByMemberAndIsReadIsFalse(findMember);

        // 알림이 존재하면 구독자에게 알림 전송
        if (unreadNotificationCount > 0) {
            try {
                // 알림 전송
                String notificationMessage = String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount);
                sseEmitter.send(notificationMessage);
            } catch (Exception e) {
                // 구독자 제거
                sseEmitterRepository.remove(findMember);
            }
        }

        return sseEmitter;
    }

    /**
     * @Note: 구독자에게 알림 전송
     * @Author: 장세웅
     * @Since: 2025.03.31
     */
    public void sendNotification(NotificationMessageDto notificationMessageDto, Member member) {
        // 구독자 조회
        SseEmitter sseEmitter = sseEmitterRepository.findByMemberId(member);
        if (!ObjectUtils.isEmpty(sseEmitter)) {
            try {
                // 알림 전송
                sseEmitter.send(notificationMessageDto);
            } catch (Exception e) {
                // 구독자 제거
                sseEmitterRepository.remove(member);
            }
        }
    }
}
