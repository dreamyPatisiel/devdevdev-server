package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.*;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final MemberProvider memberProvider;
    private final NotificationRepository notificationRepository;
    private final TechArticleCommonService techArticleCommonService;

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
        if (!findNotification.getIsRead()) {
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
     * @param pageable 페이징 정보
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

    /**
     * @Note: 알림 페이지 조회
     * @Author: 유소영
     * @Since: 2025.04.11
     * @param pageable 페이징 정보
     * @param notificationId 커서용 알림 ID
     * @param authentication 회원 인증 정보
     */
    public SliceCustom<NotificationResponse> getNotifications(Pageable pageable, Long notificationId,
                                                              Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원 알림 페이징 조회
        SliceCustom<Notification> notifications = notificationRepository.findNotificationsByMemberAndCursor(pageable,
                notificationId, findMember);

        // 데이터 가공
        // NotificationType 에 따라 다른 DTO로 변환
        Map<Long, ElasticTechArticle> elasticTechArticles = getTechArticleIdToElastic(notifications.getContent());

        List<NotificationResponse> response = notifications.getContent().stream()
                .map(notification -> {
                    if (notification.getType() == NotificationType.SUBSCRIPTION) {
                        return (NotificationResponse) NotificationNewArticleResponse.from(notification,
                                getTechArticleMainResponse(notification, elasticTechArticles));
                    } else {
                        throw new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_TYPE);
                    }
                })
                .toList();

        return new SliceCustom<>(response, pageable, notifications.hasNext(), notifications.getTotalElements());
    }

    // NotificationType.SUBSCRIPTION 알림의 경우 TechArticleMainResponse 생성
    private TechArticleMainResponse getTechArticleMainResponse(Notification notification,
                                                                Map<Long, ElasticTechArticle> elasticTechArticles) {
        TechArticle techArticle = notification.getTechArticle();
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        ElasticTechArticle elasticTechArticle = elasticTechArticles.get(notification.getId());

        return TechArticleMainResponse.of(techArticle, elasticTechArticle, companyResponse);
    }

    // 알림 ID를 키로 하고, ElasticTechArticle 을 값으로 가지는 맵을 반환
    private Map<Long, ElasticTechArticle> getTechArticleIdToElastic(List<Notification> notifications) {
        // 1. NotificationType.SUBSCRIPTION 알림만 필터링하여 ElasticTechArticle 리스트 생성
        List<TechArticle> techArticles = notifications.stream()
                .filter(notification -> notification.getType() == NotificationType.SUBSCRIPTION)
                .map(Notification::getTechArticle)
                .toList();

        List<ElasticTechArticle> elasticTechArticles = techArticleCommonService.findElasticTechArticlesByTechArticles(techArticles);

        // 2. ElasticID → ElasticTechArticle 매핑
        Map<String, ElasticTechArticle> elasticIdToElastic = elasticTechArticles.stream()
                .collect(Collectors.toMap(
                        ElasticTechArticle::getId,
                        elasticTechArticle -> elasticTechArticle
                ));

        // 3. Notification ID → ElasticTechArticle 매핑
        return notifications.stream()
                .filter(notification -> notification.getType() == NotificationType.SUBSCRIPTION)
                .collect(Collectors.toMap(
                        Notification::getId,
                        notification -> elasticIdToElastic.get(notification.getTechArticle().getElasticId())
                ));
    }
}
