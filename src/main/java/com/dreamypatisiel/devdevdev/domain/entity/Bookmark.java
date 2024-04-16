package com.dreamypatisiel.devdevdev.domain.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;

    @Builder
    public Bookmark(Boolean status, Member member, TechArticle techArticle) {
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

    public static Bookmark of(Member member, TechArticle techArticle, Boolean status) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .status(status)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
    }

    public boolean isBookmarked() {
        return this.status;
    }
}