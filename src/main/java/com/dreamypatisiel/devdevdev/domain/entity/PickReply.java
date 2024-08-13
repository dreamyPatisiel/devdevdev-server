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
        @Index(name = "idx__created_by__pick__deleted_at", columnList = "created_by, pick_id, deletedAt"),
        @Index(name = "idx__reply__created_by__pick__deleted_at", columnList = "id, created_by, pick_id, deletedAt")
})
public class PickReply extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "commentContent",
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

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private Member deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id", nullable = false)
    private PickComment pickComment;

    @Builder
    private PickReply(CommentContents contents, Count blameTotalCount, Count recommendTotalCount,
                      LocalDateTime deletedAt, Member createdBy, Member deletedBy, PickComment pickComment) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.deletedAt = deletedAt;
        this.createdBy = createdBy;
        this.deletedBy = deletedBy;
        this.pickComment = pickComment;
    }

    public static PickReply create(CommentContents contents, Member createdBy, PickComment pickComment) {
        PickReply pickReply = new PickReply();
        pickReply.contents = contents;
        pickReply.blameTotalCount = Count.defaultCount();
        pickReply.recommendTotalCount = Count.defaultCount();
        pickReply.createdBy = createdBy;
        pickReply.pickComment = pickComment;

        return pickReply;
    }

    public void changeDeletedAt(LocalDateTime deletedAt, Member deletedBy) {
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }
}
