package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    private PickCommentRecommend(PickComment pickComment, Member member) {
        this.pickComment = pickComment;
        this.member = member;
    }

    public static PickCommentRecommend create(PickComment pickComment, Member member) {
        PickCommentRecommend pickCommentRecommend = new PickCommentRecommend();
        pickCommentRecommend.pickComment = pickComment;
        pickCommentRecommend.member = member;

        return pickCommentRecommend;
    }
}