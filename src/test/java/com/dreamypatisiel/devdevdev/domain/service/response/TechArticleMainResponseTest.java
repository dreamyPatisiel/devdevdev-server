package com.dreamypatisiel.devdevdev.domain.service.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TechArticleMainResponseTest {

    @Test
    @DisplayName("기술블로그 썸네일 이미지가 없으면 회사 로고 이미지가 썸네일로 들어간다.")
    void companyLogoAsDefaultThumbnailWhenNoThumbnail() {
        // given
        ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId", "타이틀", "내용",
                "http://example.com/", "설명", null, "작성자",
                "꿈빛 파티시엘", 1L, 1L, 1L, 1L, 1L);

        Company company = createCompany("꿈빛 파티시엘", "https://companylogo.png", "https://example.com",
                "https://example.com");

        TechArticle techArticle = TechArticle.of(elasticTechArticle, company);
        CompanyResponse companyResponse = CompanyResponse.from(company);

        // when
        TechArticleMainResponse techArticleMainResponse = TechArticleMainResponse.of(techArticle, elasticTechArticle,
                companyResponse);

        // then
        assertThat(techArticleMainResponse.getThumbnailUrl()).isEqualTo(company.getThumbnailImageUrl());
    }

    private static Company createCompany(String companyName, String thumbnailImageUrl, String thumbnailUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .thumbnailImageUrl(thumbnailImageUrl)
                .careerUrl(new Url(thumbnailUrl))
                .thumbnailUrl(new Url(careerUrl))
                .build();
    }

    private static ElasticTechArticle createElasticTechArticle(String id, String title,
                                                               String contents,
                                                               String techArticleUrl,
                                                               String description, String thumbnailUrl, String author,
                                                               String company, Long companyId,
                                                               Long viewTotalCount, Long recommendTotalCount,
                                                               Long commentTotalCount,
                                                               Long popularScore) {
        return ElasticTechArticle.builder()
                .id(id)
                .title(title)
                .contents(contents)
                .techArticleUrl(techArticleUrl)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .company(company)
                .companyId(companyId)
                .viewTotalCount(viewTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .build();
    }
}