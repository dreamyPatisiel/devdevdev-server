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
        @Index(name = "idx__created_by__pick__deleted_at", columnList = "created_by, pick_id, deletedAt"),
        @Index(name = "idx__comment__created_by__pick__deleted_at", columnList = "id, created_by, pick_id, deletedAt"),
        @Index(name = "idx__parent__origin_parent__deleted_at", columnList = "parent_id, origin_parent_id, deletedAt"),
        @Index(name = "idx__comment_01",
                columnList = "id, pick_id, parent_id, origin_parent_id, isPublic, recommendTotalCount, replyTotalCount")
})
public class PickComment extends BasicTime {

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

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isPublic; // true: 투표 선택지 공개, false: 투표 선택지 비공개/답글

    private LocalDateTime deletedAt;
    private LocalDateTime contentsLastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private PickComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_parent_id", referencedColumnName = "id")
    private PickComment originParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private Member deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_vote_id")
    private PickVote pickVote;

    @OneToMany(mappedBy = "pickComment")
    private List<PickCommentRecommend> pickCommentRecommends = new ArrayList<>();

    @Builder
    private PickComment(CommentContents contents, Count blameTotalCount, Count recommendTotalCount,
                        Count replyTotalCount, Boolean isPublic, PickComment parent, PickComment originParent,
                        Member createdBy, Pick pick, PickVote pickVote) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.replyTotalCount = replyTotalCount;
        this.isPublic = isPublic;
        this.parent = parent;
        this.originParent = originParent;
        this.createdBy = createdBy;
        this.pick = pick;
        this.pickVote = pickVote;
    }

    public static PickComment createPrivateVoteComment(CommentContents content, Member createdBy, Pick pick) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.isPublic = false;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.replyTotalCount = Count.defaultCount();
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPublicVoteComment(CommentContents content, Member createdBy, Pick pick,
                                                      PickVote pickVote) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.isPublic = true;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.replyTotalCount = Count.defaultCount();
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);
        pickComment.pickVote = pickVote;

        return pickComment;
    }

    // 답글 생성
    public static PickComment createRepliedComment(CommentContents content, PickComment parent,
                                                   PickComment originParent, Member createdBy, Pick pick) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.isPublic = false;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.replyTotalCount = Count.defaultCount();
        pickComment.parent = parent;
        pickComment.originParent = originParent;
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    // 연관관계 편의 메소드
    public void changePick(Pick pick) {
        pick.getPickComments().add(this);
        this.pick = pick;
    }

    public void changeDeletedAt(LocalDateTime now, Member deletedBy) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;
    }

    // 댓글 수정
    public void modifyCommentContents(CommentContents contents, LocalDateTime lastModifiedContentsAt) {
        this.contents = contents;
        this.contentsLastModifiedAt = lastModifiedContentsAt;
    }

    public boolean isModified() {
        return contentsLastModifiedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }

    public void incrementReplyTotalCount() {
        this.replyTotalCount = Count.plusOne(this.replyTotalCount);
    }

    public void incrementRecommendTotalCount() {
        this.recommendTotalCount = Count.plusOne(this.recommendTotalCount);
    }

    public void incrementBlameTotalCount() {
        this.blameTotalCount = Count.plusOne(this.blameTotalCount);
    }

    public void decrementRecommendTotalCount() {
        this.recommendTotalCount = Count.minusOne(this.recommendTotalCount);
    }

    public boolean isVotePrivate() {
        return this.isPublic.equals(false);
    }
}
