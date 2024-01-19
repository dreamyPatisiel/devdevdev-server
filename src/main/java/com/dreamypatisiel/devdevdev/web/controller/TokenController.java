package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.exception.TokenNotFoundException;
import com.dreamypatisiel.devdevdev.global.security.jwt.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final JwtMemberService jwtMemberService;

    /**
     * 리프레시 토큰 요청
     * 1. 엑세스 토큰이 만료된 상황
     * 2. 클라이언트에서 리프레시 토큰을 요청한다.
     */
    @GetMapping("/refresh")
    public ResponseEntity<BasicResponse<Object>> refreshAuth(HttpServletRequest request, HttpServletResponse response) throws ServletException {
//        String refreshToken = request.getHeader("Refresh");
        String refreshToken = request.getParameter("refresh");
        log.info("refresh 요청 => "+refreshToken);

        try {
            log.info("토큰 검증");
            tokenService.validateRefreshToken(refreshToken);
            String email = tokenService.getEmail(refreshToken);
            String socialType = tokenService.getSocialType(refreshToken);

            log.info("토큰 생성 with "+email+" , "+socialType);
            // 새로운 토큰 생성
            Token newToken = tokenService.generateToken(email, socialType);

            // 쿠키 설정
            log.info("쿠키 설정");
            CookieUtils.configJwtCookie(response, newToken);

            // save refresh token
            log.info("refresh 저장 "+email+", "+socialType);
            jwtMemberService.findMemberByEmailAndSocialTypeAndSaveRefreshToken(new Email(email), SocialType.valueOf(socialType), newToken.getRefreshToken());

            return ResponseEntity.ok(BasicResponse.success("CREATE_NEW_TOKEN"));

        } catch (TokenNotFoundException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.fail("리프레시 토큰 정보 없음",HttpStatus.FORBIDDEN.value()));

        } catch (TokenInvalidException e) {
            log.info("로그아웃합니다^^");
            request.logout(); // 로그아웃 처리
            CookieUtils.deleteCookie(request, response, JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
            CookieUtils.deleteCookie(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BasicResponse.fail("Logged out due to suspicious behavior",HttpStatus.UNAUTHORIZED.value()));

        } catch (Exception e) {
            throw new RuntimeException();

        }
    }
}
