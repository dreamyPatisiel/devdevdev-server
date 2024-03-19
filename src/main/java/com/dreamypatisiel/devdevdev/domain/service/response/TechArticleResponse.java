package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 제외
@RequiredArgsConstructor
public class TechArticleResponse {

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    public Long id;
    public String elasticId;
    public String thumbnailUrl;
    public String title;
    public String company;
    public LocalDate regDate;
    public String author;
    public String description;
    public Long viewTotalCount;
    public Long recommendTotalCount;
    public Long commentTotalCount;
    public Long popularScore;
    public Boolean isBookmarked;
    public Float score;

    @Builder
    private TechArticleResponse(Long id, String elasticId, String thumbnailUrl, String title, String company, LocalDate regDate,
                               String author, String description, Long viewTotalCount, Long recommendTotalCount,
                               Long commentTotalCount, Long popularScore, Boolean isBookmarked, Float score) {
        this.id = id;
        this.elasticId = elasticId;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.company = company;
        this.regDate = regDate;
        this.author = author;
        this.description = description;
        this.isBookmarked = isBookmarked;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.score = score;
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .title(elasticTechArticle.getTitle())
                .company(elasticTechArticle.getCompany())
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getTechArticleUrl())
                .description(truncateString(elasticTechArticle.getContents(), DESCRIPTION_MAX_LENGTH))
                .build();
    }
    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, boolean isBookmarked) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .title(elasticTechArticle.getTitle())
                .company(elasticTechArticle.getCompany())
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getTechArticleUrl())
                .description(truncateString(elasticTechArticle.getContents(), DESCRIPTION_MAX_LENGTH))
                .isBookmarked(isBookmarked)
                .build();
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, Float score) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .title(elasticTechArticle.getTitle())
                .company(elasticTechArticle.getCompany())
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getTechArticleUrl())
                .description(truncateString(elasticTechArticle.getContents(), DESCRIPTION_MAX_LENGTH))
                .score(score)
                .build();
    }

    public static TechArticleResponse of(ElasticTechArticle elasticTechArticle, TechArticle techArticle, Float score, boolean isBookmarked) {
        return TechArticleResponse.builder()
                .id(techArticle.getId())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .elasticId(elasticTechArticle.getId())
                .thumbnailUrl(elasticTechArticle.getThumbnailUrl())
                .title(elasticTechArticle.getTitle())
                .company(elasticTechArticle.getCompany())
                .regDate(elasticTechArticle.getRegDate())
                .author(elasticTechArticle.getTechArticleUrl())
                .description(truncateString(elasticTechArticle.getContents(), DESCRIPTION_MAX_LENGTH))
                .score(score)
                .isBookmarked(isBookmarked)
                .build();
    }

    private static String truncateString(String string, int maxLength) {
        if(string.length() <= maxLength) return string;
        return string.substring(0, maxLength);
    }
}
