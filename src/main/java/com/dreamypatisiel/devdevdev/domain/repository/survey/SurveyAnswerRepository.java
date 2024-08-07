package com.dreamypatisiel.devdevdev.domain.repository.survey;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.repository.survey.custom.SurveyAnswerRepositoryCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long>, SurveyAnswerRepositoryCustom {
    List<SurveyAnswer> findAllByMember(Member member);
}
