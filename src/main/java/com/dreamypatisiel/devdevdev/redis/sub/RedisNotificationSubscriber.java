package com.dreamypatisiel.devdevdev.redis.sub;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.service.SseEmitterService;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisNotificationSubscriber implements MessageListener {

    public static final String TECH_ARTICLE_NOTIFICATION_FORMAT = "%s에서 새로운 글이 올라왔어요!";

    private final SseEmitterService sseEmitterService;

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * @Note: redis pub 발생시 구독자에게 메시지 전송
     * @Author: 장세웅
     * @Since: 2025.03.27
     */
    @Transactional
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ObjectMapper om = new ObjectMapper();
            PublishTechArticleRequest request = om.readValue(message.getBody(),
                    PublishTechArticleRequest.class);

            // 기업 구독 목록 조회(member fetch join)
            List<Subscription> findSubscriptions = subscriptionRepository.findWithMemberByCompanyIdOrderByMemberDesc(
                    request.getCompanyId());

            if (findSubscriptions.isEmpty()) {
                return;
            }

            // 기업을 구독중인 모든 회원 추출
            Set<Member> members = findSubscriptions.stream()
                    .map(Subscription::getMember)
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // 순서 유지

            // 새롭게 publish 된 기술블로그 아이디 추출
            Set<Long> techArticleIds = request.getTechArticles().stream()
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

            // 메시지 생성
            Company company = findSubscriptions.getFirst().getCompany();
            String notificationMessage = String.format(TECH_ARTICLE_NOTIFICATION_FORMAT, company.getName().getCompanyName());

            // 알림이 없는 회원들의 알림 생성
            membersWithoutNotifications.forEach(memberWithoutNotification ->
                    techArticleIds.forEach(techArticleId -> {
                        // 알림 이력 생성 및 저장
                        Notification notification = Notification.createTechArticleNotification(
                                memberWithoutNotification, new TechArticle(techArticleId), notificationMessage);
                        notificationRepository.save(notification);

                        // 알림 전송
                        sseEmitterService.sendNotification(new NotificationMessageDto(notification),
                                memberWithoutNotification);
                    }));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
