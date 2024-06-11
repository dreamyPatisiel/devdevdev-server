package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import java.util.List;

public interface SurveyAnswerRepositoryCustom {
    boolean existsByIdIn(Member member, List<Long> ids);
}
