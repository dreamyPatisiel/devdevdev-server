package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.exception.AnnualIncomeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class AnnualIncomeTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 9_999_999, 10_000_000})
    @DisplayName("1원~10,000,000만원 범위의 연봉을 생성할 수 있다.")
    void annualIncome(int annualIncome) {
        // given // when // then
        assertThatCode(() -> new AnnualIncome(annualIncome))
                .doesNotThrowAnyException();
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 10_000_001})
    @DisplayName("1원~10,000,000만원 범위가 아닌 연봉을 생성할 경우 예외가 발생한다.")
    void annualIncomeException(int annualIncome) {
        // given // when // then
        assertThatThrownBy(() -> new AnnualIncome(annualIncome))
                .isInstanceOf(AnnualIncomeException.class)
                .hasMessage(AnnualIncome.INVALID_ANNUAL_INCOME_MESSAGE);
    }
}