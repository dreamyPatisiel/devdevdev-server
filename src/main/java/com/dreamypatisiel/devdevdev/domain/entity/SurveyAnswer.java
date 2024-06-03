package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
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

    @Column(length = 500)
    private String customMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id")
    private SurveyQuestion surveyQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_option_id")
    private SurveyQuestionOption surveyQuestionOption;

    @Builder
    private SurveyAnswer(String customMessage, Member member, SurveyQuestion surveyQuestion,
                        SurveyQuestionOption surveyQuestionOption) {
        this.customMessage = customMessage;
        this.member = member;
        this.surveyQuestion = surveyQuestion;
        this.surveyQuestionOption = surveyQuestionOption;
    }
}
