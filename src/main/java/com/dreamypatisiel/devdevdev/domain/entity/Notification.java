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
        @Index(name = "idx_notification_01", columnList = "id, member_id"),
        @Index(name = "idx_notification_02", columnList = "member_id, is_read"),
        @Index(name = "idx_notification_03", columnList = "member_id, type"),
        @Index(name = "idx_notification_04", columnList = "member_id, tech_article_id")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_comment_id")
    private TechComment techComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id")
    private PickComment pickComment;

    @Column(nullable = false)
    private boolean isRead = false;

    @Builder
    private Notification(String message, NotificationType type, Member member,
                         TechArticle techArticle, TechComment techComment, PickComment pickComment, boolean isRead) {
        this.message = message;
        this.type = type;
        this.member = member;
        this.techArticle = techArticle;
        this.techComment = techComment;
        this.pickComment = pickComment;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
