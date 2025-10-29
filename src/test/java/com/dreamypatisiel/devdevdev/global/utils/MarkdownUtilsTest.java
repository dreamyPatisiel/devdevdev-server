package com.dreamypatisiel.devdevdev.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MarkdownUtils 테스트")
class MarkdownUtilsTest {

    @Test
    @DisplayName("복합 마크다운을 텍스트로 변환한다")
    void convertMarkdownToText() {
        // given
        String markdown = """
                # 제목
                
                **굵은 글씨**와 *이탤릭* 그리고 [링크](https://example.com)
                
                - 항목1
                - 항목2
                
                `코드`
                """;

        // when
        String result = MarkdownUtils.convertMarkdownToText(markdown);

        // then
        assertThat(result).contains("제목");
        assertThat(result).contains("굵은 글씨와 이탤릭 그리고");
        assertThat(result).contains("링크");
        assertThat(result).contains("항목1");
        assertThat(result).contains("항목2");
        assertThat(result).contains("코드");
        assertThat(result).doesNotContain("#");
        assertThat(result).doesNotContain("**");
        assertThat(result).doesNotContain("[");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열은 그대로 반환한다")
    void convertMarkdownToText_nullOrEmpty(String input) {
        // when
        String result = MarkdownUtils.convertMarkdownToText(input);

        // then
        assertThat(result).isEqualTo(input);
    }
}

