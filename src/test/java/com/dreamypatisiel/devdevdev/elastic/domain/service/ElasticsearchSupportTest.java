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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
    public static Long FIRST_TECH_ARTICLE_ID;
    public static TechArticle firstTechArticle;
    public static Long COMPANY_ID;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository,
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");

        Company savedCompany = companyRepository.save(company);

        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= TEST_ARTICLES_COUNT; i++) {
            ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId_" + i, "타이틀" + i,
                    i > 1 ? createRandomDate() : LocalDate.of(2024, 3, 11), "내용",
                    "http://example.com/" + i, "설명", "http://example.com/", "작성자",
                    savedCompany.getName().getCompanyName(), savedCompany.getId(), (long) i, (long) i,
                    (long) i,
                    (long) i * 10);
            elasticTechArticles.add(elasticTechArticle);
        }
        Iterable<ElasticTechArticle> elasticTechArticleIterable = elasticTechArticleRepository.saveAll(
                elasticTechArticles);

        List<TechArticle> techArticles = new ArrayList<>();
        for (ElasticTechArticle elasticTechArticle : elasticTechArticleIterable) {
            TechArticle techArticle = TechArticle.of(elasticTechArticle, savedCompany);
            techArticles.add(techArticle);
        }
        List<TechArticle> savedTechArticles = techArticleRepository.saveAll(techArticles);
        firstTechArticle = savedTechArticles.getFirst();
        FIRST_TECH_ARTICLE_ID = firstTechArticle.getId();
        COMPANY_ID = savedCompany.getId();
    }

    @AfterAll
    static void tearDown(@Autowired TechArticleRepository techArticleRepository,
                         @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {
        elasticTechArticleRepository.deleteAll();
        techArticleRepository.deleteAllInBatch();
    }

    private static LocalDate createRandomDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 10);

        // 시작 날짜와 종료 날짜 사이의 차이 중 랜덤한 일 수 선택
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);

        return startDate.plusDays(randomDays);
    }

    private static ElasticTechArticle createElasticTechArticle(String id, String title, LocalDate regDate,
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

    private static Company createCompany(String companyName, String thumbnailImageUrl, String thumbnailUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .thumbnailImageUrl(thumbnailImageUrl)
                .careerUrl(new Url(thumbnailUrl))
                .thumbnailUrl(new Url(careerUrl))
                .build();
    }
}
