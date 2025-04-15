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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false)
    private TechArticle techArticle;

    @Builder
    private Bookmark(Boolean status, Member member, TechArticle techArticle) {
        this.status = status;
        this.member = member;
        this.techArticle = techArticle;
    }

    public static Bookmark from(Member member, TechArticle techArticle) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .build();
    }

    public static Bookmark create(Member member, TechArticle techArticle) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .status(true)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
    }

    public void cancelBookmark() {
        this.status = false;
    }

    public void registerBookmark() {
        this.status = true;
    }

    public boolean isBookmarked() {
        return this.status;
    }
}