package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.AnnualIncomeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class
AnnualIncomeTest {

    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(AnnualIncome.MIN_ANNUAL_INCOME+"원 ~ "+AnnualIncome.MAX_ANNUAL_INCOME
            +"만원 범위의 연봉을 생성할 수 있다.")
    void annualIncome(int annualIncome) {
        // given // when // then
        assertThatCode(() -> new AnnualIncome(annualIncome))
                .doesNotThrowAnyException();
    }
    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(AnnualIncome.MIN_ANNUAL_INCOME+"원 ~ "+AnnualIncome.MAX_ANNUAL_INCOME
            +"만원 범위가 아닌 연봉을 생성할 경우 예외가 발생한다.")
    void annualIncomeException(int annualIncome) {
        // given // when // then
        assertThatThrownBy(() -> new AnnualIncome(annualIncome))
                .isInstanceOf(AnnualIncomeException.class)
                .hasMessage(AnnualIncome.getInvalidRangeExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {AnnualIncome.MIN_ANNUAL_INCOME, AnnualIncome.MAX_ANNUAL_INCOME};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {AnnualIncome.MIN_ANNUAL_INCOME-1, AnnualIncome.MAX_ANNUAL_INCOME+1};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }
}