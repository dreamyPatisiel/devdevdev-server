package com.dreamypatisiel.devdevdev.global.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BigDecimalUtilsTest {

    @Test
    @DisplayName("퍼센트를 게산한다.")
    void toPercentageOf() {
        // given
        BigDecimal value = BigDecimal.valueOf(5);
        BigDecimal total = BigDecimal.valueOf(500);

        // when
        BigDecimal result = BigDecimalUtils.toPercentageOf(value, total);

        // then
        assertThat(result.toBigInteger()).isEqualTo(1);
    }

    @Test
    @DisplayName("퍼센트로 값을 계산한다.")
    void percentOf() {
        // given
        BigDecimal percent = BigDecimal.valueOf(10);
        BigDecimal total = BigDecimal.valueOf(500);

        // when
        BigDecimal result = BigDecimalUtils.percentOf(percent, total);

        // then
        assertThat(result.toBigInteger()).isEqualTo(50);
    }
}