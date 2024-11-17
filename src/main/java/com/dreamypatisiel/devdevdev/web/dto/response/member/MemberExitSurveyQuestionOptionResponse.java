package com.dreamypatisiel.devdevdev.web.dto.response.member;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import lombok.Builder;
import lombok.Data;

@Data
public class MemberExitSurveyQuestionOptionResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final int sortOrder;

    @Builder
    public MemberExitSurveyQuestionOptionResponse(Long id, String title, String content, int sortOrder) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
    }

    public static MemberExitSurveyQuestionOptionResponse of(SurveyQuestionOption surveyQuestionOption) {
        return MemberExitSurveyQuestionOptionResponse.builder()
                .id(surveyQuestionOption.getId())
                .title(surveyQuestionOption.getTitle())
                .content(surveyQuestionOption.getContent())
                .sortOrder(surveyQuestionOption.getSortOrder())
                .build();
    }
}
