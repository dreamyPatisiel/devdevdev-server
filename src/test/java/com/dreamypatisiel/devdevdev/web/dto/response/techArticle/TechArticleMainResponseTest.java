package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TechArticleMainResponseTest {

    @Test
    @DisplayName("ElasticTechArticle의 썸네일 이미지가 있다면 썸네일 이미지로 설정되어야 한다.")
    public void setThumbnailImageWhenPresent() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png",
                "https://officialUrl.com", "https://careerUrl.com");

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);

        ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId", "타이틀", LocalDate.now(),
                "내용", "http://example.com/", "설명", "http://thumbnailImage.com/image.png", "작성자",
                company.getName().getCompanyName(), company.getId(), 0L, 0L, 0L, 0L);

        CompanyResponse companyResponse = CompanyResponse.from(company);

        // when
        TechArticleMainResponse techArticleMainResponse = TechArticleMainResponse
                .of(techArticle, elasticTechArticle, companyResponse);

        // then
        assertEquals("http://thumbnailImage.com/image.png", techArticleMainResponse.getThumbnailUrl());
        assertFalse(techArticleMainResponse.getIsLogoImage());
    }

    @Test
    @DisplayName("ElasticTechArticle의 썸네일 이미지가 없다면 회사 로고 이미지로 대체하고, isLogoImage가 true로 설정되어야 한다.")
    public void setLogoImageWhenThumbnailIsAbsent() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png",
                "https://officialUrl.com", "https://careerUrl.com");

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);

        ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId", "타이틀", LocalDate.now(),
                "내용", "http://example.com/", "설명", null, "작성자",
                company.getName().getCompanyName(), company.getId(), 0L, 0L, 0L, 0L);

        CompanyResponse companyResponse = CompanyResponse.from(company);

        // when
        TechArticleMainResponse techArticleMainResponse = TechArticleMainResponse
                .of(techArticle, elasticTechArticle, companyResponse);

        // then
        assertEquals(company.getOfficialImageUrl().getUrl(), techArticleMainResponse.getThumbnailUrl());
        assertTrue(techArticleMainResponse.getIsLogoImage());
    }

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialUrl(new Url(officialUrl))
                .careerUrl(new Url(careerUrl))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }

    private static ElasticTechArticle createElasticTechArticle(String id, String title, LocalDate regDate,
                                                               String contents, String techArticleUrl,
                                                               String description, String thumbnailUrl, String author,
                                                               String company, Long companyId,
                                                               Long viewTotalCount, Long recommendTotalCount,
                                                               Long commentTotalCount, Long popularScore) {
        return ElasticTechArticle.builder()
                .id(id)
                .title(title)
                .regDate(regDate)
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