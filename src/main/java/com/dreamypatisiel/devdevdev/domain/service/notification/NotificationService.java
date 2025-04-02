package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final MemberProvider memberProvider;
    private final NotificationRepository notificationRepository;

    /**
     * @Author: 유소영
     * @Since: 2025.03.28
     * @Note: 알림 단건 읽기
     * @param notificationId 알림 ID
     * @param authentication 회원 정보
     * @return NotificationReadResponse
     */
    @Transactional
    public NotificationReadResponse readNotification(Long notificationId, Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 알림 조회
        Notification findNotification = notificationRepository.findByIdAndMember(notificationId, findMember)
                .orElseThrow(() -> new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE));

        // 알림 읽기 처리 (이미 읽은 알림의 경우이라도 예외를 발생시키지 않고 처리)
        if (!findNotification.isRead()) {
            findNotification.markAsRead();
        }

        // 응답 반환
        return NotificationReadResponse.from(findNotification);
    }
}
