package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PickMultiServiceHandler {

    private PickService pickService;
    private final EmbeddingsService embeddingsService;

    @Transactional
    public PickRegisterResponse registerPickAndSaveEmbedding(RegisterPickRequest registerPickRequest,
                                                             Authentication authentication,
                                                             OpenAIResponse<Embedding> embeddingOpenAIResponse) {

        validatePickService(pickService);

        // 픽픽픽 작성
        PickRegisterResponse response = pickService.registerPick(registerPickRequest, authentication);

        // 임베딩 값 저장
        embeddingsService.saveEmbedding(response.getPickId(), embeddingOpenAIResponse);

        return response;
    }

    @Transactional
    public PickModifyResponse modifyPickAndSaveEmbedding(Long pickId,
                                                         ModifyPickRequest modifyPickRequest,
                                                         Authentication authentication,
                                                         OpenAIResponse<Embedding> embeddingOpenAIResponse) {

        validatePickService(pickService);

        // 픽픽픽 수정
        PickModifyResponse response = pickService.modifyPick(pickId, modifyPickRequest, authentication);

        // 임베딩 값 저장
        embeddingsService.saveEmbedding(response.getPickId(), embeddingOpenAIResponse);

        return response;
    }

    public void selectPickService(PickService pickService) {
        validatePickService(pickService);
        this.pickService = pickService;
    }

    private void validatePickService(PickService pickService) {
        if (ObjectUtils.isEmpty(pickService)) {
            throw new IllegalStateException("Pick service cannot be empty");
        }
    }
}
