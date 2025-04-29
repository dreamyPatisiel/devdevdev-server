package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public abstract class NotificationPopupResponse {
    private final Long id;
    private final NotificationType type;
    private final String title;
    private final LocalDateTime createdAt;
    private final Boolean isRead;

    public NotificationPopupResponse(Long id, NotificationType type, String title, LocalDateTime createdAt,
                                     boolean isRead) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }
}