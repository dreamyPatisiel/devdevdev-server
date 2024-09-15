package com.dreamypatisiel.devdevdev.web.dto.response.member;

import java.util.List;
import lombok.Data;

@Data
public class MemberExitSurveyAnswerResponse {
    private final List<Long> id;

    public MemberExitSurveyAnswerResponse(List<Long> id) {
        this.id = id;
    }
}
