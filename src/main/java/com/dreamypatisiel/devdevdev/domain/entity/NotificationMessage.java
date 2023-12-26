package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Embeddable
@Access(AccessType.FIELD)
@NoArgsConstructor
public class NotificationMessage {
    private String message;

    public NotificationMessage(String message) {
        this.message = message;
    }

}
