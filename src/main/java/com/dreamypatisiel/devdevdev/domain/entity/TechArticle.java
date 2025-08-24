package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_tech_article_01", columnList = "title"),
        @Index(name = "idx_tech_article_02", columnList = "contents"),
        @Index(name = "idx_tech_article_03", columnList = "title, contents")
})
public class TechArticle extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Title title;

    @Column(columnDefinition = "LONGTEXT")
    private String contents;

    @Column(length = 255)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private LocalDate regDate;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "tech_article_url")
    )
    private Url techArticleUrl;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "thumbnail_url")
    )
    private Url thumbnailUrl;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "view_total_count")
    )
    private Count viewTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count")
    )
    private Count recommendTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "comment_total_count")
    )
    private Count commentTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "popular_score")
    )
    private Count popularScore;

    @OneToMany(mappedBy = "techArticle")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "techArticle")
    private List<TechArticleRecommend> recommends = new ArrayList<>();

    public TechArticle(Long id) {
        this.id = id;
    }

    @Builder
    private TechArticle(Title title, String contents, String author, Company company,
                        LocalDate regDate, Url techArticleUrl, Url thumbnailUrl,
                        Count viewTotalCount, Count recommendTotalCount, Count commentTotalCount, Count popularScore) {
        this.title = title;
        this.contents = contents;
        this.author = author;
        this.company = company;
        this.regDate = regDate;
        this.techArticleUrl = techArticleUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
    }

    public void changeBookmarks(List<Bookmark> bookmarks) {
        for (Bookmark bookmark : bookmarks) {
            bookmark.changeTechArticle(this);
            this.getBookmarks().add(bookmark);
        }
    }

    public void changePopularScore(TechArticlePopularScorePolicy policy) {
        this.popularScore = this.calculatePopularScore(policy);
    }

    private Count calculatePopularScore(TechArticlePopularScorePolicy policy) {
        return policy.calculatePopularScore(this);
    }

    public void changeCompany(Company company) {
        company.getTechArticles().add(this);
        this.company = company;
    }

    public void incrementViewTotalCount() {
        this.viewTotalCount = Count.plusOne(this.viewTotalCount);
    }

    public void incrementCommentCount() {
        this.commentTotalCount = Count.plusOne(this.commentTotalCount);
    }

    public void decrementCommentCount() {
        this.commentTotalCount = Count.minusOne(this.commentTotalCount);
    }

    public void incrementRecommendTotalCount() {
        this.recommendTotalCount = Count.plusOne(this.recommendTotalCount);
    }

    public void decrementRecommendTotalCount() {
        this.recommendTotalCount = Count.minusOne(this.recommendTotalCount);
    }
}
