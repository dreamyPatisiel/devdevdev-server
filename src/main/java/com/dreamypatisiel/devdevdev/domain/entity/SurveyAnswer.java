package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyAnswer extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "message",
            column = @Column(name = "custom_message", length = 500)
    )
    private CustomSurveyAnswer customMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id", nullable = false)
    private SurveyQuestion surveyQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_option_id", nullable = false)
    private SurveyQuestionOption surveyQuestionOption;

    @Builder
    private SurveyAnswer(CustomSurveyAnswer customMessage, Member member, SurveyQuestion surveyQuestion,
                         SurveyQuestionOption surveyQuestionOption) {
        this.customMessage = customMessage;
        this.member = member;
        this.surveyQuestion = surveyQuestion;
        this.surveyQuestionOption = surveyQuestionOption;
    }

    public static SurveyAnswer create(CustomSurveyAnswer customMessage, Member member, SurveyQuestion surveyQuestion,
                                      SurveyQuestionOption surveyQuestionOption) {
        SurveyAnswer surveyAnswer = new SurveyAnswer();
        surveyAnswer.customMessage = customMessage;
        surveyAnswer.member = member;
        surveyAnswer.surveyQuestion = surveyQuestion;
        surveyAnswer.surveyQuestionOption = surveyQuestionOption;

        return surveyAnswer;
    }

    public static SurveyAnswer createWithoutCustomMessage(Member member, SurveyQuestion surveyQuestion,
                                                          SurveyQuestionOption surveyQuestionOption) {
        SurveyAnswer surveyAnswer = new SurveyAnswer();
        surveyAnswer.member = member;
        surveyAnswer.surveyQuestion = surveyQuestion;
        surveyAnswer.surveyQuestionOption = surveyQuestionOption;

        return surveyAnswer;
    }
}
