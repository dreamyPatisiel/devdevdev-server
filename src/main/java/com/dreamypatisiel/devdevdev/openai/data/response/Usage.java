package com.dreamypatisiel.devdevdev.openai.data.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.ToString;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@ToString
public class Usage {

    private int promptTokens;
    private int totalTokens;

    public Usage(int promptTokens, int totalTokens) {
        this.promptTokens = promptTokens;
        this.totalTokens = totalTokens;
    }
}
