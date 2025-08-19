package com.dreamypatisiel.devdevdev.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HangulUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"꿈빛 파티시엘", "Hello꿈빛", "ㄱㄴㄷ", "댑댑댑", "123꿈빛파티시엘", "!@#꿈빛$%^"})
    @DisplayName("한글이 포함된 문자열이면 true를 리턴한다.")
    void hasHangulWithKorean(String input) {
        // when // then
        assertThat(HangulUtils.hasHangul(input)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello World", "spring", "!@#$%", "", "   ", "123456789"})
    @DisplayName("한글이 포함되지 않은 문자열은 false를 리턴한다.")
    void hasHangulWithoutKorean(String input) {
        // when // then
        assertThat(HangulUtils.hasHangul(input)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "꿈빛, ㄲㅜㅁㅂㅣㅊ",
        "꿈빛 파티시엘, ㄲㅜㅁㅂㅣㅊ ㅍㅏㅌㅣㅅㅣㅇㅔㄹ",
        "개발자, ㄱㅐㅂㅏㄹㅈㅏ",
        "Hello꿈빛, Helloㄲㅜㅁㅂㅣㅊ"
    })
    @DisplayName("한글 문자열을 자모음으로 분해한다.")
    void convertToJamo(String input, String expected) {
        // when
        String result = HangulUtils.convertToJamo(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "안녕!@#하세요$%^, ㅇㅏㄴㄴㅕㅇ!@#ㅎㅏㅅㅔㅇㅛ$%^",
            "Spring Boot 3.0, Spring Boot 3.0",
            "한글123영어, ㅎㅏㄴㄱㅡㄹ123ㅇㅕㅇㅇㅓ"
    })
    @DisplayName("특수문자와 혼합된 문자열을 자모음으로 분해한다.")
    void convertToJamoWithSpecialCharacters(String input, String expected) {
        // when
        String result = HangulUtils.convertToJamo(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "꿈빛 파티시엘, ㄲㅂ ㅍㅌㅅㅇ",
        "댑댑댑, ㄷㄷㄷ",
        "댑구리 99, ㄷㄱㄹ 99"
    })
    @DisplayName("한글 문자열에서 초성을 추출한다.")
    void extractChosung(String input, String expected) {
        // when
        String result = HangulUtils.extractChosung(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "꿈빛!@#파티시엘$%^, ㄲㅂ!@#ㅍㅌㅅㅇ$%^",
        "React.js개발자, React.jsㄱㅂㅈ",
        "Spring Boot 3.0, Spring Boot 3.0",
        "꿈빛123개발자, ㄲㅂ123ㄱㅂㅈ"
    })
    @DisplayName("특수문자와 혼합된 문자열에서 초성을 추출한다.")
    void extractChosungWithSpecialCharacters(String input, String expected) {
        // when
        String result = HangulUtils.extractChosung(input);

        // then
        assertThat(result).isEqualTo(expected);
    }
}