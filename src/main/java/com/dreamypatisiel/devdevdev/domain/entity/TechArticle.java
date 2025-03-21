package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
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
        @Index(name = "idx_tech_article_01", columnList = "elasticId")
})
public class TechArticle extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Title title;

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

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "tech_article_url")
    )
    private Url techArticleUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column
    private String elasticId;

    @OneToMany(mappedBy = "techArticle")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "techArticle")
    private List<TechArticleRecommend> recommends = new ArrayList<>();

    @Builder
    private TechArticle(Title title, Count viewTotalCount, Count recommendTotalCount, Count commentTotalCount,
                        Count popularScore,
                        Url techArticleUrl, Company company, String elasticId) {
        this.title = title;
        this.techArticleUrl = techArticleUrl;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.company = company;
        this.elasticId = elasticId;
    }

    public static TechArticle createTechArticle(ElasticTechArticle elasticTechArticle, Company company) {
        TechArticle techArticle = TechArticle.builder()
                .title(new Title(elasticTechArticle.getTitle()))
                .techArticleUrl(new Url(elasticTechArticle.getTechArticleUrl()))
                .viewTotalCount(new Count(elasticTechArticle.getViewTotalCount()))
                .recommendTotalCount(new Count(elasticTechArticle.getRecommendTotalCount()))
                .commentTotalCount(new Count(elasticTechArticle.getCommentTotalCount()))
                .popularScore(new Count(elasticTechArticle.getPopularScore()))
                .elasticId(elasticTechArticle.getId())
                .build();

        techArticle.changeCompany(company);

        return techArticle;
    }

    public static TechArticle createTechArticle(Title title, Url techArticleUrl, Count viewTotalCount,
                                                Count recommendTotalCount, Count commentTotalCount, Count popularScore,
                                                String elasticId, Company company) {
        return TechArticle.builder()
                .title(title)
                .techArticleUrl(techArticleUrl)
                .viewTotalCount(viewTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .elasticId(elasticId)
                .company(company)
                .build();
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
