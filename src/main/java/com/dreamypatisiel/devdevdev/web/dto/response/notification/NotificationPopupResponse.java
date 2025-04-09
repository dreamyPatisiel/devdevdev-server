package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public abstract class NotificationPopupResponse {
    private final Long id;
    private final NotificationType type;
    private final String title;
    private final LocalDate createdAt;
    private final boolean isRead;

    public NotificationPopupResponse(Long id, NotificationType type, String title, LocalDate createdAt,
                                     boolean isRead) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }
}