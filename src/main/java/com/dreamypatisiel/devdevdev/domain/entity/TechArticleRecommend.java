package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechArticleRecommend extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO Mysql 컬럼 추가 필요
    @Column(nullable = false)
    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false)
    private TechArticle techArticle;

    @Builder
    private TechArticleRecommend(Boolean status, Member member, TechArticle techArticle) {
        this.status = status;
        this.member = member;
        this.techArticle = techArticle;
    }

    public static TechArticleRecommend from(Member member, TechArticle techArticle) {
        return TechArticleRecommend.builder()
                .member(member)
                .techArticle(techArticle)
                .build();
    }

    public static TechArticleRecommend create(Member member, TechArticle techArticle) {
        return TechArticleRecommend.builder()
                .member(member)
                .techArticle(techArticle)
                .status(true)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
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
}
