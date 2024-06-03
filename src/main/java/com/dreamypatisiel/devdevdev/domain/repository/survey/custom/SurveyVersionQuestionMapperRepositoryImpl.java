package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QSurveyQuestion.surveyQuestion;
import static com.dreamypatisiel.devdevdev.domain.entity.QSurveyVersion.surveyVersion;
import static com.dreamypatisiel.devdevdev.domain.entity.QSurveyVersionQuestionMapper.surveyVersionQuestionMapper;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SurveyVersionQuestionMapperRepositoryImpl implements SurveyVersionQuestionMapperRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public List<SurveyVersionQuestionMapper> findMapperWithVersionAndQuestion() {

        return query.selectFrom(surveyVersionQuestionMapper)
                .leftJoin(surveyVersionQuestionMapper.surveyVersion, surveyVersion).fetchJoin()
                .leftJoin(surveyVersionQuestionMapper.surveyQuestion, surveyQuestion).fetchJoin()
                .where(surveyVersion.id.eq(findMaxSurveyVersionIdSubQuery(query)))
                .orderBy(surveyQuestion.sortOrder.desc())
                .fetch();
    }

    private JPQLQuery<Long> findMaxSurveyVersionIdSubQuery(JPQLQueryFactory query) {
        return query.select(surveyVersion.id)
                .from(surveyVersion)
                .where(surveyVersion.isActive.eq(true))
                .orderBy(surveyVersion.createdAt.desc())
                .limit(1);
    }
}
