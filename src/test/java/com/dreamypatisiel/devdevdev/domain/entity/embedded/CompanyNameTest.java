package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CompanyNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
class CompanyNameTest {
    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(CompanyName.MIN_COMPANY_NAME_LENGTH+" ~ "+CompanyName.MAX_COMPANY_NAME_LENGTH
    +" 길이의 회사 이름을 생성할 수 있다.")
    void company(String companyName) {
        // given // when // then
        assertThatCode(() -> new CompanyName(companyName))
                .doesNotThrowAnyException();
    }
    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(CompanyName.MIN_COMPANY_NAME_LENGTH+" ~ "+CompanyName.MAX_COMPANY_NAME_LENGTH
            +" 길이가 아닌 회사 이름을 생성할 경우 예외가 발생한다.")
    void companyNameException(String companyName) {
        // given // when // then
        assertThatThrownBy(() -> new CompanyName(companyName))
                .isInstanceOf(CompanyNameException.class)
                .hasMessage(CompanyName.getInvalidLengthExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {CompanyName.MIN_COMPANY_NAME_LENGTH, CompanyName.MAX_COMPANY_NAME_LENGTH};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {0, CompanyName.MAX_COMPANY_NAME_LENGTH+1};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    public static String createStringBy(int length) {
        return "a".repeat(Math.max(0, length));
    }
}