package com.dreamypatisiel.devdevdev.domain.service;

import static com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService.ACCESS_DENIED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.domain.entity.ApiKey;
import com.dreamypatisiel.devdevdev.domain.repository.ApiKeyRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ApiKeyServiceTest {

    @Autowired
    ApiKeyService apiKeyService;

    @Autowired
    ApiKeyRepository apiKeyRepository;

    @Test
    @DisplayName("외부 service 에 대한 apiKey 를 검증한다.")
    void validateApiKey() {
        // given
        String serviceName = "test-service";
        String key = "test-key";

        ApiKey apiKey = ApiKey.builder()
                .serviceName(serviceName)
                .apiKey(key)
                .build();

        apiKeyRepository.save(apiKey);

        // when // then
        assertThatCode(() -> apiKeyService.validateApiKey(serviceName, key))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("apiKey 가 존재하지 않으면 예외가 발생한다.")
    void validateApiKeyNotFoundException() {
        // given // when // then
        assertThatThrownBy(() -> apiKeyService.validateApiKey("test-service", "test-key"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("지원하는 서비스가 아닙니다.");
    }

    @Test
    @DisplayName("apiKey 가 일치하지 않으면 예외가 발생한다.")
    void validateApiKeyAccessDeniedException() {
        // given
        String serviceName = "test-service";
        String key = "test-key";
        String invalidKey = "invalid-key";

        ApiKey apiKey = ApiKey.builder()
                .serviceName(serviceName)
                .apiKey(key)
                .build();

        apiKeyRepository.save(apiKey);

        // when // then
        assertThatThrownBy(() -> apiKeyService.validateApiKey(serviceName, invalidKey))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ACCESS_DENIED_MESSAGE);
    }
}