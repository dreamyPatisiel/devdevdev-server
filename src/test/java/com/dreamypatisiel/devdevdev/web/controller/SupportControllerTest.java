package com.dreamypatisiel.devdevdev.web.controller;

import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.LocalInitData;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SupportControllerTest {

    protected String DEFAULT_PATH_V1 = "/devdevdev/api/v1";

    @Autowired
    protected ObjectMapper om;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected TokenService tokenService;
    @MockBean
    protected TimeProvider timeProvider;

    @MockBean
    protected EmbeddingRequestHandler embeddingRequestHandler;
    @Autowired
    protected RestTemplate restTemplate;
    @Value("${open-ai.api-key}")
    protected String openAIApiKey;
    @Autowired
    protected RedisTemplate<?, ?> redisTemplate;

    protected String refreshToken;
    protected String userId = "userId";
    protected String password = "1234";
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
    void setupToken() {
        when(timeProvider.getDateNow()).thenReturn(date);
        when(timeProvider.getLocalDateTimeNow()).thenReturn(LocalDateTime.of(2024, 2, 16, 0, 0, 0, 0));

        Token token = tokenService.generateTokenBy(email, socialType, role);
        refreshToken = token.getRefreshToken();
        accessToken = token.getAccessToken();
    }

    @AfterEach
    void tearDown() {
        RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        DefaultStringRedisConnection defaultStringRedisConnection = new DefaultStringRedisConnection(redisConnection,
                redisSerializer);

        defaultStringRedisConnection.flushAll();
    }
}
