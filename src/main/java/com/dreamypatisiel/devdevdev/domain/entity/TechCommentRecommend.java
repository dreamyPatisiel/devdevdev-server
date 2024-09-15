package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechCommentRecommend extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_comment_id", nullable = false)
    private TechComment techComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Boolean recommendedStatus;

    @Builder
    private TechCommentRecommend(TechComment techComment, Member member, Boolean recommendedStatus) {
        this.techComment = techComment;
        this.member = member;
        this.recommendedStatus = recommendedStatus;
    }

    public static TechCommentRecommend create(TechComment techComment, Member member) {
        return TechCommentRecommend.builder()
                .techComment(techComment)
                .member(member)
                .recommendedStatus(true)
                .build();
    }

    public void cancelRecommend() {
        this.recommendedStatus = false;
    }

    public void recommend() {
        this.recommendedStatus = true;
    }

    public boolean isRecommended() {
        return this.recommendedStatus;
    }
}