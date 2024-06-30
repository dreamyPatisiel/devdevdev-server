package com.dreamypatisiel.devdevdev.limiter;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LimiterPlanTest {

    @ParameterizedTest
    @EnumSource(LimiterPlan.class)
    @DisplayName("설정한 정책에 맞는 처리율 제한 장치 정책을 반환한다.")
    void resolvePlan(LimiterPlan plan) {
        // given // when
        Bandwidth bandwidth = LimiterPlan.resolvePlan(plan.name());

        // then
        assertThat(bandwidth).isEqualTo(plan.getLimit());
    }

    @Test
    @DisplayName("설정한 정책이 처리율 제한 장치에 없으면 DEFAULT의 Bandwidth가 반환된다.")
    void resolvePlanDEFAULT() {
        // given
        String myPlan = "myPlan";

        // when // then
        assertThat(LimiterPlan.resolvePlan(myPlan)).isEqualTo(LimiterPlan.DEFAULT.getLimit());
    }

    @ParameterizedTest
    @EnumSource(LimiterPlan.class)
    @DisplayName("처리율 제한 장치 플랜에 알맞은 LimitPlan을 반환한다.")
    void getLimiterPlan(LimiterPlan plan) {
        // given // when
        LimiterPlan limiterPlan = LimiterPlan.getLimiterPlan(plan.name());

        // then
        assertThat(limiterPlan).isEqualTo(plan);
    }
}