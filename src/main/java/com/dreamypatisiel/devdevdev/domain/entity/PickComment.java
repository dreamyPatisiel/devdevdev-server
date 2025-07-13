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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_pick_comment_01", columnList = "created_by, pick_id, deletedAt"),
        @Index(name = "idx_pick_comment_02", columnList = "id, created_by, pick_id, deletedAt"),
        @Index(name = "idx_pick_comment_03", columnList = "parent_id, origin_parent_id, deletedAt"),
        @Index(name = "idx_pick_comment_04",
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
    @JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_pick_comment_01"))
    private PickComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_pick_comment_02"))
    private PickComment originParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_pick_comment_03"))
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", foreignKey = @ForeignKey(name = "fk_pick_comment_04"))
    private Member deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_anonymous_by", foreignKey = @ForeignKey(name = "fk_pick_comment_05"))
    private AnonymousMember createdAnonymousBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_anonymous_by", foreignKey = @ForeignKey(name = "fk_pick_comment_06"))
    private AnonymousMember deletedAnonymousBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pick_comment_07"))
    private Pick pick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_vote_id", foreignKey = @ForeignKey(name = "fk_pick_comment_08"))
    private PickVote pickVote;

    @OneToMany(mappedBy = "pickComment")
    private List<PickCommentRecommend> pickCommentRecommends = new ArrayList<>();

    @Builder
    private PickComment(CommentContents contents, Count blameTotalCount, Count recommendTotalCount,
                        Count replyTotalCount, Boolean isPublic, PickComment parent, PickComment originParent,
                        Member createdBy, AnonymousMember createdAnonymousBy, Pick pick, PickVote pickVote) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.replyTotalCount = replyTotalCount;
        this.isPublic = isPublic;
        this.parent = parent;
        this.originParent = originParent;
        this.createdBy = createdBy;
        this.createdAnonymousBy = createdAnonymousBy;
        this.pick = pick;
        this.pickVote = pickVote;
    }

    public static PickComment createPrivateVoteCommentByMember(CommentContents content, Member createdBy, Pick pick) {
        PickComment pickComment = createPickComment(content, null, null);
        pickComment.isPublic = false;
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPublicVoteCommentByMember(CommentContents content, Member createdBy, Pick pick,
                                                              PickVote pickVote) {

        PickComment pickComment = createPickComment(content, null, null);
        pickComment.isPublic = true;
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);
        pickComment.pickVote = pickVote;

        return pickComment;
    }

    // 답글 생성
    public static PickComment createRepliedCommentByMember(CommentContents content, PickComment parent,
                                                           PickComment originParent, Member createdBy, Pick pick) {
        PickComment pickComment = createPickComment(content, parent, originParent);
        pickComment.isPublic = false;
        pickComment.createdBy = createdBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPrivateVoteCommentByAnonymousMember(CommentContents content,
                                                                        AnonymousMember createdAnonymousBy, Pick pick) {
        PickComment pickComment = createPickComment(content, null, null);
        pickComment.isPublic = false;
        pickComment.createdAnonymousBy = createdAnonymousBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPublicVoteCommentByAnonymousMember(CommentContents content,
                                                                       AnonymousMember createdAnonymousBy, Pick pick,
                                                                       PickVote pickVote) {
        PickComment pickComment = createPickComment(content, null, null);
        pickComment.isPublic = true;
        pickComment.createdAnonymousBy = createdAnonymousBy;
        pickComment.changePick(pick);
        pickComment.pickVote = pickVote;

        return pickComment;
    }

    // 답글 생성
    public static PickComment createRepliedCommentByAnonymousMember(CommentContents content, PickComment parent,
                                                                    PickComment originParent, AnonymousMember createdAnonymousBy,
                                                                    Pick pick) {
        PickComment pickComment = createPickComment(content, parent, originParent);
        pickComment.isPublic = false;
        pickComment.createdAnonymousBy = createdAnonymousBy;
        pickComment.changePick(pick);

        return pickComment;
    }

    private static PickComment createPickComment(@Nonnull CommentContents content,
                                                 @Nullable PickComment parent,
                                                 @Nullable PickComment originParent) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.replyTotalCount = Count.defaultCount();
        pickComment.parent = parent;
        pickComment.originParent = originParent;

        return pickComment;
    }

    // 연관관계 편의 메소드
    public void changePick(Pick pick) {
        pick.getPickComments().add(this);
        this.pick = pick;
    }

    public void changeDeletedAtByMember(LocalDateTime now, Member deletedBy) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;
    }

    public void changeDeletedAtByAnonymousMember(LocalDateTime now, AnonymousMember deletedAnonymousBy) {
        this.deletedAt = now;
        this.deletedAnonymousBy = deletedAnonymousBy;
    }

    // 댓글 수정
    public void modifyCommentContents(CommentContents contents, LocalDateTime lastModifiedContentsAt) {
        this.contents = contents;
        this.contentsLastModifiedAt = lastModifiedContentsAt;
    }

    public boolean isModified() {
        return this.contentsLastModifiedAt != null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isDeletedByMember() {
        return this.deletedBy != null;
    }

    public boolean isDeletedByAnonymousMember() {
        return this.deletedAnonymousBy != null;
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
