package com.dreamypatisiel.devdevdev.domain.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;

    @Builder
    public Bookmark(Boolean value, Member member, TechArticle techArticle) {
        this.value = value;
        this.member = member;
        this.techArticle = techArticle;
    }

    public static Bookmark from(Member member, TechArticle techArticle) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .build();
    }

    public static Bookmark from(Member member, TechArticle techArticle, boolean value) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .value(value)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
    }

    public void toggleBookmark(){
        this.value = !value;
    }

    public boolean isBookmarked() {
        return this.value;
    }
}