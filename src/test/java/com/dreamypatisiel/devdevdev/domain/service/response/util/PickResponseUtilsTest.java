package com.dreamypatisiel.devdevdev.domain.service.response.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PickResponseUtilsTest {

    @ParameterizedTest
    @CsvSource(value = {"alsdudr97@naver.com:als******", "merooongg@naver.com:mer******",
            "dreamy5patisiel@gmail.com:dre************", "mmj9908@naver.com:mmj****"}, delimiter = ':')
    @DisplayName("이메일 도메인을 제거하고 아이디를 마스킹 처리한다.")
    void sliceAndMaskEmail(String email, String expected) {
        // given // when
        String result = CommonResponseUtil.sliceAndMaskEmail(email);

        // then
        assertThat(result).isEqualTo(expected);
    }
}