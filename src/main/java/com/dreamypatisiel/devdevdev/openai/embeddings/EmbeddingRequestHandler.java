package com.dreamypatisiel.devdevdev.openai.embeddings;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.BEARER_PREFIX;
import static com.dreamypatisiel.devdevdev.openai.constant.OpenAIConstant.DOMAIN;
import static com.dreamypatisiel.devdevdev.openai.constant.OpenAIConstant.V1_EMBEDDINGS;

import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import com.dreamypatisiel.devdevdev.openai.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EmbeddingRequestHandler {

    @Value("${open-ai.api-key}")
    private String openAIApiKey;

    private final RestTemplate restTemplate;

    /**
     * @Note: POST: https://api.openai.com/v1/embeddings 로 요청하여 embedding 값을 얻는다.
     * <a href="https://platform.openai.com/docs/guides/embeddings">참고</a>
     * @Author: 장세웅
     * @Since: 2024.06.11
     */
    public OpenAIResponse<Embedding> postEmbeddings(EmbeddingRequest request) {

        String uri = UriUtils.createUriByDomainAndEndpoint(DOMAIN, V1_EMBEDDINGS);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(AUTHORIZATION_HEADER, BEARER_PREFIX + openAIApiKey);
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return restTemplate.exchange(
                        uri,
                        HttpMethod.POST,
                        new HttpEntity<>(request, httpHeaders),
                        new ParameterizedTypeReference<OpenAIResponse<Embedding>>() {
                        })
                .getBody();
    }
}
