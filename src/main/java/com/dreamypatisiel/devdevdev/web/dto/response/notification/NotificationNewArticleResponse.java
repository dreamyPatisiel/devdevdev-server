package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class NotificationNewArticleResponse extends NotificationResponse {
    private final TechArticleMainResponse techArticle;

    @Builder
    public NotificationNewArticleResponse(Long notificationId, LocalDateTime createdAt,
                                          boolean isRead, TechArticleMainResponse techArticle) {
        super(notificationId, NotificationType.SUBSCRIPTION, createdAt, isRead);
        this.techArticle = techArticle;
    }

    public static NotificationNewArticleResponse from(Notification notification, TechArticleMainResponse techArticle) {
        return NotificationNewArticleResponse.builder()
                .notificationId(notification.getId())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .techArticle(techArticle)
                .build();
    }
}