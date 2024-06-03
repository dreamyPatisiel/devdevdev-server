package com.dreamypatisiel.devdevdev.domain.repository.survey;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    @EntityGraph(attributePaths = {"surveyQuestionOptions"})
    Optional<SurveyQuestion> findWithQuestionOptionsById(Long id);
}
