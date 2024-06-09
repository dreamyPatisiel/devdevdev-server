package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QSurveyAnswer.surveyAnswer;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SurveyAnswerRepositoryImpl implements SurveyAnswerRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public boolean hasAnsweredAllQuestions(Member member, List<Long> ids) {
        // 주어진 질문 ID 리스트에 대해 해당 멤버의 답변 여부를 체크
        long answeredQuestionCount = query
                .select(surveyAnswer.surveyQuestion.id)
                .from(surveyAnswer)
                .where(surveyAnswer.member.eq(member)
                        .and(surveyAnswer.surveyQuestion.id.in(ids)))
                .groupBy(surveyAnswer.surveyQuestion.id)
                .fetchCount();

        // 답변이 있는 질문의 수와 주어진 질문 ID 리스트의 크기가 동일하면 true 반환
        return answeredQuestionCount == ids.size();
    }
}
