package com.dreamypatisiel.devdevdev.domain.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
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

    public static Bookmark create(Member member, TechArticle techArticle, boolean status) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .status(status)
                .build();
    }

    public void changeTechArticle(TechArticle techArticle) {
        this.techArticle = techArticle;
    }

    public void changeStatus(boolean status) {
        this.status = status;
    }

    public boolean isBookmarked() {
        return this.status;
    }
}