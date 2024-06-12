package com.dreamypatisiel.devdevdev.openai.embeddings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
class CosineSimilarityCalculatorTest {

    @Test
    @DisplayName("코사인 유사도를 계산한다.")
    void cosineSimilarity() {
        // given // when
        double cosineSimilarity1 = CosineSimilarityCalculator.cosineSimilarity(List.of(1.0, 1.0, 1.0),
                List.of(0.1, 0.2, 0.3));
        double cosineSimilarity2 = CosineSimilarityCalculator.cosineSimilarity(List.of(1.0, 1.0, 1.0),
                List.of(0.2, 0.3, 0.4));
        double cosineSimilarity3 = CosineSimilarityCalculator.cosineSimilarity(List.of(1.0, 1.0, 1.0),
                List.of(0.3, 0.4, 0.5));
        double cosineSimilarity4 = CosineSimilarityCalculator.cosineSimilarity(List.of(1.0, 1.0, 1.0),
                List.of(0.4, 0.5, 0.6));
        double cosineSimilarity5 = CosineSimilarityCalculator.cosineSimilarity(List.of(1.0, 1.0, 1.0),
                List.of(0.5, 0.6, 0.7));

        // then
        log.info("cosineSimilarity1={}", cosineSimilarity1);
        log.info("cosineSimilarity2={}", cosineSimilarity2);
        log.info("cosineSimilarity3={}", cosineSimilarity3);
        log.info("cosineSimilarity4={}", cosineSimilarity4);
        log.info("cosineSimilarity5={}", cosineSimilarity5);

        assertThat(cosineSimilarity5)
                .isGreaterThan(cosineSimilarity4)
                .isGreaterThan(cosineSimilarity3)
                .isGreaterThan(cosineSimilarity2)
                .isGreaterThan(cosineSimilarity1);
    }
}