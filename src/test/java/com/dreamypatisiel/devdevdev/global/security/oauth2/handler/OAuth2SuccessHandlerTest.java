package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

class OAuth2SuccessHandlerTest {

    @Test
    @DisplayName("")
    void fromHttpUrl() {
        // given
        String domain = "http://localhost:3000";
        String path = "/devdevdev/api/v1/members";
        String uriString = UriComponentsBuilder.fromHttpUrl(domain + path).toUriString();

        // when // then
        assertThat(uriString).isEqualTo("http://localhost:3000/devdevdev/api/v1/members");
    }
}