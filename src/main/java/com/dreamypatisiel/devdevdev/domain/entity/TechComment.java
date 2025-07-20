package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private LocalDateTime deletedAt;
    private LocalDateTime contentsLastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_tech_comment_01"))
    private TechComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_tech_comment_02"))
    private TechComment originParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_tech_comment_03"))
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", foreignKey = @ForeignKey(name = "fk_tech_comment_04"))
    private Member deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_anonymous_by", foreignKey = @ForeignKey(name = "fk_tech_comment_05"))
    private AnonymousMember createdAnonymousBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_anonymous_by", foreignKey = @ForeignKey(name = "fk_tech_comment_06"))
    private AnonymousMember deletedAnonymousBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tech_comment_07"))
    private TechArticle techArticle;

    @OneToMany(mappedBy = "techComment")
    private List<TechCommentRecommend> recommends = new ArrayList<>();

    @Builder
    private TechComment(CommentContents contents, Count blameTotalCount, Count recommendTotalCount, Count replyTotalCount,
                        TechComment parent, TechComment originParent, Member createdBy, Member deletedBy,
                        AnonymousMember createdAnonymousBy, AnonymousMember deletedAnonymousBy, TechArticle techArticle,
                        LocalDateTime deletedAt) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.replyTotalCount = replyTotalCount;
        this.parent = parent;
        this.originParent = originParent;
        this.createdBy = createdBy;
        this.deletedBy = deletedBy;
        this.createdAnonymousBy = createdAnonymousBy;
        this.deletedAnonymousBy = deletedAnonymousBy;
        this.techArticle = techArticle;
        this.deletedAt = deletedAt;
    }

    public static TechComment createMainTechComment(CommentContents contents, Member createdBy,
                                                    TechArticle techArticle) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(Count.defaultCount())
                .recommendTotalCount(Count.defaultCount())
                .replyTotalCount(Count.defaultCount())
                .build();
    }

    public static TechComment createRepliedTechComment(CommentContents contents, Member createdBy,
                                                       TechArticle techArticle, TechComment originParent,
                                                       TechComment parent) {
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

    public void modifyCommentContents(CommentContents contents, LocalDateTime contentsLastModifiedAt) {
        this.contents = contents;
        this.contentsLastModifiedAt = contentsLastModifiedAt;
    }

    public boolean isModified() {
        return contentsLastModifiedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void incrementReplyTotalCount() {
        this.replyTotalCount = Count.plusOne(this.replyTotalCount);
    }

    public void decrementReplyTotalCount() {
        this.replyTotalCount = Count.minusOne(this.replyTotalCount);
    }

    public void incrementRecommendTotalCount() {
        this.recommendTotalCount = Count.plusOne(this.recommendTotalCount);
    }

    public void decrementRecommendTotalCount() {
        this.recommendTotalCount = Count.minusOne(this.recommendTotalCount);
    }

    public void incrementBlameTotalCount() {
        this.blameTotalCount = Count.plusOne(this.blameTotalCount);
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }

    public boolean isCreatedAnonymousMember() {
        return this.createdBy == null && this.createdAnonymousBy != null;
    }

    public boolean isCreatedMember() {
        return this.createdBy != null && this.createdAnonymousBy == null;
    }
}
