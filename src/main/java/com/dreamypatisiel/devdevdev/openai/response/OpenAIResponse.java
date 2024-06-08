package com.dreamypatisiel.devdevdev.openai.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@ToString
public class OpenAIResponse<T> {
    private String object;
    private List<T> data;
    private String model;
    private Usage usage;

    public OpenAIResponse(String object, List<T> data, String model, Usage usage) {
        this.object = object;
        this.data = data;
        this.model = model;
        this.usage = usage;
    }
}
