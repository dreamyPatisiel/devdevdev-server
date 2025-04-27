package com.dreamypatisiel.devdevdev.redis.sub;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class NotificationMessageDto implements Serializable {
    private String message;
    private String createdAt;

    public NotificationMessageDto(String message, LocalDateTime createdAt) {
        this.message = message;
        this.createdAt = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
