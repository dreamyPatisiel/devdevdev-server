package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyQuestion extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "surveyQuestion")
    private List<SurveyQuestionOption> surveyQuestionOptions = new ArrayList<>();

    @Builder
    private SurveyQuestion(String title, String content, int sortOrder) {
        this.title = title;
        this.content = content;
        this.sortOrder = sortOrder;
    }
}
