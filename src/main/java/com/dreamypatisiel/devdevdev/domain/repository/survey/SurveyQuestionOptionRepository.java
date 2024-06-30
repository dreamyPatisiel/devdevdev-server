package com.dreamypatisiel.devdevdev.domain.repository.survey;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {

    @EntityGraph(attributePaths = {"surveyQuestion"})
    List<SurveyQuestionOption> findWithQuestionByIdInAndSurveyQuestionId(List<Long> ids, Long surveyQuestionId);
}
