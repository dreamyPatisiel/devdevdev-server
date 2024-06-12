package com.dreamypatisiel.devdevdev.openai.request;

import lombok.Data;

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
