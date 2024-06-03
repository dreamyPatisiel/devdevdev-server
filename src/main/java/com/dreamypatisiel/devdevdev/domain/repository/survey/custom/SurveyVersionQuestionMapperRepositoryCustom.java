package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import java.util.List;

public interface SurveyVersionQuestionMapperRepositoryCustom {
    List<SurveyVersionQuestionMapper> findMapperWithVersionAndQuestion();
}
