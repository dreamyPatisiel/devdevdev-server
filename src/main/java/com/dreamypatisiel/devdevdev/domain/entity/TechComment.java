package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx__comment__tech_article__created_by__deleted_at", columnList = "id, tech_article_id, created_by, deletedAt"),
        @Index(name = "idx__comment__tech_article__deleted_at", columnList = "id, tech_article_id, deletedAt")
})
public class TechComment extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "commentContents",
            column = @Column(name = "contents", length = 1000)
    )
    private CommentContents contents;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "blame_total_count", columnDefinition = "bigint default 0")
    )
    private Count blameTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count", columnDefinition = "bigint default 0")
    )
    private Count recommendTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "reply_total_count", columnDefinition = "bigint default 0")
    )
    private Count replyTotalCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private TechComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_parent_id", referencedColumnName = "id")
    private TechComment originParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private Member deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false)
    private TechArticle techArticle;

    private LocalDateTime deletedAt;

    @Builder
    public TechComment(CommentContents contents, Count blameTotalCount, Count recommendTotalCount, Count replyTotalCount,
                       TechComment parent, TechComment originParent, Member createdBy, Member deletedBy,
                       TechArticle techArticle, LocalDateTime deletedAt) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.replyTotalCount = replyTotalCount;
        this.parent = parent;
        this.originParent = originParent;
        this.createdBy = createdBy;
        this.deletedBy = deletedBy;
        this.techArticle = techArticle;
        this.deletedAt = deletedAt;
    }

    public static TechComment createMainTechComment(CommentContents contents, Member createdBy, TechArticle techArticle) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(Count.defaultCount())
                .recommendTotalCount(Count.defaultCount())
                .replyTotalCount(Count.defaultCount())
                .build();
    }

    public static TechComment createRepliedTechComment(CommentContents contents, Member createdBy, TechArticle techArticle, TechComment originParent, TechComment parent) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(Count.defaultCount())
                .recommendTotalCount(Count.defaultCount())
                .replyTotalCount(Count.defaultCount())
                .originParent(originParent)
                .parent(parent)
                .build();
    }

    public void changeDeletedAt(LocalDateTime deletedAt, Member deletedBy) {
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public void changeCommentContents(CommentContents contents) {
        this.contents = contents;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void incrementReplyTotalCount() {
        this.replyTotalCount.incrementCount();
    }

    public void decrementReplyTotalCount() {
        this.replyTotalCount.decrementCount();
    }
}
