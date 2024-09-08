package com.dreamypatisiel.devdevdev.web.dto.response.member;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Nickname;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class MemberExitSurveyQuestionResponse {
    public static final String regexNickName = "#nickName";

    private final Long id;
    private final String title;
    private final String content;
    private final int sortOrder;
    private List<MemberExitSurveyQuestionOptionResponse> surveyQuestionOptions;

    @Builder
    public MemberExitSurveyQuestionResponse(Long id, String title, String content, int sortOrder,
                                            List<MemberExitSurveyQuestionOptionResponse> surveyQuestionOptions) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
        this.surveyQuestionOptions = surveyQuestionOptions;
    }

    public static MemberExitSurveyQuestionResponse of(SurveyQuestion surveyQuestion, Nickname nickName) {
        return MemberExitSurveyQuestionResponse.builder()
                .id(surveyQuestion.getId())
                .title(addMemberNickName(surveyQuestion.getTitle(), nickName.getNickname()))
                .content(surveyQuestion.getContent())
                .sortOrder(surveyQuestion.getSortOrder())
                .surveyQuestionOptions(mapToMemberExitSurveyQuestionOptionResponse(surveyQuestion))
                .build();
    }

    /**
     * @Note: #nickName을 회원의 닉네임으로 치환한다.<br/> e.g) <br/> AS IS: #nickName님, 탈퇴하시는 이유를 알려주세요. <br/> TO BE: 게으른 댑댑이님,
     * 탈퇴하시는 이유를 알려주세요.
     * @Author: 장세웅
     * @Since: 2024.05.26
     */
    private static String addMemberNickName(String target, String memberNickName) {
        if (StringUtils.hasText(target)) {
            return target.replaceAll(regexNickName, memberNickName);
        }

        return target;
    }

    private static List<MemberExitSurveyQuestionOptionResponse> mapToMemberExitSurveyQuestionOptionResponse(
            SurveyQuestion question) {
        return question.getSurveyQuestionOptions().stream()
                .map(MemberExitSurveyQuestionOptionResponse::of)
                .toList();
    }
}
