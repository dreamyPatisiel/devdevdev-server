package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_notification_01", columnList = "id, member_id"),
        @Index(name = "idx_notification_02", columnList = "member_id, is_read"),
        @Index(name = "idx_notification_03", columnList = "member_id, type"),
        @Index(name = "idx_notification_04", columnList = "member_id, tech_article_id"),
})
public class Notification extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_01"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", foreignKey = @ForeignKey(name = "fk_notification_02"))
    private TechArticle techArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_comment_id", foreignKey = @ForeignKey(name = "fk_notification_03"))
    private TechComment techComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id", foreignKey = @ForeignKey(name = "fk_notification_04"))
    private PickComment pickComment;

    @Column(nullable = false)
    private String message;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Builder
    private Notification(Member member, TechArticle techArticle, TechComment techComment, PickComment pickComment,
                         String message, NotificationType type, Boolean isRead) {
        this.member = member;
        this.techArticle = techArticle;
        this.techComment = techComment;
        this.pickComment = pickComment;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
    }

    public static Notification createTechArticleNotification(Member member, TechArticle techArticle, String message) {
        return Notification.builder()
                .member(member)
                .techArticle(techArticle)
                .message(message)
                .type(NotificationType.SUBSCRIPTION)
                .isRead(false)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isEqualsMember(Member member) {
        return this.member.isEqualsId(member.getId());
    }
}
