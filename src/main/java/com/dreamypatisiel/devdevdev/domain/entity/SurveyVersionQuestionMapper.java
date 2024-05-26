package com.dreamypatisiel.devdevdev.domain.entity;

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
public class SurveyVersionQuestionMapper extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suervey_version_id")
    private SurveyVersion surveyVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suervey_question_id")
    private SurveyQuestion surveyQuestion;

    @Builder
    private SurveyVersionQuestionMapper(SurveyVersion surveyVersion, SurveyQuestion surveyQuestion) {
        this.surveyVersion = surveyVersion;
        this.surveyQuestion = surveyQuestion;
    }
}
