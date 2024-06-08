package com.dreamypatisiel.devdevdev.openai.embeddings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.openai.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmbeddingRequestHandlerTest {

    @Autowired
    EmbeddingRequestHandler embeddingRequestHandler;

    @Disabled
    @Test
    @DisplayName("open ai embedding post 요청을 한다.")
    void postEmbedding() {
        // given
        EmbeddingRequest request = EmbeddingRequest.createTextEmbedding3Small("hello world");

        // when
        OpenAIResponse<Embedding> embeddingOpenAIResponse = embeddingRequestHandler.postEmbedding(request);

        // then
        System.out.println(embeddingOpenAIResponse);
        assertAll(
                () -> assertThat(embeddingOpenAIResponse).isNotNull(),
                () -> assertThat(embeddingOpenAIResponse.getObject()).isNotNull(),
                () -> assertThat(embeddingOpenAIResponse.getData()).isNotEmpty(),
                () -> assertThat(embeddingOpenAIResponse.getModel()).isEqualTo("text-embedding-3-small"),
                () -> assertThat(embeddingOpenAIResponse.getUsage()).isNotNull(),
                () -> assertThat(embeddingOpenAIResponse.getUsage().getPromptTokens()).isNotNull(),
                () -> assertThat(embeddingOpenAIResponse.getUsage().getTotalTokens()).isNotNull()
        );
    }
}