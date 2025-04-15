package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class NotificationPopupNewArticleResponse extends NotificationPopupResponse {
    private final String companyName;
    private final Long techArticleId;

    @Builder
    public NotificationPopupNewArticleResponse(Long id, String title, LocalDate createdAt,
                                               boolean isRead, String companyName, Long techArticleId) {
        super(id, NotificationType.SUBSCRIPTION, title, createdAt, isRead);
        this.companyName = companyName;
        this.techArticleId = techArticleId;
    }

    public static NotificationPopupNewArticleResponse from(Notification notification) {
        return NotificationPopupNewArticleResponse.builder()
                .id(notification.getId())
                .createdAt(notification.getCreatedAt().toLocalDate())
                .isRead(notification.getIsRead())
                .title(notification.getTechArticle().getTitle().getTitle())
                .companyName(notification.getTechArticle().getCompany().getName().getCompanyName())
                .techArticleId(notification.getTechArticle().getId())
                .build();
    }
}