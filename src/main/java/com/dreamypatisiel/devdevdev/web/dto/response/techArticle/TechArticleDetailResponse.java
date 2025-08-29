package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import java.time.LocalDate;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import static com.dreamypatisiel.devdevdev.web.dto.util.TechArticleResponseUtils.*;

@Data
public class TechArticleDetailResponse {

    private static final int CONTENTS_MAX_LENGTH = 1000;

    public final String title;
    public final String contents;
    public final String author;
    public final CompanyResponse company;
    public final LocalDate regDate;
    public final String techArticleUrl;
    public final String thumbnailUrl;
    public final Long viewTotalCount;
    public final Long recommendTotalCount;
    public final Long commentTotalCount;
    public final Long popularScore;
    public final Boolean isBookmarked;
    public final Boolean isRecommended;

    @Builder
    private TechArticleDetailResponse(String title, String contents, String author, CompanyResponse company, LocalDate regDate,
                                      String thumbnailUrl, String techArticleUrl,
                                      Long viewTotalCount, Long recommendTotalCount, Long commentTotalCount, Long popularScore,
                                      Boolean isBookmarked, Boolean isRecommended) {
        this.title = title;
        this.contents = contents;
        this.author = author;
        this.company = company;
        this.regDate = regDate;
        this.thumbnailUrl = thumbnailUrl;
        this.techArticleUrl = techArticleUrl;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.isBookmarked = isBookmarked;
        this.isRecommended = isRecommended;
    }

    public static TechArticleDetailResponse of(TechArticle techArticle, AnonymousMember anonymousMember) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleDetailResponse.builder()
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl()))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isRecommended(isRecommendedByAnonymousMember(techArticle, anonymousMember))
                .isBookmarked(false)
                .build();
    }

    public static TechArticleDetailResponse of(TechArticle techArticle, Member member) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleDetailResponse.builder()
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl()))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isRecommended(isRecommendedByMember(techArticle, member))
                .isBookmarked(isBookmarkedByMember(techArticle, member))
                .build();
    }

    private static String truncateString(String contents, int maxLength) {
        if (ObjectUtils.isEmpty(contents) || contents.length() <= maxLength) {
            return contents;
        }
        return contents.substring(0, maxLength);
    }

    private static String getThumbnailUrl(Url thumbnailUrl) {
        if (ObjectUtils.isEmpty(thumbnailUrl)) {
            return null;
        }
        return thumbnailUrl.getUrl();
    }
}
