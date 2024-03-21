package com.dreamypatisiel.devdevdev.domain.entity;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;

    @Builder
    private Bookmark(Member member, TechArticle techArticle) {
        this.member = member;
        this.techArticle = techArticle;
    }

    public static Bookmark from(Member member, TechArticle techArticle) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
    }

}