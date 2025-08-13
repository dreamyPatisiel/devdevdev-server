package com.dreamypatisiel.devdevdev.web.controller.member;

//import com.dreamypatisiel.devdevdev.LocalInitData;

import com.dreamypatisiel.devdevdev.LocalInitData;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.limiter.TokenBucketResolver;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "토큰 API", description = "토큰 생성 및 리프레시 API")
@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtMemberService jwtMemberService;
    private final TokenService tokenService;
    private final TokenBucketResolver tokenBucketResolver;

    @Operation(summary = "리프레시 요청", description = "쿠키에 담긴 RefreshToken을 통해 AccessToken을 재발급합니다.", deprecated = true)
    @PostMapping("/refresh")
    public ResponseEntity<BasicResponse<Object>> getRefreshToken(HttpServletRequest request,
                                                                 HttpServletResponse response) {

        // 쿠키에서 refresh를 꺼내온다.
        String refreshToken = CookieUtils.getRequestCookieValueByName(request,
                JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        // 쿠키 검증 및 토큰 재발급
        Token newToken = jwtMemberService
                .validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(refreshToken);

        // 쿠키 설정
        CookieUtils.configJwtCookie(response, newToken);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Profile({"test", "local", "dev"})
    @Operation(summary = "USER 테스트 계정 토큰 생성", description = "USER 테스트 계정의 토큰을 생성하고 해당 계정의 refresh 토큰을 갱신합니다.")
    @GetMapping("/test/user")
    public ResponseEntity<BasicResponse<Token>> createUserToken() {

        // 테스트 계정 토큰 생성
        Token newToken = tokenService.generateTokenBy(LocalInitData.userEmail,
                LocalInitData.userSocialType.name(), LocalInitData.userRole.name());

        // 테스트 계정의 refresh 갱신
        jwtMemberService.updateMemberRefreshToken(newToken.getRefreshToken());

        return ResponseEntity.ok(BasicResponse.success(newToken));
    }

    @Profile({"test", "local", "dev"})
    @Operation(summary = "ADMIN 테스트 계정 토큰 생성", description = "ADMIN 테스트 계정의 토큰을 생성하고 해당 계정의 refresh 토큰을 갱신합니다.")
    @GetMapping("/test/admin")
    public ResponseEntity<BasicResponse<Token>> createAdminToken() {

        // 테스트 계정 토큰 생성
        Token newToken = tokenService.generateTokenBy(LocalInitData.adminEmail,
                LocalInitData.adminSocialType.name(), LocalInitData.adminRole.name());

        // 테스트 계정의 refresh 갱신
        jwtMemberService.updateMemberRefreshToken(newToken.getRefreshToken());

        return ResponseEntity.ok(BasicResponse.success(newToken));
    }
}
