package com.dreamypatisiel.devdevdev.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PickPopularScorePolicyTest {

    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;

    @Test
    @DisplayName("픽픽픽 인기점수 정책에 맞게 픽픽픽 인기점수를 계산한다.")
    void calculatePopularScore() {
        // given
        Count viewCount = new Count(1);
        Count commentCount = new Count(1);
        Count voteCount = new Count(1);
        Pick pick = createPick(viewCount, commentCount, voteCount);

        // when
        Count popularScore = pickPopularScorePolicy.calculatePopularScore(pick);

        // then
        assertThat(popularScore).isEqualTo(new Count(10L));
    }

    @Test
    @DisplayName("픽픽픽 인기점수를 계산할 때 픽픽픽 정책이 인자로 들어오지 않으면 예외가 발생한다.")
    void calculatePopularScoreException() {
        // given // when // then
        assertThatThrownBy(() -> pickPopularScorePolicy.calculatePopularScore(new Object()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PickPopularScorePolicy.INVALID_PICK_TYPE_MESSAGE);
    }

    private Pick createPick(Count viewCount, Count commentCount, Count voteCount) {
        return Pick.builder()
                .viewTotalCount(viewCount)
                .commentTotalCount(commentCount)
                .voteTotalCount(voteCount)
                .build();
    }
}