package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechArticle extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @JoinColumn(name = "company_id")
    private Company company;

    private String elasticId;

    @OneToMany(mappedBy = "techArticle")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Builder
    private TechArticle(Count viewTotalCount, Count recommendTotalCount, Count commentTotalCount, Count popularScore,
                       Url techArticleUrl, Company company, String elasticId) {
        this.techArticleUrl = techArticleUrl;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.company = company;
        this.elasticId = elasticId;
    }
    public static TechArticle from(ElasticTechArticle elasticTechArticle) {
        return TechArticle.builder()
                .techArticleUrl(new Url(elasticTechArticle.getTechArticleUrl()))
                .viewTotalCount(new Count(elasticTechArticle.getViewTotalCount()))
                .recommendTotalCount(new Count(elasticTechArticle.getRecommendTotalCount()))
                .commentTotalCount(new Count(elasticTechArticle.getCommentTotalCount()))
                .popularScore(new Count(elasticTechArticle.getPopularScore()))
                .elasticId(elasticTechArticle.getId())
                .build();
    }

    public void changeBookmarks(List<Bookmark> bookmarks) {
        for(Bookmark bookmark : bookmarks) {
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
}
