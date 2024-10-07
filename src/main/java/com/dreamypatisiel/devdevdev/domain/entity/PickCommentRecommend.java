package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
        @Index(name = "idx__pick_comment_id__member_id", columnList = "pick_comment_id, member_id")
})
public class PickCommentRecommend extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id", nullable = false)
    private PickComment pickComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Boolean recommendedStatus;

    @Builder
    private PickCommentRecommend(PickComment pickComment, Member member, Boolean recommendedStatus) {
        this.pickComment = pickComment;
        this.member = member;
        this.recommendedStatus = recommendedStatus;
    }

    public static PickCommentRecommend create(PickComment pickComment, Member member) {
        PickCommentRecommend pickCommentRecommend = new PickCommentRecommend();
        pickCommentRecommend.pickComment = pickComment;
        pickCommentRecommend.member = member;
        pickCommentRecommend.recommendedStatus = true;

        return pickCommentRecommend;
    }

    // 연관관계 편의 메소드
    public void changePickComment(PickComment pickComment) {
        this.pickComment = pickComment;
        pickComment.getPickCommentRecommends().add(this);
    }

    public boolean isRecommended() {
        return this.recommendedStatus.equals(true);
    }

    public void cancelRecommend() {
        this.recommendedStatus = false;
    }

    public void recommend() {
        this.recommendedStatus = true;
    }
}