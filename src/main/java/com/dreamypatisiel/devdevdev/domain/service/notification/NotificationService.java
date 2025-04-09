package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupNewArticleResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final MemberProvider memberProvider;
    private final NotificationRepository notificationRepository;

    /**
     * @Note: 알림 단건 읽기
     * @Author: 유소영
     * @Since: 2025.03.28
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

    /**
     * @Note: 회원의 모든 알림을 읽음 처리
     * @Author: 유소영
     * @Since: 2025.03.29
     * @param authentication 회원 인증 정보
     */
    @Transactional
    public void readAllNotifications(Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 읽지 않은 모든 알림 조회
        notificationRepository.bulkMarkAllAsReadByMemberId(findMember.getId());
    }

    /**
     * @Note: 알림 팝업 조회
     * @Author: 유소영
     * @Since: 2025.04.09
     * @param authentication 회원 인증 정보
     */
    public SliceCustom<NotificationPopupResponse> getNotificationPopup(Pageable pageable, Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 최근 5개 알림 조회
        SliceCustom<Notification> notifications = notificationRepository.findNotificationsByMemberOrderByCreatedAtDesc(pageable, findMember);

        // 데이터 가공
        // NotificationType 에 따라 다른 DTO로 변환
        List<NotificationPopupResponse> response = notifications.getContent().stream()
                .map(notification -> {
                    if (notification.getType() == NotificationType.SUBSCRIPTION) {
                        return (NotificationPopupResponse) NotificationPopupNewArticleResponse.from(notification);
                    } else {
                        throw new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_TYPE);
                    }
                })
                .toList();

        return new SliceCustom<>(response, pageable, notifications.hasNext(), notifications.getTotalElements());
    }
}
