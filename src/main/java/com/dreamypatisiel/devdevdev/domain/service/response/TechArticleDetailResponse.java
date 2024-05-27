package com.dreamypatisiel.devdevdev.domain.service.response;

import static com.dreamypatisiel.devdevdev.domain.service.response.util.TechArticleResponseUtils.isBookmarkedByMember;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
public class TechArticleDetailResponse {

    private static final int CONTENTS_MAX_LENGTH = 1000;

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

    @Builder
    private TechArticleDetailResponse(String elasticId, String thumbnailUrl, String techArticleUrl, String title,
                                      String contents,
                                      CompanyResponse company, LocalDate regDate, String author, Long viewTotalCount,
                                      Long recommendTotalCount, Long commentTotalCount, Long popularScore,
                                      Boolean isBookmarked) {
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
    }

    public static TechArticleDetailResponse of(TechArticle techArticle,
                                               ElasticTechArticle elasticTechArticle,
                                               CompanyResponse companyResponse) {
        return TechArticleDetailResponse.builder()
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

    public static TechArticleDetailResponse of(TechArticle techArticle,
                                               ElasticTechArticle elasticTechArticle,
                                               CompanyResponse companyResponse,
                                               Member member) {
        return TechArticleDetailResponse.builder()
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

    private static String truncateString(String elasticTechArticleContents, int maxLength) {
        if (elasticTechArticleContents.length() <= maxLength) {
            return elasticTechArticleContents;
        }

        return elasticTechArticleContents.substring(0, maxLength);
    }
}
