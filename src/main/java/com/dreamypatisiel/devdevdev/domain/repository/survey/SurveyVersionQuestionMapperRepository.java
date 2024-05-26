package com.dreamypatisiel.devdevdev.domain.repository.survey;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.dreamypatisiel.devdevdev.domain.repository.survey.custom.SurveyVersionQuestionMapperRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyVersionQuestionMapperRepository extends JpaRepository<SurveyVersionQuestionMapper, Long>,
        SurveyVersionQuestionMapperRepositoryCustom {
}
