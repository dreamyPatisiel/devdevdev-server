package com.dreamypatisiel.devdevdev.domain.policy;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TechArticlePopularScorePolicyTest {

    @Autowired
    TechArticlePopularScorePolicy techArticlePopularScorePolicy;

    @Test
    @DisplayName("기술블로그 인기점수 정책에 맞게 기술블로그 인기점수를 계산한다.")
    void calculatePopularScore() {
        // given
        Count viewCount = new Count(1);
        Count commentCount = new Count(1);
        Count recommendCount = new Count(1);
        TechArticle techArticle = createTechArticle(viewCount, commentCount, recommendCount);

        // when
        Count popularScore = techArticlePopularScorePolicy.calculatePopularScore(techArticle);

        // then
        assertThat(popularScore).isEqualTo(new Count(10L));
    }

    @Test
    @DisplayName("기술블로그 인기점수를 계산할 때 기술블로그 정책이 인자로 들어오지 않으면 예외가 발생한다.")
    void calculatePopularScoreException() {
        // given // when // then
        assertThatThrownBy(() -> techArticlePopularScorePolicy.calculatePopularScore(new Object()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(TechArticlePopularScorePolicy.INVALID_TECH_ARTICLE_TYPE_MESSAGE);
    }

    private TechArticle createTechArticle(Count viewCount, Count commentCount, Count recommendCount) {
        return TechArticle.builder()
                .viewTotalCount(viewCount)
                .commentTotalCount(commentCount)
                .recommendTotalCount(recommendCount)
                .build();
    }
}