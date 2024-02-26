package com.dreamypatisiel.devdevdev.web.docs;

import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.LocalInitData;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
public class SupportControllerDocsTest {

    protected String DEFAULT_PATH_V1 = "/devdevdev/api/v1";

    @Autowired
    protected ObjectMapper om;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected TokenService tokenService;
    @MockBean
    protected TimeProvider timeProvider;

    protected String refreshToken;
    protected String accessToken;
    protected String email = "dreamy5patisiel@kakao.com";
    protected String socialType = SocialType.KAKAO.name();
    protected String role = Role.ROLE_USER.name();
    protected String userEmail = LocalInitData.userEmail;
    protected String userRole = Role.ROLE_USER.name();
    protected String adminEmail = LocalInitData.adminEmail;
    protected String adminRole = Role.ROLE_ADMIN.name();
    protected Date date = new Date();

    @BeforeEach
    void setupRefreshToken() {
        when(timeProvider.getDateNow()).thenReturn(date);

        Token token = tokenService.generateTokenBy(email, socialType, role);
        refreshToken = token.getRefreshToken();
        accessToken = token.getAccessToken();
    }
}
