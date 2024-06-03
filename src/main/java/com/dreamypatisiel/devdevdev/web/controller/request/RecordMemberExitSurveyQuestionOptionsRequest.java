package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
public class RecordMemberExitSurveyQuestionOptionsRequest {

    @NotNull(message = "아이디는 필수 입니다.")
    private final Long id;
    private String message;

    @Builder
    public RecordMemberExitSurveyQuestionOptionsRequest(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public static Map<Long, String> convertToMap(
            List<RecordMemberExitSurveyQuestionOptionsRequest> memberExitSurveyQuestionOptions) {

        return memberExitSurveyQuestionOptions.stream()
                .sorted(Comparator.comparingLong(RecordMemberExitSurveyQuestionOptionsRequest::getId))
                .collect(Collectors.toMap(
                        RecordMemberExitSurveyQuestionOptionsRequest::getId,
                        RecordMemberExitSurveyQuestionOptionsRequest::getMessage)
                );
    }

    public static List<Long> convertToIds(
            List<RecordMemberExitSurveyQuestionOptionsRequest> memberExitSurveyQuestionOptions) {

        return memberExitSurveyQuestionOptions.stream()
                .map(RecordMemberExitSurveyQuestionOptionsRequest::getId)
                .toList();
    }

    public static List<Long> convertToIdsFilteredMessageNull(
            List<RecordMemberExitSurveyQuestionOptionsRequest> surveyQuestionOptions) {

        return surveyQuestionOptions.stream()
                .filter(RecordMemberExitSurveyQuestionOptionsRequest::isMessageNotNull)
                .map(RecordMemberExitSurveyQuestionOptionsRequest::getId)
                .toList();
    }

    private boolean isMessageNotNull() {
        return this.message != null;
    }
}
