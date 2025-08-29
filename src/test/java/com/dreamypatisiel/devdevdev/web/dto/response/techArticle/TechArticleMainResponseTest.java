package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TechArticleMainResponseTest {

    @Test
    @DisplayName("기술블로그의 썸네일 이미지가 있다면 썸네일 이미지로 설정되어야 한다.")
    public void setThumbnailImageWhenPresent() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png",
                "https://officialUrl.com", "https://careerUrl.com");
        TechArticle techArticle = createTechArticle(company, new Url("http://thumbnailImage.com/image.png"));

        // when
        TechArticleMainResponse techArticleMainResponse = TechArticleMainResponse.of(techArticle);

        // then
        assertEquals("http://thumbnailImage.com/image.png", techArticleMainResponse.getThumbnailUrl());
        assertFalse(techArticleMainResponse.getIsLogoImage());
    }

    @Test
    @DisplayName("기술블로그의 썸네일 이미지가 없다면 회사 로고 이미지로 대체하고, isLogoImage가 true로 설정되어야 한다.")
    public void setLogoImageWhenThumbnailIsAbsent() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png",
                "https://officialUrl.com", "https://careerUrl.com");
        TechArticle techArticle = createTechArticle(company, null);

        // when
        TechArticleMainResponse techArticleMainResponse = TechArticleMainResponse.of(techArticle);

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

    private TechArticle createTechArticle(Company company, Url thumbnailUrl) {
        return TechArticle.builder()
                .title(new Title("타이틀 "))
                .contents("내용 ")
                .company(company)
                .author("작성자")
                .regDate(LocalDate.now())
                .techArticleUrl(new Url("https://example.com/article"))
                .thumbnailUrl(thumbnailUrl)
                .commentTotalCount(new Count(1))
                .recommendTotalCount(new Count(1))
                .viewTotalCount(new Count(1))
                .popularScore(new Count(1))
                .build();
    }
}