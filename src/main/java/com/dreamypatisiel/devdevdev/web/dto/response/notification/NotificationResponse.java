package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public abstract class NotificationResponse {
    private final Long notificationId;
    private final NotificationType type;
    private final LocalDate createdAt;
    private final boolean isRead;

    public NotificationResponse(Long notificationId, NotificationType type, LocalDate createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.type = type;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }
}