package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Note: 픽픽픽, 기술블로그 서비스에 신고를 담당하는 엔티티
 * @Author: 장세웅
 * @Since: 2024.09.16
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blame extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id")
    private PickComment pickComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_comment_id")
    private TechComment techComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id")
    private Pick pick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blame_type_id", nullable = false)
    private BlameType blameType;

    @Column(length = 100)
    private String customReason;

    @Builder
    private Blame(PickComment pickComment, TechComment techComment, Pick pick, TechArticle techArticle, Member member,
                  BlameType blameType, String customReason) {
        this.pickComment = pickComment;
        this.techComment = techComment;
        this.pick = pick;
        this.techArticle = techArticle;
        this.member = member;
        this.blameType = blameType;
        this.customReason = customReason;
    }

    public static Blame create(PickComment pickComment, TechComment techComment, Pick pick, TechArticle techArticle,
                               Member member, BlameType blameType) {
        Blame blame = new Blame();
        blame.pick = pick;
        blame.pickComment = pickComment;
        blame.techArticle = techArticle;
        blame.techComment = techComment;
        blame.member = member;
        blame.blameType = blameType;

        return blame;
    }

    public static Blame createBlamePick(Pick pick, Member member, BlameType blameType) {
        Blame blame = new Blame();
        blame.pick = pick;
        blame.member = member;
        blame.blameType = blameType;

        return blame;
    }

    public static Blame createBlamePickWithCustomReason(Pick pick, Member member, BlameType blameType,
                                                        String customReason) {
        Blame blame = createBlamePick(pick, member, blameType);
        blame.customReason = customReason;

        return blame;
    }

    public static Blame createBlamePickComment(Pick pick, PickComment pickComment, Member member, BlameType blameType) {
        Blame blame = new Blame();
        blame.pick = pick;
        blame.pickComment = pickComment;
        blame.member = member;
        blame.blameType = blameType;

        return blame;
    }

    public static Blame createBlamePickCommentWithCustomReason(Pick pick, PickComment pickComment, Member member,
                                                               BlameType blameType, String customReason) {
        Blame blame = createBlamePickComment(pick, pickComment, member, blameType);
        blame.customReason = customReason;

        return blame;
    }

    public static Blame createBlameTechComment(TechArticle techArticle, TechComment techComment, Member member,
                                               BlameType blameType) {
        Blame blame = new Blame();
        blame.techArticle = techArticle;
        blame.techComment = techComment;
        blame.member = member;
        blame.blameType = blameType;

        return blame;
    }

    public static Blame createBlameTechCommentWithCustomReason(TechArticle techArticle, TechComment techComment,
                                                               Member member, BlameType blameType,
                                                               String customReason) {
        Blame blame = createBlameTechComment(techArticle, techComment, member, blameType);
        blame.customReason = customReason;

        return blame;
    }
}
