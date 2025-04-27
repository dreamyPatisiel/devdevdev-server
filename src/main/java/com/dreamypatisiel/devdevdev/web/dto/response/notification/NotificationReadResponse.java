package com.dreamypatisiel.devdevdev.web.dto.response.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import lombok.Builder;
import lombok.Data;

@Data
public class NotificationReadResponse {
    public final Long id;
    public final Boolean isRead;

    @Builder
    public NotificationReadResponse(Long id, Boolean isRead) {
        this.id = id;
        this.isRead = isRead;
    }

    public static NotificationReadResponse from(Notification notification) {
        return NotificationReadResponse.builder()
                .id(notification.getId())
                .isRead(notification.getIsRead())
                .build();
    }
}
