package com.dreamypatisiel.devdevdev.redis.sub;

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
}
