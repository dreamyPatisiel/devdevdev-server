package com.dreamypatisiel.devdevdev.domain.service;

import static com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService.ACCESS_DENIED_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.ApiKey;
import com.dreamypatisiel.devdevdev.domain.repository.ApiKeyRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public void validateApiKey(String serviceName, String apiKey) {

        // API Key 조회
        ApiKey findApiKey = apiKeyRepository.findByServiceName(serviceName)
                .orElseThrow(() -> new NotFoundException("지원하는 서비스가 아닙니다."));

        if (!findApiKey.isEqualsKey(apiKey)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }
}
