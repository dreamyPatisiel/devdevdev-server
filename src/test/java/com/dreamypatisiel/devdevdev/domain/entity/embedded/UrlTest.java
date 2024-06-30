package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.UrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com", "https://example.com", "http://example.com/page",
            "http://example.com/page?query=123", "http://example.url.com/page", "http://example.co.kr"})
    @DisplayName("유효한 형식의 url을 생성할 수 있다.")
    void createUrl(String url) {
        // given // when // then
        assertThatCode(() -> new Url(url))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.com", "example", "www.example@com",
            "http://example.com//page", "http//example.com", "http://example,com"})
    @DisplayName("유효하지 않은 형식의 url를 생성할 시 예외가 발생한다.")
    void createUrlException(String url) {
        // given // when // then
        assertThatThrownBy(() -> new Url(url))
                .isInstanceOf(UrlException.class)
                .hasMessage(Url.INVALID_URL_MESSAGE);
    }
}