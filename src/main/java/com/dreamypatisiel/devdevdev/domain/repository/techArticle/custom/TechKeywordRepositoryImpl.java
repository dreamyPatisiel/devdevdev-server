package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.TechKeyword;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.dreamypatisiel.devdevdev.domain.entity.QTechKeyword.techKeyword;

@RequiredArgsConstructor
public class TechKeywordRepositoryImpl implements TechKeywordRepositoryCustom {

    public static final String MATCH_AGAINST_FUNCTION = "match_against";
    private final JPQLQueryFactory query;

    @Override
    public List<TechKeyword> searchKeyword(String inputJamo, String inputChosung, Pageable pageable) {
        BooleanExpression jamoMatch = Expressions.booleanTemplate(
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1}) > 0.0",
                techKeyword.jamoKey, inputJamo
        );
        
        BooleanExpression chosungMatch = Expressions.booleanTemplate(
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1}) > 0.0",
                techKeyword.chosungKey, inputChosung
        );

        // 스코어 계산을 위한 expression
        var jamoScore = Expressions.numberTemplate(Double.class,
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1})",
                techKeyword.jamoKey, inputJamo
        );
        
        var chosungScore = Expressions.numberTemplate(Double.class,
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1})",
                techKeyword.chosungKey, inputChosung
        );

        return query
                .selectFrom(techKeyword)
                .where(jamoMatch.or(chosungMatch))
                .orderBy(
                    // 더 높은 스코어를 우선으로 정렬
                    Expressions.numberTemplate(Double.class, 
                        "GREATEST({0}, {1})", jamoScore, chosungScore).desc(),
                    // 동일한 스코어라면 키워드 길이가 짧은 것을 우선으로 정렬
                    techKeyword.keyword.length().asc()
                )
                .limit(pageable.getPageSize())
                .fetch();
    }
}
