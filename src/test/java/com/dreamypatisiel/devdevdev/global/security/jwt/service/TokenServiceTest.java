package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TokenServiceTest {

    @Autowired
    TokenService tokenService;

    @Test
    @DisplayName("")
    void test() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Imhvd2lzaXRnb2luZ0BrYWthby5jb20iLCJzb2NpYWxUeXBlIjoiS0FLQU8iLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzA2MTEyMDcwLCJleHAiOjE3MDYxMTM4NzB9.9tQpsDlwaue7hDiDaSNDpJFm8-vep7JCvysDPDGiNBs";

        // when
        Authentication authentication = tokenService.getAuthentication(token);
        SocialMemberDto socialMemberDto = (SocialMemberDto) authentication.getPrincipal();

        // then
        assertThat(authentication.getPrincipal()).isNotNull();
        assertAll(
                () -> assertThat(socialMemberDto.getEmail()).isNotNull(),
                () -> assertThat(socialMemberDto.getRole()).isNotNull(),
                () -> assertThat(socialMemberDto.getSocialType()).isNotNull()
        );
    }
}