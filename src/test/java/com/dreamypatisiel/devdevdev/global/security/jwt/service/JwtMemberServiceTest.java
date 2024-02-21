package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.TokenExpireTime;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class JwtMemberServiceTest {

    @Autowired
    JwtMemberService jwtMemberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TokenService tokenService;
    @MockBean
    TimeProvider timeProvider;

    String refreshToken;
    String email = "dreamy5patisiel@kakao.com";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    Date date = new Date();

    @BeforeEach
    void setupRefreshToken() {
        when(timeProvider.getDateNow()).thenReturn(date);

        Token token = tokenService.generateTokenBy(email, socialType, role);
        refreshToken = token.getRefreshToken();
    }

    @Test
    @DisplayName("인자로 들어온 리프레시 토큰을 검증하고"
            + " 문제가 없으면 토큰을 새롭게 생성하여 회원의 리프레시 토큰을 갱신한 후"
            + " 새롭게 생성한 토큰을 반환한다.")
    void validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        when(timeProvider.getDateNow()).thenReturn(new Date(date.getTime() + TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME));

        // when
        Token token = jwtMemberService.validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(refreshToken);

        // then
        String newRefreshToken = token.getRefreshToken();
        Member findMember = memberRepository.findMemberByRefreshToken(newRefreshToken).get();

        assertAll(
                () -> assertThat(refreshToken).isNotEqualTo(newRefreshToken),
                () -> assertThat(findMember.isRefreshTokenEquals(newRefreshToken)).isTrue(),
                () -> assertThat(findMember.isRefreshTokenEquals(refreshToken)).isFalse()
        );
    }

    @Test
    @DisplayName("리프레시 토큰을 갱신할 때 리프레시 토큰의 클레임으로 데이터베이스에서 회원을 조회했을 때 회원이 없으면 예외가 발생한다.")
    void validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewTokenException_INVALID_MEMBER_NOT_FOUND_MESSAGE() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        when(timeProvider.getDateNow()).thenReturn(date);
        String otherEmail = "ralph@kakao.com";
        Token token = tokenService.generateTokenBy(otherEmail, socialType, role);
        String otherRefreshToken = token.getRefreshToken();

        // when // then
        assertThatThrownBy(() -> jwtMemberService.validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(otherRefreshToken))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("리프레시 토큰을 갱신할 때 회원에 저장된 리프레시 토큰의 정보가 다르면 예외가 발생한다.")
    void validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewTokenException_REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        when(timeProvider.getDateNow()).thenReturn(new Date(date.getTime() + TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME));
        Token token = tokenService.generateTokenBy(email, socialType, role);
        String otherRefreshToken = token.getRefreshToken();

        // when // then
        assertThatThrownBy(() -> jwtMemberService.validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(otherRefreshToken))
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage(TokenInvalidException.REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("리프레시 토큰의 클레임으로 조회한 회원의 리프레시 토큰을 갱신한다.")
    void updateMemberRefreshToken() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        when(timeProvider.getDateNow()).thenReturn(new Date(date.getTime() + TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME));
        Token token = tokenService.generateTokenBy(email, socialType, role);
        String otherRefreshToken = token.getRefreshToken();

        // when
        jwtMemberService.updateMemberRefreshToken(otherRefreshToken);

        // then
        Member findMember = memberRepository.findMemberByRefreshToken(otherRefreshToken).get();
        assertAll(
                () -> assertThat(findMember.isRefreshTokenEquals(refreshToken)).isFalse(),
                () -> assertThat(findMember.isRefreshTokenEquals(otherRefreshToken)).isTrue()
        );
    }

    @Test
    @DisplayName("리프레시 토큰의 클레임으로 조회한 회원이 없으면 예외가 발생한다.")
    void updateMemberRefreshTokenException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        when(timeProvider.getDateNow()).thenReturn(new Date(date.getTime() + TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME));
        String otherEmail = "ralph@kakao.com";
        Token token = tokenService.generateTokenBy(otherEmail, socialType, role);
        String otherRefreshToken = token.getRefreshToken();

        // when // then
        assertThatThrownBy(() -> jwtMemberService.updateMemberRefreshToken(otherRefreshToken))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원의 리프레시 토큰을 비활성화 상태로 변경한다.")
    void updateMemberRefreshTokenToDisabled() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal
                .createByEmailAndRoleAndSocialType(email, role, socialType);

        // when
        jwtMemberService.updateMemberRefreshTokenToDisabled(userPrincipal);

        // then
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email),
                SocialType.valueOf(socialType)).get();
        assertThat(findMember.getRefreshToken()).isEqualTo(Token.DISABLED);
    }

    @Test
    @DisplayName("회원의 리프레시 토큰을 비활성화 상태로 변경할 때 회원이 없으면 예외가 발생한다.")
    void updateMemberRefreshTokenToDisabledException() {
        // given
        UserPrincipal userPrincipal = UserPrincipal
                .createByEmailAndRoleAndSocialType(email, role, socialType);

        // when // then
        assertThatThrownBy(() -> jwtMemberService.updateMemberRefreshTokenToDisabled(userPrincipal))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

}