package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_01", columnList = "member_id"),
        @Index(name = "idx_notification_02", columnList = "id, member_id"),
        @Index(name = "idx_notification_03", columnList = "member_id, is_read")
})
public class Notification extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String message;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private boolean isRead = false;

    @Builder
    private Notification(String message, NotificationType type, Member member, boolean isRead) {
        this.message = message;
        this.type = type;
        this.member = member;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
