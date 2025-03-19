package com.dreamypatisiel.devdevdev.elastic.domain.service;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

//@ExtendWith(ContainerExtension.class)
//@SpringBootTest(classes = {ElasticsearchTestConfig.class})
@SpringBootTest
@Transactional
public class ElasticsearchSupportTest {

    public static final int TEST_ARTICLES_COUNT = 20;
    public static TechArticle firstTechArticle;
    public static Company company;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository,
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {
        company = createCompany("꿈빛 파티시엘", "https://example.net/image.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        // 엘라스틱 기술블로그 데이터를 최신순->오래된순, 조회수많은순->적은순, 댓글많은순->적은순의 순서로 생성한다.
        LocalDate baseDate = LocalDate.of(2024, 8, 30);
        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= TEST_ARTICLES_COUNT; i++) {
            ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId_" + i, "타이틀_" + i,
                    baseDate.minusDays(i), "내용", "http://example.com/" + i, "설명", "http://example.com/", "작성자",
                    company.getName().getCompanyName(), company.getId(), (long) TEST_ARTICLES_COUNT - i,
                    (long) TEST_ARTICLES_COUNT - i, (long) TEST_ARTICLES_COUNT - i,
                    (long) (TEST_ARTICLES_COUNT - i) * 10);
            elasticTechArticles.add(elasticTechArticle);
        }
        Iterable<ElasticTechArticle> elasticTechArticleIterable = elasticTechArticleRepository.saveAll(
                elasticTechArticles);

        // 엘라스틱 기술블로그를 토대로 RDB 기술블로그 데이터를 생성한다.
        List<TechArticle> techArticles = new ArrayList<>();
        for (ElasticTechArticle elasticTechArticle : elasticTechArticleIterable) {
            TechArticle techArticle = TechArticle.createTechArticle(elasticTechArticle, company);
            techArticles.add(techArticle);
        }
        List<TechArticle> savedTechArticles = techArticleRepository.saveAll(techArticles);
        firstTechArticle = savedTechArticles.getFirst();
    }

    @AfterAll
    static void tearDown(@Autowired TechArticleRepository techArticleRepository,
                         @Autowired ElasticTechArticleRepository elasticTechArticleRepository,
                         @Autowired CompanyRepository companyRepository) {
        elasticTechArticleRepository.deleteAll();
        techArticleRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
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

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialUrl(new Url(officialUrl))
                .careerUrl(new Url(careerUrl))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }
}
