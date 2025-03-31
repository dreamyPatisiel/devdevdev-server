package com.dreamypatisiel.devdevdev.global.redis.sub;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationMessageDto {
    private String message;
    private LocalDateTime createdAt;

    public NotificationMessageDto(Notification notification) {
        this.message = notification.getMessage();
        this.createdAt = notification.getCreatedAt();
    }
}
