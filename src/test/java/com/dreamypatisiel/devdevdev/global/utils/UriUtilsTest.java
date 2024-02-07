package com.dreamypatisiel.devdevdev.global.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UriUtilsTest {
    @Test
    @DisplayName("도메인과 엔드포인트로 uri를 생성한다.")
    void createUriByDomainAndPath() {
        // given
        String domain = "http://localhost:8080";
        String path = "/devdevdev/api/v1/home";
        // when
        String uri = UriUtils.createUriByDomainAndEndpoint(domain, path);

        // then
        assertThat(uri).isEqualTo(domain + path);
    }
}