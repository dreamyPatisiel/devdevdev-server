package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechArticleRecommend extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_member_id")
    private AnonymousMember anonymousMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false)
    private TechArticle techArticle;

    @Builder
    private TechArticleRecommend(boolean status, Member member, AnonymousMember anonymousMember, TechArticle techArticle) {
        this.status = status;
        this.member = member;
        this.anonymousMember = anonymousMember;
        this.techArticle = techArticle;
    }


    public static TechArticleRecommend create(Member member, TechArticle techArticle) {
        return TechArticleRecommend.builder()
                .member(member)
                .techArticle(techArticle)
                .status(true)
                .build();
    }

    public static TechArticleRecommend create(AnonymousMember anonymousMember, TechArticle techArticle) {
        return TechArticleRecommend.builder()
                .anonymousMember(anonymousMember)
                .techArticle(techArticle)
                .status(true)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
        techArticle.getRecommends().add(this);
    }

    public void cancelRecommend() {
        this.status = false;
    }

    public void registerRecommend() {
        this.status = true;
    }

    public boolean isRecommended() {
        return this.status;
    }

    public boolean isAnonymousMemberNotNull() {
        return this.anonymousMember != null;
    }
    public boolean isMemberNotNull() {
        return this.member != null;
    }

}
