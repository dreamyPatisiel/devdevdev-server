package com.dreamypatisiel.devdevdev.openai.data.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class Embedding {
    private String object;
    private int index;
    private List<Double> embedding;

    public Embedding(String object, int index, List<Double> embedding) {
        this.object = object;
        this.index = index;
        this.embedding = embedding;
    }
}
