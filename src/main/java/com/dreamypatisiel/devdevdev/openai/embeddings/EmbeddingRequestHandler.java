package com.dreamypatisiel.devdevdev.openai.embeddings;

import static com.dreamypatisiel.devdevdev.openai.constant.OpenAIConstant.DOMAIN;
import static com.dreamypatisiel.devdevdev.openai.constant.OpenAIConstant.V1_EMBEDDINGS;

import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import com.dreamypatisiel.devdevdev.openai.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EmbeddingRequestHandler {

    private final RestTemplate restTemplate;
    private final EmbeddingsService embeddingsService;

    /**
     * @Note: POST: https://api.openai.com/v1/embeddings
     */
    public OpenAIResponse<Embedding> postEmbedding(EmbeddingRequest request) {

        String uri = UriUtils.createUriByDomainAndEndpoint(DOMAIN, V1_EMBEDDINGS);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer sk-proj-EYrsTK9TMWdUohCHtWgOT3BlbkFJFS98Z5yPlYnZyKd0sqa5");
        httpHeaders.set("Content-Type", "application/json");

        return restTemplate.exchange(
                        uri,
                        HttpMethod.POST,
                        new HttpEntity<>(request, httpHeaders),
                        new ParameterizedTypeReference<OpenAIResponse<Embedding>>() {
                        })
                .getBody();
    }
}
