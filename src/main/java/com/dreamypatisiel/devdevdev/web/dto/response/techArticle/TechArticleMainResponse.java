package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.Objects;

import static com.dreamypatisiel.devdevdev.web.dto.util.TechArticleResponseUtils.isBookmarkedByMember;

@Data
public class TechArticleMainResponse {

    public static final int CONTENTS_MAX_LENGTH = 1000;

    public final Long id;
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
    public final Boolean isLogoImage;
    public final Boolean isBookmarked;
    public final Float score;

    @Builder
    private TechArticleMainResponse(Long id, String title, String contents, String author, CompanyResponse company,
                                    LocalDate regDate, String thumbnailUrl, String techArticleUrl,
                                    Long viewTotalCount, Long recommendTotalCount, Long commentTotalCount, Long popularScore,
                                    Boolean isLogoImage, Boolean isBookmarked, Float score) {
        this.id = id;
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
        this.isLogoImage = isLogoImage;
        this.isBookmarked = isBookmarked;
        this.score = score;
    }


    public static TechArticleMainResponse of(TechArticle techArticle, Member member) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleMainResponse.builder()
                .id(techArticle.getId())
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl().getUrl(), companyResponse))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isLogoImage(ObjectUtils.isEmpty(techArticle.getThumbnailUrl().getUrl()))
                .isBookmarked(isBookmarkedByMember(techArticle, member))
                .build();
    }

    public static TechArticleMainResponse of(TechArticle techArticle) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleMainResponse.builder()
                .id(techArticle.getId())
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl().getUrl(), companyResponse))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isLogoImage(ObjectUtils.isEmpty(techArticle.getThumbnailUrl().getUrl()))
                .isBookmarked(false)
                .build();
    }

    public static TechArticleMainResponse of(TechArticle techArticle, Member member, Float score) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleMainResponse.builder()
                .id(techArticle.getId())
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl().getUrl(), companyResponse))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isLogoImage(ObjectUtils.isEmpty(techArticle.getThumbnailUrl().getUrl()))
                .isBookmarked(isBookmarkedByMember(techArticle, member))
                .score(getValidScore(score))
                .build();
    }

    public static TechArticleMainResponse of(TechArticle techArticle, Float score) {
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());
        return TechArticleMainResponse.builder()
                .id(techArticle.getId())
                .title(techArticle.getTitle().getTitle())
                .contents(truncateString(techArticle.getContents(), CONTENTS_MAX_LENGTH))
                .author(techArticle.getAuthor())
                .company(companyResponse)
                .regDate(techArticle.getRegDate())
                .thumbnailUrl(getThumbnailUrl(techArticle.getThumbnailUrl().getUrl(), companyResponse))
                .techArticleUrl(techArticle.getTechArticleUrl().getUrl())
                .viewTotalCount(techArticle.getViewTotalCount().getCount())
                .recommendTotalCount(techArticle.getRecommendTotalCount().getCount())
                .commentTotalCount(techArticle.getCommentTotalCount().getCount())
                .popularScore(techArticle.getPopularScore().getCount())
                .isLogoImage(ObjectUtils.isEmpty(techArticle.getThumbnailUrl().getUrl()))
                .isBookmarked(false)
                .score(getValidScore(score))
                .build();
    }

    private static String getThumbnailUrl(String thumbnailUrl, CompanyResponse companyResponse) {
        // 썸네일 이미지가 없다면 회사 로고로 내려준다.
        if (ObjectUtils.isEmpty(thumbnailUrl)) {
            return companyResponse.getOfficialImageUrl();
        }
        return thumbnailUrl;
    }

    private static Float getValidScore(Float score) {
        return Objects.isNull(score) || Float.isNaN(score) ? null : score;
    }

    private static String truncateString(String contents, int maxLength) {
        if (ObjectUtils.isEmpty(contents) || contents.length() <= maxLength) {
            return contents;
        }
        return contents.substring(0, maxLength);
    }
}
