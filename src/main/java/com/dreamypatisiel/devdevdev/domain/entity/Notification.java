package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BasicTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private NotificationMessage message;

    @Enumerated(value = EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
