package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.redis.pub.NotificationPublisher;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.RedisPublishRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationNewArticleResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupNewArticleResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    public static final long TIMEOUT = 5 * 60 * 1000L;
    public static final long HEARTBEAT_INTERVAL = 5 * 60 * 1000L;
    public static final String UNREAD_NOTIFICATION_FORMAT = "읽지 않은 알림이 %d개가 있어요.";
    public static final String MAIN_TECH_ARTICLE_NOTIFICATION_FORMAT = "%s에서 새로운 기슬블로그 %d개가 올라왔어요!";
    public static final String TECH_ARTICLE_NOTIFICATION_FORMAT = "%s에서 새로운 글이 올라왔어요!";
    public static final String ACCESS_DENIED_MESSAGE = "접근할 수 없는 권한 입니다.";

    private final MemberProvider memberProvider;
    private final TimeProvider timeProvider;
    private final NotificationPublisher notificationPublisher;

    private final TechArticleCommonService techArticleCommonService;

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SseEmitter createSseEmitter(Long timeout) {
        return new SseEmitter(timeout);
    }


    /**
     * @param notificationId 알림 ID
     * @param authentication 회원 정보
     * @return NotificationReadResponse
     * @Note: 알림 단건 읽기
     * @Author: 유소영
     * @Since: 2025.03.28
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
     * @param authentication 회원 인증 정보
     * @Note: 회원의 모든 알림을 읽음 처리
     * @Author: 유소영
     * @Since: 2025.03.29
     */
    @Transactional
    public void readAllNotifications(Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 읽지 않은 모든 알림 조회
        notificationRepository.bulkMarkAllAsReadByMemberId(findMember.getId());
    }

    /**
     * @param pageable       페이징 정보
     * @param authentication 회원 인증 정보
     * @Note: 알림 팝업 조회
     * @Author: 유소영
     * @Since: 2025.04.09
     */
    public SliceCustom<NotificationPopupResponse> getNotificationPopup(Pageable pageable, Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 최근 알림 조회(default: 5개)
        SliceCustom<Notification> notifications =
                notificationRepository.findNotificationsByMemberAndTypeOrderByCreatedAtDesc(pageable,
                        NotificationType.getEnabledTypes(), findMember);

        // 데이터 가공
        // NotificationType 에 따라 다른 DTO로 변환
        List<NotificationPopupResponse> response = notifications.getContent().stream()
                .map(this::mapToPopupResponse)
                .toList();

        return new SliceCustom<>(response, pageable, notifications.hasNext(), notifications.getTotalElements());
    }

    private static final Map<NotificationType, Function<Notification, NotificationPopupResponse>> POPUP_RESPONSE_MAPPER =
            Map.of(
                    // TODO: 현재는 SUBSCRIPTION 타입만 제공, 알림 타입이 추가될 경우 각 타입에 맞는 응답 DTO 변환 매핑 필요
                    NotificationType.SUBSCRIPTION, NotificationPopupNewArticleResponse::from
            );

    private NotificationPopupResponse mapToPopupResponse(Notification notification) {
        return POPUP_RESPONSE_MAPPER
                .getOrDefault(notification.getType(), n -> {
                    throw new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_TYPE);
                })
                .apply(notification);
    }

    /**
     * @param pageable       페이징 정보
     * @param notificationId 커서용 알림 ID
     * @param authentication 회원 인증 정보
     * @Note: 알림 페이지 조회
     * @Author: 유소영
     * @Since: 2025.04.11
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
                .map(notification -> mapToNotificationResponse(notification, elasticTechArticles))
                .toList();

        return new SliceCustom<>(response, pageable, notifications.hasNext(), notifications.getTotalElements());
    }

    private NotificationResponse mapToNotificationResponse(Notification notification,
                                                           Map<Long, ElasticTechArticle> elasticTechArticles) {
        // TODO: 현재는 SUBSCRIPTION 타입만 제공, 알림 타입이 추가될 경우 각 타입에 맞는 응답 DTO 변환 매핑 필요
        if (notification.getType() == NotificationType.SUBSCRIPTION) {
            return NotificationNewArticleResponse.from(notification,
                    getTechArticleMainResponse(notification, elasticTechArticles));
        }
        throw new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_TYPE);
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

        List<ElasticTechArticle> elasticTechArticles = techArticleCommonService.findElasticTechArticlesByTechArticles(
                techArticles);

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

    /**
     * @Note: 실시간 알림을 받을 구독자 추가 및 알림 전송
     * @Author: 장세웅
     * @Since: 2025.03.31
     */
    @Transactional
    public SseEmitter addClientAndSendNotification(Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        SseEmitter sseEmitter = addClient(findMember);

        sendUnreadNotifications(findMember, sseEmitter);

        return sseEmitter;
    }

    private void sendUnreadNotifications(Member findMember, SseEmitter sseEmitter) {
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
    }

    private SseEmitter addClient(Member findMember) {
        // 구독자 생성
        SseEmitter sseEmitter = createSseEmitter(TIMEOUT);
        sseEmitterRepository.save(findMember, sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitterRepository.remove(findMember));
        sseEmitter.onTimeout(() -> sseEmitterRepository.remove(findMember));
        sseEmitter.onError(throwable -> sseEmitterRepository.remove(findMember));

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
        List<Notification> findSubscriptionNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByNull(
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

    public <T extends RedisPublishRequest> Long publish(NotificationType channel, T message) {
        return notificationPublisher.publish(channel, message);
    }

    /**
     * @Note: 회원이 읽지 않은 알림 총 개수 조회
     * @Author: 유소영
     * @Since: 2025.04.29
     */
    public Long getUnreadNotificationCount(Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원이 읽지 않은 알림 개수 조회
        return notificationRepository.countByMemberAndIsReadFalse(findMember);
    }
}
