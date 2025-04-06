package com.dreamypatisiel.devdevdev.redis.sub;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.service.NotificationService;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisNotificationSubscriber implements MessageListener {

    private final NotificationService notificationService;

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
            String channel = om.readValue(pattern, String.class);
            if (channel.equals(NotificationType.SUBSCRIPTION.name())) {

                PublishTechArticleRequest publishTechArticleRequest = om.readValue(message.getBody(),
                        PublishTechArticleRequest.class);

                // 구독자에게 메인 알림 전송 및 알림 저장
                notificationService.sendMainTechArticleNotifications(publishTechArticleRequest);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
