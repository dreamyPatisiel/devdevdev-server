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
        @Index(name = "idx__member__pick__deleted_at", columnList = "member_id, pick_id, deletedAt"),
        @Index(name = "idx__comment__member__pick__deleted_at", columnList = "id, member_id, pick_id, deletedAt")
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
            column = @Column(name = "blame_total_count")
    )
    private Count blameTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count")
    )
    private Count recommendTotalCount;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isPublic; // true: 투표 선택지 공개, false: 투표 선택지 비공개

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_vote_id")
    private PickVote pickVote;


    @Builder
    private PickComment(CommentContents contents, Count blameTotalCount, Count recommendTotalCount, Boolean isPublic,
                        Member member,
                        Pick pick, PickVote pickVote) {
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.isPublic = isPublic;
        this.member = member;
        this.pick = pick;
        this.pickVote = pickVote;
    }

    public static PickComment createPrivateVoteComment(CommentContents content, Member member, Pick pick) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.isPublic = false;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.member = member;
        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPublicVoteComment(CommentContents content, Member member, Pick pick,
                                                      PickVote pickVote) {
        PickComment pickComment = new PickComment();
        pickComment.contents = content;
        pickComment.isPublic = true;
        pickComment.blameTotalCount = Count.defaultCount();
        pickComment.recommendTotalCount = Count.defaultCount();
        pickComment.member = member;
        pickComment.changePick(pick);
        pickComment.pickVote = pickVote;

        return pickComment;
    }

    // 연관관계 편의 메소드
    public void changePick(Pick pick) {
        pick.getPickComments().add(this);
        this.pick = pick;
    }

    public void changeDeletedAt(LocalDateTime now) {
        this.deletedAt = now;
    }

    public void changeCommentContents(CommentContents contents) {
        this.contents = contents;
    }
}
