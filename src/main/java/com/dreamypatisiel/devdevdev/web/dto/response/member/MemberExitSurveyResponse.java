package com.dreamypatisiel.devdevdev.web.dto.response.member;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class MemberExitSurveyResponse {
    private final Long surveyVersionId;
    private List<MemberExitSurveyQuestionResponse> surveyQuestions;

    @Builder
    public MemberExitSurveyResponse(Long surveyVersionId, List<MemberExitSurveyQuestionResponse> surveyQuestions) {
        this.surveyVersionId = surveyVersionId;
        this.surveyQuestions = surveyQuestions;
    }

    public static MemberExitSurveyResponse of(Long surveyVersionId,
                                              List<MemberExitSurveyQuestionResponse> surveyQuestions) {
        return MemberExitSurveyResponse.builder()
                .surveyVersionId(surveyVersionId)
                .surveyQuestions(surveyQuestions)
                .build();
    }
}
