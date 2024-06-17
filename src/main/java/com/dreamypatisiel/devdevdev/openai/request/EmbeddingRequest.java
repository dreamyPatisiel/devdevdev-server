package com.dreamypatisiel.devdevdev.openai.request;

import lombok.Data;

/**
 * @Note: 모델 종류: text-embedding-3-small, text-embedding-3-large, text-embedding-ada-002
 * @Author: 장세웅
 * @Since: 2024.06.17
 */
@Data
public class EmbeddingRequest {
    private final String input;
    private String model;

    public static EmbeddingRequest createTextEmbedding3Small(String input) {
        EmbeddingRequest request = new EmbeddingRequest(input);
        request.model = "text-embedding-3-small";
        //request.model = "text-embedding-3-large";
        //request.model = "text-embedding-ada-002";

        return request;
    }
}
