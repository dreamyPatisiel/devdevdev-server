package com.dreamypatisiel.devdevdev.web.dto.request.techArticle;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RegisterKeywordRequest {

    @NotEmpty(message = "검색어 목록을 작성해주세요.")
    private List<String> words;

    @Builder
    public RegisterKeywordRequest(List<String> words) {
        this.words = words;
    }
}
