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
import lombok.NoArgsConstructor;

@Entity
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
        blame.pickComment = pickComment;
        blame.techComment = techComment;
        blame.pick = pick;
        blame.techArticle = techArticle;
        blame.member = member;
        blame.blameType = blameType;

        return blame;
    }

    public static Blame createWithCustomReason(PickComment pickComment, TechComment techComment, Pick pick,
                                               TechArticle techArticle,
                                               Member member, BlameType blameType, String customReason) {
        Blame blame = create(pickComment, techComment, pick, techArticle, member, blameType);
        blame.customReason = customReason;

        return blame;
    }
}
