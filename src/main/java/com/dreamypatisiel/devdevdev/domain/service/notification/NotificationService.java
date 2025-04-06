package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.redis.pub.NotificationPublisher;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.RedisPublishRequest;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    public static final long TIMEOUT = 60 * 1000L;
    public static final String UNREAD_NOTIFICATION_FORMAT = "읽지 않은 알림이 %d개가 있어요.";
    public static final String MAIN_TECH_ARTICLE_NOTIFICATION_FORMAT = "%s에서 새로운 기슬블로그 %d개가 올라왔어요!";
    public static final String TECH_ARTICLE_NOTIFICATION_FORMAT = "%s에서 새로운 글이 올라왔어요!";
    public static final String ACCESS_DENIED_MESSAGE = "접근할 수 없는 권한 입니다.";

    private final MemberProvider memberProvider;
    private final TimeProvider timeProvider;
    private final NotificationPublisher notificationPublisher;

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SseEmitter createSseEmitter(Long timeout) {
        return new SseEmitter(timeout);
    }

    /**
     * @Note: 실시간 알림을 받을 구독자 추가 및
     * @Author: 장세웅
     * @Since: 2025.03.31
     */
    @Transactional
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
                // 알림 메시지 생성
                String notificationMessage = String.format(UNREAD_NOTIFICATION_FORMAT, unreadNotificationCount);
                NotificationMessageDto notificationMessageDto = new NotificationMessageDto(
                        notificationMessage, timeProvider.getLocalDateTimeNow());

                // 알림 전송
                sseEmitter.send(notificationMessageDto);
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
    @Transactional
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

    @Transactional
    public void sendMainTechArticleNotifications(PublishTechArticleRequest publishTechArticleRequest) {
        // 기업 구독 목록 조회(member fetch join)
        List<Subscription> findSubscriptions = subscriptionRepository.findWithMemberByCompanyIdOrderByMemberDesc(
                publishTechArticleRequest.getCompanyId());

        if (findSubscriptions.isEmpty()) {
            return;
        }

        // 기업을 구독중인 모든 회원 추출
        Set<Member> members = findSubscriptions.stream()
                .map(Subscription::getMember)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 순서 유지

        // 새롭게 publish 된 기술블로그 아이디 추출
        Set<Long> techArticleIds = publishTechArticleRequest.getTechArticles().stream()
                .map(PublishTechArticle::getId)
                .collect(Collectors.toSet());

        // 회원들의 기술블로그 구독 알림 이력 조회
        List<Notification> findSubscriptionNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByMemberDesc(
                members, techArticleIds);

        // 구독한 기업에 대해서 새로운 글 알림이 존재하지 않은 회원 추출
        Set<Member> membersWithoutNotifications = members.stream()
                .filter(member -> findSubscriptionNotifications.stream()
                        .noneMatch(notification -> notification.isEqualsMember(member)))
                .collect(Collectors.toSet());

        // 알림 메시지 생성
        Company company = findSubscriptions.getFirst().getCompany();
        String companyName = company.getName().getCompanyName();
        String notificationMessage = String.format(TECH_ARTICLE_NOTIFICATION_FORMAT, companyName);

        // 알림이 없는 회원들의 알림 이력 생성
        membersWithoutNotifications.forEach(memberWithoutNotification ->
                techArticleIds.forEach(techArticleId -> {
                    // 알림 이력 생성 및 저장
                    Notification notification = Notification.createTechArticleNotification(
                            memberWithoutNotification, new TechArticle(techArticleId), notificationMessage);
                    notificationRepository.save(notification);
                }));

        // 메인 알림 메시지 생성
        String mainNotificationMessage = String.format(MAIN_TECH_ARTICLE_NOTIFICATION_FORMAT,
                companyName, techArticleIds.size());

        // 메인 알림 전송
        membersWithoutNotifications.forEach(memberWithoutNotification -> {
            NotificationMessageDto notificationMessageDto = new NotificationMessageDto(mainNotificationMessage,
                    timeProvider.getLocalDateTimeNow());
            sendNotification(notificationMessageDto, memberWithoutNotification);
        });
    }

    public <T extends RedisPublishRequest> Long publish(Authentication authentication, NotificationType channel, T message) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);
        if (!findMember.isAdmin()) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        return notificationPublisher.publish(channel, message);
    }
}
