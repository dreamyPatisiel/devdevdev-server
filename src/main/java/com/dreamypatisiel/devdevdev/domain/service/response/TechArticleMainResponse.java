package com.dreamypatisiel.devdevdev.domain.service.response;

import static com.dreamypatisiel.devdevdev.domain.service.response.util.TechArticleResponseUtils.isBookmarkedByMember;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

@Data
public class TechArticleMainResponse {

    public static final int CONTENTS_MAX_LENGTH = 1000;

    public final Long id;
    public final String elasticId;
    public final String thumbnailUrl;
    public final String techArticleUrl;
    public final String title;
    public final String contents;
    public final CompanyResponse company;
    public final LocalDate regDate;
    public final String author;
    public final Long viewTotalCount;
    public final Long recommendTotalCount;
    public final Long commentTotalCount;
    public final Long popularScore;
    public final Boolean isBookmarked;
    public final Float score;

    @Builder
    private TechArticleMainResponse(Long id, String elasticId, String thumbnailUrl, String techArticleUrl, String title,
                                    String contents,
                                    CompanyResponse company, LocalDate regDate, String author, Long viewTotalCount,
                                    Long recommendTotalCount, Long commentTotalCount, Long popularScore,
                                    Boolean isBookmarked, Float score) {
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

    public static TechArticleMainResponse of(TechArticle techArticle,
                                             ElasticTechArticle elasticTechArticle,
                                             CompanyResponse companyResponse) {
        return TechArticleMainResponse.builder()
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

    public static TechArticleMainResponse of(TechArticle techArticle,
                                             ElasticTechArticle elasticTechArticle,
                                             CompanyResponse companyResponse,
                                             Member member) {
        return TechArticleMainResponse.builder()
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
                .isBookmarked(isBookmarkedByMember(techArticle, member))
                .build();
    }

    public static TechArticleMainResponse of(TechArticle techArticle,
                                             ElasticTechArticle elasticTechArticle,
                                             CompanyResponse companyResponse,
                                             Float score) {
        return TechArticleMainResponse.builder()
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

    public static TechArticleMainResponse of(TechArticle techArticle,
                                             ElasticTechArticle elasticTechArticle,
                                             CompanyResponse companyResponse,
                                             Float score,
                                             Member member) {
        return TechArticleMainResponse.builder()
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
                .isBookmarked(isBookmarkedByMember(techArticle, member))
                .build();
    }

    private static Float getValidScore(Float score) {
        return Objects.isNull(score) || Float.isNaN(score) ? null : score;
    }

    private static String truncateString(String elasticTechArticleContents, int maxLength) {
        if (elasticTechArticleContents.length() <= maxLength) {
            return elasticTechArticleContents;
        }

        return elasticTechArticleContents.substring(0, maxLength);
    }
}
