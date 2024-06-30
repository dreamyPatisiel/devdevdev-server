package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import com.dreamypatisiel.devdevdev.domain.entity.SurveyAnswer;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SurveyAnswerJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TimeProvider timeProvider;

    public void saveAll(List<SurveyAnswer> surveyAnswers) {

        // SQL
        String sql = "insert into survey_answer(custom_message, member_id, survey_question_id, survey_question_option_id, created_at, last_modified_at) values(?, ?, ?, ?, ?, ?)";

        // 배치 쿼리 수행
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                // customMessage 가 null 이 아니면
                if (surveyAnswers.get(i).getCustomMessage() != null) {
                    // 삽입
                    ps.setString(1, surveyAnswers.get(i).getCustomMessage().getMessage());
                } else {
                    ps.setString(1, null);
                }

                ps.setLong(2, surveyAnswers.get(i).getMember().getId());
                ps.setLong(3, surveyAnswers.get(i).getSurveyQuestion().getId());
                ps.setLong(4, surveyAnswers.get(i).getSurveyQuestionOption().getId());
                ps.setTimestamp(5, Timestamp.valueOf(timeProvider.getLocalDateTimeNow()));
                ps.setTimestamp(6, null);
            }

            @Override
            public int getBatchSize() {
                return surveyAnswers.size();
            }
        });
    }
}
