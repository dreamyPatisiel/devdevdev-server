package com.dreamypatisiel.devdevdev.redis.sub;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationMessageDto {
    private String message;
    private LocalDateTime createdAt;

    public NotificationMessageDto(String message, LocalDateTime createdAt) {
        this.message = message;
        this.createdAt = createdAt;
    }

    public NotificationMessageDto(Notification notification) {
        this.message = notification.getMessage();
        this.createdAt = notification.getCreatedAt();
    }
}
