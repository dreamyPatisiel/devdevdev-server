package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TechArticleRepositoryTest {

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("elasticIds 리스트의 elasticId에 해당하는 기술블로그 엔티티를 순서대로 가져올 수 있다.")
    void findAllByElasticIdIn() {
        // given
        TechArticle techArticle1 = createTechArticle("elasticId1");
        TechArticle techArticle2 = createTechArticle("elasticId2");
        TechArticle techArticle3 = createTechArticle("elasticId3");
        TechArticle techArticle4 = createTechArticle("elasticId4");

        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3, techArticle4));

        List<String> elasticIds = List.of("elasticId1", "elasticId3", "elasticId2");

        // when
        List<TechArticle> techArticles = techArticleRepository.findAllByElasticIdIn(elasticIds);

        // then
        assertThat(techArticles).hasSize(3)
                .extracting(TechArticle::getElasticId)
                .containsExactly("elasticId1", "elasticId3", "elasticId2");
    }

    private static TechArticle createTechArticle(String elasticId) {
        return TechArticle.builder()
                .elasticId(elasticId)
                .build();
    }
}