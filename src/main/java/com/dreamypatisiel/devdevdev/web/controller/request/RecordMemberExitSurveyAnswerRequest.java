package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class RecordMemberExitSurveyAnswerRequest {

    @NotNull(message = "아이디는 필수 입니다.")
    private final Long questionId;

    @Valid
    private final List<RecordMemberExitSurveyQuestionOptionsRequest> memberExitSurveyQuestionOptions;

    @Builder
    public RecordMemberExitSurveyAnswerRequest(Long questionId,
                                               List<RecordMemberExitSurveyQuestionOptionsRequest> memberExitSurveyQuestionOptions) {
        this.questionId = questionId;
        this.memberExitSurveyQuestionOptions = memberExitSurveyQuestionOptions;
    }
}
