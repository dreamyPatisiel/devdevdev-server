package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberResponse;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtMemberServiceTest {

    @Autowired
    JwtMemberService jwtMemberService;
    @Mock
    MemberRepository memberRepository;

    @Disabled
    @Test
    @DisplayName("email과 socialType에 맞는 회원을 가져올 수 있다.")
    void findMemberByEmailAndSocialTypeTest() {
        // given
//        Member membere =
        Email email = new Email("merooongg");
        SocialType socialType = SocialType.KAKAO;

//        when(memberRepository.findMemberByEmailAndSocialType(email, socialType)).thenReturn(Optional.of(Member));
        //^_^ 네
        // when

        // then

    }

    @Test
    @DisplayName("email과 socialType에 맞는 회원을 찾아 리프레시토큰을 업데이트할 수 있다.")
    void updateMemberRefreshTokenTest() {
        // given

        // when

        // then
    }

    @Test
    @DisplayName("email과 socialType에 맞는 회원이 없다면 리프레시토큰을 업데이트할 수 있다.")
    void updateMemberRefreshTokenExceptionTest() {
        // given

        // when

        // then
    }

}