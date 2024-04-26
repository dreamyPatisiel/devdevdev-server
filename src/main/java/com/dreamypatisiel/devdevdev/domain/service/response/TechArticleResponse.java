package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TechArticleResponse {

    public static final int CONTENTS_MAX_LENGTH = 1000;

    public Long id;
    public String elasticId;
    public String thumbnailUrl;
    public String techArticleUrl;
    public String title;
    public String contents;
    public CompanyResponse company;
    public LocalDate regDate;
    public String author;
    public Long viewTotalCount;
    public Long recommendTotalCount;
    public Long commentTotalCount;
    public Long popularScore;
    public Boolean isBookmarked;
    public Float score;

    @Builder
    private TechArticleResponse(Long id, String elasticId, String thumbnailUrl, String techArticleUrl, String title, String contents,
                                CompanyResponse company, LocalDate regDate, String author, Long viewTotalCount,
                                Long recommendTotalCount, Long commentTotalCount, Long popularScore, Boolean isBookmarked, Float score) {
        this.id = id;
        this.elasticId = elasticId;
        this.thumbnailUrl = thumbnailUrl;
        this.techArticleUrl = techArticleUrl;
        this.title = title;
        this.contents = contents;
        this.company = company;
        this.regDate = regDate;
        this.author = author;
        this.isBookmarked = isBookmarked;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.score = score;
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, CompanyResponse companyResponse) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .techArticleUrl(elasticTechArticle.getTechArticleUrl())
                .title(elasticTechArticle.getTitle())
                .company(companyResponse)
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getAuthor())
                .contents(truncateString(elasticTechArticle.getContents(), CONTENTS_MAX_LENGTH))
                .isBookmarked(false)
                .build();
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, CompanyResponse companyResponse, boolean isBookmarked) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .techArticleUrl(elasticTechArticle.getTechArticleUrl())
                .title(elasticTechArticle.getTitle())
                .company(companyResponse)
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getAuthor())
                .contents(truncateString(elasticTechArticle.getContents(), CONTENTS_MAX_LENGTH))
                .isBookmarked(isBookmarked)
                .build();
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, CompanyResponse companyResponse, Float score) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .techArticleUrl(elasticTechArticle.getTechArticleUrl())
                .title(elasticTechArticle.getTitle())
                .contents(truncateString(elasticTechArticle.getContents(), CONTENTS_MAX_LENGTH))
                .company(companyResponse)
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getAuthor())
                .isBookmarked(false)
                .score(getValidScore(score))
                .build();
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, CompanyResponse companyResponse, Float score, boolean isBookmarked) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .techArticleUrl(elasticTechArticle.getTechArticleUrl())
                .title(elasticTechArticle.getTitle())
                .contents(truncateString(elasticTechArticle.getContents(), CONTENTS_MAX_LENGTH))
                .company(companyResponse)
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getAuthor())
                .score(getValidScore(score))
                .isBookmarked(isBookmarked)
                .build();
    }

    private static Float getValidScore(Float score) {
        return Float.isNaN(score) ? null : score;
    }

    private static String truncateString(String elasticTechArticleContents, int maxLength) {
        if(elasticTechArticleContents.length() <= maxLength) {
            return elasticTechArticleContents;
        }

        return elasticTechArticleContents.substring(0, maxLength);
    }
}
